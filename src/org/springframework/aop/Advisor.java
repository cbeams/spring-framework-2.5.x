/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop;


/**
 * Base interface for advice. InterceptionAdvice and IntroductionAdvice
 * are the allowed subclasses.
 * 
 * @version $Id: Advisor.java,v 1.1 2003-11-15 15:29:55 johnsonr Exp $
 */
public abstract interface Advisor {
	
	
	// Aspect getAspect();
	
	/**
	 * Is this advice 
	 */
	boolean isPerInstance();

}
