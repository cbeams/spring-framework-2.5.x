/*
 * Copyright 2002-2005 the original author or authors.
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
import org.springframework.util.Assert;

/**
 * Advisor driven by a TransactionAttributeSource, used to exclude
 * a TransactionInterceptor from methods that are non-transactional.
 *
 * <p>Because the AOP framework caches advice calculations, this is normally
 * faster than just letting the TransactionInterceptor run and find out
 * itself that it has no work to do.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean
 */
public class TransactionAttributeSourceAdvisor extends StaticMethodMatcherPointcutAdvisor {
	
	private TransactionAttributeSource transactionAttributeSource;


	/**
	 * Create a new TransactionAttributeSourceAdvisor.
	 */
	public TransactionAttributeSourceAdvisor() {
	}

	/**
	 * Create a new TransactionAttributeSourceAdvisor.
	 * @param interceptor the transaction interceptor to use for this advisor
	 */
	public TransactionAttributeSourceAdvisor(TransactionInterceptor interceptor) {
		setTransactionInterceptor(interceptor);
	}

	/**
	 * Set the transaction interceptor to use for this advisor.
	 */
	public void setTransactionInterceptor(TransactionInterceptor interceptor) {
		setAdvice(interceptor);
		if (interceptor.getTransactionAttributeSource() == null) {
			throw new AopConfigException(
					"Cannot construct a TransactionAttributeSourceAdvisor using a " +
					"TransactionInterceptor that has no TransactionAttributeSource configured");
		}
		this.transactionAttributeSource = interceptor.getTransactionAttributeSource();
	}


	public boolean matches(Method method, Class targetClass) {
		Assert.notNull(this.transactionAttributeSource, "transactionAttributeSource is required");
		return (this.transactionAttributeSource.getTransactionAttribute(method, targetClass) != null);
	}

}
