/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

import org.springframework.aop.framework.AopConfigException;

/**
 * Superinterface for advisors that perform one or more
 * AOP <b>introductions</b>.
 * <br>This interface cannot be implemented directly; subinterfaces
 * must provide the advice type implementing the introduction.
 * <br>
 * Introduction is the implementation of additional interfaces 
 * (not implemented by a target) via AOP advice.
 * @author Rod Johnson
 * @since 04-Apr-2003
 * @see org.springframework.aop.IntroductionInterceptor
 * @version $Id: IntroductionAdvisor.java,v 1.2 2004-01-21 20:21:35 johnsonr Exp $
 */
public interface IntroductionAdvisor extends Advisor {
	
	/**
	 * @return the filter determining which target classes this introduction
	 * should apply to. The class part of a pointcut. Note that method matching
	 * doesn't make sense to introductions.
	 */
	ClassFilter getClassFilter();
	
	/**
	 * @return the additional interfaces introduced by this Advisor
	 */
	Class[] getInterfaces();
	
	/**
	 * Can the advised interfaces be implemented by the 
	 * introduction advice? Invoked before adding an IntroductionAdvisor.
	 * @throws AopConfigException if the advised interfaces can't
	 * be implemented by the introduction advice
	 */
	void validateInterfaces() throws AopConfigException;

}
