/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;


/**
 * Advisor that delivers <b>after returning</b> advice:
 * executed on method execution without an exception being thrown.
 * Such advice is targeted by a pointcut.
 * After advice is less general than around advice. Anything that
 * can be done with after returning advice can be done with around advice.
 * However, there is still value in offering after advice, as it
 * provides a simpler programming model (no need to invoke the next
 * in a chain of interceptors).
 * @author Rod Johnson
 * @version $Id: AfterReturningAdvisor.java,v 1.1 2004-01-05 18:47:00 johnsonr Exp $
 */
public interface AfterReturningAdvisor extends Advisor, PointcutAdvisor {
	
	/**
	 * @return the after advice that should be executed if the pointcut
	 * is matched
	 */
	AfterReturningAdvice getAfterReturningAdvice();

}
