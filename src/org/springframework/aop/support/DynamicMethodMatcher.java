/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.springframework.aop.*;


/**
 * Convenient abstract superclas for static method matchers, which don't care
 * about arguments at runtime. 
 */
public abstract class DynamicMethodMatcher implements MethodMatcher {

	/**
	 * @see org.springframework.aop.pointcut.MethodMatcher#isRuntimeMethodMatcher()
	 */
	public final boolean isRuntime() {
		return true;
	}

	
	/**
	 * Can override to add preconditions for dynamic matching. This implementation
	 * always returns true.
	 * @see org.springframework.aop.MethodMatcher#matches(java.lang.reflect.Method, java.lang.Class)
	 */
	public boolean matches(Method m, Class targetClass) {
		return true;
	}

}
