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
 * @version $Id: InterceptionIntroductionAdvisor.java,v 1.3 2004-01-15 10:43:20 johnsonr Exp $
 */
public interface InterceptionIntroductionAdvisor extends IntroductionAdvisor, InterceptionAdvisor {
	
	/**
	 * @return the interceptor that handles the introduced interface(s)
	 */
	IntroductionInterceptor getIntroductionInterceptor();

}
