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

import org.springframework.aop.aspectj.AspectInstanceFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.BeansException;
import org.springframework.util.StringUtils;

/**
 * Implementation of {@link AspectInstanceFactory} that locates the aspect from the
 * {@link org.springframework.beans.factory.BeanFactory} using a configured bean name.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class BeanFactoryAspectInstanceFactory implements AspectInstanceFactory, BeanFactoryAware, InitializingBean {

	private int count;

	private String beanName;

	private BeanFactory beanFactory;

	/**
	 * Sets the name of the aspect bean. This is the bean that is returned when calling
	 * {@link #getAspectInstance()}.
	 */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * Looks up the aspect bean from the {@link BeanFactory} and returns it.
	 * @see #setBeanName(String)
	 */
	public Object getAspectInstance() {
		count++;
		return this.beanFactory.getBean(this.beanName);
	}

	public int getInstantiationCount() {
		return count;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * Checks to make sure that the <code>beanName</code> property has been specified.
	 */
	public void afterPropertiesSet() throws Exception {
		if(!StringUtils.hasText(this.beanName)) {
			throw new IllegalArgumentException("Property [beanName] is required.");
		}
	}
}
