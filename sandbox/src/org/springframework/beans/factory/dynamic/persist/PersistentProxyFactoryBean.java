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

package org.springframework.beans.factory.dynamic.persist;

import org.springframework.aop.TargetSource;
import org.springframework.aop.target.dynamic.AbstractRefreshableTargetSource;
import org.springframework.aop.framework.ProxyFactoryBean;

/**
 * Special ProxyBean to use with persistent beans.
 * A subclass is required to handle these target sources
 * slightly differently.
 * You can add other interfaces and advisors/advices
 * as with a normal ProxyFactoryBean.
 * @author Rod Johnson
 */
public class PersistentProxyFactoryBean extends ProxyFactoryBean {
	
	private Class persistentClass;
	
	public PersistentProxyFactoryBean() {
		setProxyTargetClass(true);
	}
	
	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		// We need to implement this to avoid a circular dependency
		// when the targetSource enumerates beans in the factory
		// to do autowiring
		return persistentClass;
	}
	
	public void setTargetSource(TargetSource ts) {
		if (!(ts instanceof AbstractRefreshableTargetSource)) {
			throw new IllegalArgumentException("PersistentProxyFactoryBean must be used with an AbstractPersistenceStoreRefreshableTargetSource");
		}
		AbstractPersistenceStoreRefreshableTargetSource arts = (AbstractPersistenceStoreRefreshableTargetSource) ts;
		//addAdvisor(arts.getIntroductionAdvisor());
		super.setTargetSource(arts);
		this.persistentClass = arts.getPersistentClass();
	}
	
	public void setTarget(Object o) {
		throw new UnsupportedOperationException("Use setTargetSource");
	}
	
}
