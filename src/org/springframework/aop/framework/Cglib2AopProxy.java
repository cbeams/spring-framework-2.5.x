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

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

import net.sf.cglib.core.CodeGenerationException;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;

/**
 * CGLIB 2 AopProxy implementation for the Spring AOP framework.
 * Also implements the CGLIB MethodInterceptor and CallbackFilter
 * interfaces.
 *
 * <p>Objects of this type should be obtained through proxy factories,
 * configured by a AdvisedSupport implementation. This class is internal
 * to the Spring framework and need not be used directly by client code.
 *
 * <p>Proxies created using this class are threadsafe if the
 * underlying (target) class is threadsafe.
 *
 * @author Rod Johnson
 * @version $Id: Cglib2AopProxy.java,v 1.6 2004-03-19 21:35:54 johnsonr Exp $
 */
class Cglib2AopProxy implements AopProxy, MethodInterceptor, CallbackFilter {
	
	// Constants for CGLIB callback array indices
	private static final int AOP_PROXY = 0;
	
	private static final int INVOKE_TARGET = 1;
	
	private static final int NO_OVERRIDE = 2;
	
	protected final Log logger = LogFactory.getLog(getClass());

	/** Config used to configure this proxy */
	protected final AdvisedSupport advised;
	
	/**
	 * 
	 * @throws AopConfigException if the config is invalid. We try
	 * to throw an informative exception in this case, rather than let
	 * a mysterious failure happen later.
	 */
	protected Cglib2AopProxy(AdvisedSupport config) throws AopConfigException {
		if (config == null)
			throw new AopConfigException("Cannot create AopProxy with null ProxyConfig");
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE)
			throw new AopConfigException("Cannot create AopProxy with no advisors and no target source");
		this.advised = config;
		if (this.advised.getTargetSource().getTargetClass() == null) {
			throw new AopConfigException("Either an interface or a target is required for proxy creation");
		}
	}
		
	
	/**
	 * Implementation of MethodInterceptor.
	 * Callers will see exactly the exception thrown by the target, unless a hook
	 * method throws an exception.
	 */
	public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
	
		MethodInvocation invocation = null;
		Object oldProxy = null;
		boolean setProxyContext = false;
	
		TargetSource targetSource = advised.targetSource;
		Class targetClass = null;//targetSource.getTargetClass();
		Object target = null;		
		
		try {
			// Try special rules for equals() method and implementation of the
			// ProxyConfig AOP configuration interface
			if (isEqualsMethod(method)) {
				// This class implements the equals() method itself
				// We don't need to use reflection
				return new Boolean(equals(args[0]));
			}
			else if (Advised.class == method.getDeclaringClass()) {
				// Service invocations on ProxyConfig with the proxy config
				return AopProxyUtils.invokeJoinpointUsingReflection(this.advised, method, args);
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
		
			List chain = advised.getAdvisorChainFactory().getInterceptorsAndDynamicInterceptionAdvice(this.advised, proxy, method, targetClass);
			
			// Check whether we only have one InvokerInterceptor: that is, no real advice,
			// but just reflective invocation of the target.
			if (chain.isEmpty()) {
				// We can skip creating a MethodInvocation: just invoke the target directly
				// Note that the final invoker must be an InvokerInterceptor so we know it does
				// nothing but a reflective operation on the target, and no hot swapping or fancy proxying
				retVal = methodProxy.invoke(target, args);
			}
			else {
				// We need to create a method invocation...
				invocation = new MethodInvocationImpl(proxy, target, method, args, 
							targetClass, chain, methodProxy);
				
				// If we get here, we need to create a MethodInvocation
				retVal = invocation.proceed();
			}
			
			retVal = massageReturnTypeIfNecessary(proxy, target, retVal);
			return retVal;
		}
		catch (Throwable t) {
			// In CGLIB2, unlike CGLIB 1, it's necessary to wrap
			// undeclared throwable exceptions. As we don't care about JDK 1.2
			// compatibility, we use java.lang.reflect.UndeclaredThrowableException.
			if ( (t instanceof Exception) && !(t instanceof RuntimeException)) {
				// It's a checked exception: we must check it's legal
				Class[] permittedThrows = method.getExceptionTypes();
				for (int i = 0; i < permittedThrows.length; i++) {
					if (permittedThrows[i].isAssignableFrom(t.getClass())) {
						throw t;
					}
					//System.err.println("No match t=" + t + " throws=" + permittedThrows[i]);
				}
				throw new UndeclaredThrowableException(t);
			}
			
			// It's not a checked exception, so we can rethrow it
			throw t;
		}
		finally {
			if (target != null && !targetSource.isStatic()) {
				// Must have come from TargetSource
				targetSource.releaseTarget(target);
			}
			
			if (setProxyContext) {
				// Restore old proxy
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}	// intercept
	
	
	/**
	 * Wrap a return of this if necessary to be the proxy
	 */
	protected static Object massageReturnTypeIfNecessary(Object proxy, Object target, Object retVal) {
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
	 * Is the given method the equals method?
	 */
	protected final boolean isEqualsMethod(Method m) {
		return "equals".equals(m.getName()) && 
				m.getParameterTypes().length == 1 && 
				m.getParameterTypes()[0] == Object.class;
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
		if (logger.isDebugEnabled())
			logger.debug("Creating CGLIB proxy for [" + this.advised.getTargetSource().getTargetClass() + "]");
		
		Enhancer e = new Enhancer();
		try {
			e.setSuperclass(advised.getTargetSource().getTargetClass());
			e.setCallbackFilter(this);
			e.setInterfaces(AopProxyUtils.completeProxiedInterfaces(advised));
			Callback targetInvoker = canApplyCglibOptimizations() ? 
					(Callback) new StaticTargetInvoker(advised.getTargetSource().getTarget()) :
					(Callback) new DynamicTargetInvoker();
			
			e.setCallbacks(new Callback[] {
					this,				// For normal advice
					targetInvoker,		// invoke target without considering advice, if optimized
					NoOp.INSTANCE		// no override for methods mapped to this
			});
		
			return e.create();
		}
		catch (CodeGenerationException ex) {
			throw new AspectException("Couldn't generate CGLIB subclass of class '" + advised.getTargetSource().getTargetClass() + "': " +
					"Common causes of this problem include using a final class, or a non-visible class", ex);
		}
		catch (Exception ex) {
			// TargetSource getTarget failed
			throw new AopConfigException("Unexpected AOP exception", ex);
		}
	}
	
	/**
	 * Invoker used to invoke the target without creating a method invocation
	 * or evaluating an advice chain. (We know there was no advice for this method.)
	 */
	private class DynamicTargetInvoker implements MethodInterceptor {
		 /**
		 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
		 */
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object target = advised.getTargetSource().getTarget();
			Object ret = methodProxy.invoke(target, args);
			return massageReturnTypeIfNecessary(proxy, target, ret);
		}
	}
	
	/**
	 * Like DynamicTargetInvoker, for use when there's a static TargetSource
	 */
	private static class StaticTargetInvoker implements MethodInterceptor {
		private final Object target;
		public StaticTargetInvoker(Object target) {
			this.target = target;
		}
		 /**
		 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
		 */
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object ret = methodProxy.invoke(target, args);
			return massageReturnTypeIfNecessary(proxy, target, ret);
		}
	}
	
	
	/**
	 * Given the Advised object we have, can we apply CGLIB optimizations 
	 * (directly invoking the target for methods with no advice)?
	 */
	private boolean canApplyCglibOptimizations() {
		return advised.getOptimize() && 
			advised.getTargetSource().isStatic() &&
			!advised.getExposeProxy();
	}
	
	/**
	 * Implementation of CallbackFilter.accept() to return the index of the
	 * callback we need. This will mean either no overriding,
	 * AOP_PROXY (run through our intercept method) or INVOKE_TARGET 
	 * (optimized direct invocation of target without re-evaluating
	 * advice chain at runtime).
	 * @see net.sf.cglib.proxy.CallbackFilter#accept(java.lang.reflect.Method)
	 */
	public int accept(Method method) {
		
		if (method.getName().equals("finalize") && method.getDeclaringClass() == Object.class) {
			return NO_OVERRIDE; 
		}
		
		if (!canApplyCglibOptimizations()) {
			return AOP_PROXY;
		}
		
		// Could consider more aggressive optimization in which we have a distinct
		// callback with the advice chain for each method, but it's probably not
		// worth it
		
		// We can apply optimizations
		// The optimization means that we evaluate whether or not there's an
		// advice chain once only, befre each invocation. 
	
		Class targetClass = advised.getTargetSource().getTargetClass();
		
		// We must always proxy equals, to direct calls to this
		if (isEqualsMethod(method))
			return AOP_PROXY;
	
		// Proxy is not yet available, but that shouldn't matter
		List chain = advised.getAdvisorChainFactory().getInterceptorsAndDynamicInterceptionAdvice(advised, null, method, targetClass);
		boolean  haveAdvice = !chain.isEmpty();

		if (haveAdvice) {
			logger.info("CGLIB proxy for " + targetClass.getName() + 
						" WILL override " + method);
		}
		else {
			logger.info("Chain is empty for " + method + "; will NOT override");
		}
		return haveAdvice ? AOP_PROXY : INVOKE_TARGET;
	}
	

	/**
	 * Equality means interceptors and interfaces are ==.
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @param other may be a dynamic proxy wrapping an instance
	 * of this class
	 */
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		
		Cglib2AopProxy otherCglibProxy = null;
		if (other instanceof Cglib2AopProxy) {
			otherCglibProxy = (Cglib2AopProxy) other;
		}
		else if (other instanceof Factory) {
			// The 0th callback will be the Cglib2AopProxy if we're correct
			Callback callback = ((Factory) other).getCallback(AOP_PROXY);
			if (!(callback instanceof Cglib2AopProxy))
				return false;
			otherCglibProxy = (Cglib2AopProxy) callback; 
		}
		else {
			// Not a valid comparison
			return false;
		}
		
		return AopProxyUtils.equalsInProxy(advised, otherCglibProxy.advised);
	}
	
	
	/**
	 * Implementation of AOP Alliance MethodInvocation used by this AOP proxy
	 */
	private static class MethodInvocationImpl extends ReflectiveMethodInvocation {
	
		private MethodProxy methodProxy;
	
		public MethodInvocationImpl(Object proxy, Object target, Method m, Object[] arguments, Class targetClass,
				List interceptorsAndDynamicMethodMatchers,
				MethodProxy methodProxy) {
			super(proxy, target, m, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
			this.methodProxy = methodProxy;
		}
	
		/**
		 * Gives a marginal performance improvement versus using reflection to invoke the target.
		 * @see org.springframework.aop.framework.ReflectiveMethodInvocation#invokeJoinpoint()
		 */
		protected Object invokeJoinpoint() throws Throwable {
			return methodProxy.invoke(target, arguments);
		}
	}

}
