/*
 * Copyright 2002-2007 the original author or authors.
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.Advisor;
import org.springframework.aop.support.AopUtils;

/**
 * InvocationHandler implementation for the Spring AOP framework,
 * based on J2SE 1.3+ dynamic proxies.
 *
 * <p>Creates a J2SE proxy, implementing the interfaces exposed by the
 * proxy. Dynamic proxies cannot be used to proxy methods defined in
 * classes, rather than interface.
 *
 * <p>Objects of this type should be obtained through proxy factories,
 * configured by an AdvisedSupport class. This class is internal
 * to the Spring framework and need not be used directly by client code.
 *
 * <p>Proxies created using this class will be threadsafe if the
 * underlying (target) class is threadsafe.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.lang.reflect.Proxy
 * @see org.springframework.aop.framework.AdvisedSupport
 * @see org.springframework.aop.framework.ProxyFactory
 */
final class OptimizedJdkDynamicAopProxy implements AopProxy, InvocationHandler {

	/*
	 * NOTE: We could avoid the code duplication between this class and the CGLIB
	 * proxies by refactoring invoke() into a template method. However, this approach
	 * adds at least 10% performance overhead versus a copy-paste solution, so we sacrifice
	 * elegance for performance. (We have a good test suite to ensure that the different
	 * proxies behave the same :-)
	 * This way, we can also more easily take advantage of minor optimizations in each class.
	 */

	private final Log logger = LogFactory.getLog(getClass());

	/** Config used to configure this proxy */
	private final AdvisedSupport advised;

	/**
	 * Target: usually null, non-null only if we have a static target source and frozen
	 * configuration.
	 */
	private Object target;

	private Advisor[] advisors;

	private Class targetClass;

	/**
	 * Construct a new JDK proxy.
	 * @throws AopConfigException if the config is invalid. We try
	 * to throw an informative exception in this case, rather than let
	 * a mysterious failure happen later.
	 */
	protected OptimizedJdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
		if (config == null)
			throw new AopConfigException("Cannot create AopProxy with null ProxyConfig");
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE)
			throw new AopConfigException("Cannot create AopProxy with no advisors and no target source");
		this.advised = config;
		this.advisors = config.getAdvisors();

		if (!config.targetSource.isStatic()) {
			throw new AopConfigException("Can't use Optimized JDK proxy with non-static target source");
		}
		if (config.exposeProxy) {
			throw new AopConfigException("Can't use Optimized JDK proxy if proxy needs to be exposed");
		}

		try {
			// TODO must be frozen
			System.err.println("Caching lockable target");
			this.target = config.targetSource.getTarget();
			this.targetClass = target.getClass();
		}
		catch (Exception ex) {
			throw new AopConfigException("Can't obtain target from static TargetSource", ex);
		}
	}

	/**
	 * Implementation of InvocationHandler.invoke.
	 * Callers will see exactly the exception thrown by the target, unless a hook
	 * method throws an exception.
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		Class declaringClass = method.getDeclaringClass();
		
		// Try special rules for equals() method and implementation of the
		// Advised AOP configuration interface
		if (declaringClass == Object.class && "equals".equals(method.getName())) {
			// What if equals throws exception!?

			// This class implements the equals() method itself
			return new Boolean(equals(args[0]));
		}
		else if (Advised.class == declaringClass) {
			// Service invocations on ProxyConfig with the proxy config
			return method.invoke(this.advised, args);
		}

		Object retVal = null;

		// Get the interception chain for this method
		List chain =
			this.advised.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(
				this.advised, method, this.targetClass);

		// Check whether we have any advice. If we don't, we can fallback on
		// direct reflective invocation of the target, and avoid creating a MethodInvocation
		if (chain.isEmpty()) {
			// We can skip creating a MethodInvocation: just invoke the target directly
			// Note that the final invoker must be an InvokerInterceptor so we know it does
			// nothing but a reflective operation on the target, and no hot swapping or fancy proxying
			retVal = AopUtils.invokeJoinpointUsingReflection(target, method, args);
		}
		else {
			// We need to create a method invocation...
			//invocation = advised.getMethodInvocationFactory().getMethodInvocation(proxy, method, targetClass, target, args, chain, advised);
			MethodInvocation invocation = 
				new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);

			// Proceed to the joinpoint through the interceptor chain
			retVal = invocation.proceed();
		}

		// Massage return value if necessary
		if (retVal != null && retVal == target) {
			// Special case: it returned "this"
			// Note that we can't help if the target sets
			// a reference to itself in another returned object
			retVal = proxy;
		}
		return retVal;
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
		logger.debug("Creating JDK dynamic proxy");
		Class[] proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised);
		return Proxy.newProxyInstance(cl, proxiedInterfaces, this);
	}

	/**
	 * Equality means interceptors and interfaces and
	 * TargetSource are equal.
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @param other may be a dynamic proxy wrapping an instance
	 * of this class
	 */
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;

		OptimizedJdkDynamicAopProxy aopr2 = null;
		if (other instanceof OptimizedJdkDynamicAopProxy) {
			aopr2 = (OptimizedJdkDynamicAopProxy) other;
		}
		else if (Proxy.isProxyClass(other.getClass())) {
			InvocationHandler ih = Proxy.getInvocationHandler(other);
			if (!(ih instanceof OptimizedJdkDynamicAopProxy))
				return false;
			aopr2 = (OptimizedJdkDynamicAopProxy) ih;
		}
		else {
			// Not a valid comparison
			return false;
		}

		// If we get here, aopr2 is the other AopProxy
		return AopProxyUtils.equalsInProxy(this.advised, aopr2.advised);
	}

}
