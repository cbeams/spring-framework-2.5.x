/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

/**
 * Superinterface for all Advisors that are driven by a pointcut.
 * @author Rod Johnson
 * @version $Id: PointcutAdvisor.java,v 1.1 2003-11-15 15:29:55 johnsonr Exp $
 */
public interface PointcutAdvisor extends Advisor {
	
	Pointcut getPointcut();

}
