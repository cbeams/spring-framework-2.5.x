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
 * <br>Typically an implementation will understand an Advice type
 * and the matching AdvisorWrapper: e.g. MethodBeforeAdvice and BeforeAdvisor.
 * <br>There is no need for most Spring users to implement this interface;
 * do so only if you need to introduce more Advisor or Advice types to
 * Spring.
 * @author Rod Johnson
 * @version $Id: AdvisorAdapter.java,v 1.2 2003-12-11 17:24:51 johnsonr Exp $
 */
public interface AdvisorAdapter {
	
	/**
	 * Does this adapter understand this Advisor? Is it valid to invoke
	 * the getInterceptor() method with this advisor as an argument.
	 * Most implementations will support only a single Advisor type,
	 * and simply do an <code>instanceof</code> test here.
	 * @param advisor advisor to test
	 * @return whether it's valid to invoke the getInterceptor() method
	 * on this object with the advisor as argument
	 */
	boolean supportsAdvisor(Advisor advisor);
	
	/**
	 * Does this adapter understand this advice object? 
	 * Is it valid to invoke the wrap() method with the
	 * given advice as an argument?
	 * @param advice Advice such as a BeforeAdvice. There is
	 * no common interface for Advices, partly because Spring 
	 * implements the AOP Alliance interfaces.
	 * @return whether this adapter understands the given advice object 
	 */
	boolean supportsAdvice(Object advice);
	
	/**
	 * Given the advice, return an Advisor wrapping it. If the
	 * Advisor is a PointcutAdvisor, it should always match.
	 * @param advice the supportsAdvice() method must have returned
	 * true on this object
	 * @return an Advisor wrapping this advice, that will apply
	 * to all invocations.
	 */
	Advisor wrap(Object advice);
	
	/**
	 * Return an AOP Alliance Interceptor exposing the behaviour of
	 * the given advice to an interception-based AOP framework.
	 * Don't worry about any Pointcut contained in the Advisor;
	 * the AOP framework will take care of checking the pointcut.
	 * @param advisor Advisor. the supportsAdvisor() method must have
	 * returned true on this object
	 * @return an AOP Alliance interceptor for this Advisor. There's
	 * no need to cache instances for efficiency, as the AOP framework
	 * caches advice chains.
	 */
	Interceptor getInterceptor(Advisor advisor);

}
