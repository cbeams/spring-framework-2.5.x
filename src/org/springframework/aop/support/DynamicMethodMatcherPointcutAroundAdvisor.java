package org.springframework.aop.support;

import org.aopalliance.intercept.Interceptor;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

/**
 * Convenient superclass for Advisors that are also dynamic pointcuts.
 * @author Rod Johnson
 */
public abstract class DynamicMethodMatcherPointcutAroundAdvisor extends DynamicMethodMatcher
    implements InterceptionAroundAdvisor, Pointcut {

	private Interceptor interceptor;
	
	protected DynamicMethodMatcherPointcutAroundAdvisor() {
	}

	protected DynamicMethodMatcherPointcutAroundAdvisor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public void setInterceptor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public Interceptor getInterceptor() {
		return interceptor;
	}

	public boolean isPerInstance() {
		throw new UnsupportedOperationException("perInstance property of Advisor is not yet supported in Spring");
	}

	public final Pointcut getPointcut() {
		return this;
	}

	public ClassFilter getClassFilter() {
		return ClassFilter.TRUE;
	}

	public final MethodMatcher getMethodMatcher() {
		return this;
	}

}
