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

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Listener for Servlet 2.4+ containers. Exposes the request to the current thread,
 * through both LocaleContextHolder and RequestContextHolder.
 * To be registered as listener in <code>web.xml</code>.
 *
 * <p>Alternatively, Spring's RequestContextFilter and Spring's DispatcherServlet
 * also expose the same request context to the current thread.
 *
 * @author Steven Devijver
 * @author Juergen Hoeller
 * @since 2.0
 * @see javax.servlet.ServletRequestListener
 * @see org.springframework.context.i18n.LocaleContextHolder
 * @see org.springframework.web.context.scope.RequestContextHolder
 * @see org.springframework.web.filter.RequestContextFilter
 * @see org.springframework.web.servlet.DispatcherServlet
 */
public class RequestContextListener implements ServletRequestListener {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());


	public void requestInitialized(ServletRequestEvent requestEvent) {
		if (!(requestEvent.getServletRequest() instanceof HttpServletRequest)) {
			throw new IllegalArgumentException(
					"Request is not an HttpServletRequest: " + requestEvent.getServletRequest());
		}
		HttpServletRequest request = (HttpServletRequest) requestEvent.getServletRequest();
		LocaleContextHolder.setLocale(request.getLocale());
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		if (logger.isDebugEnabled()) {
			logger.debug("Bound request context to thread: " + request);
		}
	}

	public void requestDestroyed(ServletRequestEvent requestEvent) {
		ServletRequestAttributes requestAttributes =
				(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		requestAttributes.updateAccessedAttributes();
		RequestContextHolder.setRequestAttributes(null);
		LocaleContextHolder.setLocale(null);
		if (logger.isDebugEnabled()) {
			logger.debug("Cleared thread-bound request context: " + requestEvent.getServletRequest());
		}
	}

}
