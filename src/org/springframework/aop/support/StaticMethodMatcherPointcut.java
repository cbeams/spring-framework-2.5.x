
package org.springframework.aop.support;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;



public abstract class StaticMethodMatcherPointcut extends StaticMethodMatcher implements Pointcut {

	/**
	 * @see org.springframework.aop.pointcut.Pointcut#getClassFilter()
	 */
	public ClassFilter getClassFilter() {
		return ClassFilter.TRUE;
	}

	/**
	 * @see org.springframework.aop.pointcut.Pointcut#getMethodMatcher()
	 */
	public final MethodMatcher getMethodMatcher() {
		return this;
	}

}
