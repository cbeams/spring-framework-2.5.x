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

import org.springframework.beans.factory.InitializingBean;

/**
 * <p>Implementation of MethodNameResolver that used the very existence of a
 * request parameter (i.e. a request parameter with a certain name is found) as
 * an indication of what method to dispatch to. The existence of an incoming 
 * request parameter name may be mapped to a target method of the same or another
 * specified name. The actual request parameter value is ignored</p>
 * 
 * <p>This resolver is prmarilly expected to be used with web pages containing
 * multiple submit buttons. The 'name' attribute of each button should be set to
 * the mapped method name, while the 'value' attribute is normally displayed as
 * the button label by the browser, and will be ignored by this resolve.</p>
 * 
 * @author Colin Sampaleanu
 */
public class ParameterNameMethodNameResolver implements MethodNameResolver,
		InitializingBean {

	private String[] mappings;
	private String defaultMethodName;

	/**
	 * <p>An array of Strings which is used to provide one or more mappings between a
	 * request parameter name (the resolver will check if a parameter exists with that
	 * name), and a target method which should be called. Parameter names are checked
	 * in the order specified here, with the first match (parameter exists) winning.</p> 
	 * 
	 * <p>Each array element must be a string in the form</p>
	 * <pre>
	 * name
	 * </pre>
	 * or the form
	 * <pre>
	 * name1:name2
	 * </pre>
	 * In the first case, the existence of a parameter called <code>name</code> will
	 * map to a method of the same name. In the second case, the existence of a parameter
	 * called <code>name1</code> will map to a method with the name <code>name2</code>. 
	 * 
	 * @param an array of mappings
	 */
	public void setMappings(String[] mappings) {
		this.mappings = mappings;
	}

	public void afterPropertiesSet() {
		if (this.mappings == null || this.mappings.length == 0) {
			throw new IllegalArgumentException("'mappings' property is required");
		}
	}

	/**
	 * Set the name of the default handler method that should be used when no
	 * parameter was found in the request
	 */
	public void setDefaultMethodName(String defaultMethodName) {
		this.defaultMethodName = defaultMethodName;
	}

	public String getHandlerMethodName(HttpServletRequest request)
			throws NoSuchRequestHandlingMethodException {
		
		String methodName = null;
		for (int i = 0; i < mappings.length; ++i) {
			String candidate = mappings[i];
			String mappedMethodName = candidate;
			int colon = candidate.indexOf(':');
			if (colon != -1) {
				mappedMethodName = candidate.substring(colon + 1);
				candidate = candidate.substring(0, colon);
			}	
			if (request.getParameter(candidate) != null) {
				methodName = mappedMethodName;
				break;
			}
		}

		if (methodName == null) {
			methodName = this.defaultMethodName;
		}
		if (methodName == null) {
			throw new NoSuchRequestHandlingMethodException(request);
		}
		return methodName;
	}
}
