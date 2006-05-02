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
import java.io.Serializable;

import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.ClassFilter;
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
 * @see TransactionInterceptor
 * @see TransactionProxyFactoryBean
 */
public class TransactionAttributeSourceAdvisor extends AbstractPointcutAdvisor {
	
	private final TransactionAttributeSourcePointcut pointcut = new TransactionAttributeSourcePointcut();


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

		this.pointcut.setTransactionAttributeSource(interceptor.getTransactionAttributeSource());
	}

	public Pointcut getPointcut() {
		return this.pointcut;
	}

	public void setClassFilter(ClassFilter classFilter) {
		this.pointcut.setClassFilter(classFilter);
	}

	public static class TransactionAttributeSourcePointcut extends StaticMethodMatcherPointcut implements Serializable {

		private TransactionAttributeSource transactionAttributeSource;

		public void setTransactionAttributeSource(TransactionAttributeSource transactionAttributeSource) {
			this.transactionAttributeSource = transactionAttributeSource;
		}

		public boolean matches(Method method, Class targetClass) {
			Assert.notNull(this.transactionAttributeSource, "transactionAttributeSource is required");
			return (this.transactionAttributeSource.getTransactionAttribute(method, targetClass) != null);
		}

		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			TransactionAttributeSourcePointcut that = (TransactionAttributeSourcePointcut) o;

			if (transactionAttributeSource != null ? !transactionAttributeSource.equals(that.transactionAttributeSource) : that.transactionAttributeSource != null) {
				return false;
			}

			return true;
		}

		public int hashCode() {
			return (transactionAttributeSource != null ? transactionAttributeSource.hashCode() : 0);
		}
	}
}
