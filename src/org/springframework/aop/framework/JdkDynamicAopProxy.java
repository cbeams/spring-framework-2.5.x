/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;

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
 * @version $Id: JdkDynamicAopProxy.java,v 1.6 2003-12-10 11:23:56 johnsonr Exp $
 * @see java.lang.reflect.Proxy
 * @see org.springframework.aop.framework.AdvisedSupport
 * @see org.springframework.aop.framework.ProxyFactory
 */
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler {
	
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
	 * Construct a new JDK proxy.
	 * @throws AopConfigException if the config is invalid. We try
	 * to throw an informative exception in this case, rather than let
	 * a mysterious failure happen later.
	 */
	protected JdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
		if (config == null)
			throw new AopConfigException("Cannot create AopProxy with null ProxyConfig");
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE)
			throw new AopConfigException("Cannot create AopProxy with no advisors and no target source");
		this.advised = config;
	}
	
	
	/**
	 * Implementation of InvocationHandler.invoke.
	 * Callers will see exactly the exception thrown by the target, unless a hook
	 * method throws an exception.
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	
		MethodInvocation invocation = null;
		MethodInvocation oldInvocation = null;
		Object oldProxy = null;
		boolean setInvocationContext = false;
		boolean setProxyContext = false;
	
		TargetSource targetSource = advised.targetSource;
		Class targetClass = null;
		Object target = null;		
		
		try {
			// Try special rules for equals() method and implementation of the
			// Advised AOP configuration interface
			if (AopProxyUtils.EQUALS_METHOD.equals(method)) {
				// What if equals throws exception!?

				// This class implements the equals() method itself
				return new Boolean(equals(args[0]));
			}
			else if (Advised.class.equals(method.getDeclaringClass())) {
				// Service invocations on ProxyConfig with the proxy config
				return method.invoke(this.advised, args);
			}
			
			Object retVal = null;
			
			// May be null. Get as late as possible to minimize the time we "own" the target,
			// in case it comes from a pool.
			target = targetSource.getTarget();
			if (target != null) {
				targetClass = target.getClass();
			}
			
			if (this.advised.exposeProxy) {
				// Make invocation available if necessary
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}
		
			// Get the interception chain for this method
			List chain = advised.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(this.advised, proxy, method, targetClass);
			
			// Check whether we have any advice. If we don't, we can fallback on
			// direct reflective invocation of the target, and avoid creating a MethodInvocation
			// We can only do this if the AdvisedSupport config object lets us.
			if (chain.isEmpty() && !advised.exposeInvocation) {
				// We can skip creating a MethodInvocation: just invoke the target directly
				// Note that the final invoker must be an InvokerInterceptor so we know it does
				// nothing but a reflective operation on the target, and no hot swapping or fancy proxying
				retVal = AopProxyUtils.invokeJoinpointUsingReflection(target, method, args);
			}
			else {
				// We need to create a method invocation...
				//invocation = advised.getMethodInvocationFactory().getMethodInvocation(proxy, method, targetClass, target, args, chain, advised);
				
				invocation = new ReflectiveMethodInvocation(proxy, target, method.getDeclaringClass(), method, args, targetClass, chain);
			
				if (this.advised.exposeInvocation) {
					// Make invocation available if necessary.
					// Save the old value to reset when this method returns
					// so that we don't blow away any existing state
					oldInvocation = AopContext.setCurrentInvocation(invocation);
					// We need to know whether we actually set it, as
					// this block may not have been reached even if exposeInvocation
					// is true
					setInvocationContext = true;
				}
				
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
		finally {
			if (target != null && !targetSource.isStatic()) {
				// Must have come from TargetSource
				targetSource.releaseTarget(target);
			}
			
			if (setInvocationContext) {
				// Restore old invocation, which may be null
				AopContext.setCurrentInvocation(oldInvocation);
			}
			if (setProxyContext) {
				// Restore old proxy
				AopContext.setCurrentProxy(oldProxy);
			}
			
			//if (invocation != null) {
			//	advised.getMethodInvocationFactory().release(invocation);
			//}
		}
	}	// invoke
	

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
		if (logger.isInfoEnabled())
			logger.info("Creating J2SE proxy for [" + this.advised.getTargetSource().getTargetClass() + "]");
		Class[] proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(advised);
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
		
		JdkDynamicAopProxy aopr2 = null;
		if (other instanceof JdkDynamicAopProxy) {
			aopr2 = (JdkDynamicAopProxy) other;
		}
		else if (Proxy.isProxyClass(other.getClass())) {
			InvocationHandler ih = Proxy.getInvocationHandler(other);
			if (!(ih instanceof JdkDynamicAopProxy))
				return false;
			aopr2 = (JdkDynamicAopProxy) ih; 
		}
		else {
			// Not a valid comparison
			return false;
		}
		
		// If we get here, aopr2 is the other AopProxy
		return AopProxyUtils.equalsInProxy(this.advised, aopr2.advised);
	}

}
