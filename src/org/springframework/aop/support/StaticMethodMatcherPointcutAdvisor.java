/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

/**
 * Convenient superclass for Advisors that are also static
 * pointcuts.
 * @author Rod Johnson
 * @version $Id: StaticMethodMatcherPointcutAdvisor.java,v 1.1 2004-01-13 16:34:31 johnsonr Exp $
 */
public abstract class StaticMethodMatcherPointcutAdvisor extends StaticMethodMatcherPointcut implements PointcutAdvisor {

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
		return this;
	}

}
