/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.aopalliance.intercept.AspectException;


/**
 * 
 * @author Rod Johnson
 * @version $Id: AopProxyUtils.java,v 1.2 2003-12-02 11:54:10 johnsonr Exp $
 */
public abstract class AopProxyUtils {
	
	public static Method EQUALS_METHOD;
	
	// We need a static block to handle checked exceptions
	static {
		try {
			EQUALS_METHOD = Object.class.getMethod("equals", new Class[] { Object.class});
		} 
		catch (NoSuchMethodException e) {
			// Cannot happen
		} 
	}
	
	/**
	 * Get complete set of interfaces to proxy. This will always add the ProxyConfig interface.
	 * @return the complete set of interfaces to proxy
	 */
	public static Class[] completeProxiedInterfaces(AdvisedSupport advised) {
		Class[] proxiedInterfaces = advised.getProxiedInterfaces();
		if (proxiedInterfaces == null ||proxiedInterfaces.length == 0) {
			proxiedInterfaces = new Class[1];
			proxiedInterfaces[0] = Advised.class;
		}
		else {
			// Don't add the interface twice if it's already there
			if (!advised.isInterfaceProxied(Advised.class)) {
				proxiedInterfaces = new Class[advised.getProxiedInterfaces().length + 1];
				proxiedInterfaces[0] = Advised.class;
				System.arraycopy(advised.getProxiedInterfaces(), 0, proxiedInterfaces, 1, advised.getProxiedInterfaces().length);
			}
		}
		return proxiedInterfaces;
	}

	/**
	 * Invoke the target directly via reflection
	 * @return
	 */
	public static Object invokeJoinpointUsingReflection(Object target, Method m, Object[] args) throws Throwable {
		//	Use reflection to invoke the method
		 try {
			 Object rval = m.invoke(target, args);
			 return rval;
		 }
		 catch (InvocationTargetException ex) {
			 // Invoked method threw a checked exception. 
			 // We must rethrow it. The client won't see the interceptor
			 Throwable t = ex.getTargetException();
			 throw t;
		 }
		 catch (IllegalArgumentException ex) {
			throw new AspectException("AOP configuration seems to be invalid: tried calling " + m + " on [" + target + "]: " +  ex);
		 }
		 catch (IllegalAccessException ex) {
			 throw new AspectException("Couldn't access method " + m, ex);
		 }
	}

	/**
	 * Note the same as equality of the AdvisedSupport objects
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean equalsInProxy(AdvisedSupport a, AdvisedSupport b) {
		if (a == b)
			return true;
	
		if (!Arrays.equals(a.getProxiedInterfaces(), b.getProxiedInterfaces()))
			return false;

		if (!Arrays.equals(a.getAdvisors(), b.getAdvisors()))
			return false;
	
		if (a.getTargetSource() == null)
			return b.getTargetSource() == null;
	
		return a.getTargetSource().equals(b.getTargetSource());
	}
	

}
