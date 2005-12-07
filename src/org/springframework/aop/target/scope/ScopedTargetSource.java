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

package org.springframework.aop.target.scope;

import org.springframework.aop.target.AbstractPrototypeBasedTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;

/**
 * TargetSource that adds per-scope capabilities to Spring IoC
 * container. The target will be sourced from a ScopeMap strategy
 * interface, which can be backed by a variety of session stores.
 * Thus this TargetSource can be used for a variety of scopes such
 * as per HTTP request or session, or even a stateful model along the
 * lines of Stateful Session EJBs.
 *
 * <p>Targets will be created lazily in the scope. Scope map
 * implementations are expected to be thread-safe.
 *
 * <p>"scopeMap" and "targetBeanName" (inherited) are required properties.
 * The "scopeKey" property may optionally be set to specify the key for
 * the scope object in the ScopeMap.
 *
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.aop.target.scope.ScopeMap
 */
public class ScopedTargetSource extends AbstractPrototypeBasedTargetSource implements ScopingConfig {
	
	private String scopeKey;
	
	private ScopeMap scopeMap;


	/**
	 * Set the object that will be used to resolve the target object in a given scope
	 * @param scopeMap strategy interface used to resolve the target object
	 * in the gien scope
	 */
	public void setScopeMap(ScopeMap scopeMap) {
		this.scopeMap = scopeMap;
	}

	public ScopeMap getScopeMap() {
		return this.scopeMap;
	}

	/**
	 * Set the scopeKey (optional property). Will be automatically generated if not set.
	 * @param sessionEntryName
	 */
	public void setScopeKey(String sessionEntryName) {
		this.scopeKey = sessionEntryName;
	}

	public String getScopeKey() {
		return this.scopeKey;
	}

	/**
	 * Overridden to autogenerate scopeKey if the property was not set.
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		super.setBeanFactory(beanFactory);
		if (this.scopeKey == null) {
			// Autogenerate session key.
			this.scopeKey =
					getClass().getName() + "_" +  getTargetBeanName() + "_" + System.identityHashCode(this);
		}
	}


	/**
	 * Return a target, lazily initializing in the current scope.
	 * The object will be stored in the scope map.
	 */
	public Object getTarget() throws Exception {
		Object o = this.scopeMap.get(this.scopeKey);
		if (o == null) {
			// Lazily initialize in scope
			logger.info("Creating new scoped instance of object with name '" + this.scopeKey + "'");
			o = newPrototypeInstance();
			this.scopeMap.put(this.scopeKey, o);
		}
		return o;
	}

	public void copyFrom(ScopedTargetSource other) {
		super.copyFrom(other);
		this.scopeKey = other.scopeKey;
		this.scopeMap = other.scopeMap;
	}

	public String toString() {
		return "ScopedTargetSource: scopeKey='" + this.scopeKey + "'; scopeMap=[" + this.scopeMap + "]";
	}

}
