package org.springframework.context.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.Ordered;

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
public abstract class PropertyResourceConfigurer extends ApplicationObjectSupport
    implements BeanFactoryPostProcessor, Ordered {

	protected final Log logger = LogFactory.getLog(getClass());

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private String location;

	private Properties properties;

	public void setOrder(int order) {
	  this.order = order;
	}

	public int getOrder() {
	  return order;
	}

	/**
	 * Set the location of the properties file. Allows for both a URL
	 * and a (file) path, according to the respective ApplicationContext.
	 * @see org.springframework.context.ApplicationContext#getResourceAsStream
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * Set the properties directly as java.util.Properties instance.
	 * Mainly useful for testing.
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Properties props = new Properties();

		if (this.location != null) {
			logger.info("Loading properties file [" + this.location + "]");
			try {
				props.load(getApplicationContext().getResourceAsStream(this.location));
			}
			catch (IOException ex) {
				logger.warn("Could not load properties [" + this.location + "]: " + ex.getMessage());
			}
		}

		if (this.properties != null) {
			if (logger.isDebugEnabled())
				logger.debug("Applying directly specified properties [" + this.properties + "]");
			props.putAll(this.properties);
		}

		if (this.location != null || this.properties != null) {
			processProperties(beanFactory, props);
		}
		else {
			logger.warn("No property resource location specified");
		}
	}

	protected abstract void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException;

}
