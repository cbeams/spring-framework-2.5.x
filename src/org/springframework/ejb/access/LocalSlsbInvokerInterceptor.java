/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ejb.access;

import java.lang.reflect.InvocationTargetException;

import javax.ejb.EJBLocalObject;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Interceptor that invokes a local SLSB, after caching
 * the home object. A local EJB home can never go stale.
 * @author Rod Johnson
 * @version $Id: LocalSlsbInvokerInterceptor.java,v 1.2 2003-11-21 11:33:40 johnsonr Exp $
 */
public class LocalSlsbInvokerInterceptor extends AbstractSlsbInvokerInterceptor {

	protected EJBLocalObject newSessionBeanInstance() {
		if (logger.isDebugEnabled())
			logger.debug("Trying to create EJB");

		EJBLocalObject session = (EJBLocalObject) getHomeBeanWrapper().invoke(CREATE_METHOD, null);

		if (logger.isDebugEnabled())
			logger.debug("EJB created OK [" + session + "]");
		return session;
	}

	/**
	 * This is the last invoker in the chain
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		EJBLocalObject ejb = newSessionBeanInstance();
		try {
			return invocation.getMethod().invoke(ejb, invocation.getArguments());
		}
		catch (InvocationTargetException ex) {
			Throwable targetException = ex.getTargetException();
			logger.info("Local EJB method [" + invocation.getMethod() + "] threw exception: " + targetException.getMessage(), targetException);
			throw targetException;
		}
		catch (Throwable t) {
			throw new AspectException("Failed to invoke local EJB", t);
		}
	}

}
