/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import org.springframework.aop.Pointcut;
import org.springframework.aop.ThrowsAdvisor;

/**
 * Default ThrowsAdvisor implementation, holding a pointcut and 
 * throws advice. Note that a throws advice is an object.
 * @see org.springframework.aop.support.ThrowsAdviceInterceptor
 * @author Rod Johnson
 * @version $Id: DefaultThrowsAdvisor.java,v 1.2 2003-12-09 14:02:32 johnsonr Exp $
 */
public class DefaultThrowsAdvisor extends AbstractPointcutAdvisor implements ThrowsAdvisor {
	
	private final Object throwsAdvice;	

	/**
	 * @param throwsAdvice
	 * @param pointcut
	 */
	public DefaultThrowsAdvisor(Pointcut pointcut, Object throwsAdvice) {
		super(pointcut);
		this.throwsAdvice = throwsAdvice;
	}
	
	/**
	 * @see org.springframework.aop.ThrowsAdvisor#getThrowsAdvice()
	 */
	public Object getThrowsAdvice() {
		return throwsAdvice;
	}

}
