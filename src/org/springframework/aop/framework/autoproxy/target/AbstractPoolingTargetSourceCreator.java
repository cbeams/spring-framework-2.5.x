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

import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.aop.target.AbstractPoolingTargetSource;
import org.springframework.aop.target.CommonsPoolTargetSource;

/**
 * Convenient superclass for TargetSource creators that create pooling TargetSources.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.target.AbstractPoolingTargetSource
 * @see org.springframework.aop.target.CommonsPoolTargetSource
 */
public abstract class AbstractPoolingTargetSourceCreator extends AbstractBeanFactoryBasedTargetSourceCreator {

	protected final AbstractBeanFactoryBasedTargetSource createBeanFactoryBasedTargetSource(
			Class beanClass, String beanName) {

		PoolingAttribute poolingAttribute = getPoolingAttribute(beanClass, beanName);
		if (poolingAttribute == null) {
			// no pooling attribute
			return null;
		}
		else {
			AbstractPoolingTargetSource targetSource = newPoolingTargetSource(poolingAttribute);
			targetSource.setMaxSize(poolingAttribute.getSize());
			return targetSource;
		}
	}
	
	/**
	 * Create a new AbstractPoolingTargetSource. This implementation creates a
	 * CommonsPoolTargetSource, but subclasses may wish to override that behavior
	 * (potentially even using different pools for specific PoolingAttribute subclasses).
	 * <p>The created AbstractPoolingTargetSource does not have to be configured,
	 * This will all be handled by this TargetSourceCreator and its base class.
	 * @see org.springframework.aop.target.CommonsPoolTargetSource
	 */
	protected AbstractPoolingTargetSource newPoolingTargetSource(PoolingAttribute poolingAttribute) {
		return new CommonsPoolTargetSource();
	}

	/**
	 * Create a PoolingAttribute for the given bean, if any.
	 * @param beanClass the class of the bean to create a TargetSource for
	 * @param beanName the name of the bean
	 * @return the PoolingAttribute, or <code>null</code> for no pooling
	 */
	protected abstract PoolingAttribute getPoolingAttribute(Class beanClass, String beanName);

}
