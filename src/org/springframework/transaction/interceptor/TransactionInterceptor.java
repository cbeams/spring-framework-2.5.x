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

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * Interceptor providing declarative transaction management using the common
 * Spring transaction infrastructure, and the TransactionAspectSupport
 * support class. TransactionInterceptors are thread-safe.
 *
 * @version $Id: TransactionInterceptor.java,v 1.23 2004-06-30 12:32:56 johnsonr Exp $
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see TransactionProxyFactoryBean
 * @see org.springframework.transaction.PlatformTransactionManager
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport
 */
public class TransactionInterceptor extends TransactionAspectSupport implements MethodInterceptor {

	/**
	 * Return the transaction status of the current method invocation.
	 * Mainly intended for code that wants to set the current transaction
	 * rollback-only but not throw an application exception.
	 * @throws NoTransactionException
	 * if the invocation cannot be found, because the method was invoked
	 * outside an AOP invocation context
	 */
	public static TransactionStatus currentTransactionStatus() throws AspectException {
		return TransactionAspectSupport.currentTransactionStatus();
	}
	

	/**
	 * Create a new TransactionInterceptor.
	 * Does not set a default transaction manager!
	 * @see #setTransactionManager
	 * @see #setTransactionAttributeSource
	 */
	public TransactionInterceptor() {
	}

	public final Object invoke(MethodInvocation invocation) throws Throwable {
		// Work out the target class: may be null.
		// The TransactionAttributeSource should be passed the target class
		// as well as the method, which may be from an interface
		Class targetClass = (invocation.getThis() != null) ? invocation.getThis().getClass() : null;
		
		// Create transaction if necessary
		TxInfo txInfo = createTransactionIfNecessary(invocation.getMethod(), targetClass);

		Object retVal = null;
		try {
			// This is an around advice.
			// Invoke the next interceptor in the chain.
			// This will normally result in a target object being invoked.
			retVal = invocation.proceed();
		}
		catch (Throwable ex) {
			// target invocation exception
			if (txInfo != null) {
				doAfterThrowing(txInfo, ex);
			}
			throw ex;
		}
		finally {
			doFinally(txInfo);
		}
		doAfterReturningOrNonRollbackThrowable(txInfo);

		return retVal;
	}
	
}
