/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

/**
 * Advisor that performs an AOP <b>introduction</b> through an interceptor.
 * Introduction is the implementation of additional interfaces 
 * (not implemented by a target) via AOP advice.
 * @author Rod Johnson
 * @since 04-Apr-2003
 * @see org.springframework.aop.IntroductionInterceptor
 * @version $Id: InterceptionIntroductionAdvisor.java,v 1.2 2003-11-16 21:51:04 johnsonr Exp $
 */
public interface InterceptionIntroductionAdvisor extends InterceptionAdvisor {
	
	/**
	 * @return the filter determining which target classes this introduction
	 * should apply to. The class part of a pointcut. Note that method matching
	 * doesn't make sense to introductions.
	 */
	ClassFilter getClassFilter();
	
	/**
	 * @return the interceptor that handles the introduced interface(s)
	 */
	IntroductionInterceptor getIntroductionInterceptor();
	
	/**
	 * @return the additional interfaces introduced by this Advisor
	 */
	Class[] getInterfaces();
	

}
