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
 * @version $Id: DefaultBeforeAdvisor.java,v 1.3 2003-12-09 14:02:31 johnsonr Exp $
 */
public class DefaultBeforeAdvisor extends AbstractPointcutAdvisor implements BeforeAdvisor {
	
	private final BeforeAdvice advice;
	
	public DefaultBeforeAdvisor(Pointcut pointcut, BeforeAdvice advice) {
		super(pointcut);
		this.advice = advice;
	}
	
	public DefaultBeforeAdvisor(BeforeAdvice advice) {
		this(Pointcut.TRUE, advice);
	}

	/**
	 * @see org.springframework.aop.BeforeAdvisor#getBeforeAdvice()
	 */
	public BeforeAdvice getBeforeAdvice() {
		return advice;
	}

}
