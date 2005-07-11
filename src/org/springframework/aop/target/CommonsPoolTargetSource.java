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

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Jakarta Commons pooling implementation extending <code>AbstractPoolingTargetSource</code>.
 * <p/>
 * By default, an instance of <code>GenericObjectPool</code> is created. Sub-classes may change the
 * type of <code>ObjectPool</code> used by overridding the <code>createObjectPool()</code> method.
 * <p/>
 * Provides many configuration properties mirroring those of the Commons Pool <code>GenericObjectPool</code> class.
 * This properties are passed to the <code>GenericObjectPool</code> during construction. If creating a sub-class of
 * this class to change the <code>ObjectPool</code> implementation type, you must remember to pass in the values of
 * configuration properties that are relevant to your chosen implementation.
 * <p/>
 * The <code>testOnBorrow</code>, <code>testOnReturn</code> and <code>testWhileIdle</code> properties are explictly not
 * mirrored because the implementation of <code>PoolableObjectFactory</code> used by this class does not implement
 * meaningful validation.
 * 
 * @author Rod Johnson
 * @author Rob Harrop
 * @see GenericObjectPool
 * @see #createObjectPool()
 * @see #setMaxIdle(int)
 * @see #setMaxSize(int)
 * @see #setMaxWait(long)
 * @see #setMinEvictableIdleTimeMillis(long)
 * @see #setMinIdle(int)
 * @see #setNumTestsPerEvictionRun(int)
 * @see #setTimeBetweenEvictionRunsMillis(long)
 */
public class CommonsPoolTargetSource extends AbstractPoolingTargetSource
		implements PoolableObjectFactory {

	/**
	 * The Jakarta Commons <code>ObjectPool</code> used to pool target objects
	 */
	private ObjectPool pool;

	/**
	 * Corresponds to the <code>maxIdle</code> flag of the underlying pool object.
	 * @see GenericObjectPool#setMaxIdle(int)
	 * @see #setMinIdle(int)
	 */
	private int maxIdle = GenericObjectPool.DEFAULT_MAX_IDLE;

	/**
	 * Corresponds to the <code>minIdle</code> flag of the underlying pool object.
	 * @see GenericObjectPool#setMinIdle(int)
	 * @see #setMinIdle(int)
	 */
	private int minIdle = GenericObjectPool.DEFAULT_MIN_IDLE;

	/**
	 * Corresponds to the <code>maxWait</code> flag of the underlying pool object.
	 * @see GenericObjectPool#setMaxWait(long)
	 * @see #setMaxWait(long)
	 */
	private long maxWait = GenericObjectPool.DEFAULT_MAX_WAIT;

	/**
	 * Corresponds to the <code>minEvictableIdleTimeMillis</code> flag of the underlying pool object.
	 * @see GenericObjectPool#setMinEvictableIdleTimeMillis(long)
	 * @see #setMinEvictableIdleTimeMillis(long)
	 */
	private long minEvictableIdleTimeMillis = GenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;

	/**
	 * Corresponds to the <code>numTestsPerEvictionRun</code> flag of the underlying pool object.
	 * @see GenericObjectPool#setNumTestsPerEvictionRun(int)
	 * @see #setNumTestsPerEvictionRun(int)
	 */
	private int numTestsPerEvictionRun = GenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN;

	/**
	 * Corresponds to the <code>timeBetweenEvictionRunsMillis</code> flag of the underlying pool object.
	 * @see GenericObjectPool#setTimeBetweenEvictionRunsMillis(long)
	 * @see #setTimeBetweenEvictionRunsMillis(long)
	 */
	private long timeBetweenEvictionRunsMillis = GenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;

	/**
	 * Gets the value of the <code>maxIdle</code> property used to configure the pool object when created.
	 */
	protected int getMaxIdle() {
		return maxIdle;
	}

	/**
	 * Sets the value of the <code>maxIdle</code> property which is passed to the pool object when created.
	 * @see GenericObjectPool#setMaxIdle(int)
	 */
	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	/**
	 * Gets the value of the <code>minIdle</code> property used to configure the pool object when created.
	 */
	protected int getMinIdle() {
		return minIdle;
	}

	/**
	 * Sets the value of the <code>minIdle</code> property which is passed to the pool object when created.
	 * @see GenericObjectPool#setMinIdle(int)
	 */
	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	/**
	 * Gets the value of the <code>maxWait</code> property used to configure the pool object when created.
	 */
	protected long getMaxWait() {
		return maxWait;
	}

	/**
	 * Sets the value of the <code>maxWait</code> property which is passed to the pool object when created.
	 * @see GenericObjectPool#setMaxWait(long)
	 */
	public void setMaxWait(long maxWait) {
		this.maxWait = maxWait;
	}

	/**
	 * Gets the value of the <code>minEvictableIdleTimeMillis</code> property used to configure the pool object when created.
	 */
	public long getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	/**
	 * Sets the value of the <code>minEvictableIdleTimeMillis</code> property which is passed to the pool object when created.
	 * @see GenericObjectPool#setMinEvictableIdleTimeMillis(long)
	 */
	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	/**
	 * Gets the value of the <code>numTestsPerEvictionRun</code> property used to configure the pool object when created.
	 */
	public int getNumTestsPerEvictionRun() {
		return numTestsPerEvictionRun;
	}

	/**
	 * Sets the value of the <code>numTestsPerEvictionRun</code> property which is passed to the pool object when created.
	 * @see GenericObjectPool#setNumTestsPerEvictionRun(int))
	 */
	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		this.numTestsPerEvictionRun = numTestsPerEvictionRun;
	}

	/**
	 * Gets the value of the <code>timeBetweenEvictionRunsMillis</code> property used to configure the pool object when created.
	 */
	public long getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	/**
	 * Sets the value of the <code>timeBetweenEvictionRunsMillis</code> property which is passed to the pool object when created.
	 * @see GenericObjectPool#setTimeBetweenEvictionRunsMillis(long)
	 */
	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	protected final void createPool(BeanFactory beanFactory) {
		logger.info("Creating Commons object pool");
		this.pool = createObjectPool();
	}

	/**
	 * Subclasses can override this if they want to return a different Commons pool to GenericObject pool.
	 * They should apply any configuration properties to the pool here.
	 * @return an empty Commons <code>ObjectPool</code>.
	 */
	protected ObjectPool createObjectPool() {
		GenericObjectPool gop = new GenericObjectPool(this);
		gop.setMaxActive(getMaxSize());
		gop.setMaxIdle(getMaxIdle());
		gop.setMinIdle(getMaxIdle());
		gop.setMaxWait(getMaxWait());
		gop.setMinEvictableIdleTimeMillis(getMinEvictableIdleTimeMillis());
		gop.setNumTestsPerEvictionRun(getNumTestsPerEvictionRun());
		gop.setTimeBetweenEvictionRunsMillis(getTimeBetweenEvictionRunsMillis());
		return gop;
	}

	/**
	 * An object leased from the pool.
	 */
	public Object getTarget() throws Exception {
		return this.pool.borrowObject();
	}

	/**
	 * Returns the specified object to the underlying <code>ObjectPool</code>.
	 */
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
	 * Closes the underlying <code>ObjectPool</code> when destroying this object.
	 */
	public void destroy() throws Exception {
		logger.info("Closing Commons ObjectPool");
		this.pool.close();
	}


	//----------------------------------------------------------------------------
	// Implementation of org.apache.commons.pool.PoolableObjectFactory interface
	//----------------------------------------------------------------------------

	public Object makeObject() throws BeansException {
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
