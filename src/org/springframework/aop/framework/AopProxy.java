/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import net.sf.cglib.Enhancer;
import net.sf.cglib.MethodInterceptor;
import net.sf.cglib.MethodProxy;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * InvocationHandler implementation for the Spring AOP framework,
 * based on either J2SE 1.3+ dynamic proxies or CGLIB proxies.
 *
 * <p>Creates a J2SE proxy when proxied interfaces are given, a CGLIB proxy
 * for the actual target class if not. Note that the latter will only work
 * if the target class does not have final methods, as a dynamic subclass
 * will be created at runtime.
 *
 * <p>Objects of this type should be obtained through proxy factories,
 * configured by a ProxyConfig implementation. This class is internal
 * to the Spring framework and need not be used directly by client code.
 *
 * <p>Proxies created using this class can be threadsafe if the
 * underlying (target) class is threadsafe.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: AopProxy.java,v 1.5 2003-11-12 12:46:29 johnsonr Exp $
 * @see java.lang.reflect.Proxy
 * @see net.sf.cglib.Enhancer
 */
public class AopProxy implements InvocationHandler {
	
	private static Method EQUALS_METHOD;
	
	// We need a static block to handle checked exceptions
	static {
		try {
			EQUALS_METHOD = Object.class.getMethod("equals", new Class[] { Object.class});
		} 
		catch (NoSuchMethodException e) {
			// Cannot happen
		} 
	}
	
	protected final Log logger = LogFactory.getLog(getClass());

	/** Config used to configure this proxy */
	private ProxyConfig config;
	
	/** Factory for method invocation objects, to allow optimization */
	private MethodInvocationFactory methodInvocationFactory;
	
	/**
	 * 
	 * @throws AopConfigException if the config is invalid. We try
	 * to throw an informative exception in this case, rather than let
	 * a mysterious failure happen later.
	 */
	public AopProxy(ProxyConfig config, MethodInvocationFactory methodInvocationFactory) throws AopConfigException {
		if (config == null)
			throw new AopConfigException("Cannot create AopProxy with null ProxyConfig");
		if (config.getAdvices() == null || config.getAdvices().size() == 0)
			throw new AopConfigException("Cannot create AopProxy with null interceptors");
		this.config = config;
		this.methodInvocationFactory = methodInvocationFactory;
	}
	
	public AopProxy(ProxyConfig config) throws AopConfigException {
		this(config, new DefaultMethodInvocationFactory(config));
	}
	
	/**
	 * Implementation of InvocationHandler.invoke.
	 * Callers will see exactly the exception thrown by the target, unless a hook
	 * method throws an exception.
	 */
	public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	
		MethodInvocation invocation = this.methodInvocationFactory.getMethodInvocation(this.config, proxy, method, args);
		
		if (this.config.getExposeInvocation()) {
			// Make invocation available if necessary
			AopContext.setCurrentInvocation(invocation);
		}
		
