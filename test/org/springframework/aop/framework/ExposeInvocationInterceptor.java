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

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Interceptor that exposes the current MethodInvocation.
 * @author Rod Johnson
 * @version $Id: ExposeInvocationInterceptor.java,v 1.3 2004-03-19 21:35:47 johnsonr Exp $
 */
public class ExposeInvocationInterceptor implements MethodInterceptor {
	
	private static ThreadLocal invocation = new ThreadLocal();
	
	public static MethodInvocation currentInvocation() {
		MethodInvocation mi = (MethodInvocation) invocation.get();
		if (mi == null)
			throw new AspectException("No invocation set");
		return mi;
	}
	
	public static ExposeInvocationInterceptor INSTANCE = new ExposeInvocationInterceptor();
	
	private ExposeInvocationInterceptor() {
		
	}

	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation mi) throws Throwable {
		Object old = invocation.get();
		invocation.set(mi);
		try {
			return mi.proceed();
		}
		finally {
			invocation.set(old);
		}
	}

}
