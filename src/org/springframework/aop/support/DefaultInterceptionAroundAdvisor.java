
package org.springframework.aop.support;

import org.aopalliance.intercept.Interceptor;

import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.Pointcut;

/**
 * Default InterceptionAdvice implementation that
 * is a JavaBean taking Pointcut and Interceptor.
 * @author Rod Johnson
 * @version $Id: DefaultInterceptionAroundAdvisor.java,v 1.2 2003-11-21 22:45:29 jhoeller Exp $
 */
public class DefaultInterceptionAroundAdvisor implements InterceptionAroundAdvisor {
	
	private Interceptor interceptor;

	private Pointcut pointcut;
	
	private boolean isPerInstance = false;
	
	public DefaultInterceptionAroundAdvisor() {
	}
	
	public DefaultInterceptionAroundAdvisor(Pointcut pointcut, Interceptor interceptor) {
		setPointcut(pointcut);
		setInterceptor(interceptor);
	}
	
	public void setPerInstance(boolean perInstance) {
		this.isPerInstance = perInstance;
	}
	
	/**
	 * If no pointcut is supplied, the advice will always fire.
	 */
	public DefaultInterceptionAroundAdvisor(Interceptor interceptor) {
		this(Pointcut.TRUE, interceptor);
	}

	public void setInterceptor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}

	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	public Interceptor getInterceptor() {
		return this.interceptor;
	}

	public Pointcut getPointcut() {
		return this.pointcut;
	}
	
	public boolean isPerInstance() {
		return this.isPerInstance;
	}

	public String toString() {
		return getClass().getName() + ": interceptor=(" + interceptor + 
				"); Pointcut=(" + pointcut + "); perInstance=" + this.isPerInstance;
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof InterceptionAroundAdvisor))
			return false;
		InterceptionAroundAdvisor that = (InterceptionAroundAdvisor) other;
		if (!that.getInterceptor().equals(this.interceptor))
			return false;
		return Pointcuts.equals(this.getPointcut(), that.getPointcut());
	}


}
