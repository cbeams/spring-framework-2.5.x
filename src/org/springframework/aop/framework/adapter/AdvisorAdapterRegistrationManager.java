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

package org.springframework.aop.framework.adapter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * BeanPostProcessor implementation that "registers" instances of any
 * non-default AdvisorAdapters with GlobalAdvisorAdapterRegistry.
 *
 * <p>The only requirement for it to work is that it needs to be defined
 * in application context along with any arbitrary "non-native" Spring
 * AdvisorAdapters that need to be "recognized" by Spring's AOP framework.
 * 
 * @author Dmitriy Kopylenko
 */
public class AdvisorAdapterRegistrationManager implements BeanPostProcessor {

	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

	/**
	 * Specify the AdvisorAdapterRegistry to use.
	 * Default is the global AdvisorAdapterRegistry.
	 * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}

	public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
		if (bean instanceof AdvisorAdapter){
			this.advisorAdapterRegistry.registerAdvisorAdapter((AdvisorAdapter) bean);
		}
		return bean;
	}

}
