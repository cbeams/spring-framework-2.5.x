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
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PropertiesPersister;

/**
 * Allows for configuration of individual bean property values from a property resource,
 * i.e. a properties file. Useful for custom config files targetted at system
 * administrators that override bean properties configured in the application context.
 *
 * <p>2 concrete implementations are provided in the distribution:
 * <ul>
 * <li>PropertyOverrideConfigurer for "beanName.property=value" style overriding
 * (<i>pushing</i> values from a properties file into bean definitions)
 * <li>PropertyPlaceholderConfigurer for replacing "${...}" placeholders
 * (<i>pulling</i> values from a properties file into bean definitions)
 * </ul>
 *
 * <p>Property values can be converted after reading them in, through overriding
 * the <code>convertPropertyValue</code> method. For example, encrypted values
 * can be detected and decrypted accordingly before processing them.
 *
 * @author Juergen Hoeller
 * @since 02.10.2003
 * @see PropertyOverrideConfigurer
 * @see PropertyPlaceholderConfigurer
 * @see #convertPropertyValue
 */
public abstract class PropertyResourceConfigurer implements BeanFactoryPostProcessor, Ordered {

	protected final Log logger = LogFactory.getLog(getClass());

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private Properties properties;

	private Resource[] locations;

	private String fileEncoding;

	private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();

	private boolean ignoreResourceNotFound = false;


	public void setOrder(int order) {
	  this.order = order;
	}

	public int getOrder() {
	  return order;
	}

	/**
	 * Set local properties, e.g. via the "props" tag in XML bean definitions.
	 * These can be considered defaults, to be overridden by properties
	 * loaded from files.
	 * @see #setLocation
	 * @see #setLocations
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Set a location of a properties file to be loaded.
	 * @see #setLocations
	 */
	public void setLocation(Resource location) {
		this.locations = new Resource[] {location};
	}

	/**
	 * Set locations of properties files to be loaded.
	 * @see #setLocation
	 */
	public void setLocations(Resource[] locations) {
		this.locations = locations;
	}

	/**
	 * Set the encoding to use for parsing properties files.
	 * Default is none, using java.util.Properties' default encoding.
	 * @see org.springframework.util.PropertiesPersister#load
	 */
	public void setFileEncoding(String encoding) {
		this.fileEncoding = encoding;
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
	 * Set if failure to find the property resource should be ignored.
	 * True is appropriate if the properties file is completely optional.
	 * Default is false.
	 */
	public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
		this.ignoreResourceNotFound = ignoreResourceNotFound;
	}


	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Properties mergedProps = new Properties();

		if (this.properties != null) {
			// use propertyNames enumeration to also catch default properties
			for (Enumeration en = this.properties.propertyNames(); en.hasMoreElements();) {
				String key = (String) en.nextElement();
				mergedProps.setProperty(key, this.properties.getProperty(key));
			}
		}

		if (this.locations != null) {
			for (int i = 0; i < this.locations.length; i++) {
				Resource location = this.locations[i];
				if (logger.isInfoEnabled()) {
					logger.info("Loading properties from " + location + "");
				}
				try {
					InputStream is = location.getInputStream();
					try {
						if (this.fileEncoding != null) {
							this.propertiesPersister.load(mergedProps, new InputStreamReader(is, this.fileEncoding));
						}
						else {
							this.propertiesPersister.load(mergedProps, is);
						}
					}
					finally {
						is.close();
					}
				}
				catch (IOException ex) {
					String msg = "Could not load properties from " + location;
					if (this.ignoreResourceNotFound) {
						if (logger.isWarnEnabled()) {
							logger.warn(msg + ": " + ex.getMessage());
						}
					}
					else {
						throw new BeanInitializationException(msg, ex);
					}
				}
			}
		}

		processProperties(beanFactory, mergedProps);
	}

	/**
	 * Convert the given merged properties, converting property values
	 * if necessary. The result will then be processed.
	 * <p>Default implementation will invoke <code>convertPropertyValue</code>
	 * for each property value, replacing the original with the converted value.
	 * @see #convertPropertyValue
	 * @see #processProperties
	 */
	protected void convertProperties(Properties props) {
		Enumeration propertyNames = props.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			String propertyValue = props.getProperty(propertyName);
			String convertedValue = convertPropertyValue(propertyValue);
			if (!ObjectUtils.nullSafeEquals(propertyValue, convertedValue)) {
				props.setProperty(propertyName, convertedValue);
			}
		}
	}

	/**
	 * Convert the given property value from the properties source
	 * to the value that should be applied.
	 * <p>Default implementation simply returns the original value.
	 * Can be overridden in subclasses, for example to detect
	 * encrypted values and decrypt them accordingly.
	 * @param originalValue the original value from the properties source
	 * (properties file or local "properties")
	 * @return the converted value, to be used for processing
	 * @see #setProperties
	 * @see #setLocations
	 * @see #setLocation
	 */
	protected String convertPropertyValue(String originalValue) {
		return originalValue;
	}

	/**
	 * Apply the given Properties to the bean factory.
	 * @param beanFactory	the bean factory used by the application context
	 * @param props the Properties to apply
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	protected abstract void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException;

}
