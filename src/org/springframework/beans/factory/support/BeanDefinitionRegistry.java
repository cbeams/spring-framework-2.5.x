package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;

/**
 * Interface for registries that hold bean definitions,
 * i.e. RootBeanDefinition and ChildBeanDefinition instances.
 *
 * <p>Typically implemented by bean factories that work with
 * the AbstractBeanDefinition hierarchy internally.
 *
 * @author Juergen Hoeller
 * @since 26.11.2003
 * @version $Id: BeanDefinitionRegistry.java,v 1.4 2004-01-13 18:12:07 jhoeller Exp $
 */
public interface BeanDefinitionRegistry {

	/**
	 * Return the number of beans defined in the registry.
	 * @return the number of beans defined in the registry
	 */
	int getBeanDefinitionCount();

	/**
	 * Return the names of all beans defined in this registry.
	 * @return the names of all beans defined in this registry,
	 * or an empty array if none defined
	 */
	String[] getBeanDefinitionNames();

	/**
	 * Return the bean definition for the given bean name.
	 * @param name name of the bean to find a definition for
	 * @return the BeanDefinition for this prototype name. Must never return null.
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if the bean definition cannot be resolved
	 * @throws BeansException in case of errors
	 */
	AbstractBeanDefinition getBeanDefinition(String name) throws BeansException;

	/**
	 * Check if this registry contains a bean definition with the given name.
	 * @param name the name of the bean to look for
	 * @return if this bean factory contains a bean definition with the given name
	 */
	boolean containsBeanDefinition(String name);

	/**
	 * Register a new bean definition with this registry.
	 * Must support RootBeanDefinition and ChildBeanDefinition.
	 * @param name the name of the bean instance to register
	 * @param beanDefinition definition of the bean instance to register
	 * @throws BeansException if the bean definition is invalid
	 * @see RootBeanDefinition
	 * @see ChildBeanDefinition
	 */
	void registerBeanDefinition(String name, AbstractBeanDefinition beanDefinition)
			throws BeansException;

	/**
	 * Given a bean name, create an alias. We typically use this method to
	 * support names that are illegal within XML ids (used for bean names).
	 * @param name the name of the bean
	 * @param alias alias that will behave the same as the bean name
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if there is no bean with the given name
	 * @throws BeansException if the alias is already in use
	 */
	void registerAlias(String name, String alias) throws BeansException;

}