		try {
			// Try special rules for equals() method and implementation of the
			// ProxyConfig AOP configuration interface
			if (EQUALS_METHOD.equals(invocation.getMethod())) {
				// What if equals throws exception!?
				logger.debug("Intercepting equals() method in proxy");
				// This class implements the equals() method itself
				return invocation.getMethod().invoke(this, invocation.getArguments());
			}
			else if (ProxyConfig.class.equals(invocation.getMethod().getDeclaringClass())) {
				// Service invocations on ProxyConfig with the proxy config
				return invocation.getMethod().invoke(this.config, invocation.getArguments());
			}
			
			Object retVal = invocation.proceed();
			if (retVal != null && retVal == invocation.getThis()) {
				// Special case: it returned this
				// Note that we can't help if the target sets
				// a reference to itself in another returned object
				logger.debug("Replacing 'this' with reference to proxy");
				retVal = proxy;
			}
			return retVal;
		}
		finally {
			if (this.config.getExposeInvocation()) {
				AopContext.setCurrentInvocation(null);
			}
		}
	}

	/**
	 * Creates a new Proxy object for the given object, proxying
	 * the given interface. Uses the thread context class loader.
	 */
	public Object getProxy() {
		return getProxy(Thread.currentThread().getContextClassLoader());
	}

	/**
	 * Creates a new Proxy object for the given object, proxying
	 * the given interface. Uses the given class loader.
	 */
	public Object getProxy(ClassLoader cl) {
		if (!this.config.getProxyTargetClass() && this.config.getProxiedInterfaces() != null && this.config.getProxiedInterfaces().length > 0) {
			// Proxy specific interfaces: J2SE dynamic proxy is sufficient
			logger.info("Creating J2SE proxy for [" + this.config.getTarget() + "]");
			Class[] proxiedInterfaces = completeProxiedInterfaces();
			return Proxy.newProxyInstance(cl, proxiedInterfaces, this);
		}
		else {
			// Use CGLIB
			if (this.config.getTarget() == null) {
				throw new IllegalArgumentException("Either an interface or a target is required for proxy creation");
			}
			// proxy the given class itself: CGLIB necessary
			logger.info("Creating CGLIB proxy for [" + this.config.getTarget() + "]");
			// delegate to inner class to avoid AopProxy runtime dependency on CGLIB
			// --> J2SE proxies work without cglib.jar then
			return (new CglibProxyFactory()).createProxy();
		}
	}
	
	/**
	 * Get complete set of interfaces to proxy. This will always add the ProxyConfig interface.
	 * @return the complete set of interfaces to proxy
	 */
	private Class[] completeProxiedInterfaces() {
		Class[] proxiedInterfaces = this.config.getProxiedInterfaces();
		if (proxiedInterfaces == null ||proxiedInterfaces.length == 0) {
			proxiedInterfaces = new Class[1];
			proxiedInterfaces[0] = ProxyConfig.class;
		}
		else {
			// Don't add the interface twice if it's already there
			if (!this.config.isInterfaceProxied(ProxyConfig.class)) {
				proxiedInterfaces = new Class[this.config.getProxiedInterfaces().length + 1];
				proxiedInterfaces[0] = ProxyConfig.class;
				System.arraycopy(this.config.getProxiedInterfaces(), 0, proxiedInterfaces, 1, this.config.getProxiedInterfaces().length);
			}
		}
		return proxiedInterfaces;
	}

	/**
	 * Equality means interceptors and interfaces are ==.
	 * This will only work with J2SE dynamic proxies,	not with CGLIB ones
	 * (as CGLIB doesn't delegate equals calls to proxies).
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @param other may be a dynamic proxy wrapping an instance
	 * of this class
	 */
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		
		AopProxy aopr2 = null;
		if (other instanceof AopProxy) {
			aopr2 = (AopProxy) other;
		}
		else if (Proxy.isProxyClass(other.getClass())) {
			InvocationHandler ih = Proxy.getInvocationHandler(other);
			if (!(ih instanceof AopProxy))
				return false;
			aopr2 = (AopProxy) ih; 
		}
		else {
			// Not a valid comparison
			return false;
		}
		
		// If we get here, aopr2 is the other AopProxy
		if (this == aopr2)
			return true;
			
		if (!Arrays.equals(aopr2.config.getProxiedInterfaces(), this.config.getProxiedInterfaces()))
			return false;
		
		// List equality is cool
		if (!aopr2.config.getAdvices().equals(this.config.getAdvices()))
			return false;
			
		return true;
	}


	/**
	 * Putting CGLIB proxy creation in an inner class allows to avoid an AopProxy
	 * runtime dependency on CGLIB --> J2SE proxies work without cglib.jar then.
	 */
	private class CglibProxyFactory {

		private Object createProxy() {
			return Enhancer.enhance(config.getTarget().getClass(), completeProxiedInterfaces(),
				new MethodInterceptor() {
					public Object intercept(Object handler, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
						return invoke(handler, method, objects);
					}
				}
			);
		}
	}

}
