/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

import org.aopalliance.intercept.Interceptor;


/**
 * Advisor that delivers <b>around</b> advice via an AOP Alliance interceptor.
 * Such advice is targeted by a pointcut.
 * @author Rod Johnson
 * @since 04-Apr-2003
 * @version $Id: InterceptionAroundAdvisor.java,v 1.2 2003-11-16 21:53:11 johnsonr Exp $
 */
public interface InterceptionAroundAdvisor extends InterceptionAdvisor, PointcutAdvisor {
	
	/**
	 * @return the pointcut that identifies joinpoints eligible for this
	 * around advice.
	 */
	Interceptor getInterceptor(); 
	

}
