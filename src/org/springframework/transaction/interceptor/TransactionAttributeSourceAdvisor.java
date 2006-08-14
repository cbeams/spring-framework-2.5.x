/*
 * Copyright 2002-2006 the original author or authors.
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

import java.io.Serializable;
import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.util.ObjectUtils;

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
	
	private TransactionInterceptor transactionInterceptor;

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
		this.transactionInterceptor = interceptor;
	}

	/**
	 * Set the {@link ClassFilter} to use for this pointcut.
	 * Default is {@link ClassFilter#TRUE}.
	 */
	public void setClassFilter(ClassFilter classFilter) {
		this.pointcut.setClassFilter(classFilter);
	}


	public Advice getAdvice() {
		return this.transactionInterceptor;
	}

	public Pointcut getPointcut() {
		return this.pointcut;
	}


	/**
	 * Inner class that implements a Pointcut that matches if the underlying
	 * TransactionAttributeSource has an attribute for a given method.
	 */
	private class TransactionAttributeSourcePointcut extends StaticMethodMatcherPointcut implements Serializable {

		private TransactionAttributeSource getTransactionAttributeSource() {
			return (transactionInterceptor != null ? transactionInterceptor.getTransactionAttributeSource() : null);
		}

		public boolean matches(Method method, Class targetClass) {
			TransactionAttributeSource tas = getTransactionAttributeSource();
			return (tas != null && tas.getTransactionAttribute(method, targetClass) != null);
		}

		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof TransactionAttributeSourcePointcut)) {
				return false;
			}
			TransactionAttributeSourcePointcut otherPc = (TransactionAttributeSourcePointcut) other;
			return ObjectUtils.nullSafeEquals(getTransactionAttributeSource(), otherPc.getTransactionAttributeSource());
		}

		public int hashCode() {
			return TransactionAttributeSourcePointcut.class.hashCode();
		}

		public String toString() {
			return getClass().getName() + ": " + getTransactionAttributeSource();
		}
	}

}
