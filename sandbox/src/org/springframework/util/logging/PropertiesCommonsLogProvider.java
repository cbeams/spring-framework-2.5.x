package org.springframework.util.logging;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Rob Harrop
 */
public class PropertiesCommonsLogProvider implements CommonsLogProvider {

	private CommonsLogProvider fallbackProvider;

	private Properties properties;

	public Log getLogForBean(Object bean, String beanName) {

		if ((properties != null) && (properties.containsKey(beanName))) {
			return LogFactory.getLog(properties.getProperty(beanName));
		} else if (fallbackProvider != null) {
			return fallbackProvider.getLogForBean(bean, beanName);
		} else {
			throw new CommonsLogProviderException(
					"Unable to locate Log for bean: " + beanName);
		}
	}

	public void setFallbackProvider(CommonsLogProvider fallbackProvider) {
		this.fallbackProvider = fallbackProvider;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

}
