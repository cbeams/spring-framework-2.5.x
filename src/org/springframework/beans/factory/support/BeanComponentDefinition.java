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
 * ComponentDefinition based on a standard BeanDefinition, exposing the given bean
 * definition as well as inner bean definitions and bean references for the given bean.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class BeanComponentDefinition extends AbstractComponentDefinition {

	private final BeanDefinition beanDefinition;

	private final String beanName;

	private final String description;

	private BeanDefinitionHolder[] innerBeanDefinitions;

	private RuntimeBeanReference[] beanReferences;


	/**
	 * Create a new BeanComponentDefinition for the given bean.
	 * @param beanDefinition the BeanDefinition
	 * @param beanName the name of the bean
	 */
	public BeanComponentDefinition(BeanDefinition beanDefinition, String beanName) {
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		Assert.notNull(beanName, "Bean name must not be null");
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		this.description = buildDescription(this.beanDefinition);
		findInnerBeanDefinitionsAndBeanReferences();
	}

	/**
	 * Create a new BeanComponentDefinition for the given bean.
	 * @param holder the BeanDefinitionHolder encapsulating the
	 * bean definition as well as the name of the bean
	 */
	public BeanComponentDefinition(BeanDefinitionHolder holder) {
		this(holder.getBeanDefinition(), holder.getBeanName());
	}


	private String buildDescription(BeanDefinition beanDefinition) {
		StringBuffer sb = new StringBuffer();
		sb.append("Bean '").append(getName()).append("'");
		String beanType = beanDefinition.getBeanClassName();
		if (beanType != null) {
			sb.append(" of type [" + beanType + "]");
		}
		return sb.toString();
	}

	private void findInnerBeanDefinitionsAndBeanReferences() {
		List innerBeans = new ArrayList();
		List references = new ArrayList();
		PropertyValues propertyValues = this.beanDefinition.getPropertyValues();
		for (int i = 0; i < propertyValues.getPropertyValues().length; i++) {
			PropertyValue propertyValue = propertyValues.getPropertyValues()[i];
			Object value = propertyValue.getValue();
			if (value instanceof BeanDefinitionHolder) {
				innerBeans.add(value);
			}
			else if (value instanceof RuntimeBeanReference) {
				references.add(value);
			}
		}
		this.innerBeanDefinitions =
				(BeanDefinitionHolder[]) innerBeans.toArray(new BeanDefinitionHolder[innerBeans.size()]);
		this.beanReferences =
				(RuntimeBeanReference[]) references.toArray(new RuntimeBeanReference[references.size()]);
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

	public BeanDefinitionHolder[] getInnerBeanDefinitions() {
		return innerBeanDefinitions;
	}

	public RuntimeBeanReference[] getBeanReferences() {
		return this.beanReferences;
	}

	public Object getSource() {
		return this.beanDefinition.getSource();
	}

}
