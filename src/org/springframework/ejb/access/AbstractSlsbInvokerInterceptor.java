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

import org.springframework.jndi.JndiObjectLocator;

/**
 * Superclass for AOP interceptors invoking local or remote Stateless Session Beans.
 *
 * <p>Such an interceptor must be the last interceptor in the advice chain.
 * In this case, there is no direct target object: The call is handled in a
 * special way, getting executed on an EJB instance retrieved via an EJB home.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class AbstractSlsbInvokerInterceptor extends JndiObjectLocator
		implements MethodInterceptor {

	private boolean lookupHomeOnStartup = true;

	private boolean cacheHome = true;

	/**
	 * The EJB's home object, potentially cached.
	 * The type must be Object as it could be either EJBHome or EJBLocalHome.
	 */
	private Object cachedHome;

	/**
	 * The no-arg create() method required on EJB homes, potentially cached.
	 */
	private Method createMethod;


	/**
	 * Set whether to look up the EJB home object on startup.
	 * Default is true.
	 * <p>Can be turned off to allow for late start of the EJB server.
	 * In this case, the EJB home object will be fetched on first access.
	 * @see #setCacheHome
	 */
	public void setLookupHomeOnStartup(boolean lookupHomeOnStartup) {
		this.lookupHomeOnStartup = lookupHomeOnStartup;
	}

	/**
	 * Set whether to cache the EJB home object once it has been located.
	 * Default is true.
	 * <p>Can be turned off to allow for hot restart of the EJB server.
	 * In this case, the EJB home object will be fetched for each invocation.
	 * @see #setLookupHomeOnStartup
	 */
	public void setCacheHome(boolean cacheHome) {
		this.cacheHome = cacheHome;
	}


	/**
	 * Fetches EJB home on startup, if necessary.
	 * @see #setLookupHomeOnStartup
	 * @see #refreshHome
	 */
	public void afterPropertiesSet() throws NamingException {
		super.afterPropertiesSet();
		if (this.lookupHomeOnStartup) {
			// look up EJB home and create method
			refreshHome();
		}
	}

	/**
	 * Refresh the cached home object, if applicable.
	 * Also caches the create method on the home object.
	 * @throws NamingException if thrown by the JNDI lookup
	 * @see #lookup
	 * @see #getCreateMethod
	 */
	protected void refreshHome() throws NamingException {
		Object home = lookup();
		this.createMethod = getCreateMethod(home);
		if (this.cacheHome) {
			this.cachedHome = home;
		}
	}

	/**
	 * Determine the create method of the given EJB home object.
	 * @param home the EJB home object
	 * @return the create method
	 * @throws AspectException if the method couldn't be retrieved
	 */
	protected Method getCreateMethod(Object home) throws AspectException {
		try {
			// cache the EJB create() method that must be declared on the home interface
			return home.getClass().getMethod("create", null);
		}
		catch (NoSuchMethodException ex) {
			throw new AspectException(
					"EJB home [" + this.cachedHome + "] has no no-arg create() method");
		}
	}

	/**
	 * Return the EJB home object to use. Called for each invocation.
	 * <p>Default implementation returns the home created on initialization,
	 * if any; else, it invokes lookup to get a new proxy for each invocation.
	 * <p>Can be overridden in subclasses, for example to cache a home for
	 * a given amount of time before recreating it, or to test the home
	 * whether it is still alive.
	 * @return the EJB home object to use for an invocation
	 * @throws NamingException if proxy creation failed
	 * @see #lookup
	 * @see #getCreateMethod
	 */
	protected Object getHome() throws NamingException {
		if (!this.cacheHome || (this.lookupHomeOnStartup && !isHomeRefreshable())) {
			return (this.cachedHome != null ? this.cachedHome: lookup());
		}
		else {
			synchronized (this) {
				if (this.cachedHome == null) {
					this.cachedHome = lookup();
					this.createMethod = getCreateMethod(this.cachedHome);
				}
				return this.cachedHome;
			}
		}
	}

	/**
	 * Return whether the cached EJB home object is potentially
	 * subject to on-demand refreshing. Default is false.
	 */
	protected boolean isHomeRefreshable() {
		return false;
	}

	/**
	 * Invoke the create() method on the cached EJB home.
	 * @return a new EJBObject or EJBLocalObject
	 * @throws NamingException if thrown by JNDI
	 * @throws InvocationTargetException if thrown by the create method
	 */
	protected Object create() throws NamingException, InvocationTargetException {
		try {
			Object home = getHome();
			Method createMethodToUse = this.createMethod;
			if (createMethodToUse == null) {
				createMethodToUse = getCreateMethod(home);
			}
			// invoke cached EJB home object
			return createMethodToUse.invoke(home, null);
		}
		catch (IllegalAccessException ex) {
			throw new AspectException("Could not access EJB home create() method", ex);
		}
	}

}
