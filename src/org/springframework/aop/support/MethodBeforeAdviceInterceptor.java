/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.MethodBeforeAdvice;


/**
 * Interceptor to wrap a MethodBeforeAdvice. In future we may also offer a more efficient alternative
 * solution in cases where there is no interception advice and therefore no need to
 * create a MethodInvocation object.
 * <br>Used internally by the AOP framework: application developers should not need
 * to use this class directly.
 * @author Rod Johnson
 * @version $Id: MethodBeforeAdviceInterceptor.java,v 1.1 2003-12-05 13:05:14 johnsonr Exp $
 */
public class MethodBeforeAdviceInterceptor implements MethodInterceptor {
	
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