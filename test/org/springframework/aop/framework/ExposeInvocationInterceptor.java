/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Interceptor that exposes the current MethodInvocation.
 * @author Rod Johnson
 * @version $Id: ExposeInvocationInterceptor.java,v 1.1 2003-12-11 09:01:26 johnsonr Exp $
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
