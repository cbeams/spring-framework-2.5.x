/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;

import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.StaticMethodMatcherPointcutAroundAdvisor;

/**
 * InterceptionAroundAdvisor driven by a TransactionAttributeSource.
 * Used to exclude TransactionInterceptor from methods that are non transactional.
 * Because the AOP framework caches advice calculations, this is normally faster
 * than just letting the TransactionInterceptor run and find out itself
 * that it has no work to do.
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean
 * @author Rod Johnson
 * @version $Id: TransactionAttributeSourceTransactionAroundAdvisor.java,v 1.2 2003-12-03 13:58:24 johnsonr Exp $
 */
public class TransactionAttributeSourceTransactionAroundAdvisor extends StaticMethodMatcherPointcutAroundAdvisor {
	
	private TransactionAttributeSource transactionAttributeSource;
	
	public TransactionAttributeSourceTransactionAroundAdvisor(TransactionInterceptor ti) {
		super(ti);
		if (ti.getTransactionAttributeSource() == null)
			throw new AopConfigException("Cannot construct a TransactionAttributeSourceTransactionAroundAdvisor using a TransactionInterceptor that has no TransactionAttributeSource configured");
		this.transactionAttributeSource = ti.getTransactionAttributeSource();
	}

	/**
	 * @see org.springframework.aop.MethodMatcher#matches(java.lang.reflect.Method, java.lang.Class)
	 */
	public boolean matches(Method m, Class targetClass) {
		return transactionAttributeSource.getTransactionAttribute(m, targetClass) != null;
	}

}
