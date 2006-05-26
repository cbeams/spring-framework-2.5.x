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

package org.springframework.web;

import javax.servlet.ServletException;

/**
 * Exception thrown when a request handler does not support a
 * specific request method.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public class HttpRequestMethodNotSupportedException extends ServletException {

	private String method;


	/**
	 * Create a new HttpRequestMethodNotSupportedException.
	 * @param method the unsupported HTTP request method
	 */
	public HttpRequestMethodNotSupportedException(String method) {
		this(method, "Request method '" + method + "' not supported");
	}

	/**
	 * Create a new HttpRequestMethodNotSupportedException.
	 * @param method the unsupported HTTP request method
	 * @param msg the detail message
	 */
	public HttpRequestMethodNotSupportedException(String method, String msg) {
		super(msg);
		this.method = method;
	}


	/**
	 * Return the HTTP request method that caused the failure.
	 */
	public String getMethod() {
		return method;
	}

}
