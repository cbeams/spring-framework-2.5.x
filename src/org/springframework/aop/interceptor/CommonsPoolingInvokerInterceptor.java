/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.interceptor;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.springframework.beans.factory.BeanFactory;

/**
 * Jakarta Commons pooling implementation extending AbstractPoolingInvokerInterceptor
 * @author Rod Johnson
 * @version $Id: CommonsPoolingInvokerInterceptor.java,v 1.1 2003-10-08 08:13:20 johnsonr Exp $
 */
public class CommonsPoolingInvokerInterceptor 
				extends AbstractPoolingInvokerInterceptor
				implements PoolableObjectFactory {

	/**
	 * Jakarta Commons object pool
	 */
	private ObjectPool pool;

	/**
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	protected final void createPool(BeanFactory beanFactory) throws Exception {
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
		gop.setMaxActive(getPoolSize());
		return gop;
	}

	/**
	 * @see org.springframework.aop.interceptor.AbstractPoolingInvokerInterceptor#acquireTarget()
	 */
	protected Object acquireTarget() throws Exception {
		return this.pool.borrowObject();
	}

	/**
	 * @see org.springframework.aop.interceptor.AbstractPoolingInvokerInterceptor#releaseTarget(java.lang.Object)
	 */
	protected void releaseTarget(Object target) throws Exception {
		this.pool.returnObject(target);
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


	//---------------------------------------------------------------------
	// Implementation of org.apache.commons.pool.PoolableObjectFactory interface
	//---------------------------------------------------------------------
	/**
	 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
	 */
	public Object makeObject() throws Exception {
		return createTarget();
	}

	/**
	 * @see org.apache.commons.pool.PoolableObjectFactory#destroyObject(java.lang.Object)
	 */
	public void destroyObject(Object o) throws Exception {
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
