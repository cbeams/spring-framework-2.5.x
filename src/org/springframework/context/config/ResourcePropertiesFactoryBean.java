package org.springframework.context.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Allows for making a properties file from a resource location available
 * as Properties instance in an application context. Can be used for to
 * populate any bean property of type Properties via a bean reference.
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
 * @see org.springframework.context.ApplicationContext#getResourceAsStream
 * @see org.springframework.beans.factory.config.PropertiesFactoryBean
 */
public class ResourcePropertiesFactoryBean extends PropertiesFactoryBean implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * Set the location of the properties file as application context resource,
	 * e.g. "/myprops.properties" (relative to the resource base path in this case).
	 */
	protected Properties loadProperties() throws IOException {
		logger.info("Loading properties file from context resource location [" + getLocation() + "]");
		Properties properties = new Properties();
		properties.load(this.applicationContext.getResourceAsStream(getLocation()));
		return properties;
	}

}
