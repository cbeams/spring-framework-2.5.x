package org.springframework.beans.factory.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassLoaderUtils;

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

	private String location;

	private Properties properties;

	private boolean singleton = true;

	private Properties singletonInstance;

	/**
	 * Set the location of the properties file as class path resource,
	 * e.g. "/myprops.properties".
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	protected String getLocation() {
		return location;
	}

	/**
	 * Set local properties on this FactoryBean, e.g. via the "props" tag
	 * in XML bean definitions.
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Create a singleton instance on initialization if in singleton mode.
	 */
	public void afterPropertiesSet() throws IOException {
		if (this.location == null && this.properties == null) {
			throw new IllegalArgumentException("Either location (e.g. '/myprops.properties') or properties must be set");
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
		Properties result = (this.location != null) ? loadProperties() : new Properties();
		if (this.properties != null) {
			result.putAll(this.properties);
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
		logger.info("Loading properties file from class path location [" + this.location + "]");
		Properties properties = new Properties();
		properties.load(ClassLoaderUtils.getResourceAsStream(getClass(), this.location));
		return properties;
	}

	public Class getObjectType() {
		return Properties.class;
	}

	public boolean isSingleton() {
		return singleton;
	}

	/**
	 * Set if a singleton should be created, or a new object
	 * on each request else. Default is true.
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

}
