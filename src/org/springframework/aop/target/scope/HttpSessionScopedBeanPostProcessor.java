/*
 * Copyright 2004-2005 the original author or authors.
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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.filter.HttpSessionScopeMap;
import org.springframework.web.filter.ThreadBoundHttpSessionScopeIdentifierResolver;

/**
 * <p>BeanPostProcessor returning prototype bean instances from the HTTP session.
 * 
 * <p>If after initialization a bean with the same name is found in the HTTP session this
 * instance is returned. If no instance is found the instance created by the bean factory
 * is stored in the HTTP session and returned.
 * 
 * <p>This BeanPostProcessor takes the hit of creating and initializing a prototype bean
 * each time an instance is requested. These instances are discarded if an instance with
 * the same name is found in the HTTP session.
 * 
 * @author Steven Devijver
 * @since Oct 3, 2005
 */
public class HttpSessionScopedBeanPostProcessor implements BeanPostProcessor {

	private BeanFactory beanFactory = null;
	private ScopeMap scopeMap = new HttpSessionScopeMap();
	private ScopeIdentifierResolver scopeIdentifierResolver = new ThreadBoundHttpSessionScopeIdentifierResolver();
	
	public HttpSessionScopedBeanPostProcessor(BeanFactory beanFactory) {
		super();
		this.beanFactory = beanFactory;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		boolean isSingleton = this.beanFactory.isSingleton(beanName);
		
		if (isSingleton) {
			return bean;
		}
		
		Object o = this.scopeMap.get(this.scopeIdentifierResolver.getScopeIdentifier(), beanName);
		if (o == null) {
			this.scopeMap.put(this.scopeIdentifierResolver.getScopeIdentifier(), beanName, bean);
			return bean;
		} else {
			return o;
		}
	}

}
