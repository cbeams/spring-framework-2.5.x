/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.context.support;

import javax.servlet.ServletContext;

import org.springframework.web.context.RootWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * Utilities common to all WebApplicationContext implementations.
 *
 * <p>Features a convenient method to retrieve the root WebApplicationContext
 * for a given ServletContext. This is e.g. useful for accessing a Spring
 * context from within custom web views or Struts actions.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: WebApplicationContextUtils.java,v 1.5 2003-12-06 15:52:00 jhoeller Exp $
 * @see #getWebApplicationContext
 * @see org.springframework.web.context.ContextLoader
 */
public abstract class WebApplicationContextUtils {

	/**
	 * Find the root WebApplicationContext for this web app, which is
	 * typically loaded via ContextLoaderListener or ContextLoaderServlet.
	 * @param sc ServletContext to find the web application context for
	 * @return the root WebApplicationContext for this web app, or null if none
	 */
	public static WebApplicationContext getWebApplicationContext(ServletContext sc) {
		return (WebApplicationContext) sc.getAttribute(RootWebApplicationContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE_NAME);
	}

	/**
	 * Find the root WebApplicationContext for this web app, which is
	 * typically loaded via ContextLoaderListener or ContextLoaderServlet.
	 * @param sc ServletContext to find the web application context for
	 * @return the root WebApplicationContext for this web app, or null if none
	 * @throws IllegalStateException if the root WebApplicationContext could not be found
	 */
	public static WebApplicationContext getRequiredWebApplicationContext(ServletContext sc) throws IllegalStateException {
		WebApplicationContext wac =
				(WebApplicationContext) sc.getAttribute(RootWebApplicationContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE_NAME);
		if (wac == null) {
			throw new IllegalStateException("No WebApplicationContext found: no ContextLoaderListener registered?");
		}
		return wac;
	}

	/**
	 * Expose the given WebApplcicationContext as an attribute of the
	 * ServletContext it references.
	 */
	public static void publishWebApplicationContext(WebApplicationContext wac) {
		// Set WebApplicationContext as an attribute in the ServletContext
		// so other components in this web application can access it
		ServletContext sc = wac.getServletContext();
		if (sc == null) {
			throw new IllegalArgumentException("ServletContext can't be null in WebApplicationContext " + wac);
		}
		sc.setAttribute(RootWebApplicationContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE_NAME, wac);
	}

}
