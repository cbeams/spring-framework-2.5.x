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
 * @author Juergen Hoeller
 * @since 13.03.2003
 * @see org.springframework.web.util.Log4jWebConfigurer
 * @see org.springframework.web.util.Log4jConfigListener
 */
public abstract class Log4jConfigurer {

	public static final long DEFAULT_REFRESH_INTERVAL = FileWatchdog.DEFAULT_DELAY;

	public static final String XML_FILE_EXTENSION = ".xml";

	/**
	 * Initialize Log4J with the given configuration and the default refresh interval.
	 * (org.apache.log4j.helpers.FileWatchdog.DEFAULT_DELAY, normally 60 seconds).
	 * @param location location of the property config file
	 * @throws FileNotFoundException if the location specifies an invalid file path
	 */
	public static void initLogging(String location) throws FileNotFoundException {
		initLogging(location, DEFAULT_REFRESH_INTERVAL);
	}

	/**
	 * Initialize Log4J with the given configuration.
	 * Assumes an XML file in case of a ".xml" file extension.
	 * @param location location of the config file
	 * @param refreshInterval interval between config file refresh checks, in milliseconds
	 * @throws FileNotFoundException if the location specifies an invalid file path
	 */
	public static void initLogging(String location, long refreshInterval) throws FileNotFoundException {
		if (!(new File(location)).exists()) {
			throw new FileNotFoundException("Log4j config file [" + location + "] not found");
		}
		if (location.toLowerCase().endsWith(XML_FILE_EXTENSION)) {
			DOMConfigurator.configureAndWatch(location, refreshInterval);
		} else {
			PropertyConfigurator.configureAndWatch(location, refreshInterval);
		}
	}

	/**
	 * Shutdown Log4J to release all file locks.
	 */
	public static void shutdownLogging() {
		LogManager.shutdown();
	}

	/**
	 * Set the specified system property to the current working directory.
	 * This can be used e.g. for test environments, for applications that leverage
	 * Log4jWebConfigurer's "webAppRootKey" support in a web environment.
	 * @param key system property key to use
	 * @see org.springframework.web.util.Log4jWebConfigurer
	 */
	public static void setWorkingDirSystemProperty(String key) {
		System.setProperty(key, new File("").getAbsolutePath());
	}

}
