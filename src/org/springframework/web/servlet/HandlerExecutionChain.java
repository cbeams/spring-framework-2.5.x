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

package org.springframework.web.servlet;

/**
 * Handler execution chain, consisting of handler object and any
 * preprocessing interceptors. Returned by HandlerMapping's
 * getHandler method.
 * @author Juergen Hoeller
 * @since 20.06.2003
 * @see HandlerMapping#getHandler
 * @see HandlerInterceptor
 */
public class HandlerExecutionChain {

	private Object handler;

	private HandlerInterceptor[] interceptors;

	/**
	 * Create new HandlerExecutionChain.
	 * @param handler the handler object to execute
	 */
	public HandlerExecutionChain(Object handler) {
		this.handler = handler;
	}

	/**
	 * Create new HandlerExecutionChain.
	 * @param handler the handler object to execute
	 * @param interceptors the array of interceptors to apply
	 * (in the given order) before the handler itself executes
	 */
	public HandlerExecutionChain(Object handler, HandlerInterceptor[] interceptors) {
		this.handler = handler;
		this.interceptors = interceptors;
	}

	/**
	 * Return the handler object to execute.
	 * @return the handler object (should not be null)
	 */
	public Object getHandler() {
		return handler;
	}

	/**
	 * Return the array of interceptors to apply (in the given order)
	 * before the handler itself executes.
	 * @return the array of HandlerInterceptors instances (may be null)
	 */
	public HandlerInterceptor[] getInterceptors() {
		return interceptors;
	}

}
