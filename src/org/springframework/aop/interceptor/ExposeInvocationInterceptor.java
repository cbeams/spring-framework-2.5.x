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

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Interceptor that exposes the current MethodInvocation.
 * We occasionally need to do this--for example, when a pointcut
 * or target object needs to know the Invocation context.
 * <br>Don't use this interceptor unless this is really necessary.
 * Target objects should not normally know about Spring AOP,
 * as this creates a dependency on Spring. Target objects
 * should be plain POJOs as far as possible.
 * <br>If used, this interceptor will normally be the first
 * in the interceptor chain.
 * @author Rod Johnson
 */
public class ExposeInvocationInterceptor implements MethodInterceptor, Serializable {
	
	private static ThreadLocal invocation = new ThreadLocal();
	
	/**
	 * Return the AOP Alliance MethodInvocation object associated with the current
	 * invocation. 
	 * @return the invocation object associated with the current invocation
	 * @throws AspectException if there is no AOP invocation
	 * in progress, or if the ExposeInvocationInterceptor was not
	 * added to this interceptor chain.
	 */
	public static MethodInvocation currentInvocation() throws AspectException {
		MethodInvocation mi = (MethodInvocation) invocation.get();
		if (mi == null)
			throw new AspectException("No MethodInvocation found: " +
					"Check that an AOP invocation is in progress, and that the ExposeInvocationInterceptor is in the interceptor chain");
		return mi;
	}
	
	/**
	 * Singleton instance of this class
	 */
	public static final ExposeInvocationInterceptor INSTANCE = new ExposeInvocationInterceptor();
	
	/**
	 * Ensure that only the canonical instance can be used
	 */
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
	
	/**
	 * Required to support serialization.
	 * Replaces with canonical instance on deserialization,
	 * protecting Singleton pattern. 
	 * Alternative to overriding equals().
	 */
	private Object readResolve() throws ObjectStreamException {
		return INSTANCE;
	}

}
