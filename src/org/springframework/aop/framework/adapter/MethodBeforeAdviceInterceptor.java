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

import java.io.Serializable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.MethodBeforeAdvice;

/**
 * Interceptor to wrap a MethodBeforeAdvice. In future we may also offer a more
 * efficient alternative solution in cases where there is no interception advice
 * and therefore no need to create a MethodInvocation object.
 *
 * <p>Used internally by the AOP framework: application developers should not need
 * to use this class directly.
 * 
 * <p>You can use this class to wrap Spring MethodBeforeAdvice implementations
 * for use in other AOP frameworks supporting the AOP Alliance
 * interfaces.
 *
 * @author Rod Johnson
 */
public final class MethodBeforeAdviceInterceptor implements MethodInterceptor, Serializable {
	
	private MethodBeforeAdvice advice;
	
	public MethodBeforeAdviceInterceptor(MethodBeforeAdvice advice) {
		this.advice = advice;
	}

	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation mi) throws Throwable {
		advice.before(mi.getMethod(), mi.getArguments(), mi.getThis() );
		return mi.proceed();
	}
	
}