/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.MethodAfterReturningAdvice;

/**
 * Adapter to enable AfterReturningAdvisor and MethodAfterReturningAdvice
 * to be used in the Spring AOP framework.
 * <br>This involves wrapping these advice types in interceptors.
 * @author Rod Johnson
 * @version $Id: AfterReturningAdviceAdapter.java,v 1.4 2004-02-22 10:39:01 johnsonr Exp $
 */
class AfterReturningAdviceAdapter implements AdvisorAdapter {

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#supportsAdvice(java.lang.Object)
	 */
	public boolean supportsAdvice(Object advice) {
		return advice instanceof MethodAfterReturningAdvice;
	}


	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#getInterceptor(org.springframework.aop.Advisor)
	 */
	public Interceptor getInterceptor(Advisor advisor) {
		MethodAfterReturningAdvice advice = (MethodAfterReturningAdvice) advisor.getAdvice();
		return new AfterReturningAdviceInterceptor(advice);
	}

}
