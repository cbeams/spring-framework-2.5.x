/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.aop.ThrowsAdvisor;
import org.springframework.aop.support.DefaultThrowsAdvisor;

/**
 * 
 * @author Rod Johnson
 * @version $Id: ThrowsAdviceAdapter.java,v 1.1 2003-12-11 14:51:37 johnsonr Exp $
 */
class ThrowsAdviceAdapter implements AdvisorAdapter {

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#supportsAdvisor(org.springframework.aop.Advisor)
	 */
	public boolean supportsAdvisor(Advisor advisor) {
		return advisor instanceof ThrowsAdvisor;
	}

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#supportsAdvice(java.lang.Object)
	 */
	public boolean supportsAdvice(Object advice) {
		return advice instanceof ThrowsAdvice;
	}

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#wrap(java.lang.Object)
	 */
	public Advisor wrap(Object advice) {
		return new DefaultThrowsAdvisor((ThrowsAdvice) advice);
	}

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#getInterceptor(org.springframework.aop.Advisor)
	 */
	public Interceptor getInterceptor(Advisor advisor) {
		return new ThrowsAdviceInterceptor(((ThrowsAdvisor) advisor).getThrowsAdvice());
	}

}
