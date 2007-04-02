/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.context.request;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.util.WebUtils;

/**
 * Servlet-based implementation of the {@link RequestAttributes} interface.
 *
 * <p>Accesses objects from servlet request and HTTP session scope,
 * with no distinction between "session" and "global session".
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see javax.servlet.ServletRequest#getAttribute
 * @see javax.servlet.http.HttpSession#getAttribute
 */
public class ServletRequestAttributes extends AbstractRequestAttributes {

	/**
	 * Constant identifying the {@link String} prefixed to the name of a
	 * destruction callback when it is stored in a {@link HttpSession}.
	 */
	public static final String DESTRUCTION_CALLBACK_NAME_PREFIX =
			ServletRequestAttributes.class.getName() + ".DESTRUCTION_CALLBACK.";

	// We'll create a lot of these objects, so we don't want a new logger every time.
	private static final Log logger = LogFactory.getLog(ServletRequestAttributes.class);

	// Determine whether Servlet 2.3's HttpSessionBindingListener interface is available.
	private final static boolean bindingListenerAvailable =
			ClassUtils.isPresent(
					"javax.servlet.http.HttpSessionBindingListener", ServletRequestAttributes.class.getClassLoader());


	private final HttpServletRequest request;

	private HttpSession session;

	private final Map sessionAttributesToUpdate = new HashMap();


	/**
	 * Create a new ServletRequestAttributes instance for the given request.
	 * @param request current HTTP request
	 */
	public ServletRequestAttributes(HttpServletRequest request) {
		Assert.notNull(request, "Request must not be null");
		this.request = request;
		// Fetch existing session reference early, to have it available even
		// after request completion (for example, in a custom child thread).
		this.session = request.getSession(false);
	}


	/**
	 * Exposes the native {@link HttpServletRequest} that we're wrapping.
	 */
	public final HttpServletRequest getRequest() {
		return this.request;
	}

	/**
	 * Exposes the {@link HttpSession} that we're wrapping.
	 * @param allowCreate whether to allow creation of a new session if none exists yet
	 */
	protected final HttpSession getSession(boolean allowCreate) {
		try {
			this.session = this.request.getSession(allowCreate);
			return this.session;
		}
		catch (IllegalStateException ex) {
			// Couldn't access session... let's check why.
			if (this.session == null) {
				// No matter what happened - we cannot offer a session.
				throw ex;
			}
			// We have a fallback session reference...
			// Let's see whether it is appropriate to return it.
			if (allowCreate) {
				boolean canAskForExistingSession = false;
				try {
					this.session = this.request.getSession(false);
					canAskForExistingSession = true;
				}
				catch (IllegalStateException ex2) {
				}
				if (canAskForExistingSession) {
					// Could ask for existing session, hence the IllegalStateException
					// came from trying to create a new session too late -> rethrow.
					throw ex;
				}
			}
			// Else: Could not even ask for existing session, hence we assume that
			// the request has been completed and the session is accessed later on
			// (for example, in a custom child thread).
			return this.session;
		}
	}


	public Object getAttribute(String name, int scope) {
		if (scope == SCOPE_REQUEST) {
			return this.request.getAttribute(name);
		}
		else {
			HttpSession session = getSession(false);
			if (session != null) {
				Object value = session.getAttribute(name);
				if (value != null) {
					this.sessionAttributesToUpdate.put(name, value);
				}
				return value;
			}
			else {
				return null;
			}
		}
	}

	public void setAttribute(String name, Object value, int scope) {
		if (scope == SCOPE_REQUEST) {
			this.request.setAttribute(name, value);
		}
		else {
			HttpSession session = getSession(true);
			session.setAttribute(name, value);
			this.sessionAttributesToUpdate.remove(name);
		}
	}

	public void removeAttribute(String name, int scope) {
		if (scope == SCOPE_REQUEST) {
			this.request.removeAttribute(name);
			removeRequestDestructionCallback(name);
		}
		else {
			HttpSession session = getSession(false);
			if (session != null) {
				session.removeAttribute(name);
				this.sessionAttributesToUpdate.remove(name);
				// Remove any registered destruction callback as well.
				session.removeAttribute(DESTRUCTION_CALLBACK_NAME_PREFIX + name);
			}
		}
	}

	public void registerDestructionCallback(String name, Runnable callback, int scope) {
		if (scope == SCOPE_REQUEST) {
			registerRequestDestructionCallback(name, callback);
		}
		else {
			registerSessionDestructionCallback(name, callback);
		}
	}

	public String getSessionId() {
		return getSession(true).getId();
	}

	public Object getSessionMutex() {
		return WebUtils.getSessionMutex(getSession(true));
	}


	/**
	 * Update all accessed session attributes through <code>session.setAttribute</code>
	 * calls, explicitly indicating to the container that they might have been modified.
	 */
	protected void updateAccessedSessionAttributes() {
		HttpSession session = getSession(false);
		if (session != null) {
			for (Iterator it = this.sessionAttributesToUpdate.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String name = (String) entry.getKey();
				Object newValue = entry.getValue();
				Object oldValue = session.getAttribute(name);
				if (oldValue == newValue) {
					session.setAttribute(name, newValue);
				}
			}
		}
		this.sessionAttributesToUpdate.clear();
	}

	/**
	 * Register the given callback as to be executed after session termination.
	 * @param name the name of the attribute to register the callback for
	 * @param callback the callback to be executed for destruction
	 */
	private void registerSessionDestructionCallback(String name, Runnable callback) {
		if (bindingListenerAvailable) {
			HttpSession session = getSession(true);
			session.setAttribute(DESTRUCTION_CALLBACK_NAME_PREFIX + name,
					new DestructionCallbackBindingListener(callback));
		}
		else {
			if (logger.isWarnEnabled()) {
				logger.warn("Could not register destruction callback [" + callback + "] for attribute '" +
						name + "' in session scope because Servlet 2.3 API is not available");
			}
		}
	}


	/**
	 * Adapter that implements the Servlet 2.3 HttpSessionBindingListener
	 * interface, wrapping a request destruction callback.
	 */
	private static class DestructionCallbackBindingListener implements HttpSessionBindingListener, Serializable {

		private final Runnable destructionCallback;

		public DestructionCallbackBindingListener(Runnable destructionCallback) {
			this.destructionCallback = destructionCallback;
		}

		public void valueBound(HttpSessionBindingEvent event) {
		}

		public void valueUnbound(HttpSessionBindingEvent event) {
			this.destructionCallback.run();
		}
	}

}
