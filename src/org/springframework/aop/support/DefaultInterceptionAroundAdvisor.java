
package org.springframework.aop.support;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.Pointcut;


/**
 * Default InterceptionAdvice implementation that
 * is a JavaBean taking Pointcut and Interceptor.
 * 
 * @author Rod Johnson
 * @version $Id: DefaultInterceptionAroundAdvisor.java,v 1.1 2003-11-16 12:54:58 johnsonr Exp $
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
	 * If no pointcut is supplied, the advice will always fire
	 * @param interceptor
	 */
	public DefaultInterceptionAroundAdvisor(Interceptor interceptor) {
		this(Pointcut.TRUE, interceptor);
	}

	/**
	 * @param interceptor
	 */
	public void setInterceptor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}

	/**
	 * @param pointcut
	 */
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	/**
	 * @see org.springframework.aop.framework.Advice#getInterceptor()
	 */
	public Interceptor getInterceptor() {
		return this.interceptor;
	}

	/**
	 * @see org.springframework.aop.framework.Advice#getPointcut()
	 */
	public Pointcut getPointcut() {
		return this.pointcut;
	}
	
	/**
	 * @see org.springframework.aop.Advice#isPerInstance()
	 */
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
