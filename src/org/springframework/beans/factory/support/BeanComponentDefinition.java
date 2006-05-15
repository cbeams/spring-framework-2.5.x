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
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.util.Assert;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class BeanComponentDefinition extends AbstractComponentDefinition {

	private final BeanDefinition beanDefinition;

	private final String beanName;

	private String description;

	public BeanComponentDefinition(BeanDefinition beanDefinition, String beanName) {
		Assert.notNull(beanDefinition, "'beanDefinition' cannot be null.");
		Assert.notNull(beanName, "'beanName' cannot be null.");
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		createDescription();
	}

	public BeanComponentDefinition(BeanDefinitionHolder holder) {
		Assert.notNull(holder, "'holder' cannot be null.");
		this.beanDefinition = holder.getBeanDefinition();
		this.beanName = holder.getBeanName();
		createDescription();
	}

	private void createDescription() {
		String beanType = ((AbstractBeanDefinition) this.beanDefinition).getBeanClassName();
		this.description = "Bean '" + getName() + "' of type '" + beanType + "'";
	}

	public String getName() {
		return this.beanName;
	}

	public String getDescription() {
		return this.description;
	}

	public BeanDefinition[] getBeanDefinitions() {
		return new BeanDefinition[]{this.beanDefinition};
	}

	public Object getSource() {
		return this.beanDefinition.getSource();
	}
}
