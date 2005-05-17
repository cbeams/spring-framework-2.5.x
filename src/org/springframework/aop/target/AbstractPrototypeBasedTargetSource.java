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

package org.springframework.aop.target;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;

/**
 * Base class for dynamic TargetSources that can create new prototype bean
 * instances to support a pooling or new-instance-per-invocation strategy.
 *
 * <p>Such TargetSources must run in a BeanFactory, as it needs to call the
 * <code>getBean</code> method to create a new prototype instance.
 * Therefore, this base class extends AbstractBeanFactoryBasedTargetSource.
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.beans.factory.BeanFactory#getBean
 * @see PrototypeTargetSource
 * @see ThreadLocalTargetSource
 * @see CommonsPoolTargetSource
 */
public abstract class AbstractPrototypeBasedTargetSource extends AbstractBeanFactoryBasedTargetSource {

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		super.setBeanFactory(beanFactory);

		// Check whether the target bean is defined as prototype.
		if (beanFactory.isSingleton(getTargetBeanName())) {
			throw new BeanDefinitionStoreException(
				"Cannot use PrototypeBasedTargetSource against singleton bean with name '" + getTargetBeanName() + "': " +
                "instances would not be independent");
		}
	}

	/**
	 * Subclasses should use this method to create a new prototype instance.
	 */
	protected Object newPrototypeInstance() throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new target from bean '" + getTargetBeanName() + "'");
		}
		return getBeanFactory().getBean(getTargetBeanName());
	}

}
