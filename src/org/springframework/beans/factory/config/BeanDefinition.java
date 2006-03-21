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

import org.springframework.beans.MutablePropertyValues;

/**
 * A BeanDefinition describes a bean instance, which has property values,
 * constructor argument values, and further information supplied by
 * concrete implementations.
 *
 * <p>This is just a minimal interface: The main intention is to allow
 * BeanFactoryPostProcessors (like PropertyPlaceholderConfigurer) to
 * access and modify property values.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 19.03.2004
 * @see ConfigurableListableBeanFactory#getBeanDefinition
 * @see BeanFactoryPostProcessor
 * @see PropertyPlaceholderConfigurer
 * @see org.springframework.beans.factory.support.RootBeanDefinition
 * @see org.springframework.beans.factory.support.ChildBeanDefinition
 */
public interface BeanDefinition {

	/**
	 * Role hint indicating that a <code>BeanDefinition</code> is a major part
	 * of the application. Typically corresponds to a user-defined bean.
	 */
	int ROLE_APPLICATION = 0;

	/**
	 * Role hint indicating that a <code>BeanDefinition</code> is a supporting
	 * part of some larger configuration, typically an outer
	 * {@link org.springframework.beans.factory.support.ComponentDefinition}.
	 * <code>SUPPORT</code> beans are considered important enough to be aware
	 * of when looking more closely at a particular
	 * {@link org.springframework.beans.factory.support.ComponentDefinition}, but
	 * not when looking at the overall configuration of an application.
	 */
	int ROLE_SUPPORT = 1;

	/**
	 * Role hint indicating that a <code>BeanDefinition</code> is providing
	 * an entirely background role and has no relevance to the end-user. This
	 * hint is used when registering beans that are completely part of the internal
	 * workings of a {@link org.springframework.beans.factory.support.ComponentDefinition}. 
	 */
	int ROLE_INFRASTRUCTURE = 2;


	/**
	 * Return whether this bean is "abstract", i.e. not meant to be instantiated.
	 */
	boolean isAbstract();

	/**
	 * Return whether this a <b>Singleton</b>, with a single, shared instance
	 * returned on all calls.
	 */
	boolean isSingleton();

	/**
	 * Return whether this bean should be lazily initialized, i.e. not
	 * eagerly instantiated on startup. Only applicable to a singleton bean.
	 */
	boolean isLazyInit();


	/**
	 * Return the constructor argument values for this bean.
	 * Can be modified during bean factory post-processing.
	 * @return the ConstructorArgumentValues object (never <code>null</code>)
	 */
	ConstructorArgumentValues getConstructorArgumentValues();

	/**
	 * Return the property values to be applied to a new instance of the bean.
	 * Can be modified during bean factory post-processing.
	 * @return the MutablePropertyValues object (never <code>null</code>)
	 */
	MutablePropertyValues getPropertyValues();


	/**
	 * Return a description of the resource that this bean definition
	 * came from (for the purpose of showing context in case of errors).
	 */
	String getResourceDescription();

	/**
	 * Return the <code>Object</code> that was the source of this definition in the
	 * configuration. May be <code>null</code>. The exact type of this source
	 * <code>Object</code> will depend on the configuration mechanism used.
	 */
	Object getSource();

	/**
	 * Get the role hint for this <code>BeanDefinition</code>. The role hint
	 * provides tool with an indication of the importance of a particular
	 * <code>BeanDefinition</code>.
	 * @see #ROLE_APPLICATION
	 * @see #ROLE_INFRASTRUCTURE
	 * @see #ROLE_SUPPORT
	 */
	int getRole();


	/**
	 * Attach a generic, keyed metadata attribute to this bean definition.
	 * Users should take care to prevent overlaps with other metadata attributes by
	 * using fully-qualified names, perhaps using class or package names as prefix.
	 * @param key the unique attribute key
	 * @param value the attribute value to be attached
	 */
	void setAttribute(String key, Object value);

	/**
	 * Get the metadata attribute for the given key, if any.
	 * @return the attribute value, or <code>null</code> if none
	 */
	Object getAttribute(String key);

	/**
	 * Return the names of all registered attributes as String array.
	 */
	String[] attributeNames();

}
