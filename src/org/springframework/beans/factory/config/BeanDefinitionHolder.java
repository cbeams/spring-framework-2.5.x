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

import org.springframework.util.Assert;

/**
 * Holder for a BeanDefinition with name and aliases.
 * Can be registered as a placeholder for an inner bean.
 *
 * <p>Recognized by AbstractAutowireCapableBeanFactory for inner
 * bean definitions. Registered by DefaultXmlBeanDefinitionParser,
 * which also uses it as general holder for a parsed bean definition.
 *
 * <p>Can also be used for programmatic registration of inner bean
 * definitions. If you don't care about BeanNameAware and the like,
 * registering RootBeanDefinition or ChildBeanDefinition is good enough.
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see org.springframework.beans.factory.BeanNameAware
 * @see org.springframework.beans.factory.support.RootBeanDefinition
 * @see org.springframework.beans.factory.support.ChildBeanDefinition
 */
public class BeanDefinitionHolder {

	private final BeanDefinition beanDefinition;

	private final String beanName;

	private final String[] aliases;


	/**
	 * Create a new BeanDefinitionHolder.
	 * @param beanDefinition the BeanDefinition to wrap
	 * @param beanName the name of the bean, as specified for the bean definition
	 */
	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
		this(beanDefinition, beanName, null);
	}

	/**
	 * Create a new BeanDefinitionHolder.
	 * @param beanDefinition the BeanDefinition to wrap
	 * @param beanName the name of the bean, as specified for the bean definition
	 * @param aliases alias names for the bean, or <code>null</code> if none
	 */
	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, String[] aliases) {
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		Assert.notNull(beanName, "Bean name must not be null");
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		this.aliases = aliases;
	}


	/**
	 * Return the wrapped BeanDefinition.
	 */
	public BeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}

	/**
	 * Return the primary name of the bean, as specified for the bean definition.
	 */
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * Return the alias names for the bean, as specified directly for the bean definition.
	 * @return the array of alias names, or <code>null</code> if none
	 */
	public String[] getAliases() {
		return this.aliases;
	}


	public String toString() {
		return "Bean definition with name '" + this.beanName + "': " + this.beanDefinition;
	}

}
