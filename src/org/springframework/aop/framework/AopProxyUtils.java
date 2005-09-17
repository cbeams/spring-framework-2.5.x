/*
 * Copyright 2002-2005 the original author or authors.
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

import java.util.Arrays;

import org.springframework.aop.support.AopUtils;
import org.springframework.util.Assert;

/**
 * Miscellaneous utilities for AOP proxy users and AOP proxy implementations.
 * Mainly for internal use within the framework.
 *
 * <p>See AopUtils for a collection of generic AOP utility methods
 * which do not depend on the AOP framework itself.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.support.AopUtils
 */
public abstract class AopProxyUtils {

	/**
	 * Return the target class of the given bean instance.
	 * <p>Returns the target class for an AOP proxy and the plain bean class else.
	 * @param proxy the instance to check (might be an AOP proxy)
	 * @return the target class
	 * @see org.springframework.aop.framework.Advised#getTargetSource
	 * @see org.springframework.aop.TargetSource#getTargetClass
	 */
	public static Class getTargetClass(Object proxy) {
		if (AopUtils.isCglibProxy(proxy)) {
			return proxy.getClass().getSuperclass();
		}
		if (proxy instanceof Advised) {
			return ((Advised) proxy).getTargetSource().getTargetClass();
		}
		return proxy.getClass();
	}

	/**
	 * Get complete set of interfaces to proxy. This will always add the Advised interface
	 * unless the AdvisedSupport's "opaque" flag is true.
	 * @return the complete set of interfaces to proxy
	 */
	public static Class[] completeProxiedInterfaces(AdvisedSupport advised) {
		// Won't include Advised, which may be necessary.
		Class[] specifiedInterfaces = advised.getProxiedInterfaces();
		Class[] proxiedInterfaces = specifiedInterfaces;
		if (!advised.isOpaque() && !advised.isInterfaceProxied(Advised.class)) {
			// We need to add the Advised interface.
			proxiedInterfaces = new Class[specifiedInterfaces.length + 1];
			proxiedInterfaces[0] = Advised.class;
			System.arraycopy(specifiedInterfaces, 0, proxiedInterfaces, 1, specifiedInterfaces.length);
		}
		return proxiedInterfaces;
	}

	/**
	 * Extract the user-specified interfaces that the given proxy implements,
	 * i.e. all non-Advised interfaces that the proxy implements.
	 * @param proxy the proxy to analyze (usually a JDK dynamic proxy)
	 * @return all user-specified interfaces that the proxy implements,
	 * in the original order (never <code>null</code> or empty)
	 * @see Advised
	 */
	public static Class[] proxiedUserInterfaces(Object proxy) {
		Class[] proxyInterfaces = proxy.getClass().getInterfaces();
		if (proxy instanceof Advised) {
			Assert.isTrue((proxyInterfaces.length > 1),
					"JDK proxy must implement at least 1 interface aside from Advised");
			Class[] beanInterfaces = new Class[proxyInterfaces.length - 1];
			System.arraycopy(proxyInterfaces, 1, beanInterfaces, 0, beanInterfaces.length);
			return beanInterfaces;
		}
		else {
			Assert.notEmpty(proxyInterfaces,
					"JDK proxy must implement at least 1 interface aside from Advised");
			return proxyInterfaces;
		}
	}

	/**
	 * Check equality of the proxies behind the given AdvisedSupport objects.
	 * Not the same as equality of the AdvisedSupport objects:
	 * rather, equality of interfaces, advisors and target sources.
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

	/**
	 * Check equality of the proxied interfaces behind the given AdvisedSupport objects.
	 */
	public static boolean equalsProxiedInterfaces(AdvisedSupport a, AdvisedSupport b) {
		return Arrays.equals(a.getProxiedInterfaces(), b.getProxiedInterfaces());
	}
	
	/**
	 * Check equality of the advisors behind the given AdvisedSupport objects.
	 */
	public static boolean equalsAdvisors(AdvisedSupport a, AdvisedSupport b) {
		return Arrays.equals(a.getAdvisors(), b.getAdvisors());
	}

}
