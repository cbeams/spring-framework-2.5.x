/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;

/**
 * Interface allowing extension to the Spring AOP framework to allow
 * handling of new Advisors and Advice types.
 * Implementing objects can wrap Advice objects in an Advisor,
 * or convert an arbitrary Advisor type to InterceptionAroundAdvisor
 * for use in the Spring AOP framework.
 * <b>Typically an implementation will understand an Advice type
 * and the matching AdvisorWrapper: e.g. MethodBeforeAdvice and BeforeAdvisor.
 * @author Rod Johnson
 * @version $Id: AdvisorAdapter.java,v 1.1 2003-12-11 14:49:49 johnsonr Exp $
 */
public interface AdvisorAdapter {
	
	boolean supportsAdvisor(Advisor advisor);
	
	boolean supportsAdvice(Object advice);
	
	/**
	 * @param advice the supportsAdvice() method must have returned
	 * true on this object
	 * @return
	 */
	Advisor wrap(Object advice);
	
	/**
	 * Don't worry about pointcut
	 * @param advisor the supportsAdvisor() method must have
	 * returned true on this object
	 * @return
	 */
	Interceptor getInterceptor(Advisor advisor);

}
