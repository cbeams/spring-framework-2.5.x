/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.AfterReturningAdvisor;
import org.springframework.aop.MethodAfterReturningAdvice;
import org.springframework.aop.support.DefaultAfterReturningAdvisor;

/**
 * Adapter to enable AfterReturningAdvisor and MethodAfterReturningAdvice
 * to be used in the Spring AOP framework.
 * <br>This involves wrapping these advice types in interceptors.
 * @author Rod Johnson
 * @version $Id: AfterReturningAdviceAdapter.java,v 1.2 2004-01-06 10:14:51 johnsonr Exp $
 */
class AfterReturningAdviceAdapter implements AdvisorAdapter {

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#supportsAdvisor(org.springframework.aop.Advisor)
	 */
	public boolean supportsAdvisor(Advisor advisor) {
		return advisor instanceof AfterReturningAdvisor;
	}

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#supportsAdvice(java.lang.Object)
	 */
	public boolean supportsAdvice(Object advice) {
		return advice instanceof MethodAfterReturningAdvice;
	}

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#wrap(java.lang.Object)
	 */
	public Advisor wrap(Object advice) {
		return new DefaultAfterReturningAdvisor((MethodAfterReturningAdvice) advice);
	}

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#getInterceptor(org.springframework.aop.Advisor)
	 */
	public Interceptor getInterceptor(Advisor advisor) {
		AfterReturningAdvisor aa = (AfterReturningAdvisor) advisor;
		MethodAfterReturningAdvice advice = (MethodAfterReturningAdvice) aa.getAfterReturningAdvice();
		return new AfterReturningAdviceInterceptor(advice) ;
	}

}
