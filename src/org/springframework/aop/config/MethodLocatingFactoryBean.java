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

package org.springframework.aop.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.BeansException;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * {@link FactoryBean} implementation that locates a {@link Method} on a specified bean.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class MethodLocatingFactoryBean implements FactoryBean, BeanFactoryAware, InitializingBean {

	private BeanFactory beanFactory;

	private String beanName;

	private String methodName;

	private Method method;

	/**
	 * Sets the name of the bean to locate the {@link Method} on.
	 */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * Sets the name of the {@link Method} to locate.
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object getObject() throws Exception {
		return this.method;
	}

	public Class getObjectType() {
		return Method.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void afterPropertiesSet() throws Exception {
		if(!StringUtils.hasText(this.beanName)) {
			throw new IllegalArgumentException("Property [beanName] is required.");
		}

		if(!StringUtils.hasText(this.methodName)) {
			throw new IllegalArgumentException("Property [methodName] is required.");
		}

		Class beanClass = this.beanFactory.getBean(this.beanName).getClass();

		Method[] methods = beanClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if(this.methodName.equals(method.getName())) {
				this.method = method;
			}
		}

		if(this.method == null) {
			throw new IllegalArgumentException("Unable to locate method [" 
					+ this.methodName + "] on bean [" + this.beanName + "].");
		}
	}
}
