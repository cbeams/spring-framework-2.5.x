/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.MethodAfterReturningAdvice;


/**
 * Interceptor to wrap a MethodAfterReturningAdvice. In future we may also offer a more efficient alternative
 * solution in cases where there is no interception advice and therefore no need to
 * create a MethodInvocation object.
 * <br>Used internally by the AOP framework: application developers should not need
 * to use this class directly.
 * @author Rod Johnson
 * @version $Id: AfterReturningAdviceInterceptor.java,v 1.1 2004-01-05 18:47:00 johnsonr Exp $
 */
final class AfterReturningAdviceInterceptor implements MethodInterceptor {
	
	private MethodAfterReturningAdvice advice;
	
	public AfterReturningAdviceInterceptor(MethodAfterReturningAdvice advice) {
		this.advice = advice;
	}

	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation mi) throws Throwable {
		Object retval = mi.proceed();
		advice.afterReturning(retval, mi.getMethod(), mi.getArguments(), mi.getThis() );
		return retval;
	}
	
}