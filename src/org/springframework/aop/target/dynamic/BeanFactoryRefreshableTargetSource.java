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

package org.springframework.aop.target.dynamic;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ChildBeanDefinition;

/**
 * @author Rob Harrop
 * @author Rod Johnson
 * @since 2.0M2
 */
public class BeanFactoryRefreshableTargetSource extends AbstractRefreshableTargetSource {
   private String beanName;

	private DefaultListableBeanFactory childFactory;

	/**
	 *
	 * @param factory
	 * @param beanName
	 * @param childFactory optional, must be a child of factory.
	 * Allows shared child factory.
	 */
	public BeanFactoryRefreshableTargetSource(BeanFactory factory, String beanName, DefaultListableBeanFactory childFactory) {
		//super(initialTarget);
		this.beanName = beanName;
		this.childFactory = (childFactory == null) ?
			new DefaultListableBeanFactory(factory) :
			childFactory;

		// The child bean definition is a prototype, so whenever
		// we call getBean() on it we'll get a fresh object,
		// configured the same way.
		// Apart from that, the child bean definition will be
		// the same as the parent: all properties are inherited
		ChildBeanDefinition definition = createChildBeanDefinition(beanName);
		definition.setSingleton(false);
		this.childFactory.registerBeanDefinition(beanName, definition);
	}


	protected Object freshTarget() {
		return this.childFactory.getBean(this.beanName);
	}

	protected ChildBeanDefinition createChildBeanDefinition(String beanName) {
		return new ChildBeanDefinition(beanName, null);
	}
}
