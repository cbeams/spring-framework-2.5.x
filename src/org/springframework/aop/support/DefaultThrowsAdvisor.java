/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import org.springframework.aop.Pointcut;
import org.springframework.aop.ThrowsAdvisor;

/**
 * 
 * @author Rod Johnson
 * @version $Id: DefaultThrowsAdvisor.java,v 1.1 2003-12-07 14:30:46 johnsonr Exp $
 */
public class DefaultThrowsAdvisor implements ThrowsAdvisor {
	
	private final Object throwsAdvice;
	
	private final Pointcut pointcut;
	

	/**
	 * @param throwsAdvice
	 * @param pointcut
	 */
	public DefaultThrowsAdvisor(Pointcut pointcut, Object throwsAdvice) {
		this.throwsAdvice = throwsAdvice;
		this.pointcut = pointcut;
	}
	/**
	 * @see org.springframework.aop.ThrowsAdvisor#getThrowsAdvice()
	 */
	public Object getThrowsAdvice() {
		return throwsAdvice;
	}

	/**
	 * @see org.springframework.aop.Advisor#isPerInstance()
	 */
	public boolean isPerInstance() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.springframework.aop.PointcutAdvisor#getPointcut()
	 */
	public Pointcut getPointcut() {
		return pointcut;
	}

}
