/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.MethodMatcher;

/**
 * Internal framework class.
 * This class is required because if we put an Interceptor that implements InterceptionAdvice
 * in the interceptor list passed to MethodInvocationImpl, it may be mistaken for an
 * advice that requires dynamic method matching.
 * @author Rod Johnson
 * @see 
 * @version $Id: InterceptorAndDynamicMethodMatcher.java,v 1.2 2003-11-13 00:55:52 dkopylenko Exp $
 */
class InterceptorAndDynamicMethodMatcher {
	
	final MethodMatcher methodMatcher;
	
	final MethodInterceptor interceptor;
	
	public InterceptorAndDynamicMethodMatcher(MethodInterceptor interceptor, MethodMatcher methodMatcher) {
		this.interceptor = interceptor;
		this.methodMatcher = methodMatcher;
	}

}
