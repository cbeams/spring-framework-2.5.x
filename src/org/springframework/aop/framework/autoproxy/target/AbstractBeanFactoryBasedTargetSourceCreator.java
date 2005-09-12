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

package org.springframework.aop.framework.autoproxy.target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.TargetSourceCreator;
import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Convenient superclass for TargetSourceCreators that require creating
 * multiple instances of a prototype bean.
 *
 * <p>Uses an internal BeanFactory to manage the target instances,
 * copying the original bean definition to this internal factory.
 * This is necessary because the original BeanFactory will just
 * contain the proxy instance created through auto-proxying.
 *
 * <p>Requires running in an AbstractBeanFactory.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource
 * @see org.springframework.beans.factory.support.AbstractBeanFactory
 */
public abstract class AbstractBeanFactoryBasedTargetSourceCreator
		implements TargetSourceCreator, BeanFactoryAware, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private AbstractBeanFactory beanFactory;

	private DefaultListableBeanFactory internalBeanFactory;


	public final void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof AbstractBeanFactory)) {
			throw new IllegalArgumentException(
					"Cannot do auto-TargetSource creation with a BeanFactory that doesn't extend AbstractBeanFactory: " +
					beanFactory);
		}
		this.beanFactory = (AbstractBeanFactory) beanFactory;
		this.internalBeanFactory = new DefaultListableBeanFactory(beanFactory);
	}

	/**
	 * Return the BeanFactory that this TargetSourceCreators runs in.
	 */
	protected final BeanFactory getBeanFactory() {
		return beanFactory;
	}


	public final TargetSource getTargetSource(Class beanClass, String beanName) {
		AbstractBeanFactoryBasedTargetSource targetSource =
				createBeanFactoryBasedTargetSource(beanClass, beanName);
		if (targetSource == null) {
			return null;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Configuring AbstractBeanFactoryBasedTargetSource: " + targetSource);
		}

		// We need to override just this bean definition, as it may reference other beans
		// and we're happy to take the parent's definition for those.
		// Always use a prototype.
		RootBeanDefinition bd = this.beanFactory.getMergedBeanDefinition(beanName);
		RootBeanDefinition bdCopy = new RootBeanDefinition(bd);
		bdCopy.setSingleton(!isPrototypeBased());
		this.internalBeanFactory.registerBeanDefinition(beanName, bdCopy);

		// Complete configuring the PrototypeTargetSource.
		targetSource.setTargetBeanName(beanName);
		targetSource.setBeanFactory(this.internalBeanFactory);

		return targetSource;
	}

	/**
	 * Return whether this TargetSourceCreator is prototype-based.
	 * The singleton flag of the target bean definition will be set accordingly.
	 */
	protected boolean isPrototypeBased() {
		return true;
	}

	/**
	 * Subclasses must implement this method to return a new AbstractPrototypeBasedTargetSource
	 * if they wish to create a custom TargetSource for this bean, or <code>null</code> if they are
	 * not interested it in, in which case no special target source will be created.
	 * Subclasses should not call <code>setTargetBeanName</code> or <code>setBeanFactory</code>
	 * on the AbstractPrototypeBasedTargetSource: This class' implementation of
	 * <code>getTargetSource()</code> will do that.
	 * @param beanClass the class of the bean to create a TargetSource for
	 * @param beanName the name of the bean
	 * @return the AbstractPrototypeBasedTargetSource, or <code>null</code> if we don't match this
	 */
	protected abstract AbstractBeanFactoryBasedTargetSource createBeanFactoryBasedTargetSource(
			Class beanClass, String beanName);


	/**
	 * Destroys the internal BeanFactory on shutdown of the TargetSourceCreator.
	 */
	public void destroy() {
		this.internalBeanFactory.destroySingletons();
	}

}
