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

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.Interceptor;

import org.springframework.aop.Advisor;
import org.springframework.aop.AfterReturningAdvice;

/**
 * Adapter to enable AfterReturningAdvisor and MethodAfterReturningAdvice
 * to be used in the Spring AOP framework.
 *
 * <p>This involves wrapping these advice types in interceptors.
 * 
 * @author Rod Johnson
 */
class AfterReturningAdviceAdapter implements AdvisorAdapter {

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#supportsAdvice
	 */
	public boolean supportsAdvice(Advice advice) {
		return advice instanceof AfterReturningAdvice;
	}


	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#getInterceptor
	 */
	public Interceptor getInterceptor(Advisor advisor) {
		AfterReturningAdvice advice = (AfterReturningAdvice) advisor.getAdvice();
		return new AfterReturningAdviceInterceptor(advice);
	}

}
