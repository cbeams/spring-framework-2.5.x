/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import org.aopalliance.intercept.Interceptor;

import org.springframework.aop.Advisor;

/**
 * Interface to be implemented by classes that hold the configuration
 * of a factory of AOP proxies. This configuration includes
 * the Interceptors and Advisors, and the proxied interfaces.
 *
 * <p>Any AOP proxy obtained from Spring can be cast to this interface to allow
 * manipulation of its AOP advice.
 *
 * @author Rod Johnson
 * @since 13-Mar-2003
 * @version $Id: Advised.java,v 1.4 2003-11-21 22:45:29 jhoeller Exp $
 * @see org.springframework.aop.framework.AdvisedSupport
 */
public interface Advised {
	
	/**
	 * Get whether the factory should expose the AOP Alliance method invocation as a ThreadLocal
	 * @return whether the factory should expose the AOP Alliance method invocation as a ThreadLocal
	 * @see AopContext
	 */
	boolean getExposeInvocation();
	
	/**
	 * Get whether the factory should expose the proxy as a ThreadLocal. 
	 * This can be necessary if a target object needs to invoke a method on itself
	 * benefitting from advice. (If it invokes a method on <code>this</code> no advice
	 * will apply.) Getting the proxy is analogous to an EJB calling getEJBObject().
	 * @return whether the factory should expose the proxy as a ThreadLocal
	 * @see AopContext
	 */
	boolean getExposeProxy();
	
	/**
	 * Return the Advisors applying to this proxy.
	 * @return a list of Advisors applying to this proxy. Cannot return null,
	 * but may return the empty array.
	 */
	Advisor[] getAdvisors();
	
	/**
	 * Return the interfaces proxied by the AOP proxy. Will not
	 * include the target class, which may also be proxied.
	 * @return the interfaces proxied by the AOP proxy
	 */
	Class[] getProxiedInterfaces();
	
	/**
	 * Return whether this interface is proxied
	 * @param intf interface to test
	 * @return whether the interface is proxied
	 */
	boolean isInterfaceProxied(Class intf);
	
	/**
	 * Add the given interceptor to the tail of the advice (interceptor) chain.
	 * This will be wrapped in a DefaultInterceptionAroundAdvisor with a pointcut
	 * that always applies, and returned from the getAdvisors() method in this
	 * wrapped form.
	 * @param interceptor to add to the tail of the chain
	 * @see #addInterceptor(int, Interceptor)
	 */
	void addInterceptor(Interceptor interceptor);

	/**
	 * Add an interceptor at the specified position in the interceptor chain.
	 * @param pos index from 0 (head)
	 * @param interceptor interceptor to add at the specified position in the
	 * interceptor chain
	 */
	void addInterceptor(int pos, Interceptor interceptor);
	
	
	/** 
	 * Add an Advisor at the end of the advisor chain
	 * @param advisor Advisor to add to the end of the chain
	 */
	void addAdvisor(Advisor advisor);

	/** 
	 * Add an Advisor at the specified position in the chain
	 * @param advisor advisor to add at the specified position in the chain
	 * @param pos position in chain (0 is head). Must be valid.
	 */
	void addAdvisor(int pos, Advisor advisor);
	
	/**
	 * Remove the interceptor
	 * @param interceptor to remove
	 * @return true if the interceptor was found and removed,
	 * otherwise false
	 */
	boolean removeInterceptor(Interceptor interceptor);
	
	/**
	 * Can return null if there is no target, in which case interfaces and
	 * advice supply all behaviour. Returns true if we have
	 * a target interceptor. A target interceptor must be the last
	 * interceptor in the chain.
	 * @return Object
	 */
	Object getTarget();
	
	/**
	 * Should we proxy the target class as well as any interfaces?
	 * Can use this to force CGLIB proxying.
	 * @return whether we proxy the target class as well as any interfaces
	 */
	boolean getProxyTargetClass();
	
	/**
	 * As toString() will normally pass to the target, 
	 * this returns the equivalent for the ProxyConfig
	 * @return a string description of the proxy configuration
	 */
	String toProxyConfigString();

}
