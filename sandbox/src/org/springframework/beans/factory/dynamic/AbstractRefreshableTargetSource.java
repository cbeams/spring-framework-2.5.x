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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

/**
 * Superclass for TargetSources that are threadsafe yet
 * support refresh operations. This class can return an IntroductionAdvisor
 * allowing control over the dynamic target. See the getInroductionAdvisor() method.
 * Subclasses must implement the abstract refreshedTarget() method to return
 * an up-to-date target.
 * <p>Call the refresh() method before use.
 * @author Rod Johnson
 */
public abstract class AbstractRefreshableTargetSource 
	implements TargetSource, DynamicObject {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private int loads;
	
	private long lastRefresh;
	
	private Object currentTarget;
	
	private long expiry;
	
	private boolean autoRefresh;
	
	private long lastRefreshTime;
	
	private long lastCheck;
	
	private boolean modified;
	
	private DelegatingIntroductionInterceptor dii = new DelegatingIntroductionInterceptor(this);
	
	public AbstractRefreshableTargetSource() {
		lastRefresh = lastCheck = System.currentTimeMillis();
		loads = 0;
		// Don't want to expose the TargetSource interface in the introduction
		suppressInterface(TargetSource.class);
	}
	
	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#isAutoRefresh()
	 */
	public boolean isAutoRefresh() {
		return autoRefresh;
	}
	
	/**
	 * Enable autorefresh. Default is disabled. Autorefresh will impact performance.
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#setAutoRefresh(boolean)
	 */
	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}
	
	
	public void setExpirySeconds(long expirySeconds) {
		this.expiry = expirySeconds * 1000;
	}
	
	public synchronized void refresh() {
		currentTarget = refreshedTarget();
		lastRefreshTime = System.currentTimeMillis();
		++loads;
		modified = false;
	}
	
	/**
	 * Subclass must implement this to return a refreshed target.
	 * @return
	 */
	protected abstract Object refreshedTarget();
	
	public boolean isLoaded() {
		return currentTarget != null;
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.ExpirableObject#getLastRefreshMillis()
	 */
	public long getLastRefreshMillis() {
		return lastRefresh;
	}
	/**
	 * @see org.springframework.beans.factory.dynamic.ExpirableObject#getLoadCount()
	 */
	public int getLoadCount() {
		return loads;
	}

	
	/**
	 * @see org.springframework.aop.TargetSource#getTarget()
	 */
	public synchronized Object getTarget() throws Exception {
		if (autoRefresh && isModified()) {
			refresh();
		}
		return this.currentTarget;
	}
	
	public boolean isModified() {
		if (modified) {
			return true;
		}
		
		boolean flag =  System.currentTimeMillis() - lastCheck > expiry;
		lastCheck = System.currentTimeMillis();
		return flag;
	}
	
	/**
	 * This can be invoked when a cache is updated etc.
	 *
	 */
	public synchronized void markModified() {
		this.modified = true;
	}
	
	/**
	 * @see org.springframework.aop.TargetSource#getTargetClass()
	 */
	public Class getTargetClass() {
		return (currentTarget != null) ? currentTarget.getClass() : null;
	}
	/**
	 * @see org.springframework.aop.TargetSource#isStatic()
	 */
	public final boolean isStatic() {
		return false;
	}
	
	/**
	 * @see org.springframework.aop.TargetSource#releaseTarget(java.lang.Object)
	 */
	public void releaseTarget(Object target) throws Exception {
		// Do nothing
	}
	
	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#getExpiryMillis()
	 */
	public long getExpiryMillis() {
		return expiry;
	}
	
	/**
	 * Return an IntroductionAdvisor that will delegate all DynamicObject methods to this object.
	 * It will also implement any additional interfaces implemented by subclasses unless they are
	 * suppressed by calling suppressInterface().
	 * @return
	 */
	public IntroductionAdvisor getIntroductionAdvisor() {	
		return new DefaultIntroductionAdvisor(dii);
	}
	
	/**
	 * Ensure that the IntroductionAdvisor returned by this object doesn't implement
	 * the specified interface. We might want to do this if a subclass of this class implements
	 * an interface (such as BeanFactoryAware) that should not be exposed.
	 * @param intf interface implemented by a subclass.
	 */
	public void suppressInterface(Class intf) {
		dii.suppressInterface(intf);
	}
}
