package org.springframework.aop.support;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

/**
 * Convenient superclass for Advisors that are also dynamic pointcuts.
 * @author Rod Johnson
 */
public abstract class DynamicMethodMatcherPointcutAdvisor extends DynamicMethodMatcher
    implements PointcutAdvisor, Pointcut {

	private Object advice;
	
	protected DynamicMethodMatcherPointcutAdvisor() {
	}

	protected DynamicMethodMatcherPointcutAdvisor(Object advice) {
		this.advice = advice;
	}
	
	public void setAdvice(Object advice) {
		this.advice = advice;
	}
	
	public Object getAdvice() {
		return advice;
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
