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
 * <p>Invoker for a local Stateless Session Bean.
 * Caches the home object. A local EJB home can never go stale.
 * 
 * <p>See {@link org.springframework.jndi.AbstractJndiLocator} for info on
 * how to specify the JNDI location of the target EJB.
 *
 * <p>In a bean container, this class is normally best used as a singleton. However,
 * if that bean container pre-instantiates singletons (as do the XML ApplicationContext
 * variants) you may have a problem if the bean container is loaded before the EJB
 * container loads the target EJB. That is because the JNDI lookup will be performed in
 * the init method of this class and cached, but the EJB will not have been bound at the
 * target location yet. The solution is to not pre-instantiate this factory object, but
 * allow it to be created on first use. In the XML containers, this is controlled via
 * the "lazy-init" attribute.
 *
 * @author Rod Johnson
 * @version $Id: LocalSlsbInvokerInterceptor.java,v 1.7 2004-03-17 17:19:42 jhoeller Exp $
 */
public class LocalSlsbInvokerInterceptor extends AbstractSlsbInvokerInterceptor {

	protected EJBLocalObject newSessionBeanInstance() throws InvocationTargetException {
		if (logger.isDebugEnabled()) {
			logger.debug("Trying to create reference to local EJB");
		}

		// call superclass to invoke the EJB create method on the cached home
		EJBLocalObject session = (EJBLocalObject) create();

		if (logger.isDebugEnabled()) {
			logger.debug("Obtained reference to local EJB: " + session);
		}
		return session;
	}

	/**
	 * This is the last invoker in the chain: invoke the EJB.
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
		catch (Throwable ex) {
			throw new AspectException("Failed to invoke local EJB [" + getJndiName() + "]", ex);
		}
	}

}
