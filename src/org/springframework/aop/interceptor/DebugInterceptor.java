/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * AOP Alliance MethodInterceptor that can be introduced in a chain to display
 * verbose information about intercepted invocations to the console.
 * @author Rod Johnson
 * @version $Id: DebugInterceptor.java,v 1.2 2003-12-31 14:38:53 johnsonr Exp $
 */
public class DebugInterceptor implements MethodInterceptor {
	
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
