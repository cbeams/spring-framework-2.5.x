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

package org.springframework.beans.factory.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

/**
 * Allows for making a properties file from a classpath location available
 * as Properties instance in a bean factory. Can be used for to populate
 * any bean property of type Properties via a bean reference.
 *
 * <p>Supports loading from a properties file and/or setting local properties
 * on this FactoryBean. The created Properties instance will be merged from
 * loaded and local values. If neither a location nor local properties are set,
 * an exception will be thrown on initialization.
 *
 * <p>Can create a singleton or a new object on each request.
 * Default is singleton.
 *
 * @author Juergen Hoeller
 * @see java.util.Properties
 */
public class PropertiesFactoryBean implements FactoryBean, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private Properties properties;

	private Resource[] locations;

	private String charset;

	private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();

	private boolean singleton = true;

	private Properties singletonInstance;


	/**
	 * Set local properties, e.g. via the "props" tag in XML bean definitions.
	 * These can be considered defaults, to be overridden by properties
	 * loaded from files.
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Set a location of a properties file to be loaded.
	 */
	public void setLocation(Resource location) {
		this.locations = new Resource[] {location};
	}

	/**
	 * Set locations of properties files to be loaded.
	 */
	public void setLocations(Resource[] locations) {
		this.locations = locations;
	}

	/**
	 * Set the charset to use for parsing properties files.
	 * Default is none, using java.util.Properties' default charset.
	 * @see org.springframework.util.PropertiesPersister#load
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * Set the PropertiesPersister to use for parsing properties files.
	 * The default is DefaultPropertiesPersister.
	 * @see org.springframework.util.DefaultPropertiesPersister
	 */
	public void setPropertiesPersister(PropertiesPersister propertiesPersister) {
		this.propertiesPersister = propertiesPersister;
	}

	/**
	 * Set if a singleton should be created, or a new object
	 * on each request else. Default is true.
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	/**
	 * Create a singleton instance on initialization if in singleton mode.
	 */
	public void afterPropertiesSet() throws IOException {
		if (this.properties == null && this.locations == null) {
			throw new IllegalArgumentException("Either properties or location(s) must be set");
		}
		if (this.singleton) {
			this.singletonInstance = mergeProperties();
		}
	}


	/**
	 * Return either singleton instance or newly created instance,
	 * depending on the singleton property of this FactoryBean.
	 * Delegates to mergeProperties for actual instance creation.
	 * @see #mergeProperties
	 */
	public Object getObject() throws IOException {
		if (this.singleton) {
			return this.singletonInstance;
		}
		else {
			return mergeProperties();
		}
	}

	/**
	 * Return a merged Properties instance containing both the
	 * loaded properties and properties set on this FactoryBean.
	 */
	protected Properties mergeProperties() throws IOException {
		Properties result = new Properties();
		if (this.properties != null) {
			result.putAll(this.properties);
		}
		if (this.locations != null) {
			result.putAll(loadProperties());
		}
		return result;
	}

	/**
	 * Load the Properties instance. Invoked either by afterPropertiesSet
	 * or by getObject, depending on singleton or prototype mode.
	 * @return the freshly loaded Properties instance
	 * @throws java.io.IOException in case of I/O errors.
	 */
	protected Properties loadProperties() throws IOException {
		Properties props = new Properties();
		for (int i = 0; i < this.locations.length; i++) {
			Resource location = this.locations[i];
			logger.info("Loading props file from " + location);
			InputStream is = location.getInputStream();
			try {
				if (this.charset != null) {
					this.propertiesPersister.load(props, new InputStreamReader(is, this.charset));
				}
				else {
					this.propertiesPersister.load(props, is);
				}
			}
			finally {
				is.close();
			}
		}
		return props;
	}

	public Class getObjectType() {
		return Properties.class;
	}

	public boolean isSingleton() {
		return singleton;
	}

}
