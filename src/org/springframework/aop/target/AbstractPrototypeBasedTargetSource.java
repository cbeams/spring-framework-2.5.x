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

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.aopalliance.aop.AspectException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.TargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Base class for dynamic TargetSources that can create new prototype bean
 * instances to support a pooling or new-instance-per-invocation strategy.
 *
 * <p>Such TargetSources must run in a BeanFactory, as it needs to call the
 * getBean() method to create a new prototype instance.
 * 
 * <p>PrototypeBasedTargetSources are serializable. This involves disconnecting
 * the current target and turning into a SingletonTargetSource.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.beans.factory.BeanFactory#getBean
 */
public abstract class AbstractPrototypeBasedTargetSource
		implements TargetSource, BeanFactoryAware, InitializingBean, Serializable {

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
	 * factory, resulting in the same behavior as the InvokerInterceptor.
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

		// check whether the target bean is defined as prototype
		if (this.owningBeanFactory.isSingleton(this.targetBeanName)) {
			throw new BeanDefinitionStoreException(
				"Cannot use PrototypeTargetSource against a singleton bean: instances would not be independent");
		}

		// determine type of the target bean
		if (beanFactory instanceof ConfigurableListableBeanFactory) {
			this.targetClass =
			    ((ConfigurableListableBeanFactory) beanFactory).getBeanDefinition(this.targetBeanName).getBeanClass();
		}
		else {
			if (logger.isInfoEnabled()) {
				logger.info("Getting bean with name '" + this.targetBeanName + "' to find class");
			}
			this.targetClass = this.owningBeanFactory.getBean(this.targetBeanName).getClass();
		}
	}

	public void afterPropertiesSet() {
		if (this.targetBeanName == null) {
			throw new IllegalStateException("targetBeanName is required");
		}
	}


	public Class getTargetClass() {
		return this.targetClass;
	}

	public boolean isStatic() {
		return false;
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


	/**
	 * Replaces this object with a SingletonTargetSource on serialization.
	 * Protected as otherwise it won't be invoked for subclasses.
	 * (The writeReplace() method must be visible to the class being serialized.)
	 * <p>With this implementation of this method, there is no need to mark
	 * non-serializable fields in this class or subclasses as transient.
	 */
	protected Object writeReplace() throws ObjectStreamException {
		if (logger.isDebugEnabled()) {
			logger.debug("Disconnecting TargetSource [" + this + "]");
		}
		try {
			TargetSource disconnectedTargetSource =  new SingletonTargetSource(getTarget());
			return disconnectedTargetSource;
		}
		catch (Exception ex) {
			throw new AspectException("Can't get target", ex);
		}
	}

}
