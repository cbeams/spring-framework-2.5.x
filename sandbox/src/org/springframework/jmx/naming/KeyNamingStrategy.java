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

package org.springframework.jmx.naming;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.jmx.util.ObjectNameManager;

/**
 * <code>ObjectNamingStrategy</code> implementation that builds
 * <code>ObjectName</code> instances from the key used to in the
 * "beans" map passed to <code>MBeanExporter</code>.
 *
 * <p>Can also check object name mappings, given as <code>Properties</code>
 * or as mappingLocations of properties files. The key used to look up is the key
 * used in <code>MBeanExporter</code>'s "beans" map. If no mapping is found
 * for a given key, the key itself is used to build an <code>ObjectName</code>.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @see #setMappings
 * @see #setMappingLocation
 * @see #setMappingLocations
 * @see org.springframework.jmx.MBeanExporter#setBeans
 */
public class KeyNamingStrategy implements ObjectNamingStrategy, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private Properties mappings;

	private Resource[] mappingLocations;

	private Properties mergedMappings;


	/**
	 * Set local properties, containing object name mappings, e.g. via
	 * the "props" tag in XML bean definitions. These can be considered
	 * defaults, to be overridden by properties loaded from files.
	 */
	public void setMappings(Properties mappings) {
		this.mappings = mappings;
	}

	/**
	 * Set a location of a properties file to be loaded,
	 * containing object name mappings.
	 */
	public void setMappingLocation(Resource location) {
		this.mappingLocations = new Resource[] {location};
	}

	/**
	 * Set location of properties files to be loaded,
	 * containing object name mappings.
	 */
	public void setMappingLocations(Resource[] mappingLocations) {
		this.mappingLocations = mappingLocations;
	}

	public void afterPropertiesSet() throws IOException {
		this.mergedMappings = new Properties();

		if (this.mappings != null) {
			// use propertyNames enumeration to also catch default properties
			for (Enumeration en = this.mappings.propertyNames(); en.hasMoreElements();) {
				String key = (String) en.nextElement();
				this.mergedMappings.setProperty(key, this.mappings.getProperty(key));
			}
		}

		if (this.mappingLocations != null) {
			for (int i = 0; i < this.mappingLocations.length; i++) {
				Resource location = this.mappingLocations[i];
				if (logger.isInfoEnabled()) {
					logger.info("Loading JMX object name mappings file from " + location);
				}
				InputStream is = location.getInputStream();
				try {
					this.mergedMappings.load(is);
				}
				finally {
					is.close();
				}
			}
		}
	}


	/**
	 * Attempts to retrieve the ObjectName via the given key,
	 * trying to find a mapped value in the mappings first.
	 */
	public ObjectName getObjectName(Object managedResource, String key) throws MalformedObjectNameException {
		String objectName = null;
		if (this.mergedMappings != null) {
			objectName = this.mergedMappings.getProperty(key);
		}
		if (objectName == null) {
			objectName = key;
		}
		return ObjectNameManager.getInstance(objectName);
	}

}
