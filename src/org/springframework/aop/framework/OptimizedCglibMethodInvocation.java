/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.MethodProxy;

import org.aopalliance.intercept.AspectException;

/**
 * 
 * @author Rod Johnson
 * @version $Id: OptimizedCglibMethodInvocation.java,v 1.1 2003-12-01 18:28:24 johnsonr Exp $
 */
public class OptimizedCglibMethodInvocation extends ReflectiveMethodInvocation {
	
	private MethodProxy methodProxy;

	/**
	 * 
	 */
	public OptimizedCglibMethodInvocation() {
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
	public OptimizedCglibMethodInvocation(Object proxy, Object target, Class targetInterface, Method m, Object[] arguments, Class targetClass,
			List interceptorsAndDynamicMethodMatchers,
			MethodProxy methodProxy) {
		super(proxy, target, targetInterface, m, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
		this.methodProxy = methodProxy;
	}
	
	protected Object invokeJoinpoint() throws Throwable {
		 try {
			 Object rval = methodProxy.invokeSuper(target, arguments);
			 return rval;
		 }
		 catch (InvocationTargetException ex) {
			 // Invoked method threw a checked exception. 
			 // We must rethrow it. The client won't see the interceptor
			 Throwable t = ex.getTargetException();
			 throw t;
		 }
		 catch (IllegalArgumentException ex) {
			throw new AspectException("AOP configuration seems to be invalid: tried calling " + method + " on [" + target + "]: " +  ex);
		 }
		 catch (IllegalAccessException ex) {
			 throw new AspectException("Couldn't access method " + method, ex);
		 }
	}

}
