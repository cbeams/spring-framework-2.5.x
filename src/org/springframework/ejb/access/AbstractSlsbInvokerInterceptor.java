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
import java.lang.reflect.Method;

import javax.naming.NamingException;

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.beans.FatalBeanException;
import org.springframework.jndi.AbstractJndiLocator;

/**
 * Superclass for AOP interceptors invoking remote or local Stateless Session Beans.
 *
 * <p>Such an interceptor must be the last interceptor in the advice chain.
 * In this case, there is no target object.
 *
 * @author Rod Johnson
 * @version $Id: AbstractSlsbInvokerInterceptor.java,v 1.11 2004-05-18 07:54:00 jhoeller Exp $
 */
public abstract class AbstractSlsbInvokerInterceptor extends AbstractJndiLocator
		implements MethodInterceptor {

	private boolean cacheHome = true;

	/**
	 * The EJB's home interface.
	 * The type must be Object as it could be either EJBHome or EJBLocalHome.
	 */
	private Object cachedHome;

	/**
	 * The no-arg create() method required on EJB homes,
	 * but not part of EJBLocalHome. We cache this in the located() method.
	 */
	private Method createMethod;

	/**
	 * Set whether to cache the EJB home object. Default is true.
	 * <p>Can be turned off to allow for hot redeploy of the target EJB
	 * respectively restart of the EJB container.
	 */
	public void setCacheHome(boolean cacheHome) {
		this.cacheHome = cacheHome;
	}

	/**
 	 * Implementation of AbstractJndiLocator's callback, to cache the home wrapper.
	 * Invokes afterLocated() after execution.
	 * @see #afterLocated
	 */
	protected void located(Object jndiObject) {
		// cache the home object
		this.cachedHome = jndiObject;
		try {
			// cache the EJB create() method that must be declared on the home interface
			this.createMethod = this.cachedHome.getClass().getMethod("create", null);
		}
		catch (NoSuchMethodException ex) {
			throw new FatalBeanException("Cannot create EJB proxy: EJB home [" + this.cachedHome +
																	 "] has no no-arg create() method");
		}
		
		// invoke any subclass initialization behavior
		afterLocated();
	}

	/**
	 * Initialization hook after the AbstractJndiLocator's located callback.
	 * This implementation does nothing.
	 * @see #located
	 */
	protected void afterLocated() {
	}

	/**
	 * Return the cached home object.
	 */
	protected Object getCachedEjbHome() {
		return cachedHome;
	}

	/**
	 * Invoke the create() method on the cached EJB home.
	 * @return a new EJBObject or EJBLocalObject
	 */
	protected Object create() throws NamingException, InvocationTargetException {
		if (!this.cacheHome) {
			lookup();
		}
		try {
			return this.createMethod.invoke(this.cachedHome, null);
		}
		catch (IllegalArgumentException ex) {
			// can't happen
			throw new AspectException("Inconsistent state: could not call ejbCreate() method without arguments", ex);
		}
		catch (IllegalAccessException ex) {
			throw new AspectException("Could not access ejbCreate() method", ex);
		}
	}

}
