/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.interceptor;

import java.lang.reflect.InvocationTargetException;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.ProxyInterceptor;
import org.springframework.aop.framework.MethodInvocationImpl;

/**
 * Abstract implementation of Interceptor interface that 
 * invokes a  target object using reflection.
 * Subclasses should always be the last interceptor in the chain.
 * This class does not invoke proceed() on the MethodInvocation. 
 * <br>
 * The target must be provided by subclasses: for example,
 * from on a simple object reference, obtained from a pool or
 * held in a ThreadLocal. 
 * @author Rod Johnson
 * @version $Id: AbstractReflectionInvokerInterceptor.java,v 1.3 2003-11-11 18:31:53 johnsonr Exp $
 */
public abstract class AbstractReflectionInvokerInterceptor implements MethodInterceptor, ProxyInterceptor {
	
	/** Commons logging logger */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Subclasses must implement this method to return the
	 * target to use during an invocation.
	 * @return the target object that will be invoked reflectively
	 * @see org.springframework.aop.framework.ProxyInterceptor#getTarget()
	 */
	public abstract Object getTarget();

	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(MethodInvocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// Set the target on the invocation
		if (invocation instanceof MethodInvocationImpl) {
			((MethodInvocationImpl) invocation).setTarget(getTarget());
		}
		
		// Use reflection to invoke the method
		try {
			Object rval = invocation.getMethod().invoke(getTarget(), invocation.getArguments());
			return rval;
		}
		catch (InvocationTargetException ex) {
			// Invoked method threw a checked exception. 
			// We must rethrow it. The client won't see the interceptor
			Throwable t = ex.getTargetException();
			throw t;
		}
		catch (IllegalAccessException ex) {
			throw new AspectException("Couldn't access method " + invocation.getMethod() + ", ", ex);
		}
	}

}
