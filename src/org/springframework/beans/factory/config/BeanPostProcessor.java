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

/**
 * Allows for custom modification of new bean instances, e.g.
 * checking for marker interfaces or wrapping them with proxies.
 *
 * <p>Application contexts can auto-detect BeanPostProcessor beans in their
 * bean definitions and apply them before any other beans get created.
 * Plain bean factories allow for programmatic registration of post-processors.
 *
 * <p>Typically, post-processors that populate beans via marker interfaces
 * or the like will implement postProcessBeforeInitialization, and post-processors
 * that wrap beans with proxies will normally implement postProcessAfterInitialization.
 *
 * @author Juergen Hoeller
 * @since 10.10.2003
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 */
public interface BeanPostProcessor {

	/**
	 * Apply this BeanPostProcessor to the given new bean instance <i>before</i> any bean
	 * initialization callbacks (like InitializingBean's afterPropertiesSet or a custom
	 * init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * @param bean the new bean instance
	 * @param name the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	Object postProcessBeforeInitialization(Object bean, String name) throws BeansException;

	/**
	 * Apply this BeanPostProcessor to the given new bean instance <i>after</i> any bean
	 * initialization callbacks (like InitializingBean's afterPropertiesSet or a custom
	 * init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * @param bean the new bean instance
	 * @param name the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	Object postProcessAfterInitialization(Object bean, String name) throws BeansException;

}
