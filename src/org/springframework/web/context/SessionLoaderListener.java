/*
 * Copyright 2004-2005 the original author or authors.
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
package org.springframework.web.context;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.RequestHolder;

/**
 * Listener for Servlet 2.4 containers. Extend {@link org.springframework.web.context.SessionLoaderListenerServlet23}
 * and implements {@link javax.servlet.ServletRequestListener}. This listener binds the request to the current thread
 * and removes it when the request is being destroyed.
 * 
 * @see org.springframework.web.context.SessionLoaderListenerServlet23
 * @see javax.servlet.ServletRequestListener
 * @author Steven Devijver
 * @since Oct 4, 2005
 */
public class SessionLoaderListener extends SessionLoaderListenerServlet23 implements ServletRequestListener {

	public void requestDestroyed(ServletRequestEvent requestEvent) {
		RequestHolder.clear();
	}
	
	public void requestInitialized(ServletRequestEvent requestEvent) {
		if (requestEvent.getServletRequest() instanceof HttpServletRequest) {
			RequestHolder.bind((HttpServletRequest)requestEvent.getServletRequest());
		} else {
			throw new IllegalArgumentException("Request does not implement javax.servlet.http.HttpServletRequest");
		}
	}

}
