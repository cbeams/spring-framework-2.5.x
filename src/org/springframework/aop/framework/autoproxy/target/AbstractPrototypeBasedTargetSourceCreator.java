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

package org.springframework.aop.framework.autoproxy.target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.TargetSourceCreator;
import org.springframework.aop.target.AbstractPrototypeBasedTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * Convenient superclass for TargetSourceCreators that require creating
 * multiple instances of a prototype bean.
 * @author Rod Johnson
 * @see org.springframework.aop.target.AbstractPrototypeBasedTargetSource
 */
public abstract class AbstractPrototypeBasedTargetSourceCreator implements TargetSourceCreator {

	protected final Log logger = LogFactory.getLog(getClass());

	public final TargetSource getTargetSource(Object bean, String beanName, BeanFactory factory) {
		AbstractPrototypeBasedTargetSource prototypeTargetSource = createPrototypeTargetSource(bean, beanName, factory);
		if (prototypeTargetSource == null) {
			return null;
		}

		else {
			if (!(factory instanceof ConfigurableListableBeanFactory)) {
				logger.warn("Cannot do auto TargetSource creation with a BeanFactory " +
						"that doesn't implement ConfigurableListableBeanFactory");
				return null;
			}

			logger.debug("Configuring AbstractPrototypeBasedTargetSource");
			ConfigurableListableBeanFactory listableFactory = (ConfigurableListableBeanFactory) factory;
			BeanDefinition definition = listableFactory.getBeanDefinition(beanName);

			// Infinite cycle will result if we don't use a different factory,
			// because a getBean() call with this beanName will go through the autoproxy
			// infrastructure again.
			// We need to override just this bean definition, as it may reference other beans
			// and we're happy to take the parent's definition for those.
			DefaultListableBeanFactory beanFactory2 = new DefaultListableBeanFactory(factory);

			// Override the prototype bean.
			beanFactory2.registerBeanDefinition(beanName, definition);
			
			// Complete configuring the PrototypeTargetSource.
			prototypeTargetSource.setTargetBeanName(beanName);
			prototypeTargetSource.setBeanFactory(beanFactory2);

			return prototypeTargetSource;
		}
	}

	/**
	 * Subclasses must implement this method to return a new AbstractPrototypeBasedTargetSource
	 * if they wish to create a custom TargetSource for this bean, or null if they are
	 * not interested it in, in which case no special target source will be created.
	 * Subclasses should not call <code>setTargetBeanName</code> or <code>setBeanFactory</code>
	 * on the AbstractPrototypeBasedTargetSource: This class's implementation of
	 * <code>getTargetSource()</code> will do that.
	 * @return the AbstractPrototypeBasedTargetSource, or null if we don't match this
	 */
	protected abstract AbstractPrototypeBasedTargetSource createPrototypeTargetSource(
			Object bean, String beanName, BeanFactory factory);

}
