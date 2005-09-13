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

import java.util.Properties;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.transaction.PlatformTransactionManager;

/**
 * AOP Alliance MethodInterceptor providing declarative transaction
 * management using the common Spring transaction infrastructure.
 *
 * <p>Derives from the TransactionAspectSupport class. That class contains
 * the necessary calls into Spring's underlying transaction API:
 * subclasses such as this are responsible for calling superclass methods
 * such as <code>createTransactionIfNecessary</code> in the correct order,
 * in the event of normal invocation return or an exception.
 *
 * <p>TransactionInterceptors are thread-safe.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see TransactionProxyFactoryBean
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport
 * @see org.springframework.transaction.PlatformTransactionManager
 */
public class TransactionInterceptor extends TransactionAspectSupport implements MethodInterceptor {

	/**
	 * Create a new TransactionInterceptor.
	 * Transaction manager and transaction attributes still need to be set.
	 * @see #setTransactionManager
	 * @see #setTransactionAttributes(java.util.Properties)
	 * @see #setTransactionAttributeSource(TransactionAttributeSource)
	 */
	public TransactionInterceptor() {
	}

	/**
	 * Create a new TransactionInterceptor.
	 * @param ptm the transaction manager to perform the actual transaction management
	 * @param attributes the transaction attributes in properties format
	 * @see #setTransactionManager
	 * @see #setTransactionAttributes(java.util.Properties)
	 */
	public TransactionInterceptor(PlatformTransactionManager ptm, Properties attributes) {
		setTransactionManager(ptm);
		setTransactionAttributes(attributes);
	}

	/**
	 * Create a new TransactionInterceptor.
	 * @param ptm the transaction manager to perform the actual transaction management
	 * @param tas the attribute source to be used to find transaction attributes
	 * @see #setTransactionManager
	 * @see #setTransactionAttributeSource(TransactionAttributeSource)
	 */
	public TransactionInterceptor(PlatformTransactionManager ptm, TransactionAttributeSource tas) {
		setTransactionManager(ptm);
		setTransactionAttributeSource(tas);
	}


	public Object invoke(MethodInvocation invocation) throws Throwable {
		// Work out the target class: may be <code>null</code>.
		// The TransactionAttributeSource should be passed the target class
		// as well as the method, which may be from an interface
		Class targetClass = (invocation.getThis() != null) ? invocation.getThis().getClass() : null;
		
		// Create transaction if necessary.
		TransactionInfo txInfo = createTransactionIfNecessary(invocation.getMethod(), targetClass);

		Object retVal = null;
		try {
			// This is an around advice.
			// Invoke the next interceptor in the chain.
			// This will normally result in a target object being invoked.
			retVal = invocation.proceed();
		}
		catch (Throwable ex) {
			// target invocation exception
			doCloseTransactionAfterThrowing(txInfo, ex);
			throw ex;
		}
		finally {
			doFinally(txInfo);
		}
		doCommitTransactionAfterReturning(txInfo);
		return retVal;
	}
	
}
