/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Simple MethodInvocationFactory implementation that 
 * constructs a new MethodInvocationImpl on every call.
 * @author Rod Johnson
 * @version $Id: SimpleMethodInvocationFactory.java,v 1.2 2003-11-29 13:36:33 johnsonr Exp $
 */
public class SimpleMethodInvocationFactory implements MethodInvocationFactory {

	/**
	 * @see org.springframework.aop.framework.MethodInvocationFactory#getMethodInvocation(org.springframework.aop.framework.Advised, java.util.List, java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	public MethodInvocation getMethodInvocation(Object proxy, Method method, Class targetClass, Object target, Object[] args, List interceptorsAndDynamicInterceptionAdvice, AdvisedSupport advised) {
		return new MethodInvocationImpl(
			proxy,
			target,
			method.getDeclaringClass(),
			method,
			args,
			targetClass,
			interceptorsAndDynamicInterceptionAdvice);
	}
	
	public void release(MethodInvocation invocation) {
		// Not necessary to implement for this implementation
		//	TODO move into AOP Alliance
		//((MethodInvocationImpl) invocation).clear();
	}

}
