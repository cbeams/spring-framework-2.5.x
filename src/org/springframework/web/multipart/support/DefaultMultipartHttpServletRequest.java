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

package org.springframework.web.multipart.support;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Default implementation of the MultipartHttpServletRequest interface.
 * Provides management of pre-generated parameter values.
 * @author Trevor D. Cook
 * @author Juergen Hoeller
 * @since 29-Sep-2003
 * @see org.springframework.web.multipart.MultipartResolver
 */
public class DefaultMultipartHttpServletRequest extends AbstractMultipartHttpServletRequest {

	private final Map parameters;

	/**
	 * Wrap the given HttpServletRequest in a MultipartHttpServletRequest.
	 * @param request the request to wrap
	 * @param parameters a map of the parameters,
	 * with Strings as keys and String arrays as values
	 * @param multipartFiles a map of the multipart files
	 */
	public DefaultMultipartHttpServletRequest(HttpServletRequest request, Map multipartFiles, Map parameters) {
		super(request);
		setMultipartFiles(multipartFiles);
		this.parameters = Collections.unmodifiableMap(parameters);
	}

	public Enumeration getParameterNames() {
		return Collections.enumeration(this.parameters.keySet());
	}

	public String getParameter(String name) {
		String[] values = getParameterValues(name);
		return (values != null && values.length > 0 ? values[0] : null);
	}

	public String[] getParameterValues(String name) {
		return (String[]) this.parameters.get(name);
	}

	public Map getParameterMap() {
		return this.parameters;
	}

}
