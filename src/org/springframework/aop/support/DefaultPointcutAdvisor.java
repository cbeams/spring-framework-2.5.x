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
 * @version $Id: DefaultPointcutAdvisor.java,v 1.1 2004-02-22 09:48:24 johnsonr Exp $
 */
public class DefaultPointcutAdvisor implements PointcutAdvisor {
	
	private Pointcut pointcut;
	
	private Object advice;
	
	public DefaultPointcutAdvisor() {
	}
	
	public DefaultPointcutAdvisor(Object advice) {
		this(Pointcut.TRUE, advice);
	}
	
	public DefaultPointcutAdvisor(Pointcut pointcut, Object advice) {
		this.pointcut = pointcut;
		this.advice = advice;
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

	/**
	 * @return
	 */
	public Object getAdvice() {
		return advice;
	}

	/**
	 * @param object
	 */
	public void setAdvice(Object object) {
		advice = object;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof DefaultPointcutAdvisor)) 
			return false;
		DefaultPointcutAdvisor other = (DefaultPointcutAdvisor) o;
		return other.advice.equals(this.advice) && 
			other.pointcut.equals(this.pointcut);
	}

}
