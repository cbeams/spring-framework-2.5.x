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

package org.springframework.beans.factory.dynamic;

import org.springframework.aop.TargetSource;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

/**
 * Superclass for TargetSources that are threadsafe yet
 * support refresh operations.
 * @author Rod Johnson
 */
public abstract class AbstractRefreshableTargetSource extends DelegatingIntroductionInterceptor implements TargetSource, DynamicObject {
	
	private int loads;
	
	private long lastRefresh;
	
	private Object target;
	
	private ExpirableObject expirableObject;
	
	private long expiry;
	
	private boolean autoRefresh;
	
	private long lastRefreshTime;
	
	private long lastCheck;
	
	public AbstractRefreshableTargetSource(Object initialTarget) {
		lastRefresh = lastCheck = System.currentTimeMillis();
		loads = 1;
		suppressInterface(TargetSource.class);
		this.target = initialTarget;
	}
	
	public void setExpirableObject(ExpirableObject expirableObject) {
		this.expirableObject = expirableObject;
	}
	
	public void setExpirySeconds(long expirySeconds) {
		this.expiry = expirySeconds * 1000;
	}
	
	public synchronized void refresh() {
		target = refreshedTarget();
		lastRefreshTime = System.currentTimeMillis();
		++loads;
	}
	
	protected abstract Object refreshedTarget();

	/**
	 * @see org.springframework.beans.factory.dynamic.ExpirableObject#getLastRefreshMillis()
	 */
	public long getLastRefreshMillis() {
		return lastRefresh;
	}
	/**
	 * @see org.springframework.beans.factory.dynamic.ExpirableObject#getLoads()
	 */
	public int getLoads() {
		return loads;
	}
	/**
	 * @see org.springframework.beans.factory.dynamic.ExpirableObject#isModified()
	 */
	public boolean isModified() {
		if (expirableObject != null) {
			return expirableObject.isModified();
		}
		throw new UnsupportedOperationException();
	}
	/**
	 * @see org.springframework.aop.TargetSource#getTarget()
	 */
	public synchronized Object getTarget() throws Exception {
		if (autoRefresh &&
				System.currentTimeMillis() - lastCheck > expiry) {
			if (isModified()) {
				refresh();
			}
			lastCheck = System.currentTimeMillis();
		}
		return this.target;
	}
	/**
	 * @see org.springframework.aop.TargetSource#getTargetClass()
	 */
	public Class getTargetClass() {
		return target.getClass();
	}
	/**
	 * @see org.springframework.aop.TargetSource#isStatic()
	 */
	public boolean isStatic() {
		return false;
	}
	/**
	 * @see org.springframework.aop.TargetSource#releaseTarget(java.lang.Object)
	 */
	public void releaseTarget(Object target) throws Exception {
		// Do nothing
	}
	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#getExpiry()
	 */
	public long getExpiry() {
		return expiry;
	}
	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#isAutoRefresh()
	 */
	public boolean isAutoRefresh() {
		return autoRefresh;
	}
	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#setAutoRefresh(boolean)
	 */
	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}
}
