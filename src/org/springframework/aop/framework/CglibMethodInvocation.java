/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.MethodProxy;

/**
 * Invocation for CGLIB that invokes the target using the
 * CGLIB MethodProxy.
 * @author Rod Johnson
 * @version $Id: CglibMethodInvocation.java,v 1.2 2003-12-03 11:32:32 johnsonr Exp $
 */
final class CglibMethodInvocation extends ReflectiveMethodInvocation {
	
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
	public CglibMethodInvocation(Object proxy, Object target, Class targetInterface, Method m, Object[] arguments, Class targetClass,
			List interceptorsAndDynamicMethodMatchers,
			MethodProxy methodProxy) {
		super(proxy, target, targetInterface, m, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
		this.methodProxy = methodProxy;
	}
	
	/**
	 * Gives a marginal performance improvement versus using reflection to invoke the target.
	 * @see org.springframework.aop.framework.ReflectiveMethodInvocation#invokeJoinpoint()
	 */
	protected Object invokeJoinpoint() throws Throwable {
		return methodProxy.invoke(target, arguments);
	}

}
