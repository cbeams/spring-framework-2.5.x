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

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Jakarta Commons pooling implementation extending AbstractPoolingTargetSource.
 * @author Rod Johnson
 */
public class CommonsPoolTargetSource extends AbstractPoolingTargetSource
				implements PoolableObjectFactory {

	/** Jakarta Commons object pool */
	private ObjectPool pool;

	protected final void createPool(BeanFactory beanFactory) {
		logger.info("Creating Commons object pool");
		this.pool = createObjectPool();
	}

	/**
	 * Subclasses can override this if they want to return a different
	 * Commons pool to GenericObject pool.
	 * They should apply properties to the pool here.
	 * @return an empty Commons pool 
	 */
	protected ObjectPool createObjectPool() {
		GenericObjectPool gop = new GenericObjectPool(this);
		gop.setMaxActive(getMaxSize());
		return gop;
	}

	public Object getTarget() throws Exception {
		return this.pool.borrowObject();
	}

	public void releaseTarget(Object target) throws Exception {
		this.pool.returnObject(target);
	}

	public int getActiveCount() throws UnsupportedOperationException {
		return this.pool.getNumActive();
	}

	public int getIdleCount() throws UnsupportedOperationException {
		return this.pool.getNumIdle();
	}

	/**
	 * @deprecated in favor of getActiveCount
	 * @see #getActiveCount
	 */
	public int getActive() {
		return this.pool.getNumActive();
	}

	/**
	 * @deprecated in favor of getIdleCount
	 * @see #getIdleCount
	 */
	public int getFree() {
		return this.pool.getNumIdle();
	}
	
	
	//---------------------------------------------------------------------
	// Implementation of DisposableBean interface
	//---------------------------------------------------------------------

	public void destroy() throws Exception {
		logger.info("Closing Commons object pool");
		this.pool.close();
	}


	//----------------------------------------------------------------------------
	// Implementation of org.apache.commons.pool.PoolableObjectFactory interface
	//----------------------------------------------------------------------------

	public Object makeObject() {
		return newPrototypeInstance();
	}

	public void destroyObject(Object o) throws Exception {
		if (o instanceof DisposableBean) {
			((DisposableBean) o).destroy();
		}
	}

	public boolean validateObject(Object o) {
		return true;
	}

	public void activateObject(Object o) throws Exception {
	}

	public void passivateObject(Object o) throws Exception {
	}

}
