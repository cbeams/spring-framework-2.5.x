/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * Subinterface of MethodInterceptor that allows additional 
 * interfaces to be implemented by the interceptor, and available
 * via a proxy using that interceptor. This is commonly 
 * referred to as <b>introduction</b>.
 * @see org.springframework.aop.IntroductionAdvice
 * @author Rod Johnson
 * @version $Id: IntroductionInterceptor.java,v 1.1 2003-11-11 18:31:51 johnsonr Exp $
 */
public interface IntroductionInterceptor extends MethodInterceptor {
	
	// TODO supports: bob
	// But enumerating may also be appropriate (Roger?)
	// could look at its interfaces and check if they're supported?
	
	/**
	 * Return the introduced interfaces added by this object
	 * @return Class[]
	 */
	//Class[] getIntroducedInterfaces();
	
	/**
	 * Does this IntroductionInterceptor implement the given interface?
	 * @param intf interface to check
	 */
	boolean implementsInterface(Class intf);

}
