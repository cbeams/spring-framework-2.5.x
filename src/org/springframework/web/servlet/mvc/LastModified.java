/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;

/**
 * Supports last modified HTTP requests to facilitate content caching.
 * Same contract as for the Servlet API's getLastModified() method.
 *
 * <p>Delegated to by SimpleControllerHandlerAdapter's getLastModified method.
 * Any controller within our MVC framework can implement this.
 *
 * @author Rod Johnson
 * @see SimpleControllerHandlerAdapter
 */
public interface LastModified {
	
	/**
	 * Same contract as for HttpServlet's getLastModified method.
	 * Invoked <b>before</b> request processing.
	 * <p>The return value will be sent to the HTTP client as Last-Modified header,
	 * and compared with If-Modified-Since headers that the client sends back.
	 * The content will only get regenerated if there has been a modification.
	 * @param request current HTTP request
	 * @return the time the underlying resource was last modified
	 * @see org.springframework.web.servlet.HandlerAdapter#getLastModified
	 * @see javax.servlet.http.HttpServlet#getLastModified
	 */
	long getLastModified(HttpServletRequest request);
	
}
