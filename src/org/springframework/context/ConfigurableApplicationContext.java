package org.springframework.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;

/**
 * SPI interface to be implemented by most if not all application contexts.
 * Provides means to configure an application context in addition to the
 * application context client methods in the ApplicationContext interface.
 *
 * <p>Configuration and lifecycle methods are encapsulated here to avoid
 * making them obvious to ApplicationContext client code.
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 */
public interface ConfigurableApplicationContext extends ApplicationContext {

	/**
	 * Set the parent of this application context.
	 * <p>Note that the parent shouldn't be changed: It should only be set outside
	 * a constructor if it isn't available when an object of this class is created,
	 * for example in case of WebApplicationContext setup.
	 * @param parent the parent context
	 * @see org.springframework.web.context.ConfigurableWebApplicationContext
	 */
	void setParent(ApplicationContext parent);

	/**
	 * Return the internal bean factory of this application context.
	 * Can be used to access specific functionality of the factory.
	 */
	ConfigurableListableBeanFactory getBeanFactory();

	/**
	 * Load or refresh the persistent representation of the configuration,
	 * which might an XML file, properties file, or relational database schema.
	 * @throws org.springframework.context.ApplicationContextException if the config cannot be loaded
	 * @throws org.springframework.beans.BeansException if the bean factory could not be initialized
	 */
	void refresh() throws BeansException;

	/**
	 * Close this application context, releasing all resources and locks that the
	 * implementation might hold. This includes disposing all cached singleton beans.
	 * <p>Note: Does <i>not</i> invoke close on a parent context.
	 * @throws org.springframework.context.ApplicationContextException if there were fatal errors
	 */
	void close() throws ApplicationContextException;

}
