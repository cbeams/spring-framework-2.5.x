/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.web.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Bootstrap listener for custom Log4J initialization in a web environment.
 * Delegates to Log4jWebConfigurer (see its javadoc for configuration details).
 *
 * <b>WARNING: Assumes an expanded WAR file</b>, both for loading the configuration
 * file and for writing the log files. If you want to keep your WAR unexpanded or
 * don't need application-specific log files within the WAR directory, don't use
 * Log4J setup within the application (thus, don't use Log4jConfigListener or
 * Log4jConfigServlet). Instead, use a global, VM-wide Log4J setup (for example,
 * in JBoss) or JDK 1.4's <code>java.util.logging</code> (which is global too).
 *
 * <p>This listener should be registered before ContextLoaderListener in web.xml,
 * when using custom Log4J initialization.
 *
 * <p>For Servlet 2.2 containers and Servlet 2.3 ones that do not
 * initalize listeners before servlets, use Log4jConfigServlet.
 * See the ContextLoaderServlet javadoc for details.
 *
 * @author Juergen Hoeller
 * @since 13.03.2003
 * @see Log4jWebConfigurer
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
