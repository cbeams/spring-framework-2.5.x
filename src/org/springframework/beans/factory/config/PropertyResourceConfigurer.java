package org.springframework.beans.factory.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;

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
 * @author Juergen Hoeller
 * @since 02.10.2003
 * @see PropertyOverrideConfigurer
 * @see PropertyPlaceholderConfigurer
 */
public abstract class PropertyResourceConfigurer implements BeanFactoryPostProcessor, Ordered {

	protected final Log logger = LogFactory.getLog(getClass());

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private Resource location;

	private Properties properties;

	private boolean ignoreResourceNotFound = false;

	public void setOrder(int order) {
	  this.order = order;
	}

	public int getOrder() {
	  return order;
	}

	/**
	 * Set the location of the properties file. Allows for both a URL
	 * and a (file) path, according to the respective ApplicationContext.
	 * @see org.springframework.context.ApplicationContext#getResource
	 */
	public void setLocation(Resource location) {
		this.location = location;
	}

	/**
	 * Set the properties directly as java.util.Properties instance.
	 * Mainly useful for testing.
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
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
		Properties props = new Properties();

		if (this.location != null) {
			logger.info("Loading properties file from " + this.location + "");
			try {
				InputStream is = this.location.getInputStream();
				try {
					props.load(is);
				}
				finally {
					is.close();
				}
			}
			catch (IOException ex) {
				String msg = "Could not load properties file from " + this.location;
				if (this.ignoreResourceNotFound) {
					logger.warn(msg + ": " + ex.getMessage());
				}
				else {
					throw new BeanInitializationException(msg, ex);
				}
			}
		}

		if (this.properties != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Applying directly specified properties [" + this.properties + "]");
			}
			props.putAll(this.properties);
		}

		if (this.location != null || this.properties != null) {
			processProperties(beanFactory, props);
		}
		else {
			logger.warn("No property resource location specified");
		}
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
