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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ClassUtils;

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
 * <p>Proxies are serializable so long as all Advisors
 * are serializable (meaning both Advices and Pointcuts)
 * and the TargetSource is serializable.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.lang.reflect.Proxy
 * @see org.springframework.aop.framework.AdvisedSupport
 * @see org.springframework.aop.framework.ProxyFactory
 */
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {
	
	/*
	 * NOTE: We could avoid the code duplication between this class and the CGLIB
	 * proxies by refactoring invoke() into a template method. However, this approach
	 * adds at least 10% performance overhead versus a copy-paste solution, so we sacrifice
	 * elegance for performance. (We have a good test suite to ensure that the different
	 * proxies behave the same :-)
	 * This way, we can also more easily take advantage of minor optimizations in each class.
	 */
	
	/** We use a static Log to avoid serialization issues */
	private static Log logger = LogFactory.getLog(JdkDynamicAopProxy.class);

	/** Config used to configure this proxy */
	private AdvisedSupport advised;


	/**
	 * Construct a new JDK proxy.
	 * @throws AopConfigException if the config is invalid. We try
	 * to throw an informative exception in this case, rather than let
	 * a mysterious failure happen later.
	 */
	protected JdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
		if (config == null) {
			throw new AopConfigException("Cannot create AopProxy with null ProxyConfig");
		}
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("Cannot create AopProxy with no advisors and no target source");
		}
		this.advised = config;
	}
	

	public Object getProxy() {
		return getProxy(ClassUtils.getDefaultClassLoader());
	}

	public Object getProxy(ClassLoader classLoader) {
		if (logger.isDebugEnabled()) {
			Class targetClass = this.advised.getTargetSource().getTargetClass();
			logger.debug("Creating JDK dynamic proxy" +
					(targetClass != null ? " for [" + targetClass.getName() + "]" : ""));
		}
		Class[] proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised);
		return Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);
	}


	/**
	 * Implementation of InvocationHandler.invoke.
	 * Callers will see exactly the exception thrown by the target, unless a hook
	 * method throws an exception.
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodInvocation invocation = null;
		Object oldProxy = null;
		boolean setProxyContext = false;

		TargetSource targetSource = this.advised.targetSource;
		Class targetClass = null;
		Object target = null;

		try {
			// Try special rules for equals() method and implementation of the
			// Advised AOP configuration interface.

			if (AopUtils.isEqualsMethod(method)) {
				// What if equals throws exception!?
				// This class implements the equals(Object) method itself.
				return equals(args[0]) ? Boolean.TRUE : Boolean.FALSE;
			}
			if (AopUtils.isHashCodeMethod(method)) {
				// This class implements the hashCode() method itself.
				return new Integer(hashCode());
			}
			if (Advised.class == method.getDeclaringClass()) {
				// service invocations on ProxyConfig with the proxy config
				return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
			}

			Object retVal = null;
			
			if (this.advised.exposeProxy) {
				// make invocation available if necessary
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}

			// May be <code>null</code>. Get as late as possible to minimize the time we "own" the target,
			// in case it comes from a pool.
			target = targetSource.getTarget();
			if (target != null) {
				targetClass = target.getClass();
			}

			// get the interception chain for this method
			List chain = this.advised.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(
					this.advised, proxy, method, targetClass);

			// Check whether we have any advice. If we don't, we can fallback on direct
			// reflective invocation of the target, and avoid creating a MethodInvocation.
			if (chain.isEmpty()) {
				// We can skip creating a MethodInvocation: just invoke the target directly
				// Note that the final invoker must be an InvokerInterceptor so we know it does
				// nothing but a reflective operation on the target, and no hot swapping or fancy proxying
				retVal = AopUtils.invokeJoinpointUsingReflection(target, method, args);
			}
			else {
				// We need to create a method invocation...
				// invocation = advised.getMethodInvocationFactory().getMethodInvocation(
				//		 proxy, method, targetClass, target, args, chain, advised);

				invocation = new ReflectiveMethodInvocation(
						proxy, target, method, args, targetClass, chain);

				// proceed to the joinpoint through the interceptor chain
				retVal = invocation.proceed();
			}

			// massage return value if necessary
			if (retVal != null && retVal == target && method.getReturnType().isInstance(proxy)) {
				// Special case: it returned "this" and the return type of the method is type-compatible
				// Note that we can't help if the target sets
				// a reference to itself in another returned object.
				retVal = proxy;
			}
			return retVal;
		}
		finally {
			if (target != null && !targetSource.isStatic()) {
				// must have come from TargetSource
				targetSource.releaseTarget(target);
			}

			if (setProxyContext) {
				// restore old proxy
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}


	/**
	 * Proxy uses the hash code of the TargetSource.
	 */
	public int hashCode() {
		return this.advised.getTargetSource().hashCode();
	}

	/**
	 * Equality means interfaces, advisors and TargetSource are equal.
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @param other may be a dynamic proxy wrapping an instance of this class
	 */
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}

		JdkDynamicAopProxy aopr2 = null;
		if (other instanceof JdkDynamicAopProxy) {
			aopr2 = (JdkDynamicAopProxy) other;
		}
		else if (Proxy.isProxyClass(other.getClass())) {
			InvocationHandler ih = Proxy.getInvocationHandler(other);
			if (!(ih instanceof JdkDynamicAopProxy)) {
				return false;
			}
			aopr2 = (JdkDynamicAopProxy) ih;
		}
		else {
			// not a valid comparison
			return false;
		}
		
		// If we get here, aopr2 is the other AopProxy.
		return AopProxyUtils.equalsInProxy(this.advised, aopr2.advised);
	}

}
