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
 * @version $Id: MethodInvocationFactory.java,v 1.4 2003-11-15 15:30:14 johnsonr Exp $
 */
public interface MethodInvocationFactory {
	
	MethodInvocation getMethodInvocation(Advised pc, Object proxy, Method method, Object[] args);
	
	/**
	 * Cache state based on ProxyConfig.
	 * Clear any existing state.
	 */
	void refresh(Advised pc);

}
