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

package org.springframework.web.servlet.mvc.multiaction;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Exception thrown when there's no request handling method for a request.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class NoSuchRequestHandlingMethodException extends ServletException {

	private String methodName;


	/**
	 * Create a new NoSuchRequestHandlingMethodException for the given request.
	 * @param request the offending HTTP request
	 */
	public NoSuchRequestHandlingMethodException(HttpServletRequest request) {
		super("No handling method can be found for request [" + request + "]");
	}
	
	/**
	 * Create a new NoSuchRequestHandlingMethodException for the given request.
	 * @param methodName the name of the handler method that wasn't found
	 * @param controllerClass the class the handler method was expected to be in
	 */
	public NoSuchRequestHandlingMethodException(String methodName, Class controllerClass) {
		super("No request handling method with name '" + methodName +
				"' in class [" + controllerClass.getName() + "]");
		this.methodName = methodName;
	}


	/**
	 * Return the name of the offending method, if known.
	 */
	public String getMethodName() {
		return methodName;
	}

}
