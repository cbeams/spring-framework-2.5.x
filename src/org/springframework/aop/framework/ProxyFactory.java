/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.framework.support.AopUtils;


/**
 * Factory for AOP proxies for programmatic use, rather than via a bean
 * factory. This class provides a simple way of obtaining and configuring
 * AOP proxies in code.
 * @since 14-Mar-2003
 * @author Rod Johnson
 * @version $Id: ProxyFactory.java,v 1.6 2003-11-12 14:59:55 johnsonr Exp $
 */
public class ProxyFactory extends ProxyConfigSupport {

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
		addInterceptor(new InvokerInterceptor(target));
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
