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

package org.springframework.beans.factory.config;

/**
 * Holder for a BeanDefinition with name and aliases.
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
	 * @param beanDefinition the BeanDefinition
	 * @param beanName the name of the bean
	 */
	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
		this(beanDefinition, beanName, null);
	}

	/**
	 * Create a new BeanDefinitionHolder.
	 * @param beanDefinition the BeanDefinition
	 * @param beanName the name of the bean
	 * @param aliases alias names of the bean, or <code>null</code> if none
	 */
	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, String[] aliases) {
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		this.aliases = aliases;
	}

	public BeanDefinition getBeanDefinition() {
		return beanDefinition;
	}

	public String getBeanName() {
		return beanName;
	}

	public String[] getAliases() {
		return aliases;
	}

	public String toString() {
		return "Bean definition with name '" + this.beanName + "': " + this.beanDefinition;
	}

}
