/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.MethodProxy;

/**
 * 
 * @author Rod Johnson
 * @version $Id: CglibMethodInvocation.java,v 1.1 2003-12-01 18:28:24 johnsonr Exp $
 */
public class CglibMethodInvocation extends ReflectiveMethodInvocation {
	
	private MethodProxy methodProxy;

	/**
	 * 
	 */
	public CglibMethodInvocation() {
	}
	
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
		return Cglib1AopProxy.invokeJoinpointUsingMethodProxy(target, method, arguments, methodProxy);
	}

}
