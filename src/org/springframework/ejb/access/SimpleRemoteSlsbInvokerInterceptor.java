/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ejb.access;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Arrays;

import javax.ejb.EJBObject;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.remoting.RemoteAccessException;

/**
 * Basic remote invoker for Stateless Session Beans.
 * "Creates" a new EJB instance for each invocation.
 * @author Rod Johnson
 * @version $Id: SimpleRemoteSlsbInvokerInterceptor.java,v 1.3 2003-12-19 11:28:17 jhoeller Exp $
 */
public class SimpleRemoteSlsbInvokerInterceptor extends AbstractRemoteSlsbInvokerInterceptor {
	
	/**
	 * Constructor for use as JavaBean.
	 * Sets "inContainer" to false by default.
	 * @see #setInContainer
	 */
	public SimpleRemoteSlsbInvokerInterceptor() {
		setInContainer(false);
	}
	
	/**
	 * Convenient constructor for programmatic use.
	 * @see org.springframework.jndi.AbstractJndiLocator#setJndiName
	 * @see org.springframework.jndi.AbstractJndiLocator#setInContainer
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
	 * This is the last invoker in the chain.
	 * "Creates" a new EJB instance for each invocation.
	 * Can be overridden for custom invocation strategies.
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		EJBObject ejb = newSessionBeanInstance();
		try {
			Method method = invocation.getMethod();
			if (method.getDeclaringClass().isInstance(ejb)) {
				// directly implemented
				return method.invoke(ejb, invocation.getArguments());
			}
			else {
				// not directly implemented
				Method proxyMethod = ejb.getClass().getMethod(method.getName(), method.getParameterTypes());
				return proxyMethod.invoke(ejb, invocation.getArguments());
			}
		}
		catch (InvocationTargetException ex) {
			Throwable targetException = ex.getTargetException();
			logger.info("Method of remote EJB [" + getJndiName() + "] threw exception", ex.getTargetException());
			if (targetException instanceof RemoteException &&
					!Arrays.asList(invocation.getMethod().getExceptionTypes()).contains(RemoteException.class)) {
				throw new RemoteAccessException("Cannot access remote EJB [" + getJndiName() + "]", targetException);
			}
			else {
				throw targetException;
			}
		}
		catch (Throwable t) {
			throw new AspectException("Failed to invoke remote EJB [" + getJndiName() + "]", t);
		}
	}

}
