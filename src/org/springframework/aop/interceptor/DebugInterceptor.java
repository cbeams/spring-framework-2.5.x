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

package org.springframework.aop.interceptor;

import java.io.Serializable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * AOP Alliance MethodInterceptor that can be introduced in a chain to display
 * verbose information about intercepted invocations to the console.
 * @author Rod Johnson
 */
public class DebugInterceptor implements MethodInterceptor, Serializable {
	
	private int count;

	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(MethodInvocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		++count;
		System.out.println("Debug interceptor: count=" + count +
			" invocation=[" + invocation + "]");
		Object rval = invocation.proceed();
		System.out.println("Debug interceptor: next returned");
		return rval;
	}
	
	/**
	 * Return the number of times this interceptor has been invoked
	 * @return the number of times this interceptor has been invoked
	 */
	public int getCount() {
		return this.count;
	}

}
