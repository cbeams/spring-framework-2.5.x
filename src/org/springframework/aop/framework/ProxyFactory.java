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

import org.aopalliance.intercept.Interceptor;

import org.springframework.aop.support.AopUtils;


/**
 * Factory for AOP proxies for programmatic use, rather than via a bean
 * factory. This class provides a simple way of obtaining and configuring
 * AOP proxies in code.
 * @since 14-Mar-2003
 * @author Rod Johnson
 * @version $Id: ProxyFactory.java,v 1.12 2004-04-01 15:35:46 jhoeller Exp $
 */
public class ProxyFactory extends AdvisedSupport {

	public ProxyFactory() {
	}

	/**
	 * Proxy all interfaces of the given target.
	 */
	public ProxyFactory(Object target) throws AopConfigException {
		if (target == null) {
			throw new AopConfigException("Can't proxy null object");
		}
		setInterfaces(AopUtils.getAllInterfaces(target));
		setTarget(target);
	}
	
	/**
	 * No target, only interfaces. Must add interceptors.
	 */
	public ProxyFactory(Class[] interfaces) {
		setInterfaces(interfaces);
	}

	/**
	 * Create new proxy according to the settings in this factory.
	 * Can be called repeatedly. Effect will vary if we've added
	 * or removed interfaces. Can add and remove "interceptors"
	 * @return Object
	 */
	public Object getProxy() {
		AopProxy proxy = createAopProxy();
		return proxy.getProxy();
	}

	/**
	 * Create new proxy for the given interface and interceptor.
	 * Convenience method for creating a proxy for a single interceptor.
	 * @param proxyInterface the interface that the proxy should implement
	 * @param interceptor the interceptor that the proxy should invoke
	 * @return the new proxy
	 */
	public static Object getProxy(Class proxyInterface, Interceptor interceptor) {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.addInterface(proxyInterface);
		proxyFactory.addInterceptor(interceptor);
		return proxyFactory.getProxy();
	}

}
