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

package org.springframework.web.util;

import java.io.FileNotFoundException;

import javax.servlet.ServletContext;

import org.springframework.util.Log4jConfigurer;

/**
 * Convenience class that performs custom Log4J initialization for web environments,
 * allowing for log file paths within the web application, with the option to
 * perform automatic refresh checks (for runtime changes in logging configuration).
 *
 * <p><b>WARNING: Assumes an expanded WAR file</b>, both for loading the configuration
 * file and for writing the log files. If you want to keep your WAR unexpanded or
 * don't need application-specific log files within the WAR directory, don't use
 * Log4J setup within the application (thus, don't use Log4jConfigListener or
 * Log4jConfigServlet). Instead, use a global, VM-wide Log4J setup (for example,
 * in JBoss) or JDK 1.4's java.util.logging (which is global too).
 *
 * <p>Supports two init parameters at the servlet context level (i.e. context-param
 * in web.xml):
 *
 * <ul>
 * <li><i>"log4jConfigLocation":</i><br>
 * Name of the Log4J config file (relative to the web application root directory,
 * e.g. "WEB-INF/log4j.properties"). If not specified, default Log4J initialization
 * will apply (from "log4j.properties" in the classpath).
 * <li><i>"log4jRefreshInterval":</i><br>
 * Interval between config file refresh checks, in milliseconds. If not specified,
 * no refresh checks will happen, which avoids starting Log4J's watchdog thread.
 * </ul>
 *
 * <p>Note: <code>initLogging</code> should be called before any other Spring activity
 * (when using Log4J), for proper initialization before any Spring logging attempts.
 *
 * <p>Log4J's watchdog thread will asynchronously check whether the timestamp
 * of the config file has changed, using the given interval between checks.
 * A refresh interval of 1000 milliseconds (one second), which allows to
 * do on-demand log level changes with immediate effect, is not unfeasible.

 * <p><b>WARNING:</b> Log4J's watchdog thread does not terminate until VM shutdown;
 * in particular, it does not terminate on LogManager shutdown. Therefore, it is
 * recommended to <i>not</i> use config file refreshing in a production J2EE
 * environment; the watchdog thread would not stop on application shutdown there.
 *
 * <p>This configurer automatically sets the web app root system property, for
 * "${key}" substitutions within log file locations in the Log4J config file.
 * The default system property key is "webapp.root", to be used in a Log4J config
 * file like as follows:
 *
 * <p><code>log4j.appender.myfile.File=${webapp.root}/WEB-INF/demo.log</code>
 *
 * <p>Alternatively, specify a unique context-param "webAppRootKey" per web application.
 * For example, with "webAppRootKey = "demo.root":
 *
 * <p><code>log4j.appender.myfile.File=${demo.root}/WEB-INF/demo.log</code>
 *
 * <p><b>WARNING:</b> Some containers (like Tomcat) do <i>not</i> keep system properties
 * separate per web app. You have to use unique "webAppRootKey" context-params per web
 * app then, to avoid clashes. Other containers like Resin do isolate each web app's
 * system properties: Here you can use the default key (i.e. no "webAppRootKey"
 * context-param at all) without worrying.
 *
 * @author Juergen Hoeller
 * @since 12.08.2003
 * @see org.springframework.util.Log4jConfigurer
 * @see Log4jConfigListener
 * @see Log4jConfigServlet
 */
public abstract class Log4jWebConfigurer {

	/** Parameter specifying the location of the Log4J config file */
	public static final String CONFIG_LOCATION_PARAM = "log4jConfigLocation";

	/** Parameter specifying the refresh interval for checking the Log4J config file */
	public static final String REFRESH_INTERVAL_PARAM = "log4jRefreshInterval";


	public static void initLogging(ServletContext servletContext) {

		// Set the web app root system property.
		WebUtils.setWebAppRootSystemProperty(servletContext);

		// Only perform custom Log4J initialization in case of a config file.
		String location = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
		if (location != null) {

			// Interpret location as relative to the web application root directory
			if (!location.startsWith("/")) {
				location = "/" + location;
			}

			// Write log message to server log.
			servletContext.log("Initializing Log4J from [" + location + "]");

			// Perform actual Log4J initialization.
			try {
				String realPath = servletContext.getRealPath(location);
				if (realPath == null) {
					throw new FileNotFoundException(
							"ServletContext resource [" + realPath + "] cannot be resolved to absolute file path - " +
							"web application archive not expanded?");
				}

				// Check whether refresh interval was specified.
				String intervalString = servletContext.getInitParameter(REFRESH_INTERVAL_PARAM);
				if (intervalString != null) {
					// Initialize with refresh interval, i.e. with Log4J's watchdog thread,
					// checking the file in the background.
					try {
						long refreshInterval = Long.parseLong(intervalString);
						Log4jConfigurer.initLogging(realPath, refreshInterval);
					}
					catch (NumberFormatException ex) {
						throw new IllegalArgumentException("Invalid 'log4jRefreshInterval' parameter: " + ex.getMessage());
					}
				}
				else {
					// Initialize without refresh check, i.e. without Log4J's watchdog thread.
					Log4jConfigurer.initLogging(realPath);
				}
			}
			catch (FileNotFoundException ex) {
				throw new IllegalArgumentException("Invalid 'log4jConfigLocation' parameter: " + ex.getMessage());
			}
		}
	}

	/**
	 * Shutdown Log4J to release all file locks.
	 */
	public static void shutdownLogging(ServletContext servletContext) {
		servletContext.log("Shutting down Log4J");
		Log4jConfigurer.shutdownLogging();
	}

}
