/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.ejb.access;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jndi.AbstractJndiLocator;

/**
 * Superclass for AOP interceptors invoking remote or local Stateless Session Beans.
 * Such an interceptor must be the last interceptor in the advice chain. In this case,
 * there is no target object.
 * @author Rod Johnson
 * @version $Id: AbstractSlsbInvokerInterceptor.java,v 1.6 2003-12-31 14:33:05 johnsonr Exp $
 */
public abstract class AbstractSlsbInvokerInterceptor extends AbstractJndiLocator
		implements MethodInterceptor, InitializingBean {

	/** 
	 * The no-arg create() method required on EJB homes,
	 * but not part of EJBLocalHome. We cache this in the located() method.
	 */
	private Method createMethod;
	
	/**
	 * The EJB's home interface. 
	 * The type must be Object as it could be either EJBHome or EJBLocalHome.
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
	 * Invokes afterLocated() after execution.
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
	 * This implementation does nothing.
	 * @see #located
	 */
	protected void afterLocated() {
	}

	/**
	 * Invoke the create() method on the cached EJB home.
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
