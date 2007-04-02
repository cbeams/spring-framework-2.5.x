/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.web.context.request;

import java.security.Principal;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * {@link WebRequest} adapter for an {@link javax.servlet.http.HttpServletRequest}.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ServletWebRequest extends ServletRequestAttributes implements WebRequest {

	/**
	 * Create a new ServletWebRequest instance for the given request.
	 * @param request current HTTP request
	 */
	public ServletWebRequest(HttpServletRequest request) {
		super(request);
	}

	public String getParameter(String paramName) {
		return getRequest().getParameter(paramName);
	}

	public String[] getParameterValues(String paramName) {
		return getRequest().getParameterValues(paramName);
	}

	public Map getParameterMap() {
		return getRequest().getParameterMap();
	}

	public Locale getLocale() {
		return getRequest().getLocale();
	}

	public String getContextPath() {
		return getRequest().getContextPath();
	}

	public String getRemoteUser() {
		return getRequest().getRemoteUser();
	}

	public Principal getUserPrincipal() {
		return getRequest().getUserPrincipal();
	}

	public boolean isUserInRole(String role) {
		return getRequest().isUserInRole(role);
	}

	public boolean isSecure() {
		return getRequest().isSecure();
	}

}
