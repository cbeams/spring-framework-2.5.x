/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.beans.factory.config;

import java.beans.PropertyEditor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;

/**
 * Configuration interface to be implemented by most if not all bean factories.
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
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory {

	/**
	 * Set the parent of this bean factory.
	 * <p>Note that the parent shouldn't be changed: It should only be set outside
	 * a constructor if it isn't available when an object of this class is created.
	 * @param parentBeanFactory the parent bean factory
	 */
	void setParentBeanFactory(BeanFactory parentBeanFactory);

	/**
	 * Register the given custom property editor for all properties of the
	 * given type. To be invoked during factory configuration.
	 * @param requiredType type of the property
	 * @param propertyEditor editor to register
	 */
	void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor);

	/**
	 * Ignore the given dependency type for autowiring.
	 * To be invoked during factory configuration.
	 * <p>This will typically be used for dependencies that are resolved
	 * in other ways, like BeanFactory through BeanFactoryAware or
	 * ApplicationContext through ApplicationContextAware.
	 * @see org.springframework.beans.factory.BeanFactoryAware
	 * @see org.springframework.context.ApplicationContextAware
	 */
	void ignoreDependencyType(Class type);

	/**
	 * Add a new BeanPostPrcoessor that will get applied to beans created
	 * by this factory. To be invoked during factory configuration.
	 * @param beanPostProcessor the bean processor to register
	 */
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

	/**
	 * Given a bean name, create an alias. We typically use this method to
	 * support names that are illegal within XML ids (used for bean names).
	 * <p>Typically invoked during factory configuration, but can also be
	 * used for runtime registration of aliases. Therefore, a factory
	 * implementation should synchronize alias access.
	 * @param beanName name of the bean
	 * @param alias alias that will behave the same as the bean name
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if there is no bean with the given name
	 * @throws BeansException if the alias is already in use
	 */
	void registerAlias(String beanName, String alias) throws BeansException;

	/**
	 * Register the given existing object as singleton in the bean factory,
	 * under the given bean name.
	 * <p>Typically invoked during factory configuration, but can also be
	 * used for runtime registration of singletons. Therefore, a factory
	 * implementation should synchronize singleton access; it will have
	 * to do this anyway if it supports lazy initialization of singletons.
	 * @param beanName name of the bean
	 * @param singletonObject the existing object
	 * @throws BeansException if the singleton could not be registered
	 */
	void registerSingleton(String beanName, Object singletonObject) throws BeansException;

	/**
	 * Destroy all cached singletons in this factory.
	 * To be called on shutdown of a factory.
	 */
	void destroySingletons();

}
