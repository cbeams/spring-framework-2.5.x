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

import org.springframework.util.Assert;

/**
 * Miscellaneous utilities for AOP proxy implementations.
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
	 * in the original order (never null or empty)
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
