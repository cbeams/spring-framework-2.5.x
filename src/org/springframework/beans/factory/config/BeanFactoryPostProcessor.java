package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Allows for custom modification of an application context's bean definitions,
 * adapting the bean property values of the context's underlying bean factory.
 *
 * <p>Application contexts can auto-detect BeanFactoryPostProcessor beans in
 * their bean definitions and apply them before any other beans get created.
 *
 * <p>Useful for custom config files targeted at system administrators that
 * override bean properties configured in the application context.
 *
 * <p>See PropertyResourceConfigurer and its concrete implementations
 * for out-of-the-box solutions that address such configuration needs.
 *
 * @author Juergen Hoeller
 * @since 06.07.2003
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see org.springframework.context.config.PropertyResourceConfigurer
 */
public interface BeanFactoryPostProcessor {

	/**
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for overriding or adding
	 * properties even to eager-initializing beans.
	 * @param beanFactory the bean factory used by the application context
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see ConfigurableBeanFactory#overridePropertyValue
	 */
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
