/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * 
 * @author Rod Johnson
 * @version $Id: ThrowsAdviceAdapter.java,v 1.2 2004-02-22 09:48:55 johnsonr Exp $
 */
class ThrowsAdviceAdapter implements AdvisorAdapter {

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
		return new DefaultPointcutAdvisor((ThrowsAdvice) advice);
	}

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#getInterceptor(org.springframework.aop.Advisor)
	 */
	public Interceptor getInterceptor(Advisor advisor) {
		return new ThrowsAdviceInterceptor(advisor.getAdvice());
	}

}
