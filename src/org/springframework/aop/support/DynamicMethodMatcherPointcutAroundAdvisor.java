
package org.springframework.aop.support;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;



public abstract class DynamicMethodMatcherPointcutAroundAdvisor extends DynamicMethodMatcher implements InterceptionAroundAdvisor, Pointcut {

	private boolean isPerInstance;

	private Interceptor interceptor;
	
	public DynamicMethodMatcherPointcutAroundAdvisor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	protected DynamicMethodMatcherPointcutAroundAdvisor() {
	}
	
	public void setInterceptor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public void setIsPerInstance(boolean isPerInstance) {
		this.isPerInstance = isPerInstance;
	}

	/**
	 * @see org.springframework.aop.framework.InterceptionAdvice#getInterceptor()
	 */
	public Interceptor getInterceptor() {
		return interceptor;
	}

	/**
	 * @see org.springframework.aop.framework.Advice#getPointcut()
	 */
	public final Pointcut getPointcut() {
		return this;
	}

	/**
	 * @see org.springframework.aop.Pointcut#getClassFilter()
	 */
	public ClassFilter getClassFilter() {
		return ClassFilter.TRUE;
	}

	/**
	 * @see org.springframework.aop.Pointcut#getMethodMatcher()
	 */
	public final MethodMatcher getMethodMatcher() {
		return this;
	}
	

	/**
	 * @see org.springframework.aop.Advice#isPerInstance()
	 */
	public boolean isPerInstance() {
		return this.isPerInstance;
	}

}
