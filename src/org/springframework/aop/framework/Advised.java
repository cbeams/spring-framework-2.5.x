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
 * the interceptors and Advisors, and the proxied interfaces.
 * <p>Any AOP proxy obtained from Spring can be cast to ProxyConfig to allow
 * manipulation of its AOP advice.
 * 
 * @author Rod Johnson
 * @since 13-Mar-2003
 * @version $Id: Advised.java,v 1.1 2003-11-15 15:30:14 johnsonr Exp $
 */
public interface Advised {
	
	/**
	 * Get whether the factory should expose the AOP Alliance method invocation as a ThreadLocal
	 * @return whether the factory should expose the AOP Alliance method invocation as a ThreadLocal
	 */
	boolean getExposeInvocation();
	
	/**
	 * Return the Advisors applying to this proxy
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
	 * 
	 * Add the given interceptor to the tail of the interceptor chain.
	 * This will be wrapped in an AlwaysInvoked method pointcut and returned
	 * in an AlwaysInvoked wrapper from the getMethodPointcuts() method.
	 * @param interceptor to add to the tail of the chain.
	 */
	void addInterceptor(Interceptor interceptor);
	
	
	/**
	 * Add an interceptor at the specified position in the interceptor chain.
	 * @param pos index from 0 (head).
	 * @param interceptor interceptor to add at the specified position in the
	 * interceptor chain.
	 */
	void addInterceptor(int pos, Interceptor interceptor);
	
	
	/** TODO fix Javadoc
	 * Add an aspect ad the end of the interceptor chain
	 * @param aspect aspect to add to the end of the chain
	 */
	void addAdvisor(Advisor advice);

	/** TODO fix Javadoc
	 * Add a pointcut at the specified position in the chain
	 * @param aspect to add at the specified position in the chain
	 * TODO does position parameter mean anything now?
	 */
	void addAdvisor(int pos, Advisor advice);
	
	/**
	 * Remove the interceptor
	 * @param interceptor
	 * @return if the interceptor was found and removed
	 */
	boolean removeInterceptor(Interceptor interceptor);
	
	/**
	 * Can return null if now target. Returns true if we have
	 * a target interceptor. A target interceptor must be the last
	 * interceptor. Implementations should be efficient, as this
	 * will be invoked on each invocation.
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
	 * @return
	 */
	String toProxyConfigString();

}
