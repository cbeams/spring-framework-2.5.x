/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ejb.access;

import java.lang.reflect.InvocationTargetException;

import javax.ejb.EJBObject;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Basic remote invoker for EJBs.
 * "Creates" a new EJB instance for each invocation.
 * @author Rod Johnson
 * @version $Id: SimpleRemoteSlsbInvokerInterceptor.java,v 1.2 2003-11-21 11:33:40 johnsonr Exp $
 */
public class SimpleRemoteSlsbInvokerInterceptor extends AbstractRemoteSlsbInvokerInterceptor {
	
	/**
	 * JavaBean constructor
	 */
	public SimpleRemoteSlsbInvokerInterceptor() {		
	}
	
	/**
	 * Convenient constructor for programmatic use.
	 * @param jndiName
	 * @param inContainer
	 * @throws org.aopalliance.intercept.AspectException
	 */
	public SimpleRemoteSlsbInvokerInterceptor(String jndiName, boolean inContainer) throws AspectException {
		setJndiName(jndiName);
		setInContainer(inContainer);
		try {
			afterPropertiesSet();
		}
		catch (Exception ex) {
			throw new AspectException("Failed to create EJB invoker interceptor", ex);
		}
	}
	
	/**
	 * This is the last invoker in the chain
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		EJBObject ejb = newSessionBeanInstance();
		try {
			return invocation.getMethod().invoke(ejb, invocation.getArguments());
		}
		catch (InvocationTargetException ex) {
			Throwable targetException = ex.getTargetException();
			logger.info("Remote EJB method [" + invocation.getMethod() + "] threw exception: " + targetException.getMessage(), targetException);
			throw targetException;
		}
		catch (Throwable t) {
			throw new AspectException("Failed to invoke remote EJB", t);
		}
	}

}
