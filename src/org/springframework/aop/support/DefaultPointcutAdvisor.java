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
 * @version $Id: DefaultPointcutAdvisor.java,v 1.3 2004-02-27 16:40:12 jhoeller Exp $
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

	public void setAdvice(Object object) {
		advice = object;
	}

	public Object getAdvice() {
		return advice;
	}

	public Pointcut getPointcut() {
		return pointcut;
	}

	public boolean isPerInstance() {
		throw new UnsupportedOperationException("perInstance property of Advisor is not yet supported in Spring");
	}

	public boolean equals(Object o) {
		if (!(o instanceof DefaultPointcutAdvisor)) {
			return false;
		}
		DefaultPointcutAdvisor other = (DefaultPointcutAdvisor) o;
		return other.advice.equals(this.advice) && other.pointcut.equals(this.pointcut);
	}
	
	public String toString() {
		return "DefaultPointcutAdvisor: pointcut=[" + pointcut + "] advice=[" + advice + "]";
	}

}
