/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;

import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.core.Ordered;

/**
 * InterceptionAroundAdvisor driven by a TransactionAttributeSource.
 * Used to exclude TransactionInterceptor from methods that are non transactional.
 * Because the AOP framework caches advice calculations, this is normally faster
 * than just letting the TransactionInterceptor run and find out itself
 * that it has no work to do.
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean
 * @author Rod Johnson
 * @version $Id: TransactionAttributeSourceTransactionAroundAdvisor.java,v 1.5 2004-03-18 02:46:05 trisberg Exp $
 */
public class TransactionAttributeSourceTransactionAroundAdvisor extends StaticMethodMatcherPointcutAdvisor implements Ordered {
	
	/**
	 * Most advice will want to run within a transaction context,
	 * so we set the order value to be quite high. Other advisors
	 * can use a higher value than this if want to ensure that they
	 * always run within a transaction context. 
	 * <br>Order value applies only when ordering is not 
	 * specified in configuration: for example,
	 * it applies when using an AdvisorAutoProxyCreator.
	 */
	public static final int ORDER_VALUE = 10;
	
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

	/**
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public int getOrder() {
		return ORDER_VALUE;
	}

}
