/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.support;

import org.aopalliance.intercept.Interceptor;

import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.Pointcut;

/**
 * Convenient class for name-match method pointcuts that hold an Interceptor,
 * making them an Advisor.
 * @author Juergen Hoeller
 * @version $Id: NameMatchMethodPointcutAroundAdvisor.java,v 1.1 2004-02-11 17:13:09 jhoeller Exp $
 */
public class NameMatchMethodPointcutAroundAdvisor extends NameMatchMethodPointcut
    implements InterceptionAroundAdvisor {

	private Interceptor interceptor;

	public NameMatchMethodPointcutAroundAdvisor() {
	}

	public NameMatchMethodPointcutAroundAdvisor(Interceptor interceptor) {
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
