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

package org.springframework.util;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.FileWatchdog;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Convenience class that features simple methods for custom Log4J configuration.
 *
 * <p>Only needed for non-default Log4J initialization, for example with a custom
 * config location or a refresh interval. By default, Log4J will simply read its
 * configuration from a "log4j.properties" file in the root of the class path.
 *
 * <p>For web environments, the analogous Log4jWebConfigurer class can be found
 * in the web package. In a J2EE web application, Log4J is usually set up via
 * Log4jConfigListener or Log4jConfigServlet.
 *
 * @author Juergen Hoeller
 * @since 13.03.2003
 * @see org.springframework.web.util.Log4jWebConfigurer
 * @see org.springframework.web.util.Log4jConfigListener
 * @see org.springframework.web.util.Log4jConfigServlet
 */
public abstract class Log4jConfigurer {

	/**
	 * Default refresh interval, previously used for initLogging(location).
	 * @deprecated Either choose no config file refreshing (initLogging with plain
	 * location) or specify an explicit refresh interval. As of Spring 1.1.3, there
	 * is no default refresh interval; the default is no config file refreshing now.
	 * The rationale is to avoid Log4J's watchdog thread (which never terminates)
	 * unless explicitly required.
	 * @see #initLogging(String)
	 * @see #initLogging(String, long)
	 */
	public static final long DEFAULT_REFRESH_INTERVAL = FileWatchdog.DEFAULT_DELAY;

	public static final String XML_FILE_EXTENSION = ".xml";


	/**
	 * Initialize Log4J from the given location, with no config file refreshing.
	 * Assumes an XML file in case of a ".xml" file extension, and a properties
	 * file else.
	 * @param location the location of the config file
	 * @throws FileNotFoundException if the location specifies an invalid file path
	 * @see #DEFAULT_REFRESH_INTERVAL
	 */
	public static void initLogging(String location) throws FileNotFoundException {
		if (!(new File(location)).exists()) {
			throw new FileNotFoundException("Log4J config file [" + location + "] not found");
		}
		if (location.toLowerCase().endsWith(XML_FILE_EXTENSION)) {
			DOMConfigurator.configure(location);
		}
		else {
			PropertyConfigurator.configure(location);
		}
	}

	/**
	 * Initialize Log4J from the given location, with the given refresh interval
	 * for the config file. Assumes an XML file in case of a ".xml" file extension,
	 * and a properties file else.
	 * <p>Log4J's watchdog thread will asynchronously check whether the timestamp
	 * of the config file has changed, using the given interval between checks.
	 * A refresh interval of 1000 milliseconds (one second), which allows to
	 * do on-demand log level changes with immediate effect, is not unfeasible.
	 * <p><b>WARNING:</b> Log4J's watchdog thread does not terminate until VM shutdown;
	 * in particular, it does not terminate on LogManager shutdown. Therefore, it is
	 * recommended to <i>not</i> use config file refreshing in a production J2EE
	 * environment; the watchdog thread would not stop on application shutdown there.
	 * @param location the location of the config file
	 * @param refreshInterval interval between config file refresh checks, in milliseconds
	 * @throws FileNotFoundException if the location specifies an invalid file path
	 */
	public static void initLogging(String location, long refreshInterval) throws FileNotFoundException {
		if (!(new File(location)).exists()) {
			throw new FileNotFoundException("Log4J config file [" + location + "] not found");
		}
		if (location.toLowerCase().endsWith(XML_FILE_EXTENSION)) {
			DOMConfigurator.configureAndWatch(location, refreshInterval);
		}
		else {
			PropertyConfigurator.configureAndWatch(location, refreshInterval);
		}
	}

	/**
	 * Shut down Log4J, properly releasing all file locks.
	 * <p>This isn't strictly necessary, but recommended for shutting down
	 * Log4J in a scenario where the host VM stays alive (for example, when
	 * shutting down an application in a J2EE environment).
	 */
	public static void shutdownLogging() {
		LogManager.shutdown();
	}

	/**
	 * Set the specified system property to the current working directory.
	 * <p>This can be used e.g. for test environments, for applications that leverage
	 * Log4jWebConfigurer's "webAppRootKey" support in a web environment.
	 * @param key system property key to use, as expected in Log4j configuration
	 * (for example: "demo.root", used as "${demo.root}/WEB-INF/demo.log")
	 * @see org.springframework.web.util.Log4jWebConfigurer
	 */
	public static void setWorkingDirSystemProperty(String key) {
		System.setProperty(key, new File("").getAbsolutePath());
	}

}
