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
 * Miscellaneous utilities for AOP proxies.
 * @author Rod Johnson
 */
public abstract class AopProxyUtils {
	
	/**
	 * Get complete set of interfaces to proxy. This will always add the Advised interface
	 * unless the AdvisedSupport's "opaque" flag is true.
	 * @return the complete set of interfaces to proxy
	 */
	public static Class[] completeProxiedInterfaces(AdvisedSupport advised) {
		// Won't include Advised, which may be necessary.
		Class[] proxiedInterfacesOnConfig = advised.getProxiedInterfaces() == null ? new Class[0] : advised.getProxiedInterfaces();
		int lengthFromConfig = proxiedInterfacesOnConfig.length;
		int addedInterfaces = 0;
		if (!advised.getOpaque() && !advised.isInterfaceProxied(Advised.class)) {
			// We need to add Advised
			addedInterfaces = 1;
		}
		Class[] proxiedInterfaces = new Class[lengthFromConfig + addedInterfaces];
		
		System.arraycopy(proxiedInterfacesOnConfig, 0, proxiedInterfaces, addedInterfaces, proxiedInterfacesOnConfig.length);
		if (addedInterfaces == 1) {
			proxiedInterfaces[0] = Advised.class;
		}
		return proxiedInterfaces;
	}

	/**
	 * Invoke the target directly via reflection.
	 */
	public static Object invokeJoinpointUsingReflection(Object target, Method method, Object[] args)
	    throws Throwable {
		// Use reflection to invoke the method.
		try {
		 return method.invoke(target, args);
		}
		catch (InvocationTargetException ex) {
			// Invoked method threw a checked exception.
			// We must rethrow it. The client won't see the interceptor.
			throw ex.getTargetException();
		}
		catch (IllegalArgumentException ex) {
			throw new AspectException("AOP configuration seems to be invalid: tried calling " +
			    method + " on [" + target + "]: ", ex);
		}
		catch (IllegalAccessException ex) {
			throw new AspectException("Couldn't access method " + method, ex);
		}
	}
	
	/**
	 * Note the same as equality of the AdvisedSupport objects.
	 */
	public static boolean equalsInProxy(AdvisedSupport a, AdvisedSupport b) {
		if (a == b) {
			return true;
		}
		if (!equalsProxiedInterfaces(a, b)) {
			return false;
		}
		if (!equalsAdvisors(a, b)) {
			return false;
		}
		if (a.getTargetSource() == null) {
			return (b.getTargetSource() == null);
		}
		return a.getTargetSource().equals(b.getTargetSource());
	}
	
	public static boolean equalsProxiedInterfaces(AdvisedSupport a, AdvisedSupport b) {
		return Arrays.equals(a.getProxiedInterfaces(), b.getProxiedInterfaces());
	}
	
	public static boolean equalsAdvisors(AdvisedSupport a, AdvisedSupport b) {
		return Arrays.equals(a.getAdvisors(), b.getAdvisors());
	}

}
