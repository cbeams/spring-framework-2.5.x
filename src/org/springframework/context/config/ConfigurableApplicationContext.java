package org.springframework.context.config;

import org.springframework.beans.BeansException;
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
	 * Load or refresh the persistent representation of the configuration,
	 * which might for example be an XML file, properties file or
	 * relational database schema.
	 * @throws ApplicationContextException if the config cannot be loaded
	 * @throws BeansException if the bean factory could not be initialized
	 */
	void refresh() throws BeansException;

	/**
	 * Close this application context, releasing all resources and locks
	 * that the implementation might hold. This includes disposing all
	 * cached singleton beans.
	 * <p>Note: Does <i>not</i> invoke close on a parent context.
	 * @throws ApplicationContextException if there were fatal errors
	 */
	void close() throws ApplicationContextException;

}
