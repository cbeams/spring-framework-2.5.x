/**
 * Generic framework code included with 
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002). 
 * This code is free to use and modify. However, please
 * acknowledge the source and include the above URL in each
 * class using or derived from this code. 
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
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
