/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.framework.adapter.AdvisorAdapter;

/**
 * 
 * @author Dmitriy Kopylenko
 * @version $Id: SimpleBeforeAdviceAdapter.java,v 1.1 2004-02-27 14:28:16 dkopylenko Exp $
 */
public class SimpleBeforeAdviceAdapter implements AdvisorAdapter {

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#supportsAdvice(java.lang.Object)
	 */
	public boolean supportsAdvice(Object advice) {
		return advice instanceof SimpleBeforeAdvice;
	}

	/**
	 * @see org.springframework.aop.framework.adapter.AdvisorAdapter#getInterceptor(org.springframework.aop.Advisor)
	 */
	public Interceptor getInterceptor(Advisor advisor) {
		SimpleBeforeAdvice advice = (SimpleBeforeAdvice) advisor.getAdvice();
		return new SimpleBeforeAdviceInterceptor(advice) ;
	}

}
