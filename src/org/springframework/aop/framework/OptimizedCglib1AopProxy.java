/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.CodeGenerationException;
import net.sf.cglib.Enhancer;
import net.sf.cglib.MethodFilter;
import net.sf.cglib.MethodProxy;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.ReflectionUtils;

/**
 * InvocationHandler implementation for the Spring AOP framework,
 * based on CGLIB1 proxies.
 *
 * <p>Objects of this type should be obtained through proxy factories,
 * configured by a AdvisedSupport implementation. This class is internal
 * to the Spring framework and need not be used directly by client code.
 *
 * <p>Proxies created using this class can be threadsafe if the
 * underlying (target) class is threadsafe.
 * 
 * 
 * CGLIB method filter that can be used to achieve selective overrides.
 * Methods with no advice will not be overridden. This optimization isn't enabled
 * by default on AdvisedSupport, as it can break dynamic advice updates, but
 * it produces around 2.5x performance boost on methods with no advice,
 * so it's strongly recommend to enable it on AdvisedSupport for performance critical
 * applications.
	
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: OptimizedCglib1AopProxy.java,v 1.3 2003-12-05 15:19:24 johnsonr Exp $
 * @see net.sf.cglib.Enhancer
 */
final class OptimizedCglib1AopProxy extends Cglib1AopProxy implements MethodFilter {
	
	/**
	 * This class doesn't work with a dynamic TargetSource, so we can
	 * cache the target. We'll set this later to the enhanced class.
	 */
	private Object target;
	
	private final Class targetClass;
	
	/**
	 * 
	 * @throws AopConfigException if the config is invalid. We try
	 * to throw an informative exception in this case, rather than let
	 * a mysterious failure happen later.
	 */
	protected OptimizedCglib1AopProxy(AdvisedSupport config) throws AopConfigException {
		super(config);
		
		if (!config.getTargetSource().isStatic()) {
			throw new AopConfigException("Can only u8sed OptimizedCglibAopProxy with static target source");
		}

		this.targetClass = advised.getTargetSource().getTargetClass();
		try {
			// Will be changed later
			this.target = advised.getTargetSource().getTarget();
		}
		catch (Exception ex) {
			throw new AopConfigException("Cannot get target from static TargetSource", ex);
		}
	}
	
	/**
	 * Implementation of InvocationHandler.invoke.
	 * Callers will see exactly the exception thrown by the target, unless a hook
	 * method throws an exception.
	 * We override this as we can achieve minor optimizations from knowing we can't
	 * have a dynamic target source.
	 *
	 * @see net.sf.cglib.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.MethodProxy)
	 */
	public final Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
	
		MethodInvocation invocation = null;
		MethodInvocation oldInvocation = null;
		Object oldProxy = null;
		boolean setInvocationContext = false;
		boolean setProxyContext = false;	
		
		try {
			// Try special rules for equals() method and implementation of the
			// ProxyConfig AOP configuration interface
			if (isEqualsMethod(method)) {
				// What if equals throws exception!?

				// This class implements the equals() method itself
				return new Boolean(equals(args[0]));
			}
			else if (Advised.class.equals(method.getDeclaringClass())) {
				// Service invocations on ProxyConfig with the proxy config
				return method.invoke(this.advised, args);
			}
			
			Object retVal = null;
			
			List chain = advised.getAdvisorChainFactory().getInterceptorsAndDynamicInterceptionAdvice(this.advised, proxy, method, targetClass);
			
			// We need to create a method invocation...
			// Chain can't be empty as empty chains were optimized out earlier...
			
			invocation = new OptimizedCglibMethodInvocation(proxy, target, targetClass, method, args, 
							targetClass, chain, methodProxy);

		
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
			// Never need to release			
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
	}
	
	protected Object createProxy() {
		try {
			Object cglibProxy = Enhancer.enhance(advised.getTargetSource().getTargetClass(), 
					AopProxyUtils.completeProxiedInterfaces(advised),
					this,
					null, 	// ClassLoader: use default
					null,	// Don't worry about serialization and writeReplace for now
					this	// MethodFilter
			);
			
			 //	If we're doing fancy selective override stuff we must switch
			 // the target to the CGLIB instance completely, or we'll have both the old
			 // and new targets being invoked in a horrible mix
			 logger.info("Selectively overriding methods with CGLIB to maximize performance");
			 // We failed to override one method, sp we need to change the target to
			 // the CGLIB proxy
			 // Copy all the private fields. If we're going to do selective overriding
			 // through a method filter, it's crucial that the new instance of the CGLIB-enhanced
			 // class has the same state as the target
			 ReflectionUtils.shallowCopyFieldState(target, cglibProxy);
			 this.target = cglibProxy;
			 
			 return cglibProxy;
		}
		catch (CodeGenerationException ex) {
			throw new AspectException("Couldn't generate CGLIB subclass of class '" + advised.getTargetSource().getTargetClass() + "': " +
					"Common causes of this problem include using a final class, or a non-visible class", ex);
		}
	}
	

	/**
	 * @see net.sf.cglib.MethodFilter#accept(java.lang.reflect.Member)
	 */
	public boolean accept(Member member) {
		if (!(member instanceof Method))
			return false;
		Method method = (Method) member;
		
		// We must always proxy equals, to direct calls to this
		if (isEqualsMethod(method))
			return true;
		
		// Proxy is not yet available, but that shouldn't matter
		List chain = advised.getAdvisorChainFactory().getInterceptorsAndDynamicInterceptionAdvice(advised, null, (Method) member, targetClass);
		boolean  haveAdvice = !chain.isEmpty();
	
		if (haveAdvice) {
			logger.info("CGLIB proxy for " + targetClass.getName() + 
						" WILL override " + member);
		}
		else {
			logger.debug("Chain is empty for " + member + "; will NOT override");
		}
		return haveAdvice;
	}
}
