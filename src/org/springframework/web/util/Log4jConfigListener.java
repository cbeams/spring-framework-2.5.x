package org.springframework.web.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Bootstrap listener for custom Log4J initialization in a web environment.
 * Simply delegates to Log4jWebConfigurer.
 *
 * <p>This listener should be registered before ContextLoaderListener
 * in web.xml, when using custom Log4J initialization.
 *
 * <p>For Servlet 2.2 containers and Servlet 2.3 ones that do not
 * initalize listeners before servlets, use Log4jConfigServlet.
 * See the ContextLoaderServlet javadoc for details.
 *
 * @author Juergen Hoeller
 * @since 13.03.2003
 * @see org.springframework.web.util.Log4jWebConfigurer
 * @see Log4jConfigServlet
 * @see org.springframework.web.context.ContextLoaderListener
 * @see org.springframework.web.context.ContextLoaderServlet
 * @see WebAppRootListener
 */
public class Log4jConfigListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		Log4jWebConfigurer.initLogging(event.getServletContext());
	}

	public void contextDestroyed(ServletContextEvent event) {
		Log4jWebConfigurer.shutdownLogging(event.getServletContext());
	}

}
