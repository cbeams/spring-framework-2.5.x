/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.AfterReturningAdvisor;
import org.springframework.aop.MethodAfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.support.DefaultBeforeAdvisor;

/**
 * 
 * @author Rod Johnson
 * @version $Id: AfterReturningAdviceAdapter.java,v 1.1 2004-01-05 18:47:00 johnsonr Exp $
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
		return new DefaultBeforeAdvisor((MethodBeforeAdvice) advice);
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
