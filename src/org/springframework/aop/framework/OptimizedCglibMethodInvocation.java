/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.MethodProxy;

/**
 * Invokes superclass form of the method when interception is done.
 * Assumes that the target is the instance of the enhanced class,
 * and that there isn't a separate target.
 * @author Rod Johnson
 * @version $Id: OptimizedCglibMethodInvocation.java,v 1.3 2003-12-03 11:32:32 johnsonr Exp $
 */
final class OptimizedCglibMethodInvocation extends ReflectiveMethodInvocation {
	
	private MethodProxy methodProxy;

	
	/**
	 * @param proxy
	 * @param target
	 * @param targetInterface
	 * @param m
	 * @param arguments
	 * @param targetClass
	 * @param interceptorsAndDynamicMethodMatchers
	 */
	public OptimizedCglibMethodInvocation(Object proxy, Object target, Class targetInterface, Method m, Object[] arguments, Class targetClass,
			List interceptorsAndDynamicMethodMatchers,
			MethodProxy methodProxy) {
		super(proxy, target, targetInterface, m, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
		this.methodProxy = methodProxy;
	}
	
	protected Object invokeJoinpoint() throws Throwable {
		return methodProxy.invokeSuper(target, arguments);
	}

}
