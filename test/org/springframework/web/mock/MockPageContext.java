/*
 * Created on Sep 16, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
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

package org.springframework.web.mock;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * @author alef
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MockPageContext extends PageContext {
	
	private Map context = new HashMap();
	
	private Map attributeMappings = new HashMap();
	
	private ServletRequest servletRequest;
	private ServletResponse servletResponse;

	private Servlet servlet;

	private String errorPageURL;

	private boolean autoFlush;

	private int bufferSize;

	private boolean needsSession;
	
	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#initialize(javax.servlet.Servlet, javax.servlet.ServletRequest, javax.servlet.ServletResponse, java.lang.String, boolean, int, boolean)
	 */
	public void initialize(
		Servlet servlet,
		ServletRequest servletRequest,
		ServletResponse servletResponse,
		String errorPageURL, 
		boolean needsSession, 
		int bufferSize, 
		boolean autoFlush)
	throws IOException, IllegalStateException, IllegalArgumentException {
			
		this.servlet = servlet;
		this.servletRequest = servletRequest;
		this.servletResponse = servletResponse;
		this.errorPageURL = errorPageURL;
		this.needsSession = needsSession;
		this.bufferSize = bufferSize;
		this.autoFlush = autoFlush;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#release()
	 */
	public void release() {
		// nothing really...
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String key, Object value) {
		// set it in the pageContext
		context.put(key, value);
		attributeMappings.put(key, new Integer(PageContext.PAGE_SCOPE));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#setAttribute(java.lang.String, java.lang.Object, int)
	 */
	public void setAttribute(String key, Object value, int ctx) {
		// put the mapping in the mappingsmap and put the key/value 
		// in the appropriate context
		
		boolean bound = false;
		switch (ctx) {
			case PageContext.PAGE_SCOPE: 
				context.put(key, value);
				bound = true;
				break;
			case PageContext.REQUEST_SCOPE:
				servletRequest.setAttribute(key, value);
				bound = true;
				break;
			case PageContext.SESSION_SCOPE:
				HttpSession session = getSession();
				if (session != null) {
					session.setAttribute(key, value);
					bound = true;
				}
				break;
			default:
				throw new UnsupportedOperationException("Application scope not supported");
		}
		if (bound) {
			attributeMappings.put(key, new Integer(ctx));
		} else {
			// TODO what to do now?
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String key) {
		return context.get(key);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#getAttribute(java.lang.String, int)
	 */
	public Object getAttribute(String key, int ctx) {
		switch (ctx) {
			case PageContext.PAGE_SCOPE: 
				return context.get(key);
			case PageContext.REQUEST_SCOPE:
				return servletRequest.getAttribute(key);
			case PageContext.SESSION_SCOPE:
				HttpSession session = getSession();
				if (session != null) {
					return session.getAttribute(key);
				}
				break;
			default:				
		}
		return null;		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#findAttribute(java.lang.String)
	 */
	public Object findAttribute(String key) {
		System.out.println("Trying to find " + key);
		Integer whatContext = (Integer)attributeMappings.get(key);
		if (context == null) {
			return null;
		}
		switch (whatContext.intValue()) {
			case PageContext.PAGE_SCOPE: 
				return context.get(key);
			case PageContext.REQUEST_SCOPE:
				return servletRequest.getAttribute(key);
			case PageContext.SESSION_SCOPE:
				HttpSession session = getSession();
				if (session != null) {
					return session.getAttribute(key);
				}
				break;
			default:				
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		throw new UnsupportedOperationException("Not implemented in MockPageContext");

	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#removeAttribute(java.lang.String, int)
	 */
	public void removeAttribute(String arg0, int arg1) {
		throw new UnsupportedOperationException("Not implemented in MockPageContext");

	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#getAttributesScope(java.lang.String)
	 */
	public int getAttributesScope(String key) {
		Integer i = (Integer)attributeMappings.get(key);
		if (i != null) {
			return i.intValue();
		} else {			
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#getAttributeNamesInScope(int)
	 */
	public Enumeration getAttributeNamesInScope(int arg0) {
		throw new UnsupportedOperationException("Not implemented in MockPageContext");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#getOut()
	 */
	public JspWriter getOut() {
		throw new UnsupportedOperationException("Not implemented in MockPageContext");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#getSession()
	 */
	public HttpSession getSession() {		
		throw new UnsupportedOperationException("Not implemented in MockPageContext");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#getPage()
	 */
	public Object getPage() {
		throw new UnsupportedOperationException("Not implemented in MockPageContext");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#getRequest()
	 */
	public ServletRequest getRequest() {
		return servletRequest;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#getResponse()
	 */
	public ServletResponse getResponse() {
		return servletResponse;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#getException()
	 */
	public Exception getException() {		
		throw new UnsupportedOperationException("Not implemented in MockPageContext");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#getServletConfig()
	 */
	public ServletConfig getServletConfig() {		
		return servlet.getServletConfig();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#getServletContext()
	 */
	public ServletContext getServletContext() {		
		throw new UnsupportedOperationException("Not implemented in MockPageContext");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#forward(java.lang.String)
	 */
	public void forward(String arg0) throws ServletException, IOException {
		throw new UnsupportedOperationException("Not implemented in MockPageContext");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#include(java.lang.String)
	 */
	public void include(String arg0) throws ServletException, IOException {
		throw new UnsupportedOperationException("Not implemented in MockPageContext");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#handlePageException(java.lang.Exception)
	 */
	public void handlePageException(Exception arg0)
		throws ServletException, IOException {
		throw new UnsupportedOperationException("Not implemented in MockPageContext");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.PageContext#handlePageException(java.lang.Throwable)
	 */
	public void handlePageException(Throwable arg0)
		throws ServletException, IOException {
		throw new UnsupportedOperationException("Not implemented in MockPageContext");
			
	}
}
