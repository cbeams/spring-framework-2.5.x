/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.interceptor;

import java.lang.reflect.InvocationTargetException;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.ProxyInterceptor;
import org.springframework.aop.framework.MethodInvocationImpl;


/**
 * Implementation of Interceptor interface that 
 * invokes a local target object using reflection.
 * This is a simple JavaBean that caches a local object.
 * This should always be the last interceptor in the chain.
 * It does not invoke proceed() on the MethodInvocation.
 * This class is final as it has a special purpose to the AOP
 * framework and cannot be modified.
 * <br>Note that this class used to extend AbstractReflectionInvokerInterceptor
 * but at the price of a little code duplication making it implement invoke()
 * itself simplifies stack traces and produces a slight performance improvement.
 * @author Rod Johnson
 * @version $Id: InvokerInterceptor.java,v 1.2 2003-11-28 13:55:17 johnsonr Exp $
 */
public final class InvokerInterceptor implements ProxyInterceptor, MethodInterceptor {

	/** Target cached and invoked using reflection */	
	private Object target;
	
	public InvokerInterceptor() {
	}
	
	public InvokerInterceptor(Object target) {
		this.target = target;
	}
	
	public void setTarget(Object target) {
		this.target = target;
	}
	
	public Object getTarget() {
		return this.target;
	}
	
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// Set the target on the invocation
		if (invocation instanceof MethodInvocationImpl) {
			((MethodInvocationImpl) invocation).setTarget(target);
		}
	
		// Use reflection to invoke the method
		try {
			Object rval = invocation.getMethod().invoke(target, invocation.getArguments());
			return rval;
		}
		catch (InvocationTargetException ex) {
			// Invoked method threw a checked exception. 
			// We must rethrow it. The client won't see the interceptor
			Throwable t = ex.getTargetException();
			throw t;
		}
		catch (IllegalAccessException ex) {
			throw new AspectException("InvokerInterceptor couldn't access method " + invocation.getMethod(), ex);
		}
	}
	
	/**
	 * Two invoker interceptors are equal if they have the same target or if the targets
	 * are equal.
	 */
	public boolean equals(Object other) {
		if (!(other instanceof InvokerInterceptor))
			return false;
		InvokerInterceptor otherII = (InvokerInterceptor) other;
		return otherII.target == this.target || otherII.target.equals(this.target);
	}

}
