/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Factory for method invocations.
 * @author Rod Johnson
 * @version $Id: MethodInvocationFactory.java,v 1.2 2003-11-12 12:46:29 johnsonr Exp $
 */
public interface MethodInvocationFactory {
	
	MethodInvocation getMethodInvocation(ProxyConfig pc, Object proxy, Method method, Object[] args);
	
	/**
	 * Clear state
	 *
	 */
	void clear();

}
