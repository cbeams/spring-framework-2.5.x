/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Interface for registries that hold bean definitions,
 * for example RootBeanDefinition and ChildBeanDefinition instances.
 * Typically implemented by bean factories that internally
 * work with the AbstractBeanDefinition hierarchy.
 *
 * <p>This is the only interface in Spring's bean factory packages
 * that encapsulates <i>registration</i> of bean definitions.
 * The standard bean factory interfaces only cover access to a
 * fully configured factory instance.
 *
 * <p>Spring's bean definition readers expect to work on an
 * implementation of this interface. Known implementors are
 * DefaultListableBeanFactory and GenericApplicationContext,
 * for example.
 *
 * @author Juergen Hoeller
 * @since 26.11.2003
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see AbstractBeanDefinition
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 * @see DefaultListableBeanFactory
 * @see org.springframework.context.support.GenericApplicationContext
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 * @see PropertiesBeanDefinitionReader
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
	 * Check if this registry contains a bean definition with the given name.
	 * @param beanName the name of the bean to look for
	 * @return if this bean factory contains a bean definition with the given name
	 */
	boolean containsBeanDefinition(String beanName);

	/**
	 * Return the BeanDefinition for the given bean name.
	 * @param beanName name of the bean to find a definition for
	 * @return the BeanDefinition for the given name (never <code>null</code>)
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition
	 */
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * Register a new bean definition with this registry.
	 * Must support RootBeanDefinition and ChildBeanDefinition.
	 * @param beanName the name of the bean instance to register
	 * @param beanDefinition definition of the bean instance to register
	 * @throws BeansException if the bean definition is invalid
	 * @see RootBeanDefinition
	 * @see ChildBeanDefinition
	 */
	void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeansException;

	/**
	 * Return the aliases for the given bean name, if defined.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param beanName the bean name to check for aliases
	 * @return the aliases, or an empty array if none
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition
	 */
	String[] getAliases(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * Given a bean name, create an alias. We typically use this method to
	 * support names that are illegal within XML ids (used for bean names).
	 * @param beanName the name of the bean
	 * @param alias alias that will behave the same as the bean name
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if there is no bean definition with the given name
	 * @throws BeansException if the alias is already in use
	 */
	void registerAlias(String beanName, String alias) throws BeansException;

}
