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
 * @version $Id: AdvisorChainFactory.java,v 1.3 2003-12-30 01:07:10 jhoeller Exp $
 */
public interface AdvisorChainFactory extends AdvisedSupportListener {
	
	/**
	 * Return a list of Interceptor and InterceptorAndDynamicMethodMatcher.
	 */
	List getInterceptorsAndDynamicInterceptionAdvice(Advised pc, Object proxy, Method method, Class targetClass);

}
