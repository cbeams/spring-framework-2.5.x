/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.context.support;

import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;

/**
 * Convenience methods to retrieve the root WebApplicationContext for a given
 * ServletContext. This is e.g. useful for accessing a Spring context from
 * within custom web views or Struts actions.
 * @author Juergen Hoeller
 * @version $Id: WebApplicationContextUtils.java,v 1.8 2004-02-04 17:31:55 jhoeller Exp $
 * @see org.springframework.web.context.ContextLoader
 */
public abstract class WebApplicationContextUtils {
	
	/**
	 * Find the root WebApplicationContext for this web app, which is
	 * typically loaded via ContextLoaderListener or ContextLoaderServlet.
	 * @param sc ServletContext to find the web application context for
	 * @return the root WebApplicationContext for this web app, or null if none
	 * @see org.springframework.web.context.WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	public static WebApplicationContext getWebApplicationContext(ServletContext sc) {
		return (WebApplicationContext) sc.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
	}

	/**
	 * Find the root WebApplicationContext for this web app, which is
	 * typically loaded via ContextLoaderListener or ContextLoaderServlet.
	 * @param sc ServletContext to find the web application context for
	 * @return the root WebApplicationContext for this web app
	 * @throws IllegalStateException if the root WebApplicationContext could not be found
	 * @see org.springframework.web.context.WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	public static WebApplicationContext getRequiredWebApplicationContext(ServletContext sc)
	    throws IllegalStateException {
		WebApplicationContext wac = getWebApplicationContext(sc);
		if (wac == null) {
			throw new IllegalStateException("No WebApplicationContext found: no ContextLoaderListener registered?");
		}
		return wac;
	}

}
