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

package org.springframework.aop.aspectj;

import java.io.Serializable;

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Interceptor that exposes an AspectJ JoinPoint.
 * Only necessary for using AspectJ advice.
 *
 * <p>If used, this interceptor must be the second in the interceptor chain,
 * after the ExposeInvocationInterceptor, which is also required for working
 * with AspectJ advice.
 *
 * @author Rod Johnson
 * @since 2.0
 */
public class ExposeJoinPointInterceptor implements MethodInterceptor, Serializable {
	
	/** Singleton instance of this class */
	public static final ExposeJoinPointInterceptor INSTANCE = new ExposeJoinPointInterceptor();

	private static ThreadLocal joinpointHolder = new ThreadLocal();


	/**
	 * Return the AOP Alliance MethodInvocation object associated with the current
	 * invocation. 
	 * @return the invocation object associated with the current invocation
	 * @throws AspectException if there is no AOP invocation
	 * in progress, or if the ExposeInvocationInterceptor was not
	 * added to this interceptor chain.
	 */
	public static JoinPoint currentJoinPoint() throws AspectException {
		// TODO could expose separately
		return currentProceedingJoinPoint();
	}
	
	public static ProceedingJoinPoint currentProceedingJoinPoint() throws AspectException {
		ProceedingJoinPoint jp = (ProceedingJoinPoint) joinpointHolder.get();
		if (jp == null)
			throw new AspectException(
					"No AspectJ JoinPoint found: check that an AOP invocation is in progress, " +
					"and that the ExposeJoinPointInterceptor is in the interceptor chain");
		return jp;
	}


	/**
	 * Ensure that only the canonical instance can be created.
	 */
	private ExposeJoinPointInterceptor() {
	}

	public Object invoke(MethodInvocation mi) throws Throwable {
		Object old = joinpointHolder.get();
		// TODO make lazy
		joinpointHolder.set(new MethodInvocationProceedingJoinPoint(mi));
		try {
			return mi.proceed();
		}
		finally {
			joinpointHolder.set(old);
		}
	}
	
	/**
	 * Required to support serialization. Replaces with canonical instance
	 * on deserialization, protecting Singleton pattern.
	 * Alternative to overriding the <code>equals</code> method.
	 */
	private Object readResolve() {
		return INSTANCE;
	}

}
