/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

/**
 * Convenient superclass for pointcut-driven advisors, implementing
 * the getPointcut() and isPerInstance() methods.
 * @author Rod Johnson
 * @version $Id: AbstractPointcutAdvisor.java,v 1.1 2003-12-09 14:02:31 johnsonr Exp $
 */
public abstract class AbstractPointcutAdvisor implements PointcutAdvisor {
	
	private Pointcut pointcut;
	
	protected AbstractPointcutAdvisor(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	/**
	 * @see org.springframework.aop.PointcutAdvisor#getPointcut()
	 */
	public Pointcut getPointcut() {
		return pointcut;
	}

	/**
	 * @see org.springframework.aop.Advisor#isPerInstance()
	 */
	public boolean isPerInstance() {
		throw new UnsupportedOperationException("perInstance property of Advisor is not yet supported in Spring");
	}

}
