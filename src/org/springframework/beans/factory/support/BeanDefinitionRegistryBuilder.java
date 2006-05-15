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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Programmatic builder-style object to make it easier to construct bean definitions.
 * Intended for use primarily when authoring Spring 2.0 NamespaceHandlers.
 * 
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.beans.factory.support.BeanDefinitionBuilder
 * @see org.springframework.beans.factory.support.BeanDefinitionReaderUtils
 */
public class BeanDefinitionRegistryBuilder {

	private final BeanDefinitionRegistry registry;


	/**
	 * Construct a new BeanDefinitionBuilder that will register beans with this
	 * registry
	 * @param registry registry to register beans with
	 */
	public BeanDefinitionRegistryBuilder(BeanDefinitionRegistry registry) {
		this.registry = registry;
	}

	/**
	 * Get the underlying {@link BeanDefinitionRegistry}. Useful for direct access or querying.
	 */
	public BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}

	/**
	 * Register a named bean.
	 * @param name the bean name
	 * @param builder BeanDefinitionBuilder that is source of bean definition
	 * @return the builder parameter, allowing further configuration
	 */
	public BeanDefinitionBuilder register(String name, BeanDefinitionBuilder builder) {
		builder.assignBeanName(name);
		this.registry.registerBeanDefinition(name, builder.getBeanDefinition());
		return builder;
	}

	/**
	 * Register a bean without specifying a name. A unique bean name will be generated,
	 * based on the owning BeanDefinitionRegistry. The bean will still be a top-level
	 * bean, not a nested bean.
	 * @param builder BeanDefinitionBuilder that is source of bean definition
	*  @return the builder parameter, allowing further configuration
	 */
	public BeanDefinitionBuilder register(BeanDefinitionBuilder builder) {
		String generatedBeanName =
				BeanDefinitionReaderUtils.generateBeanName(builder.getBeanDefinition(), this.registry, false);
		return register(generatedBeanName, builder);
	}


}
