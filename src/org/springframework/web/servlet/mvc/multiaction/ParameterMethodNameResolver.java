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

package org.springframework.web.servlet.mvc.multiaction;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple implementation of MethodNameResolver that looks for a
 * parameter value containing the name of the method to invoke.
 *
 * <p>The name of the parameter and optionally also the name of a
 * default handler method can be specified as JavaBean properties.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setParamName
 * @see #setDefaultMethodName
 */
public class ParameterMethodNameResolver implements MethodNameResolver {

	public static final String DEFAULT_PARAM_NAME = "action";

	private String paramName = DEFAULT_PARAM_NAME;

	private String defaultMethodName;

	/**
	 * Set the parameter name we're looking for.
	 * Default is "action".
	 */
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	/**
	 * Set the name of the default handler method that should be
	 * used when no parameter was found in the request
	 */
	public void setDefaultMethodName(String defaultMethodName) {
		this.defaultMethodName = defaultMethodName;
	}

	public String getHandlerMethodName(HttpServletRequest request) throws NoSuchRequestHandlingMethodException {
		String methodName = request.getParameter(this.paramName);
		if (methodName == null) {
			methodName = this.defaultMethodName;
		}
		if (methodName == null) {
			throw new NoSuchRequestHandlingMethodException(request);
		}
		return methodName;
	}

}
