package org.springframework.web.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Bootstrap listener to start up Spring's root WebApplicationContext.
 * Simply delegates to ContextLoader.
 *
 * <p>This listener should be registered after Log4jConfigListener
 * in web.xml, if the latter is used.
 *
 * <p>For Servlet 2.2 containers and Servlet 2.3 ones that do not
 * initalize listeners before servlets, use ContextLoaderServlet.
 * See the latter's javadoc for details.
 *
 * @author Juergen Hoeller
 * @since 17.02.2003
 * @see ContextLoader
 * @see ContextLoaderServlet
 * @see org.springframework.web.util.Log4jConfigListener
 */
public class ContextLoaderListener implements ServletContextListener {

	/**
	 * Initialize the root web application context.
	 */
	public void contextInitialized(ServletContextEvent event) {
		ContextLoader.initContext(event.getServletContext());
	}

	/**
	 * Close the root web application context.
	 */ 
	public void contextDestroyed(ServletContextEvent event) {
		ContextLoader.closeContext(event.getServletContext());
	}

}
