/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ejb.access;

import java.lang.reflect.InvocationTargetException;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalObject;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.MethodInvocationException;

/**
 * Interceptor that invokes a local Stateless Session Bean, after caching
 * the home object. A local EJB home can never go stale.
 * @author Rod Johnson
 * @version $Id: LocalSlsbInvokerInterceptor.java,v 1.5 2003-12-30 01:11:55 jhoeller Exp $
 */
public class LocalSlsbInvokerInterceptor extends AbstractSlsbInvokerInterceptor {

	protected EJBLocalObject newSessionBeanInstance() throws InvocationTargetException {
		if (logger.isDebugEnabled()) {
			logger.debug("Trying to create reference to remote EJB");
		}

		// Call superclass to invoke the EJB create method on the cached home
		EJBLocalObject session = (EJBLocalObject) create();

		if (logger.isDebugEnabled()) {
			logger.debug("Obtained reference to remote EJB: " + session);
		}
		return session;
	}

	/**
	 * This is the last invoker in the chain: 
	 * invoke the EJB.
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			EJBLocalObject ejb = newSessionBeanInstance();
			return invocation.getMethod().invoke(ejb, invocation.getArguments());
		}
		catch (InvocationTargetException ex) {
			Throwable targetException = ex.getTargetException();
			logger.info("Method of local EJB [" + getJndiName() + "] threw exception", targetException);
			if (targetException instanceof CreateException) {
				throw new MethodInvocationException(targetException, "create");
			}
			else {
				throw targetException;
			}
		}
		catch (Throwable t) {
			throw new AspectException("Failed to invoke local EJB [" + getJndiName() + "]", t);
		}
	}

}
