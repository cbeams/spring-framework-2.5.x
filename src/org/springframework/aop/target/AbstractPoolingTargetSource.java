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

import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;

/**
 * Abstract superclass for pooling TargetSources that maintains a pool of
 * target instances, acquiring and releasing a target object from the pool
 * for each method invocation. This class is independent of pooling technology.
 *
 * <p>Subclasses must implement the getTarget() and releaseTarget() methods
 * to work with their chosen pool. The newPrototypeInstance() method inherited
 * from AbstractPrototypeBasedTargetSource can be used to create objects to put
 * in the pool. Subclasses must also implement some of the monitoring methods from
 * the PoolingConfig interface. This class provides the getPoolingConfigMixin()
 * method to return an IntroductionAdvisor making these stats available on proxied
 * objects.
 *
 * <p>This class implements DisposableBean to force subclasses to implement
 * a destroy() method to close down their pool.
 *
 * @author Rod Johnson
 * @see #getTarget
 * @see #releaseTarget
 * @see #destroy
 */
public abstract class AbstractPoolingTargetSource extends AbstractPrototypeBasedTargetSource
		implements PoolingConfig, DisposableBean {
	
	/** The size of the pool */
	private int maxSize;

	/**
	 * Set the maximum size of the pool.
	 */
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * Return the maximum size of the pool.
	 */
	public int getMaxSize() {
		return this.maxSize;
	}

	public final void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		super.setBeanFactory(beanFactory);
		try {
			createPool(beanFactory);
		}
		catch (BeansException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new BeanInitializationException("Could not create instance pool", ex);
		}
	}

	/**
	 * Create the pool.
	 * @param beanFactory owning BeanFactory, in case we need collaborators from it
	 * (normally our own properties are sufficient)
	 * @throws Exception to avoid placing constraints on pooling APIs
	 */
	protected abstract void createPool(BeanFactory beanFactory) throws Exception;
	
	/**
	 * Acquire an object from the pool.
	 * @return an object from the pool
	 * @throws Exception we may need to deal with checked exceptions from pool
	 * APIs, so we're forgiving with our exception signature
	 */
	public abstract Object getTarget() throws Exception;
	
	/**
	 * Return the given object to the pool.
	 * @param target object that must have been acquired from the pool
	 * via a call to getTarget()
	 * @throws Exception to allow pooling APIs to throw exception
	 * @see #getTarget
	 */
	public abstract void releaseTarget(Object target) throws Exception; 

	/**
	 * Return an IntroductionAdvisor that providing a mixin
	 * exposing statistics about the pool maintained by this object.
	 */
	public DefaultIntroductionAdvisor getPoolingConfigMixin() {
		DelegatingIntroductionInterceptor dii = new DelegatingIntroductionInterceptor(this);
		return new DefaultIntroductionAdvisor(dii, PoolingConfig.class);
	}

}
