package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;

/**
 * SPI interface to be implemented by most if not all bean factories.
 * Provides means to configure a bean factory in addition to the bean
 * factory client methods in the BeanFactory interface.
 *
 * <p>Allows for framework-internal plug'n'play even when needing access
 * to bean factory configuration methods.
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 * @see ConfigurableListableBeanFactory
 */
public interface ConfigurableBeanFactory extends BeanFactory {

	/**
	 * Add a new BeanPostPrcoessor that will get applied to beans
	 * created with this factory.
	 */
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

	/**
	 * Ignore the given dependency type for autowiring.
	 * <p>This will typically be used for dependencies that are resolved
	 * in other ways, like BeanFactory through BeanFactoryAware or
	 * ApplicationContext through ApplicationContextAware.
	 * @see org.springframework.beans.factory.BeanFactoryAware
	 * @see org.springframework.context.ApplicationContextAware
	 */
	void ignoreDependencyType(Class type);

	/**
	 * Return the registered PropertyValues for the given bean.
	 * @param beanName name of the bean
	 * @return the registered PropertyValues
	 */
	PropertyValues getPropertyValues(String beanName);

	/**
	 * Register property value for a specific bean, overriding an existing value.
	 * If no previous value exists, a new one will be added.
	 * <p>This is intended for bean factory post processing, i.e. overriding
	 * certain property values after parsing the original bean definitions.
	 * @param beanName name of the bean
	 * @param pv property name and value
	 * @throws org.springframework.beans.BeansException if the property values of the specified bean are immutable
	 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor
	 */
	void overridePropertyValue(String beanName, PropertyValue pv) throws BeansException;

	/**
	 * Destroy all cached singletons in this factory.
	 * To be called on shutdown of a factory.
	 */
	void destroySingletons();

}
