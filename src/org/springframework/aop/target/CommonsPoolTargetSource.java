/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.target;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Jakarta Commons pooling implementation extending AbstractPoolingInvokerInterceptor
 * @author Rod Johnson
 * @version $Id: CommonsPoolTargetSource.java,v 1.3 2003-12-30 01:07:12 jhoeller Exp $
 */
public class CommonsPoolTargetSource extends AbstractPoolingTargetSource
				implements PoolableObjectFactory {

	/**
	 * Jakarta Commons object pool
	 */
	private ObjectPool pool;

	/**
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	protected final void createPool(BeanFactory beanFactory) {
		logger.info("Creating Commons object pool");
		this.pool = createObjectPool();
	}

	/**
	 * Subclasses can override this if they want to return a different
	 * Commons pool to GenericObject pool.
	 * They should apply properties to the pool here
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
	
	public int getActive() {
		return this.pool.getNumActive();
	}

	public int getFree() {
		return this.pool.getNumIdle();
	}
	
	
	//---------------------------------------------------------------------
	// Implementation of DisposableBean interface
	//---------------------------------------------------------------------
	/**
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() throws Exception {
		logger.info("Closing Commons pool");
		this.pool.close();
	}


	//----------------------------------------------------------------------------
	// Implementation of org.apache.commons.pool.PoolableObjectFactory interface
	//----------------------------------------------------------------------------

	/**
	 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
	 */
	public Object makeObject() {
		return newPrototypeInstance();
	}

	/**
	 * @see org.apache.commons.pool.PoolableObjectFactory#destroyObject(java.lang.Object)
	 */
	public void destroyObject(Object o) throws Exception {
		if (o instanceof DisposableBean) {
			((DisposableBean) o).destroy();
		}
	}

	/**
	 * @see org.apache.commons.pool.PoolableObjectFactory#validateObject(java.lang.Object)
	 */
	public boolean validateObject(Object o) {
		return true;
	}

	/**
	 * @see org.apache.commons.pool.PoolableObjectFactory#activateObject(java.lang.Object)
	 */
	public void activateObject(Object o) throws Exception {
	}

	/**
	 * @see org.apache.commons.pool.PoolableObjectFactory#passivateObject(java.lang.Object)
	 */
	public void passivateObject(Object o) throws Exception {
	}

}
