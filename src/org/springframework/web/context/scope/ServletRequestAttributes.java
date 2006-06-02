/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.context.scope;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.util.Assert;
import org.springframework.web.util.WebUtils;

/**
 * Servlet-based implementation of the RequestAttributes interface.
 * Accesses objects from servlet request and HTTP session scope,
 * with no distinction between "session" and "global session".
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see javax.servlet.ServletRequest#getAttribute
 * @see javax.servlet.http.HttpSession#getAttribute
 */
public class ServletRequestAttributes implements RequestAttributes {

	private final HttpServletRequest request;

	private final Map sessionAttributesToUpdate = new HashMap();


	/**
	 * Create a new ServletRequestAttributes instance for the given request.
	 * @param request current HTTP request
	 */
	public ServletRequestAttributes(HttpServletRequest request) {
		Assert.notNull(request, "Request must not be null");
		this.request = request;
	}


	public Object getAttribute(String name, int scope) {
		if (scope == SCOPE_REQUEST) {
			return this.request.getAttribute(name);
		}
		else {
			HttpSession session = this.request.getSession(false);
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
			HttpSession session = this.request.getSession(true);
			session.setAttribute(name, value);
			this.sessionAttributesToUpdate.remove(name);
		}
	}

	public void removeAttribute(String name, int scope) {
		if (scope == SCOPE_REQUEST) {
			this.request.removeAttribute(name);
		}
		else {
			HttpSession session = this.request.getSession(false);
			if (session != null) {
				session.removeAttribute(name);
				this.sessionAttributesToUpdate.remove(name);
			}
		}
	}

	public Object getSessionMutex() {
		return WebUtils.getSessionMutex(this.request.getSession());
	}


	/**
	 * Update all accessed session attributes through <code>session.setAttribute</code>
	 * calls, explicitly indicating to the container that they might have been modified.
	 */
	public void updateAccessedAttributes() {
		HttpSession session = this.request.getSession(false);
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

}
