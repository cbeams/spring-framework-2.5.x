/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

import org.aopalliance.intercept.Interceptor;



/**
 *
 * @author Rod Johnson
 * @since 04-Apr-2003
 * @version $Id: InterceptionAroundAdvisor.java,v 1.1 2003-11-15 15:29:55 johnsonr Exp $
 */
public interface InterceptionAroundAdvisor extends InterceptionAdvisor, PointcutAdvisor {
	
	Interceptor getInterceptor(); 
	

}
