/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.context.support;

import javax.servlet.ServletContext;

import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * Utilities common to all WebApplicationContext implementations.
 *
 * <p>Features convenient methods to retrieve the root WebApplicationContext
 * for a given ServletContext. This is e.g. useful for accessing a Spring
 * context from within custom web views or Struts actions.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: WebApplicationContextUtils.java,v 1.6 2003-12-09 08:45:22 jhoeller Exp $
 * @see #getWebApplicationContext
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
	 * @return the root WebApplicationContext for this web app, or null if none
	 * @throws IllegalStateException if the root WebApplicationContext could not be found
	 * @see org.springframework.web.context.WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	public static WebApplicationContext getRequiredWebApplicationContext(ServletContext sc) throws IllegalStateException {
		WebApplicationContext wac =
				(WebApplicationContext) sc.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		if (wac == null) {
			throw new IllegalStateException("No WebApplicationContext found: no ContextLoaderListener registered?");
		}
		return wac;
	}

	/**
	 * Expose the given WebApplicationContext as an attribute of the ServletContext
	 * it references.
	 * @param wac the WebApplicationContext to expose
	 * @see org.springframework.web.context.WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	public static void publishWebApplicationContext(WebApplicationContext wac) {
		// Set WebApplicationContext as an attribute in the ServletContext
		// so other components in this web application can access it
		ServletContext sc = wac.getServletContext();
		if (sc == null) {
			throw new IllegalArgumentException("ServletContext can't be null in WebApplicationContext " + wac);
		}
		sc.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
	}

	/**
	 * Parse the given context config location into potentially separate file paths.
	 * @param location the location string
	 * @return an array of file paths
	 * @see org.springframework.web.context.ConfigurableWebApplicationContext#CONFIG_LOCATION_DELIMITERS
	 */
	public static String[] parseContextConfigLocation(String location) {
		return StringUtils.tokenizeToStringArray(location,
																						 ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS,
																						 true, true);
	}

}
