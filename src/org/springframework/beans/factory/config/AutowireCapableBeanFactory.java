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
import org.springframework.beans.factory.BeanFactory;

/**
 * Extension of the BeanFactory interface to be implemented by bean
 * factories that are capable of autowiring, provided that they want
 * to expose this functionality for existing bean instances.
 *
 * <p>This subinterface of BeanFactory is not meant to be used in normal
 * application code: Stick to BeanFactory or ListableBeanFactory for
 * typical use cases.
 *
 * <p>Integration code for other frameworks can leverage this interface to
 * wire and populate existing bean instances that Spring does not control
 * the lifecycle of. This is particularly useful for WebWork Actions or
 * Tapestry Page objects, for example.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.beans.factory.ListableBeanFactory
 */
public interface AutowireCapableBeanFactory extends BeanFactory {

	/**
	 * Constant that indicates autowiring bean properties by name.
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_BY_NAME = 1;

	/**
	 * Constant that indicates autowiring bean properties by type.
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_BY_TYPE = 2;

	/**
	 * Constant that indicates autowiring a constructor.
	 * @see #autowire
	 */
	int AUTOWIRE_CONSTRUCTOR = 3;

	/**
	 * Constant that indicates determining an appropriate autowire strategy
	 * through introspection of the bean class.
	 * @see #autowire
	 */
	int AUTOWIRE_AUTODETECT = 4;


	/**
	 * Create a new bean instance of the given class with the specified autowire
	 * strategy. All constants defined in this interface are supported here.
	 * @param beanClass the class of the bean to instantiate
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for objects
	 * (not applicable to autowiring a constructor, thus ignored there)
	 * @return the new bean instance
	 * @throws BeansException if instantiation respectively wiring failed
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_AUTODETECT
	 */
	Object autowire(Class beanClass, int autowireMode, boolean dependencyCheck)
			throws BeansException;

	/**
	 * Autowire the bean properties of the given bean instance by name or type.
	 * @param existingBean the existing bean instance
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for object
	 * @throws BeansException if wiring failed
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 */
	void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException;

	/**
	 * Apply the property values of the bean definition with the given name
	 * to the given bean instance. The bean definition can either define a
	 * fully self-contained bean, reusing its property values, or just
	 * property values meant to be used for existing bean instances.
	 * @param existingBean the existing bean instance
	 * @param name the name of the bean definition in the bean factory
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if there is no bean with the given name
	 * @throws BeansException if applying the property values failed
	 */
	void applyBeanPropertyValues(Object existingBean, String name) throws BeansException;

	/**
	 * Apply BeanPostProcessors to the given existing bean instance,
	 * invoking their postProcessBeforeInitialization methods.
	 * The returned bean instance may be a wrapper around the original.
	 * @param existingBean the new bean instance
	 * @param name the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws BeansException if any post-processing failed
	 * @see BeanPostProcessor#postProcessBeforeInitialization
	 */
	Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String name)
			throws BeansException;


	/**
	 * Apply BeanPostProcessors to the given existing bean instance,
	 * invoking their postProcessAfterInitialization methods.
	 * The returned bean instance may be a wrapper around the original.
	 * @param existingBean the new bean instance
	 * @param name the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws BeansException if any post-processing failed
	 * @see BeanPostProcessor#postProcessAfterInitialization
	 */
	Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String name)
			throws BeansException;

}
