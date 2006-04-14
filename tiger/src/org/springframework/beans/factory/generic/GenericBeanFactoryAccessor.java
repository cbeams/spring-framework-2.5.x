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

package org.springframework.beans.factory.generic;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.BeansException;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.HashMap;
import java.lang.annotation.Annotation;

/**
 * Simple wrapper around a {@link ListableBeanFactory} that provides typed, generics-based
 * access to key methods. This removes the need for casting in many cases and should
 * increase compile-time type safety.
 * <p/>
 * Provides a simple mechanism for accessing all beans with a particular {@link Annotation}.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class GenericBeanFactoryAccessor {

	/**
	 * The {@link ListableBeanFactory} being wrapped.
	 */
	private final ListableBeanFactory beanFactory;

	/**
	 * Constructs a <code>GenericBeanFactoryAccessor</code> that wraps the supplied {@link ListableBeanFactory}.
	 */
	public GenericBeanFactoryAccessor(ListableBeanFactory beanFactory) {
		Assert.notNull(beanFactory, "'beanFactory' cannot be null.");
		this.beanFactory = beanFactory;
	}

	/**
	 * @see ListableBeanFactory#getBeansOfType(Class)
	 */
	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
		return getBeanFactory().getBeansOfType(type);
	}

	/**
	 * @see ListableBeanFactory#getBeansOfType(Class, boolean, boolean)
	 */
	public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includePrototypes, boolean includeFactoryBeans) throws BeansException {
		return getBeanFactory().getBeansOfType(type, includePrototypes, includeFactoryBeans);
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#getBean(String, Class)
	 */
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return (T)getBeanFactory().getBean(name, requiredType);
	}

	/**
	 * @see org.springframework.beans.factory.BeanFactory#getBean(String)
	 */
	public <T> T getBean(String name) throws BeansException {
		return (T)getBeanFactory().getBean(name);
	}

	/**
	 * Returns all beans whose <code>Class</code> has the supplied {@link Annotation} type.
	 */
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
		Map<String, Object> results = new HashMap<String, Object>();
		for (String beanName : getBeanFactory().getBeanDefinitionNames()) {
			Class beanType = getBeanFactory().getType(beanName);
			if (beanType.getAnnotation(annotationType) != null) {
				results.put(beanName, getBeanFactory().getBean(beanName));
			}
		}
		return results;
	}

	/**
	 * Returns the wrapped {@link ListableBeanFactory}.
	 */
	public ListableBeanFactory getBeanFactory() {
		return this.beanFactory;
	}
}
