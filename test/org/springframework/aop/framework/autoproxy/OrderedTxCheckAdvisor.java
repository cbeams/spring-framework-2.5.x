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

package org.springframework.aop.framework.autoproxy;

import java.lang.reflect.Method;

import org.springframework.aop.framework.CountingBeforeAdvice;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * Before advisor that allow us to manipulate ordering to check
 * that superclass sorting works correctly.
 * It doesn't actually <i>do</i> anything except count
 * method invocations and check for presence of transaction context.
 * <br>Matches setters.
 * @author Rod Johnson
 * @version $Id: OrderedTxCheckAdvisor.java,v 1.4 2004-03-18 03:01:14 trisberg Exp $
 */
public class OrderedTxCheckAdvisor extends StaticMethodMatcherPointcutAdvisor implements Ordered, InitializingBean {

	/** Unordered by default */
	private int order = Integer.MAX_VALUE;

	/**
	 * Should we insist on the presence of a transaction attribute
	 * or refuse to accept one?
	 */
	private boolean requireTransactionContext = false;

	private class TxCountingBeforeAdvice extends CountingBeforeAdvice {
		public void before(Method m, Object[] args, Object target) throws Throwable {
			// Do transaction checks
			if (requireTransactionContext) {
				TransactionInterceptor.currentTransactionStatus();
			}
			else {
				try {
					TransactionInterceptor.currentTransactionStatus();
					throw new RuntimeException("Shouldn't have a transaction");
				}
				catch (NoTransactionException ex) {
					// This is Ok
				}
			}
			super.before(m, args, target);
		}
	}


	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @return
	 */
	public boolean isRequireTransactionContext() {
		return requireTransactionContext;
	}

	/**
	 * @param b
	 */
	public void setRequireTransactionContext(boolean b) {
		requireTransactionContext = b;
	}

	public CountingBeforeAdvice getCountingBeforeAdvice() {
		return (CountingBeforeAdvice) getAdvice();
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		setAdvice(new TxCountingBeforeAdvice());
	}

	/**
	 * @see org.springframework.aop.support.StaticMethodMatcherPointcutBeforeAdvisor#matches(java.lang.reflect.Method, java.lang.Class)
	 */
	public boolean matches(Method m, Class targetClass) {
		return m.getName().startsWith("set");
	}

}
