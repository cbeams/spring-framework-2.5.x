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
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;

/**
 * Introduction interceptor that provides DynamicScript
 * implementation for dynamic objects.
 
 * TODO merge with TargetSource? definitely faster,
 * doesn't need to cast via Advised
 
 * @author Rod Johnson
 * @version $Id: DynamicObjectInterceptor.java,v 1.2 2004-08-04 16:49:47 johnsonr Exp $
 */
public class DynamicObjectInterceptor extends DelegatingIntroductionInterceptor
		implements DynamicObject {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see org.springframework.beans.factory.script.DynamicScript#refresh()
	 */
	public void refresh() throws BeansException {
		refreshableTargetSource().refresh();
	}

	/**
	 * @return
	 */
	protected AbstractRefreshableTargetSource refreshableTargetSource() {
		TargetSource ts = ((Advised) AopContext.currentProxy()).getTargetSource();
		if (ts == null) {
			throw new IllegalStateException("No targetSource set");
		}
		if (!(ts instanceof AbstractRefreshableTargetSource)) {
			throw new IllegalStateException("Not refreshable'");
		}
		return (AbstractRefreshableTargetSource) ts;
	}
	
	protected DynamicObject dynamicTargetSource() {
		TargetSource ts = ((Advised) AopContext.currentProxy()).getTargetSource();
		if (ts == null) {
			throw new IllegalStateException("No targetSource set");
		}
		if (!(ts instanceof DynamicObject)) {
			throw new IllegalStateException("Not refreshable'");
		}
		return (DynamicObject) ts;
	}


	/**
	 * @see org.springframework.beans.factory.script.DynamicScript#getLoads()
	 */
	public int getLoads() {
		return refreshableTargetSource().getLoads();
	}

	/**
	 * @see org.springframework.beans.factory.script.DynamicScript#getLastRefreshMillis()
	 */
	public long getLastRefreshMillis() {
		return refreshableTargetSource().getLastRefreshMillis();
	}


	/**
	 * Subclasses can override this for efficiency
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#isModified()
	 */
	public boolean isModified() {
		return refreshableTargetSource().isModified();
	}


	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#getExpiry()
	 */
	public long getExpiry() {
		return dynamicTargetSource().getExpiry();
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#isAutoRefresh()
	 */
	public boolean isAutoRefresh() {
		return dynamicTargetSource().isAutoRefresh();
	}
	/**
	 * @see org.springframework.beans.factory.dynamic.DynamicObject#setAutoRefresh(boolean)
	 */
	public void setAutoRefresh(boolean autoRefresh) {
		dynamicTargetSource().setAutoRefresh(autoRefresh);
	}
}
