/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import org.springframework.aop.Pointcut;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.aop.ThrowsAdvisor;

/**
 * Default ThrowsAdvisor implementation, holding a pointcut and 
 * throws advice. Note that a throws advice is an object.
 * @see org.springframework.aop.support.ThrowsAdviceInterceptor
 * @author Rod Johnson
 * @version $Id: DefaultThrowsAdvisor.java,v 1.3 2003-12-11 09:18:57 johnsonr Exp $
 */
public class DefaultThrowsAdvisor extends AbstractPointcutAdvisor implements ThrowsAdvisor {
	
	private final ThrowsAdvice throwsAdvice;	

	/**
	 * @param throwsAdvice
	 * @param pointcut
	 */
	public DefaultThrowsAdvisor(Pointcut pointcut, ThrowsAdvice throwsAdvice) {
		super(pointcut);
		this.throwsAdvice = throwsAdvice;
	}
	
	/**
	 * Create an Advisor that always applies
	 * @param throwsAdvice wrapped advice
	 */
	public DefaultThrowsAdvisor(ThrowsAdvice throwsAdvice) {
		super(Pointcut.TRUE);
		this.throwsAdvice = throwsAdvice;
	}
	
	/**
	 * @see org.springframework.aop.ThrowsAdvisor#getThrowsAdvice()
	 */
	public ThrowsAdvice getThrowsAdvice() {
		return throwsAdvice;
	}

}
