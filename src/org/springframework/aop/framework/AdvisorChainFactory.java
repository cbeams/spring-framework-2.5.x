/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Factory for advisor chains.
 * @author Rod Johnson
 * @version $Id: AdvisorChainFactory.java,v 1.1 2003-11-28 11:17:17 johnsonr Exp $
 */
public interface AdvisorChainFactory {
	
	/**
	 * Return a list of Interceptor and InterceptorAndDynamicMethodMatcher
	 * @param pc
	 * @param proxy
	 * @param method
	 * @return
	 */
	List getInterceptorsAndDynamicInterceptionAdvice(Advised pc, Object proxy, Method method, Class targetClass);
	
	/**
	 * Cache state based on this Advised instance.
	 * Clear any existing state.
	 */
	void refresh(Advised pc);
	

}
