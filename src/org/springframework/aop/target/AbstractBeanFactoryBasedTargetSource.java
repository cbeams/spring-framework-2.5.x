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

import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.TargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * Base class for TargetSource implementations that are based on a
 * Spring BeanFactory, delegating to Spring-managed bean instances.
 *
 * <p>Subclasses can create prototype instances or lazily access a
 * singleton target, for example. See LazyInitTargetSource and
 * AbstractPrototypeBasedTargetSource's subclasses for concrete strategies.
 *
 * <p>BeanFactoryBasedTargetSources are serializable. This involves
 * disconnecting the current target and turning into a SingletonTargetSource.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 1.1.4
 * @see org.springframework.beans.factory.BeanFactory#getBean
 * @see LazyInitTargetSource
 * @see PrototypeTargetSource
 * @see ThreadLocalTargetSource
 * @see CommonsPoolTargetSource
 */
public abstract class AbstractBeanFactoryBasedTargetSource
		implements TargetSource, BeanFactoryAware, Serializable {

	protected final Log logger = LogFactory.getLog(getClass());

	/** Name of the target bean we will create on each invocation */
	private String targetBeanName;

	/**
	 * BeanFactory that owns this TargetSource. We need to hold onto this
	 * reference so that we can create new prototype instances as necessary.
	 */
	private BeanFactory beanFactory;

	/** Class of the target */
	private Class targetClass;


	/**
	 * Set the name of the target bean in the factory. This bean should be a
	 * prototype, or the same instance will always be obtained from the
	 * factory, resulting in the same behavior as the SingletonTargetSource.
	 * @param targetBeanName name of the target bean in the BeanFactory
	 * that owns this interceptor
	 * @see SingletonTargetSource
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
	 * use the <code>getBean</code> method on every invocation.
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (this.targetBeanName == null) {
			throw new IllegalStateException("targetBeanName is required");
		}

		this.beanFactory = beanFactory;

		// Determine type of the target bean.
		this.targetClass = this.beanFactory.getType(this.targetBeanName);
		if (this.targetClass == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Getting bean with name '" + this.targetBeanName + "' to determine type");
			}
			this.targetClass = this.beanFactory.getBean(this.targetBeanName).getClass();
		}
	}

	/**
	 * Return the owning BeanFactory.
	 */
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public Class getTargetClass() {
		return this.targetClass;
	}

	public boolean isStatic() {
		return false;
	}

	public void releaseTarget(Object target) throws Exception {
		// do nothing
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
			TargetSource disconnectedTargetSource = new SingletonTargetSource(getTarget());
			return disconnectedTargetSource;
		}
		catch (Exception ex) {
			logger.error("Cannot get target for disconnecting TargetSource [" + this + "]", ex);
			throw new NotSerializableException(
					"Cannot get target for disconnecting TargetSource [" + this + "]: " + ex.getMessage());
		}
	}

}
