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

package org.springframework.mock.web;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * Mock implementation of the FilterConfig interface.
 *
 * <p>Used for testing the web framework; typically not
 * necessary for testing application controllers.
 *
 * @author Juergen Hoeller
 */
public class MockFilterConfig implements FilterConfig {

	private final ServletContext servletContext;

	private final String name;

	private final Hashtable initParameters = new Hashtable();


	/**
	 * Create new MockServletConfig with empty String as name.
	 * @param servletContext the ServletContext that the servlet runs in
	 */
	public MockFilterConfig(ServletContext servletContext) {
		this(servletContext, "");
	}

	/**
	 * Create new MockServletConfig.
	 * @param servletContext the ServletContext that the servlet runs in
	 * @param name the name of the servlet
	 */
	public MockFilterConfig(ServletContext servletContext, String name) {
		this.servletContext = servletContext;
		this.name = name;
	}


	public void addInitParameter(String name, String value) {
		this.initParameters.put(name, value);
	}

	public String getInitParameter(String name) {
		return (String) this.initParameters.get(name);
	}

	public Enumeration getInitParameterNames() {
		return this.initParameters.keys();
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public String getFilterName() {
		return name;
	}

}
