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

package org.springframework.web.multipart.support;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Default implementation of the MultipartHttpServletRequest interface.
 * Provides management of pre-generated parameter values.
 *
 * @author Trevor D. Cook
 * @author Juergen Hoeller
 * @since 29.09.2003
 * @see org.springframework.web.multipart.MultipartResolver
 */
public class DefaultMultipartHttpServletRequest extends AbstractMultipartHttpServletRequest {

	private final Map multipartParameters;


	/**
	 * Wrap the given HttpServletRequest in a MultipartHttpServletRequest.
	 * @param request the servlet request to wrap
	 * @param multipartFiles a map of the multipart files
	 * @param multipartParameters a map of the parameters to expose,
	 * with Strings as keys and String arrays as values
	 */
	public DefaultMultipartHttpServletRequest(
			HttpServletRequest request, Map multipartFiles, Map multipartParameters) {

		super(request);
		setMultipartFiles(multipartFiles);
		this.multipartParameters = multipartParameters;
	}


	public Enumeration getParameterNames() {
		Set paramNames = new HashSet();
		Enumeration paramEnum = super.getParameterNames();
		while (paramEnum.hasMoreElements()) {
			paramNames.add(paramEnum.nextElement());
		}
		paramNames.addAll(this.multipartParameters.keySet());
		return Collections.enumeration(paramNames);
	}

	public String getParameter(String name) {
		String[] values = (String[]) this.multipartParameters.get(name);
		if (values != null) {
			return (values.length > 0 ? values[0] : null);
		}
		return super.getParameter(name);
	}

	public String[] getParameterValues(String name) {
		String[] values = (String[]) this.multipartParameters.get(name);
		if (values != null) {
			return values;
		}
		return super.getParameterValues(name);
	}

	public Map getParameterMap() {
		Map paramMap = new HashMap();
		paramMap.putAll(super.getParameterMap());
		paramMap.putAll(this.multipartParameters);
		return paramMap;
	}

}
