/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import org.springframework.aop.BeforeAdvice;
import org.springframework.aop.BeforeAdvisor;
import org.springframework.aop.Pointcut;

/**
 * 
 * @author Rod Johnson
 * @version $Id: DefaultBeforeAdvisor.java,v 1.1 2003-12-07 14:30:46 johnsonr Exp $
 */
public class DefaultBeforeAdvisor implements BeforeAdvisor {
	
	private final Pointcut pointcut;
	
	private final BeforeAdvice advice;
	
	public DefaultBeforeAdvisor(Pointcut pointcut, BeforeAdvice advice) {
		this.pointcut = pointcut;
		this.advice = advice;
	}

	/**
	 * @see org.springframework.aop.BeforeAdvisor#getBeforeAdvice()
	 */
	public BeforeAdvice getBeforeAdvice() {
		return advice;
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
