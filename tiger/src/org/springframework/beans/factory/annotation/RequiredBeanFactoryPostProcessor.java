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

package org.springframework.beans.factory.annotation;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class RequiredBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	private Class<? extends Annotation> requiredAnnotationType = Required.class;

	public void setRequiredAnnotationType(Class<? extends Annotation> requiredAnnotationType) {
		Assert.notNull(requiredAnnotationType, "'requiredAnnotationType' cannot be null.");
		this.requiredAnnotationType = requiredAnnotationType;
	}

	public final void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
		for (String beanDefinitionName : beanDefinitionNames) {
			BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
			processBeanDefinition(beanDefinition, beanDefinitionName);
		}
	}

	private void processBeanDefinition(BeanDefinition beanDefinition, String beanDefinitionName) {
		Class targetType = ((AbstractBeanDefinition) beanDefinition).getBeanClass();
		PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(targetType);
		List<String> invalidProperties = new ArrayList<String>();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			if (isRequired(propertyDescriptor) && !isDefined(propertyDescriptor, beanDefinition)) {
				invalidProperties.add(propertyDescriptor.getName());
			}
		}
		if (!invalidProperties.isEmpty()) {
			throw new IllegalArgumentException(createExceptionMessage(invalidProperties, beanDefinitionName));
		}

	}

	private String createExceptionMessage(List<String> invalidProperties, String beanDefinitionName) {
		StringBuilder sb = new StringBuilder();
		int size = invalidProperties.size();
		sb.append(size == 1 ? "Property" : "Properties");
		for (int i = 0; i < size; i++) {
			String propertyName = invalidProperties.get(i);
			if (i > 0) {
				if (i == (size - 1)) {
					sb.append(" and");
				}
				else {
					sb.append(",");
				}
			}
			sb.append(" '");
			sb.append(propertyName);
			sb.append('\'');
		}
		sb.append(size == 1 ? " is required" : " are required");
		sb.append(" for bean '");
		sb.append(beanDefinitionName);
		sb.append("'.");
		return sb.toString();
	}

	private boolean isDefined(PropertyDescriptor propertyDescriptor, BeanDefinition beanDefinition) {
		return (beanDefinition.getPropertyValues().getPropertyValue(propertyDescriptor.getName()) != null);
	}

	protected boolean isRequired(PropertyDescriptor propertyDescriptor) {
		Method setter = propertyDescriptor.getWriteMethod();
		return (setter != null && setter.getAnnotation(this.requiredAnnotationType) != null);
	}
}
