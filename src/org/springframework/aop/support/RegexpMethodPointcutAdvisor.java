/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.support;

import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

/**
 * Convenient class for regexp method pointcuts that hold an Interceptor,
 * making them an Advisor.
 * @author Dmitriy Kopylenko
 * @version $Id: RegexpMethodPointcutAdvisor.java,v 1.1 2004-02-22 09:48:24 johnsonr Exp $
 */
public class RegexpMethodPointcutAdvisor extends RegexpMethodPointcut
    implements PointcutAdvisor {

	private Object advice;

	public RegexpMethodPointcutAdvisor() {
	}

	public RegexpMethodPointcutAdvisor(Object advice) {
		this.advice = advice;
	}
	
	public void setAdvice(Object advice) {
		this.advice = advice;
	}
	
	public Object getAdvice() {
		return this.advice;
	}

	public boolean isPerInstance() {
		throw new UnsupportedOperationException("perInstance property of Advisor is not yet supported in Spring");
	}

	public Pointcut getPointcut() {
		return this;
	}

}
