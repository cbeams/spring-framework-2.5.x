/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import net.sf.cglib.CodeGenerationException;
import net.sf.cglib.Enhancer;
import net.sf.cglib.MethodInterceptor;
import net.sf.cglib.MethodProxy;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.interceptor.InvokerInterceptor;

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
 * @version $Id: AopProxy.java,v 1.13 2003-11-29 13:36:33 johnsonr Exp $
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
	private AdvisedSupport advised;
	
	/**
	 * 
	 * @throws AopConfigException if the config is invalid. We try
	 * to throw an informative exception in this case, rather than let
	 * a mysterious failure happen later.
	 */
	public AopProxy(AdvisedSupport config) throws AopConfigException {
		if (config == null)
			throw new AopConfigException("Cannot create AopProxy with null ProxyConfig");
		if (config.getAdvisors().length == 0)
			throw new AopConfigException("Cannot create AopProxy with null interceptors");
		this.advised = config;
	}
	
	
	/**
	 * Implementation of InvocationHandler.invoke.
	 * Callers will see exactly the exception thrown by the target, unless a hook
	 * method throws an exception.
	 */
	public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	
		MethodInvocation invocation = null;
		MethodInvocation oldInvocation = null;
		Object oldProxy = null;
		boolean setInvocationContext = false;
		boolean setProxyContext = false;
		
		try {
			// Try special rules for equals() method and implementation of the
			// ProxyConfig AOP configuration interface
			if (EQUALS_METHOD.equals(method)) {
				// What if equals throws exception!?

				// This class implements the equals() method itself
				return method.invoke(this, args);
			}
			else if (Advised.class.equals(method.getDeclaringClass())) {
				// Service invocations on ProxyConfig with the proxy config
				return method.invoke(this.advised, args);
			}
			
			Object retVal = null;
			
			Class targetClass = advised.getTarget() != null ? advised.getTarget().getClass() : method.getDeclaringClass();
		
			List chain = advised.getAdvisorChainFactory().getInterceptorsAndDynamicInterceptionAdvice(this.advised, proxy, method, targetClass);
			
			// Check whether we only have one InvokerInterceptor: that is, no real advice,
			// but just reflective invocation of the target.
			// We can only do this if the Advised config object lets us.
			if (advised.canOptimizeOutEmptyAdviceChain() && 
					chain.size() == 1 && chain.get(0).getClass() == InvokerInterceptor.class) {
				// We can skip creating a MethodInvocation: just invoke the target directly
				// Note that the final invoker must be an InvokerInterceptor so we know it does
				// nothing but a reflective operation on the target, and no hot swapping or fancy proxying
				retVal = directInvoke(advised.getTarget(), method, args);
			}
			else {
				// We need to create a method invocation...
				invocation = advised.getMethodInvocationFactory().getMethodInvocation(proxy, method, targetClass, advised.getTarget(), args, chain, advised);
			
				if (this.advised.getExposeInvocation()) {
					// Make invocation available if necessary.
					// Save the old value to reset when this method returns
					// so that we don't blow away any existing state
					oldInvocation = AopContext.setCurrentInvocation(invocation);
					// We need to know whether we actually set it, as
					// this block may not have been reached even if exposeInvocation
					// is true
					setInvocationContext = true;
				}
				
				if (this.advised.getExposeProxy()) {
					// Make invocation available if necessary
					oldProxy = AopContext.setCurrentProxy(proxy);
					setProxyContext = true;
				}
				
				// If we get here, we need to create a MethodInvocation
				retVal = invocation.proceed();
			}
			
			// Massage return value if necessary
			if (retVal != null && retVal == advised.getTarget()) {
				// Special case: it returned "this"
				// Note that we can't help if the target sets
				// a reference to itself in another returned object
				retVal = proxy;
			}
			return retVal;
		}
		finally {
			if (setInvocationContext) {
				// Restore old invocation, which may be null
				AopContext.setCurrentInvocation(oldInvocation);
			}
			if (setProxyContext) {
				// Restore old proxy
				AopContext.setCurrentProxy(oldProxy);
			}
			
			if (invocation != null) {
				advised.getMethodInvocationFactory().release(invocation);
			}
		}
	}
	
	
	/**
	 * Invoke the target directly via reflection
	 * @return
	 */
	private Object directInvoke(Object target, Method m, Object[] args) throws Throwable {
		//		Use reflection to invoke the method
		 try {
			 Object rval = m.invoke(target, args);
			 return rval;
		 }
		 catch (InvocationTargetException ex) {
			 // Invoked method threw a checked exception. 
			 // We must rethrow it. The client won't see the interceptor
			 Throwable t = ex.getTargetException();
			 throw t;
		 }
		 catch (IllegalAccessException ex) {
			 throw new AspectException("Couldn't access method " + m, ex);
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
		if (!this.advised.getProxyTargetClass() && this.advised.getProxiedInterfaces() != null && this.advised.getProxiedInterfaces().length > 0) {
			// Proxy specific interfaces: J2SE dynamic proxy is sufficient
			if (logger.isInfoEnabled())
				logger.info("Creating J2SE proxy for [" + this.advised.getTarget() + "]");
			Class[] proxiedInterfaces = completeProxiedInterfaces();
			return Proxy.newProxyInstance(cl, proxiedInterfaces, this);
		}
		else {
			// Use CGLIB
			if (this.advised.getTarget() == null) {
				throw new IllegalArgumentException("Either an interface or a target is required for proxy creation");
			}
			// proxy the given class itself: CGLIB necessary
			if (logger.isInfoEnabled())
				logger.info("Creating CGLIB proxy for [" + this.advised.getTarget() + "]");
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
		Class[] proxiedInterfaces = this.advised.getProxiedInterfaces();
		if (proxiedInterfaces == null ||proxiedInterfaces.length == 0) {
			proxiedInterfaces = new Class[1];
			proxiedInterfaces[0] = Advised.class;
		}
		else {
			// Don't add the interface twice if it's already there
			if (!this.advised.isInterfaceProxied(Advised.class)) {
				proxiedInterfaces = new Class[this.advised.getProxiedInterfaces().length + 1];
				proxiedInterfaces[0] = Advised.class;
				System.arraycopy(this.advised.getProxiedInterfaces(), 0, proxiedInterfaces, 1, this.advised.getProxiedInterfaces().length);
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
			
		if (!Arrays.equals(aopr2.advised.getProxiedInterfaces(), this.advised.getProxiedInterfaces()))
			return false;
		
		if (!Arrays.equals(aopr2.advised.getAdvisors(), this.advised.getAdvisors()))
			return false;
			
		return true;
	}


	/**
	 * Putting CGLIB proxy creation in an inner class allows to avoid an AopProxy
	 * runtime dependency on CGLIB --> J2SE proxies work without cglib.jar then.
	 */
	private class CglibProxyFactory {

		private Object createProxy() {
			try {
				return Enhancer.enhance(advised.getTarget().getClass(), completeProxiedInterfaces(),
					new MethodInterceptor() {
						public Object intercept(Object handler, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
							return invoke(handler, method, objects);
						}
					}
				);
			}
			catch (CodeGenerationException ex) {
				throw new AspectException("Couldn't generate CGLIB subclass of class '" + advised.getTarget().getClass() + "': " +
						"Common causes of this problem include using a final class, or a non-visible class", ex);
			}
		}
	}

}
