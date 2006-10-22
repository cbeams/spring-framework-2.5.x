/*
 * Copyright 2002-2006 the original author or authors.
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

import org.springframework.aop.SpringProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Miscellaneous utilities for AOP proxy users and AOP proxy implementations.
 * Mainly for internal use within the framework.
 *
 * <p>See {@link org.springframework.aop.support.AopUtils} for a collection of
 * generic AOP utility methods which do not depend on the AOP framework itself.
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
		Assert.notNull(proxy, "Proxy must not be null");
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
	 * unless the AdvisedSupport's "opaque" flag is on. Always adds the
	 * {@link org.springframework.aop.SpringProxy} interface.
	 * @return the complete set of interfaces to proxy
	 */
	public static Class[] completeProxiedInterfaces(AdvisedSupport advised) {
		// Won't include Advised, which may be necessary.
		Class[] specifiedInterfaces = advised.getProxiedInterfaces();

		boolean addSpringProxy = !advised.isInterfaceProxied(SpringProxy.class);
		boolean addAdvised = !advised.isOpaque() && !advised.isInterfaceProxied(Advised.class);

		int offset = 0;
		if(addSpringProxy && addAdvised) {
			offset = 2;
		} else if (addSpringProxy || addAdvised) {
			offset = 1;
		}

		Class[] proxiedInterfaces = new Class[specifiedInterfaces.length + offset];

		if(addSpringProxy) {
			proxiedInterfaces[0] = SpringProxy.class;
			if(addAdvised) {
				proxiedInterfaces[1] = Advised.class;
			}
		} else if (addAdvised) {
			proxiedInterfaces[0] = Advised.class;
		}

		System.arraycopy(specifiedInterfaces, 0, proxiedInterfaces, offset, specifiedInterfaces.length);

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

		boolean isSpringProxy = proxy instanceof SpringProxy;
		boolean isAdvised = proxy instanceof Advised;

		int cut = 0;
		if (isAdvised && isSpringProxy) {
			cut = 2;
		}
		else if (isAdvised || isSpringProxy) {
			cut = 1;
		}
		Class[] beanInterfaces = new Class[proxyInterfaces.length - cut];
		System.arraycopy(proxyInterfaces, cut, beanInterfaces, 0, beanInterfaces.length);
		Assert.notEmpty(beanInterfaces, "JDK proxy must have one or more interface.");
		return beanInterfaces;
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
		return equalsProxiedInterfaces(a, b) && equalsAdvisors(a, b) &&
				ObjectUtils.nullSafeEquals(a.getTargetSource(), b.getTargetSource());
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
