/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;

/**
 * Interface for registries of AdvisorAdapters.
 * <br><i>This is an SPI interface, that should not need to be implemented
 * by any Spring user.</i>
 * @author Rod Johnson
 * @version $Id: AdvisorAdapterRegistry.java,v 1.2 2003-12-11 16:51:26 johnsonr Exp $
 */
public interface AdvisorAdapterRegistry {
	
	/**
	 * Return an Advisor wrapping the given advice
	 * @param advice object that should be an advice, such as
	 * BeforeAdvice or ThrowsAdvice.
	 * @return an Advisor wrapping the given advice. Never
	 * returns null. If the advice parameter is an Advisor, return
	 * it.
	 * @throws UnknownAdviceTypeException if no registered AdvisorAdapter
	 * can wrap the supposed advice
	 */
	Advisor wrap(Object advice) throws UnknownAdviceTypeException;
	
	/**
	 * Return an AOP Alliance Interceptor to allow use of the given
	 * Advisor in an interception-based framework. 
	 * Don't worry about the pointcut associated with the Advisor,
	 * if it's a PointcutAdvisor: just return an interceptor
	 * @param advisor Advisor to find an interceptor for
	 * @return an Interceptor to expose this Advisor's behaviour
	 * @throws UnknownAdviceTypeException if the Advisor type is
	 * not understood by any registered AdvisorAdapter.
	 */
	Interceptor getInterceptor(Advisor advisor) throws UnknownAdviceTypeException;
	
	/**
	 * Register the given AdvisorAdapter. Note that it is not necessary to register
	 * adapters for InterceptionAroundAdvice or AOP Alliance Interceptors:
	 * these must be automatically recognized by an AdvisorAdapterRegistry
	 * implementation.
	 * @param adapter AdvisorAdapter that understands particular Advisor
	 * and Advice types. 
	 */
	void registerAdvisorAdapter(AdvisorAdapter adapter);

}
