/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.BeforeAdvisor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.support.DefaultBeforeAdvisor;

/**
 * 
 * @author Rod Johnson
 * @version $Id: BeforeAdviceAdapter.java,v 1.1 2003-12-11 14:51:37 johnsonr Exp $
 */
class BeforeAdviceAdapter implements AdvisorAdapter {

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#supportsAdvisor(org.springframework.aop.Advisor)
	 */
	public boolean supportsAdvisor(Advisor advisor) {
		return advisor instanceof BeforeAdvisor;
	}

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#supportsAdvice(java.lang.Object)
	 */
	public boolean supportsAdvice(Object advice) {
		return advice instanceof MethodBeforeAdvice;
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
		BeforeAdvisor ba = (BeforeAdvisor) advisor;
		MethodBeforeAdvice advice = (MethodBeforeAdvice) ba.getBeforeAdvice();
		return new MethodBeforeAdviceInterceptor(advice) ;
	}

}
