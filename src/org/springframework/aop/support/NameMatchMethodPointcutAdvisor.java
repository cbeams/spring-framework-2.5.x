/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.support;

import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

/**
 * Convenient class for name-match method pointcuts that hold an Interceptor,
 * making them an Advisor.
 * @author Juergen Hoeller
 * @version $Id: NameMatchMethodPointcutAdvisor.java,v 1.2 2004-02-27 16:40:12 jhoeller Exp $
 */
public class NameMatchMethodPointcutAdvisor extends NameMatchMethodPointcut implements PointcutAdvisor {

	private Object advice;

	public NameMatchMethodPointcutAdvisor() {
	}

	public NameMatchMethodPointcutAdvisor(Object advice) {
		this.advice = advice;
	}

	public void setAdvice(Object object) {
		advice = object;
	}

	public Object getAdvice() {
		return advice;
	}

	public Pointcut getPointcut() {
		return this;
	}

	public boolean isPerInstance() {
		throw new UnsupportedOperationException("perInstance property of Advisor is not yet supported in Spring");
	}

}
