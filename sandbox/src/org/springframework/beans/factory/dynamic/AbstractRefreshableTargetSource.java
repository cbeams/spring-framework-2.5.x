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

import org.springframework.aop.target.HotSwappableTargetSource;

/**
 * Superclass for TargetSources that are threadsafe yet
 * support refresh operations.
 * @author Rod Johnson
 * @version $Id: AbstractRefreshableTargetSource.java,v 1.2 2004-08-04 16:49:47 johnsonr Exp $
 */
public abstract class AbstractRefreshableTargetSource extends HotSwappableTargetSource implements ExpirableObject {
	
	private int loads;
	
	private long lastRefresh;
	
	public AbstractRefreshableTargetSource(Object initialTarget) {
		super(initialTarget);
		lastRefresh = System.currentTimeMillis();
		loads = 1;
	}
	
	public void refresh() {
		swap(refreshedTarget());
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
		throw new UnsupportedOperationException();
	}
}
