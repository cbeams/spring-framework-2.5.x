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
 * @version $Id: AdvisorChainFactory.java,v 1.2 2003-12-01 10:02:25 johnsonr Exp $
 */
public interface AdvisorChainFactory extends AdvisedSupportListener {
	
	/**
	 * Return a list of Interceptor and InterceptorAndDynamicMethodMatcher
	 * @param pc
	 * @param proxy
	 * @param method
	 * @return
	 */
	List getInterceptorsAndDynamicInterceptionAdvice(Advised pc, Object proxy, Method method, Class targetClass);
	

}
