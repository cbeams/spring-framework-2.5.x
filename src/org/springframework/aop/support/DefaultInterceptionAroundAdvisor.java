
package org.springframework.aop.support;

import org.aopalliance.intercept.Interceptor;

import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.Pointcut;

/**
 * Default InterceptionAdvice implementation.
 * @author Rod Johnson
 * @version $Id: DefaultInterceptionAroundAdvisor.java,v 1.3 2003-12-09 14:02:30 johnsonr Exp $
 */
public class DefaultInterceptionAroundAdvisor extends AbstractPointcutAdvisor implements InterceptionAroundAdvisor {
	
	private Interceptor interceptor;
	
	public DefaultInterceptionAroundAdvisor(Pointcut pointcut, Interceptor interceptor) {
		super(pointcut);
		setInterceptor(interceptor);
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


	public Interceptor getInterceptor() {
		return this.interceptor;
	}


	public String toString() {
		return getClass().getName() + ": interceptor=(" + interceptor + 
				"); Pointcut=(" + getPointcut() + ") ";//perInstance=" + this.isPerInstance;
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
