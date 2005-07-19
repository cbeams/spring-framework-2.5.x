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

package org.springframework.aop.framework.adapter;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.Interceptor;

import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * Default implementation of the AdvisorAdapterRegistry interface.
 * Supports Interceptor, BeforeAdvice, AfterReturningAdvice, ThrowsAdvice.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 */
public class DefaultAdvisorAdapterRegistry implements AdvisorAdapterRegistry {
	
	private final List adapters = new ArrayList(3);
	
	public DefaultAdvisorAdapterRegistry() {
		// Register well-known adapters.
		registerAdvisorAdapter(new BeforeAdviceAdapter());
		registerAdvisorAdapter(new AfterReturningAdviceAdapter());
		registerAdvisorAdapter(new ThrowsAdviceAdapter());
	}

	public Advisor wrap(Object adviceObject) throws UnknownAdviceTypeException {
		if (adviceObject instanceof Advisor) {
			return (Advisor) adviceObject;
		}
		if (!(adviceObject instanceof Advice)) {
			throw new UnknownAdviceTypeException(adviceObject);
		}
		Advice advice = (Advice) adviceObject;
		if (advice instanceof Interceptor) {
			// So well-known it doesn't even need an adapter.
			return new DefaultPointcutAdvisor(advice);
		}
		for (int i = 0; i < this.adapters.size(); i++) {
			// Check that it is supported.
			AdvisorAdapter adapter = (AdvisorAdapter) this.adapters.get(i);
			if (adapter.supportsAdvice(advice)) {
				return new DefaultPointcutAdvisor(advice);
			}
		}
		throw new UnknownAdviceTypeException(advice);
	}

	public Interceptor[] getInterceptors(Advisor advisor) throws UnknownAdviceTypeException {
		List interceptors = new ArrayList(3);
		Advice advice = advisor.getAdvice();
		if (advice instanceof Interceptor) {
			interceptors.add(advice);
		}
		for (int i = 0; i < this.adapters.size(); i++) {
			AdvisorAdapter adapter = (AdvisorAdapter) this.adapters.get(i);
			if (adapter.supportsAdvice(advice)) {
				interceptors.add(adapter.getInterceptor(advisor));
			}
		}
		if (interceptors.isEmpty()) {
			throw new UnknownAdviceTypeException(advisor.getAdvice());
		}
		return (Interceptor[]) interceptors.toArray(new Interceptor[interceptors.size()]);
	}

	public void registerAdvisorAdapter(AdvisorAdapter adapter) {
		this.adapters.add(adapter);
	}

}
