/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;


/**
 * @author Dmitriy Kopylenko
 * @version $Id: SimpleBeforeAdviceInterceptor.java,v 1.1 2004-02-27 14:28:16 dkopylenko Exp $
 */
final class SimpleBeforeAdviceInterceptor implements MethodInterceptor {
	
	private SimpleBeforeAdvice advice;
	
	public SimpleBeforeAdviceInterceptor(SimpleBeforeAdvice advice) {
		this.advice = advice;
	}

	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation mi) throws Throwable {
		advice.before();
		return mi.proceed();
	}
	
}