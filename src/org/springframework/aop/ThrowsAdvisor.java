/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;


/**
 * Advisor that delivers <b>throws</b> advice.
 * Throws advice is targeted by a pointcut.
 * <p>Throws advice is less general than around advice. Anything that
 * can be done with throws advice can be done with around advice.
 * However, there is value in offering throws advice, as it
 * provides a simpler programming model (no need to invoke the next
 * in a chain of interceptors); and strong typing for exceptions.
 * <br>
 * A throws advice object should implement methods of the form:
 * afterThrowing([Method], [args], [target], Throwable subclass) 
 * The first three arguments are optional, and only useful if
 * we want further information about the joinpoint, as in AspectJ
 * <b>after throwing</b> advice.
 * The advisor re-raises the exception when it's done.
 * @author Rod Johnson
 * @version $Id: ThrowsAdvisor.java,v 1.3 2004-01-14 10:26:35 johnsonr Exp $
 */
public interface ThrowsAdvisor extends PointcutAdvisor {
	
	/**
	 * @return the ThrowsAdvice that should be executed if the pointcut
	 * is matched. Methods are invoked on this object by reflection, allowing
	 * strong typing.
	 */
	ThrowsAdvice getThrowsAdvice();

}
