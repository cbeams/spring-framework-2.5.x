/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;


/**
 * Advisor that delivers <b>before</b> advice.
 * Such advice is targeted by a pointcut.
 * Before advice is less general than around advice. Anything that
 * can be done with before advice can be done with around advice.
 * However, there is value in offering before advice, as it
 * provides a simpler programming model (no need to invoke the next
 * in a chain of interceptors).
 * @author Rod Johnson
 * @version $Id: BeforeAdvisor.java,v 1.2 2004-01-14 10:31:38 johnsonr Exp $
 */
public interface BeforeAdvisor extends PointcutAdvisor {
	
	/**
	 * @return the BeforeAdvice that should be executed if the pointcut
	 * is matched
	 */
	BeforeAdvice getBeforeAdvice();

}
