/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ejb.access;

import java.lang.reflect.InvocationTargetException;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalObject;
import javax.naming.NamingException;

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInvocation;

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
 * @version $Id: LocalSlsbInvokerInterceptor.java,v 1.11 2004-05-18 07:54:00 jhoeller Exp $
 */
public class LocalSlsbInvokerInterceptor extends AbstractSlsbInvokerInterceptor {

	/**
	 * Return a new instance of the stateless session bean.
	 * Can be overridden to change the algorithm.
	 */
	protected EJBLocalObject newSessionBeanInstance() throws NamingException, InvocationTargetException {
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
		EJBLocalObject ejb = null;
		try {
			ejb = newSessionBeanInstance();
			return invocation.getMethod().invoke(ejb, invocation.getArguments());
		}
		catch (InvocationTargetException ex) {
			Throwable targetException = ex.getTargetException();
			logger.info("Method of local EJB [" + getJndiName() + "] threw exception", targetException);
			if (targetException instanceof CreateException) {
				throw new AspectException("Could not create local EJB [" + getJndiName() + "]", targetException);
			}
			else {
				throw targetException;
			}
		}
		catch (Throwable ex) {
			throw new AspectException("Failed to invoke local EJB [" + getJndiName() + "]", ex);
		}
		finally {
			if (ejb != null) {
				try {
					ejb.remove();
				}
				catch (Throwable ex) {
					logger.warn("Could not invoker 'remove' on Stateless Session Bean proxy", ex);
				}
			}
		}
	}

}
