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

/**
 * Advisor driven by a TransactionAttributeSource, used to exclude
 * a TransactionInterceptor from methods that are non-transactional.
 *
 * <p>Because the AOP framework caches advice calculations, this is normally
 * faster than just letting the TransactionInterceptor run and find out
 * itself that it has no work to do.
 *
 * @author Rod Johnson
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean
 */
public class TransactionAttributeSourceAdvisor extends StaticMethodMatcherPointcutAdvisor {
	
	private TransactionAttributeSource transactionAttributeSource;
	
	public TransactionAttributeSourceAdvisor(TransactionInterceptor ti) {
		super(ti);
		if (ti.getTransactionAttributeSource() == null) {
			throw new AopConfigException("Cannot construct a TransactionAttributeSourceAdvisor using a " +
																	 "TransactionInterceptor that has no TransactionAttributeSource configured");
		}
		this.transactionAttributeSource = ti.getTransactionAttributeSource();
	}

	public boolean matches(Method m, Class targetClass) {
		return (this.transactionAttributeSource.getTransactionAttribute(m, targetClass) != null);
	}

}
