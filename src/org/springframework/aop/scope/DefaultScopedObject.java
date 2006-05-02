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

package org.springframework.aop.scope;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.util.Assert;

/**
 * Simple implementation of the ScopedObject interface that simply delegates the
 * <code>remove()</code> call to the underlying BeanFactory's <code>destroyScopedBean</code>.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#destroyScopedBean
 */
public class DefaultScopedObject implements ScopedObject {

	private final ConfigurableBeanFactory beanFactory;

	private final String targetBeanName;


	/**
	 * Create a new DefaultScopedObject.
	 * @param beanFactory the BeanFactory that holds the scoped target object
	 * @param targetBeanName the name of the target bean
	 */
	public DefaultScopedObject(ConfigurableBeanFactory beanFactory, String targetBeanName) {
		Assert.notNull(beanFactory, "BeanFactory must not be null");
		Assert.notNull(targetBeanName, "Target bean name must not be null");
		this.beanFactory = beanFactory;
		this.targetBeanName = targetBeanName;
	}


	public void remove() {
		this.beanFactory.destroyScopedBean(this.targetBeanName);
	}

}
