package org.springframework.context.support;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * FactoryBean that creates a Map with String keys and Resource values from
 * properties, interpreting passed-in String values as resource locations.
 *
 * <p>Extends PropertiesFactoryBean to inherit the capability of defining
 * local properties and loading from properties files.
 *
 * <p>Implements the ResourceLoaderAware interface to automatically use
 * the context ResourceLoader if running in an ApplicationContext.
 * Uses DefaultResourceLoader else.
 *
 * @author Juergen Hoeller
 * @author Keith Donald
 * @since 25.04.2004
 * @see org.springframework.core.io.DefaultResourceLoader
 */
public class ResourceMapFactoryBean extends PropertiesFactoryBean implements ResourceLoaderAware {

	private String resourceBasePath = "";

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	/**
	 * Set a base path to prepend to each resource location value
	 * in the properties file.
	 * <p>E.g.: resourceBasePath="/images", value="/test.gif"
	 * -> location="/images/test.gif"
	 */
	public void setResourceBasePath(String resourceBasePath) {
		this.resourceBasePath = resourceBasePath;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public Class getObjectType() {
		return Map.class;
	}

	protected Object createInstance() throws Exception {
		Map resourceMap = new HashMap();
		Properties props = mergeProperties();
		for (Enumeration enum = props.propertyNames(); enum.hasMoreElements();) {
			String key = (String) enum.nextElement();
			String location = props.getProperty(key);
			resourceMap.put(key, this.resourceLoader.getResource(this.resourceBasePath + location));
		}
		return resourceMap;
	}

}
