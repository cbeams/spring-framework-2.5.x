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

package org.springframework.aop.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.aopalliance.aop.AspectException;

/**
 * Miscellaneous utilities for AOP proxies
 * @author Rod Johnson
 * @version $Id: AopProxyUtils.java,v 1.6 2004-03-19 21:35:54 johnsonr Exp $
 */
public abstract class AopProxyUtils {
	
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
	 * Invoke the target directly via reflection.
	 */
	public static Object invokeJoinpointUsingReflection(Object target, Method m, Object[] args) throws Throwable {
		//	Use reflection to invoke the method
		 try {
			 Object rval = m.invoke(target, args);
			 return rval;
		 }
		 catch (InvocationTargetException ex) {
			 // Invoked method threw a checked exception. 
			 // We must rethrow it. The client won't see the interceptor.
			 throw ex.getTargetException();
		 }
		 catch (IllegalArgumentException ex) {
			throw new AspectException("AOP configuration seems to be invalid: tried calling " + m + " on [" + target + "]: " +  ex);
		 }
		 catch (IllegalAccessException ex) {
			 throw new AspectException("Couldn't access method " + m, ex);
		 }
	}
	
	/**
	 * Note the same as equality of the AdvisedSupport objects.
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
