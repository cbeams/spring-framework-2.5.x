/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.aop.target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.TargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Base class for dynamic TargetSources that can create new prototype bean
 * instances to support a pooling or new-instance-per-invocation strategy.
 * Such TargetSources must run in a BeanFactory, as it needs to call the
 * getBean() method to create a new prototype instance.
 * @author Rod Johnson
 * @version $Id: AbstractPrototypeTargetSource.java,v 1.5 2004-03-18 10:38:13 jhoeller Exp $
 */
public abstract class AbstractPrototypeTargetSource
		implements TargetSource, BeanFactoryAware, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	/** Name of the target bean we will create on each invocation */
	private String targetBeanName;

	/**
	 * BeanFactory that owns this TargetSource. We need to hold onto this
	 * reference so that we can create new prototype instances as necessary.
	 */
	private BeanFactory owningBeanFactory;

	/** Class of the target */
	private Class targetClass;

	/**
	 * Set the name of the target bean in the factory. This bean should be a
	 * prototype, or the same instance will always be obtained from the
	 * factory, resulting in the same behaviour as the InvokerInterceptor
	 * @param targetBeanName name of the target bean in the BeanFactory
	 * that owns this interceptor
	 */
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	/**
	 * Return the name of the target bean in the factory.
	 */
	public String getTargetBeanName() {
		return this.targetBeanName;
	}

	/**
	 * Set the owning BeanFactory. We need to save a reference so that we can
	 * use the getBean() method on every invocation.
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.owningBeanFactory = beanFactory;
		if (this.owningBeanFactory.isSingleton(this.targetBeanName)) {
			throw new BeanDefinitionStoreException(
				"Cannot use PrototypeTargetSource against a Singleton bean; instances would not be independent");
		}
		logger.info("Getting bean with name '" + targetBeanName + "' to find class");
		this.targetClass = owningBeanFactory.getBean(targetBeanName).getClass();
	}

	/**
	 * Subclasses should use this method to create a new prototype instance.
	 */
	protected Object newPrototypeInstance() {
		if (logger.isInfoEnabled()) {
			logger.info("Creating new target from bean '" + this.targetBeanName + "'");
		}
		return this.owningBeanFactory.getBean(this.targetBeanName);
	}

	public Class getTargetClass() {
		return this.targetClass;
	}

	public boolean isStatic() {
		return false;
	}

	public void afterPropertiesSet() {
		if (this.targetBeanName == null) {
			throw new IllegalStateException("targetBeanName is required");
		}
	}

}
