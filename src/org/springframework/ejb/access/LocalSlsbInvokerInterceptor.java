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
 * <p>See {@link org.springframework.jndi.JndiObjectLocator} for info on
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
 * @author Juergen Hoeller
 */
public class LocalSlsbInvokerInterceptor extends AbstractSlsbInvokerInterceptor {

	/**
	 * This implementation "creates" a new EJB instance for each invocation.
	 * Can be overridden for custom invocation strategies.
	 * <p>Alternatively, override getSessionBeanInstance and
	 * releaseSessionBeanInstance to change EJB instance creation,
	 * for example to hold a single shared EJB instance.
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		EJBLocalObject ejb = null;
		try {
			ejb = getSessionBeanInstance();
			return invocation.getMethod().invoke(ejb, invocation.getArguments());
		}
		catch (InvocationTargetException ex) {
			Throwable targetEx = ex.getTargetException();
			if (logger.isDebugEnabled()) {
				logger.debug("Method of local EJB [" + getJndiName() + "] threw exception", targetEx);
			}
			if (targetEx instanceof CreateException) {
				throw new AspectException("Could not create local EJB [" + getJndiName() + "]", targetEx);
			}
			else {
				throw targetEx;
			}
		}
		catch (NamingException ex) {
			throw new AspectException("Failed to locate local EJB [" + getJndiName() + "]", ex);
		}
		catch (IllegalAccessException ex) {
			throw new AspectException("Could not access method [" + invocation.getMethod().getName() +
			    "] of local EJB [" + getJndiName() + "]", ex);
		}
		finally {
			releaseSessionBeanInstance(ejb);
		}
	}

	/**
	 * Return an EJB instance to delegate the call to.
	 * Default implementation delegates to newSessionBeanInstance.
	 * @throws NamingException if thrown by JNDI
	 * @throws InvocationTargetException if thrown by the create method
	 * @see #newSessionBeanInstance
	 */
	protected EJBLocalObject getSessionBeanInstance() throws NamingException, InvocationTargetException {
		return newSessionBeanInstance();
	}

	/**
	 * Release the given EJB instance.
	 * Default implementation delegates to removeSessionBeanInstance.
	 * @param ejb the EJB instance to release
	 * @see #removeSessionBeanInstance
	 */
	protected void releaseSessionBeanInstance(EJBLocalObject ejb) {
		removeSessionBeanInstance(ejb);
	}

	/**
	 * Return a new instance of the stateless session bean.
	 * Can be overridden to change the algorithm.
	 * @throws NamingException if thrown by JNDI
	 * @throws InvocationTargetException if thrown by the create method
	 * @see #create
	 */
	protected EJBLocalObject newSessionBeanInstance() throws NamingException, InvocationTargetException {
		if (logger.isDebugEnabled()) {
			logger.debug("Trying to create reference to local EJB");
		}

		// call superclass to invoke the EJB create method on the cached home
		Object ejbInstance = create();
		if (!(ejbInstance instanceof EJBLocalObject)) {
			throw new AspectException("EJB instance [" + ejbInstance + "] is not a local SLSB");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Obtained reference to local EJB: " + ejbInstance);
		}
		return (EJBLocalObject) ejbInstance;
	}

	/**
	 * Remove the given EJB instance.
	 * @param ejb the EJB instance to remove
	 * @see javax.ejb.EJBLocalObject#remove
	 */
	protected void removeSessionBeanInstance(EJBLocalObject ejb) {
		try {
			ejb.remove();
		}
		catch (Throwable ex) {
			logger.warn("Could not invoke 'remove' on local EJB proxy", ex);
		}
	}

}
