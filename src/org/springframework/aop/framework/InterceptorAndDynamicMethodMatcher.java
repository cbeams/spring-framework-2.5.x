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

package org.springframework.aop.framework;

import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.MethodMatcher;

/**
 * Internal framework class.
 *
 * <p>This class is required because if we put an Interceptor that implements
 * InterceptionAdvice in the interceptor list passed to MethodInvocationImpl,
 * it may be mistaken for an advice that requires dynamic method matching.
 *
 * @author Rod Johnson
 */
class InterceptorAndDynamicMethodMatcher {
	
	final MethodMatcher methodMatcher;
	
	final MethodInterceptor interceptor;
	
	public InterceptorAndDynamicMethodMatcher(MethodInterceptor interceptor, MethodMatcher methodMatcher) {
		this.interceptor = interceptor;
		this.methodMatcher = methodMatcher;
	}

}
