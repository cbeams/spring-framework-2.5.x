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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.util.Assert;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class BeanComponentDefinition extends AbstractComponentDefinition {

	private final BeanDefinition beanDefinition;

	private final String beanName;

	private String description;

	private RuntimeBeanReference[] beanReferences;


	public BeanComponentDefinition(BeanDefinition beanDefinition, String beanName) {
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		Assert.notNull(beanName, "Bean name must not be null");
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		createDescription();
		findBeanReferences();
	}

	public BeanComponentDefinition(BeanDefinitionHolder holder) {
		Assert.notNull(holder, "BeanDefinitionHolder must not be null");
		this.beanDefinition = holder.getBeanDefinition();
		this.beanName = holder.getBeanName();
		createDescription();
		findBeanReferences();
	}


	private void createDescription() {
		String beanType = this.beanDefinition.getBeanClassName();
		this.description = "Bean '" + getName() + "' of type '" + beanType + "'";
	}

	private void findBeanReferences() {
		List references = new ArrayList();
		PropertyValues propertyValues = this.beanDefinition.getPropertyValues();
		for (int i = 0; i < propertyValues.getPropertyValues().length; i++) {
			PropertyValue propertyValue = propertyValues.getPropertyValues()[i];
			Object value = propertyValue.getValue();
			if (value instanceof RuntimeBeanReference) {
				references.add(value);
			}
		}
		this.beanReferences = (RuntimeBeanReference[]) references.toArray(new RuntimeBeanReference[references.size()]);
	}


	public String getName() {
		return this.beanName;
	}

	public String getDescription() {
		return this.description;
	}

	public BeanDefinition[] getBeanDefinitions() {
		return new BeanDefinition[] {this.beanDefinition};
	}

	public RuntimeBeanReference[] getBeanReferences() {
		return this.beanReferences;
	}

	public Object getSource() {
		return this.beanDefinition.getSource();
	}

}
