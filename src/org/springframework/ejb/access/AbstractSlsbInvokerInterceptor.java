/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.ejb.access;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jndi.AbstractJndiLocator;

/**
 * Superclass for all AOP interceptors invoking Stateless Session Beans.
 * These must be the last interceptor in the interceptor chain. In this case,
 * there is no target object.
 * @author Rod Johnson
 * @version $Id: AbstractSlsbInvokerInterceptor.java,v 1.5 2003-12-30 01:11:55 jhoeller Exp $
 */
public abstract class AbstractSlsbInvokerInterceptor extends AbstractJndiLocator
		implements MethodInterceptor, InitializingBean {

	/** 
	 * No arg create() method required on EJB homes,
	 * but not part of EJBLocalHome
	 */
	private Method createMethod;
	
	/**
	 * The home interface. Must be object as it could be either EJBHome or EJBLocalHome.
	 */
	private Object cachedHome;
	
	/**
	 * @return the cached home object
	 */
	protected Object getCachedEjbHome() {
		return cachedHome;
	}
	
 	/**
 	 * Implementation of AbstractJndiLocator's callback, to cache the home wrapper.
	 * Triggers afterLocated after execution.
	 * @see #afterLocated
	 */
	protected void located(Object jndiObject) {
		// Cache the home object
		this.cachedHome = jndiObject;
		try {
			// Cache the EJB create() method that must be declared on the home interface
			createMethod = cachedHome.getClass().getMethod("create", null);
		}
		catch (NoSuchMethodException ex) {
			throw new FatalBeanException("Cannot create EJB proxy: EJB home [" + cachedHome + "] has no no-arg create() method");
		}
		
		// Invoke any subclass initialization behaviour
		afterLocated();
	}

	/**
	 * Initialization hook after the AbstractJndiLocator's located callback.
	 * @see #located
	 */
	protected void afterLocated() {
	}

	/**
	 * Invoke the create() method on the cached home.
	 * @return a new EJBObject or EJBLocalObject
	 */
	protected Object create() throws InvocationTargetException {
		try {
			return this.createMethod.invoke(this.cachedHome, null);
		}
		catch (IllegalArgumentException ex) {
			// Can't happen
			throw new FatalBeanException("Inconsistent state: could not call ejbCreate() method without arguments", ex);
		}
		catch (IllegalAccessException ex) {
			throw new FatalBeanException("Could not access ejbCreate() method", ex);
		}
	}

}
