/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;

/**
 * Factory for method invocations.
 * @author Rod Johnson
 * @version $Id: MethodInvocationFactory.java,v 1.1 2003-11-12 12:30:20 johnsonr Exp $
 */
public interface MethodInvocationFactory {
	
	MethodInvocationImpl getMethodInvocation(ProxyConfig pc, Object proxy, Method method, Object[] args);
	
	/**
	 * Clear state
	 *
	 */
	void clear();

}
