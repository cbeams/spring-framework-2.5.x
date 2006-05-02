/*
 * Copyright 2002-2006 the original author or authors.
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
 * Configuration interface to be implemented by most bean factories.
 * Provides facilities to configure a bean factory, in addition to the bean
 * factory client methods in the BeanFactory interface.
 *
 * <p>This subinterface of BeanFactory is not meant to be used in normal
 * application code: Stick to BeanFactory or ListableBeanFactory for typical
 * use cases. This interface is just meant to allow for framework-internal
 * plug'n'play even when needing access to bean factory configuration methods.
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.beans.factory.ListableBeanFactory
 * @see ConfigurableListableBeanFactory
 */
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory {

	/**
	 * Scope identifier for the standard singleton scope: "singleton".
	 * Custom scopes can be added via <code>registerScope</code>.
	 * @see #registerScope
	 */
	String SCOPE_SINGLETON = "singleton";

	/**
	 * Scope identifier for the standard prototype scope: "prototype".
	 * Custom scopes can be added via <code>registerScope</code>.
	 * @see #registerScope
	 */
	String SCOPE_PROTOTYPE = "prototype";


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
	 * Add a new BeanPostProcessor that will get applied to beans created
	 * by this factory. To be invoked during factory configuration.
	 * @param beanPostProcessor the bean processor to register
	 */
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

	/**
	 * Return the current number of registered BeanPostProcessors.
	 */
	int getBeanPostProcessorCount();

	/**
	 * Register the given scope, backed by the given ScopeMap.
	 * @param scope the scope identifier
	 * @param scopeMap the backing ScopeMap
	 */
	void registerScope(String scope, ScopeMap scopeMap);

	/**
	 * Destroy the specified scoped bean in the current target scope.
	 * @param beanName the name of the scoped bean
	 */
	void destroyScopedBean(String beanName);

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
	 * <p>The given instance is supposed to be fully initialized; the factory
	 * will not perform any initialization callbacks (in particular, it won't
	 * call InitializingBean's <code>afterPropertiesSet</code> method).
	 * The given instance will not receive any destruction callbacks
	 * (like DisposableBean's <code>destroy</code> method) either.
	 * <p><b>Register a bean definition instead of an existing instance if
	 * your bean is supposed to receive initialization and/or destruction
	 * callbacks.</b>
	 * <p>Typically invoked during factory configuration, but can also be
	 * used for runtime registration of singletons. Therefore, a factory
	 * implementation should synchronize singleton access; it will have
	 * to do this anyway if it supports lazy initialization of singletons.
	 * @param beanName name of the bean
	 * @param singletonObject the existing object
	 * @throws BeansException if the singleton could not be registered
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.beans.factory.DisposableBean#destroy
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#registerBeanDefinition
	 */
	void registerSingleton(String beanName, Object singletonObject) throws BeansException;

	/**
	 * Check if this bean factory contains a singleton instance with the given name.
	 * Only checks already instantiated singletons; does not return true for
	 * singleton bean definitions that have not been instantiated yet.
	 * <p>The main purpose of this method is to check manually registered singletons
	 * (see <code>registerSingleton</code>). Can also be used to check whether a
	 * singleton defined by a bean definition has already been created.
	 * <p>To check whether a bean factory contains a bean definition with a given name,
	 * use ListableBeanFactory's <code>containsBeanDefinition</code>. Calling both
	 * <code>containsBeanDefinition</code> and <code>containsSingleton</code> answers
	 * whether a specific bean factory contains an own bean with the given name.
	 * <p>Use BeanFactory's <code>containsBean</code> for general checks whether the
	 * factory knows about a bean with a given name (whether manually registed singleton
	 * instance or created by bean definition), also checking ancestor factories.
	 * @param beanName the name of the bean to look for
	 * @return if this bean factory contains a singleton instance with the given name
	 * @see #registerSingleton
	 * @see org.springframework.beans.factory.BeanFactory#containsBean
	 * @see org.springframework.beans.factory.ListableBeanFactory#containsBeanDefinition
	 */
	boolean containsSingleton(String beanName);

	/**
	 * Destroy all cached singletons in this factory.
	 * To be called on shutdown of a factory.
	 * <p>Should never throw an exception but rather log shutdown failures.
	 */
	void destroySingletons();

}
