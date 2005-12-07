/*
 * Copyright 2002-2005 the original author or authors.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Helper class to manage a thread-bound HttpServletRequest.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see RequestContextListener
 * @see org.springframework.web.filter.RequestContextFilter
 */
public abstract class RequestContextHolder  {
	
	/** ThreadLocal that holds the current request */
	public static ThreadLocal requestHolder = new InheritableThreadLocal();


	public static void setRequest(HttpServletRequest request) {
		RequestContextHolder.requestHolder.set(request);
	}

	/**
	 * Return the request currently bound to the thread.
	 * @return the request currently bound to the thread, or <code>null</code>
	 */
	public static HttpServletRequest getRequest() throws IllegalStateException {
		return (HttpServletRequest) requestHolder.get();
	}

	/**
	 * Return the request currently bound to the thread.
	 * @return the request currently bound to the thread
	 * @throws IllegalStateException if no request is bound to the
	 * current thread
	 */
	public static HttpServletRequest currentRequest() throws IllegalStateException {
		HttpServletRequest request = (HttpServletRequest) requestHolder.get();
		if (request == null) {
			throw new IllegalStateException("No thread-bound request: use RequestContextFilter");
		}
		return request;
	}

	/**
	 * Convenient method to return the session associated with the current request,
	 * creating one if none exists.
	 * @return the Session to which the current request belongs.
	 * @throws IllegalStateException if no request is bound to the
	 * current thread
	 */
	public static HttpSession currentSession() throws IllegalStateException {
		return currentRequest().getSession(true);
	}

}
