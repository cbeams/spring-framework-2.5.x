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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Mock implementation of the JSP PageContext interface.
 *
 * <p>Used for testing the web framework; only necessary for
 * testing applications when testing custom JSP tags.
 *
 * <p>Note: Expects initialization via the constructor rather than
 * via the PageContext.initialize method. Does not support writing to
 * a JspWriter, request dispatching, and handlePageException calls.
 *
 * @author Juergen Hoeller
 * @since 30.04.2004
 */
public class MockPageContext extends PageContext {

	private final ServletContext servletContext;

	private final HttpServletRequest request;

	private final HttpServletResponse response;

	private final ServletConfig servletConfig;

	private final Hashtable attributes = new Hashtable();


	/**
	 * Create new MockPageContext with a MockServletContext,
	 * MockHttpServletRequest, MockHttpServletResponse, MockServletConfig.
	 */
	public MockPageContext() {
		this(new MockServletContext());
	}

	/**
	 * Create new MockPageContext with a MockHttpServletRequest,
	 * MockHttpServletResponse, MockServletConfig.
	 * @param servletContext the ServletContext that the servlet runs in
	 * (only necessary when accessing the ServletContext)
	 */
	public MockPageContext(ServletContext servletContext) {
		this(servletContext, new MockHttpServletRequest(servletContext));
	}

	/**
	 * Create new MockPageContext with a MockHttpServletResponse,
	 * MockServletConfig.
	 * @param servletContext the ServletContext that the servlet runs in
	 * @param request the current HttpServletRequest
	 * (only necessary when accessing the request)
	 */
	public MockPageContext(ServletContext servletContext, HttpServletRequest request) {
		this(servletContext, request, new MockHttpServletResponse());
	}

	/**
	 * Create new MockPageContext with a MockServletConfig.
	 * @param servletContext the ServletContext that the servlet runs in
	 * @param request the current HttpServletRequest
	 * @param response the current HttpServletResponse
	 * (only necessary when writing to the response)
	 */
	public MockPageContext(
			ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) {
		this(servletContext, request, response, new MockServletConfig(servletContext));
	}

	/**
	 * Create new MockServletConfig.
	 * @param servletContext the ServletContext that the servlet runs in
	 * @param request the current HttpServletRequest
	 * @param response the current HttpServletResponse
	 * @param servletConfig the ServletConfig
	 * (hardly ever accessed from within a tag)
	 */
	public MockPageContext(
			ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
			ServletConfig servletConfig) {
		this.servletContext = servletContext;
		this.servletConfig = servletConfig;
		this.request = request;
		this.response = response;
	}


	public void initialize(
			Servlet servlet, ServletRequest request, ServletResponse response,
			String errorPageURL, boolean needsSession, int bufferSize, boolean autoFlush) {
		throw new UnsupportedOperationException("Use appropriate constructor");
	}

	public void release() {
	}

	public void setAttribute(String name, Object value) {
		this.attributes.put(name, value);
	}

	public void setAttribute(String name, Object value, int scope) {
		switch (scope) {
			case PAGE_SCOPE:
				setAttribute(name, value);
				break;
			case REQUEST_SCOPE:
				this.request.setAttribute(name, value);
				break;
			case SESSION_SCOPE:
				this.request.getSession().setAttribute(name, value);
				break;
			case APPLICATION_SCOPE:
				this.servletContext.setAttribute(name, value);
				break;
			default:
				throw new IllegalArgumentException("Invalid scope: " + scope);
		}
	}

	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}

	public Object getAttribute(String name, int scope) {
		switch (scope) {
			case PAGE_SCOPE:
				return getAttribute(name);
			case REQUEST_SCOPE:
				return this.request.getAttribute(name);
			case SESSION_SCOPE:
				HttpSession session = this.request.getSession(false);
				return (session != null ? session.getAttribute(name) : null);
			case APPLICATION_SCOPE:
				return this.servletContext.getAttribute(name);
			default:
				throw new IllegalArgumentException("Invalid scope: " + scope);
		}
	}

	public Object findAttribute(String name) {
		Object value = getAttribute(name);
		if (value == null) {
			value = getAttribute(name, REQUEST_SCOPE);
			if (value == null) {
				value = getAttribute(name, SESSION_SCOPE);
				if (value == null) {
					value = getAttribute(name, APPLICATION_SCOPE);
				}
			}
		}
		return value;
	}

	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}

	public void removeAttribute(String name, int scope) {
		switch (scope) {
			case PAGE_SCOPE:
				removeAttribute(name);
				break;
			case REQUEST_SCOPE:
				this.request.removeAttribute(name);
				break;
			case SESSION_SCOPE:
				this.request.getSession().removeAttribute(name);
				break;
			case APPLICATION_SCOPE:
				this.servletContext.removeAttribute(name);
				break;
			default:
				throw new IllegalArgumentException("Invalid scope: " + scope);
		}
	}

	public int getAttributesScope(String name) {
		if (getAttribute(name) != null) {
			return PAGE_SCOPE;
		}
		else if (getAttribute(name, REQUEST_SCOPE) != null) {
			return REQUEST_SCOPE;
		}
		else if (getAttribute(name, SESSION_SCOPE) != null) {
			return SESSION_SCOPE;
		}
		else if (getAttribute(name, APPLICATION_SCOPE) != null) {
			return APPLICATION_SCOPE;
		}
		else {
			return 0;
		}
	}

	public Enumeration getAttributeNames() {
		return this.attributes.elements();
	}

	public Enumeration getAttributeNamesInScope(int scope) {
		switch (scope) {
			case PAGE_SCOPE:
				return getAttributeNames();
			case REQUEST_SCOPE:
				return this.request.getAttributeNames();
			case SESSION_SCOPE:
				HttpSession session = this.request.getSession(false);
				return (session != null ? session.getAttributeNames() : null);
			case APPLICATION_SCOPE:
				return this.servletContext.getAttributeNames();
			default:
				throw new IllegalArgumentException("Invalid scope: " + scope);
		}
	}

	public JspWriter getOut() {
		throw new UnsupportedOperationException("getOut");
	}

	public HttpSession getSession() {
		return this.request.getSession();
	}

	public Object getPage() {
		throw new UnsupportedOperationException("getPage");
	}

	public ServletRequest getRequest() {
		return request;
	}

	public ServletResponse getResponse() {
		return response;
	}

	public Exception getException() {
		throw new UnsupportedOperationException("getException");
	}

	public ServletConfig getServletConfig() {
		return servletConfig;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public void forward(String url) throws ServletException, IOException {
		throw new UnsupportedOperationException("forward");
	}

	public void include(String url) throws ServletException, IOException {
		throw new UnsupportedOperationException("include");
	}

	public void handlePageException(Exception ex) throws ServletException, IOException {
		throw new UnsupportedOperationException("handlePageException");
	}

	public void handlePageException(Throwable throwable) throws ServletException, IOException {
		throw new UnsupportedOperationException("handlePageException");
	}

}
