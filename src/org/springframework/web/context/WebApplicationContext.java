/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.context;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.ui.context.ThemeSource;

/** 
 * Interface to provide configuration for a web application. This is read-only while
 * the application is running, but may be reloaded if the implementation supports this.
 *
 * <p>This interface adds ServletContext methods to the generic ApplicationContext
 * interface, and defines a well-known application attribute name that the root
 * context must be bound to in the bootstrap process.
 *
 * <p>In addition to standard application context lifecycle capabilities,
 * WebApplicationContext implementations need to detect ServletContextAware
 * beans and invoke the setServletContext accordingly.
 *
 * <p>Like generic application contexts, web application contexts are hierarchical.
 * There is a single root context per application, while each servlet in the application
 * (including a dispatcher servlet in the MVC framework) has its own child context.
 *
 * @author Rod Johnson
 * @since January 19, 2001
 * @version $Revision: 1.9 $
 * @see ServletContextAware
 */
public interface WebApplicationContext extends ApplicationContext, ThemeSource {

	/**
	 * Context attribute to bind root WebApplicationContext to on successful startup.
	 */
	String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class + ".ROOT";

	/**
	 * Return the standard Servlet API ServletContext for this application.
	 */
	ServletContext getServletContext();
	
}
