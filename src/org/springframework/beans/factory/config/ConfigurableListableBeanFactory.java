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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * SPI interface to be implemented by most if not all listable bean factories.
 * In addition to ConfigurableBeanFactory, it provides means to evaluate bean
 * definitions and to pre-instantiate singletons.
 *
 * <p>Allows for framework-internal plug'n'play, e.g. in AbstractApplicationContext.
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 * @see org.springframework.context.support.AbstractApplicationContext#getBeanFactory
 */
public interface ConfigurableListableBeanFactory
		extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {

	/**
	 * Return the registered BeanDefinition for the given bean, allowing access
	 * to its property values and constructor argument value (which can be
	 * modified during bean factory post-processing).
	 * <p>A returned BeanDefinition object should not be a copy but the original
	 * definition object as registered in the factory. This means that it should
	 * be castable to a more specific implementation type, if necessary.
	 * @param beanName name of the bean
	 * @return the registered BeanDefinition
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if there is no bean with the given name
	 * @throws BeansException in case of errors
	 */
	BeanDefinition getBeanDefinition(String beanName) throws BeansException;

	/**
	 * Ensure that all non-lazy-init singletons are instantiated, also considering
	 * FactoryBeans. Typically invoked at the end of factory setup, if desired.
	 * <p>As this is a startup method, it should destroy already created singletons
	 * if it fails, to avoid dangling resources. In other words, after invocation
	 * of that method, either all or no singletons at all should be instantiated.
	 * @throws BeansException if one of the singleton beans could not be created
	 */
	void preInstantiateSingletons() throws BeansException;

}
