/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;


/**
 * Advisor that delivers <b>throws</b> advice.
 * Such advice is targeted by a pointcut.
 * Throws advice is less general than around advice. Anything that
 * can be done with throws advice can be done with around advice.
 * However, there is still value in offering throws advice, as it
 * provides a simpler programming model (no need to invoke the next
 * in a chain of interceptors); and strong typing for exceptions.
 * <br>
 * A throws advice object should implement methods of the form:
 * afterThrowing([Method], [args], [target], Throwable subclass) 
 * The first three arguments are optional, and only useful if
 * we want further information about the joinpoint, as in AspectJ
 * <b>after throwing</b> advice.
 * The advice re-raises the exception when it's done.
 * @author Rod Johnson
 * @version $Id: ThrowsAdvisor.java,v 1.1 2003-12-05 16:28:26 johnsonr Exp $
 */
public interface ThrowsAdvisor extends Advisor, PointcutAdvisor {
	
	/**
	 * @return the ThrowsAdvice that should be executed if the pointcut
	 * is matched. Methods are invoked on this object by reflection, allowing
	 * strong typing.
	 */
	Object getThrowsAdvice();

}
