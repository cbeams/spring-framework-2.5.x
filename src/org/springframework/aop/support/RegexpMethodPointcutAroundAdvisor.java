/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.support;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.Pointcut;

/**
 * Convenient class for regexp method pointcuts that hold an Interceptor, making them
 * an Advisor
 * @author Dmitriy Kopylenko
 * @version $Id: RegexpMethodPointcutAroundAdvisor.java,v 1.1 2003-11-20 00:49:59 dkopylenko Exp $
 */
public class RegexpMethodPointcutAroundAdvisor extends RegexpMethodPointcut implements InterceptionAroundAdvisor {

	private boolean isPerInstance;

	private Interceptor interceptor;

	public RegexpMethodPointcutAroundAdvisor() {
	}

	public RegexpMethodPointcutAroundAdvisor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public void setInterceptor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public void setIsPerInstance(boolean isPerInstance) {
		this.isPerInstance = isPerInstance;
	}

	/**
	 * @see org.springframework.aop.InterceptionAroundAdvisor#getInterceptor()
	 */
	public Interceptor getInterceptor() {
		return this.getInterceptor();
	}

	/**
	 * @see org.springframework.aop.PointcutAdvisor#getPointcut()
	 */
	public Pointcut getPointcut() {
		return this;
	}

	/**
	 * @see org.springframework.aop.Advisor#isPerInstance()
	 */
	public boolean isPerInstance() {
		return this.isPerInstance;
	}

}
