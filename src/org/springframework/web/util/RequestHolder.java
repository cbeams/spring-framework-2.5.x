/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.web.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Helper class to manage thread-bound HttpServletRequest.
 *
 * @author Rod Johnson
 * @since 1.3
 */
public abstract class RequestHolder  {
	
	/**
	 * Holder for the current request.
	 */
	public static ThreadLocal currentRequest = new ThreadLocal();
	
	// TODO return old? use stack?
	public static void bind(HttpServletRequest request) {
		RequestHolder.currentRequest.set(request);
	}
	
	/**
	 * Clear any request bound to the thread.
	 */
	public static void clear() {
		RequestHolder.currentRequest.set(null);
	}
	
	/**
	 * Return the request currently bound to the thread.
	 * @return the request currently bound to the thread
	 * @throws IllegalStateException if no request is bound to the 
	 * current thread
	 */
	public static HttpServletRequest currentRequest() throws IllegalStateException {
		HttpServletRequest request = (HttpServletRequest) currentRequest.get();
		if (request == null) {
			throw new IllegalStateException("No thread-bound request: Try using RequestBindingFilter");		
		}
		return request;
	}
		
	/**
	 * Convenient method to return the session associated with the current request,
	 * creating one if none exists.
	 * @return the Session to which the current request belongs.
	 */
	public static HttpSession currentSession() {		
		return currentRequest().getSession(true);
	}

}
