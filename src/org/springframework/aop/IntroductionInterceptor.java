/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * Subinterface of AOP Alliance MethodInterceptor that allows additional 
 * interfaces to be implemented by the interceptor, and available
 * via a proxy using that interceptor. This is a fundamental 
 * AOP concept called <b>introduction</b>.
 * Introductions are often <b>mixins</b>, enabling the building
 * of composite objects that can achieve many of the goals of multiple inheritance
 * in Java.
 * @see org.springframework.aop.IntroductionAdvice
 * @author Rod Johnson
 * @version $Id: IntroductionInterceptor.java,v 1.2 2003-11-15 18:42:49 johnsonr Exp $
 */
public interface IntroductionInterceptor extends MethodInterceptor {
	
	/**
	 * Does this IntroductionInterceptor implement the given interface?
	 * @param intf interface to check
	 */
	boolean implementsInterface(Class intf);

}
