/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.support;

import org.aopalliance.intercept.Interceptor;

import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.Pointcut;

/**
 * Convenient class for regexp method pointcuts that hold an Interceptor,
 * making them an Advisor.
 * @author Dmitriy Kopylenko
 * @version $Id: RegexpMethodPointcutAroundAdvisor.java,v 1.3 2004-02-11 17:18:17 jhoeller Exp $
 */
public class RegexpMethodPointcutAroundAdvisor extends RegexpMethodPointcut
    implements InterceptionAroundAdvisor {

	private Interceptor interceptor;

	public RegexpMethodPointcutAroundAdvisor() {
	}

	public RegexpMethodPointcutAroundAdvisor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public void setInterceptor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public Interceptor getInterceptor() {
		return this.interceptor;
	}

	public boolean isPerInstance() {
		throw new UnsupportedOperationException("perInstance property of Advisor is not yet supported in Spring");
	}

	public Pointcut getPointcut() {
		return this;
	}

}
