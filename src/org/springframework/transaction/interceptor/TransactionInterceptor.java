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

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * Interceptor providing declarative transaction management using the common
 * Spring transaction infrastructure. TransactionInterceptors are thread-safe.
 *
 * <p>Uses the <b>Strategy</b> design pattern. A PlatformTransactionManager
 * implementation will perform the actual transaction management.
 *
 * <p>This class could set JTA as default transaction manager as that
 * implementation does not need any specific configuration. JTA is
 * <i>not</i> the default though to avoid unnecessary dependencies.
 *  
 * @version $Id: TransactionInterceptor.java,v 1.19 2004-03-18 02:46:05 trisberg Exp $
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see TransactionProxyFactoryBean
 * @see org.springframework.transaction.PlatformTransactionManager
 */
public class TransactionInterceptor implements MethodInterceptor, InitializingBean {
	
	/** Holder to support the currentTransactionStatus() method */
	private static ThreadLocal currentTransactionStatus = new ThreadLocal();

	/**
	 * Return the transaction status of the current method invocation.
	 * Mainly intended for code that wants to set the current transaction
	 * rollback-only but not throw an application exception.
	 * @throws NoTransactionException
	 * if the invocation cannot be found, because the method was invoked
	 * outside an AOP invocation context
	 */
	public static TransactionStatus currentTransactionStatus() throws AspectException {
		TransactionStatus status = (TransactionStatus) currentTransactionStatus.get();
		if (status == null) {
			throw new NoTransactionException("No TransactionInterceptor-managed TransactionStatus in scope");
		}
		return status;
	}


	protected final Log logger = LogFactory.getLog(getClass());

	/** Delegate used to create, commit and rollback transactions */
	private PlatformTransactionManager transactionManager;
	
	/** Helper used to find transaction attributes */
	private TransactionAttributeSource transactionAttributeSource;

	/**
	 * Create a new TransactionInterceptor.
	 * Does not set a default transaction manager!
	 * @see #setTransactionManager
	 * @see #setTransactionAttributeSource
	 */
	public TransactionInterceptor() {
	}

	/**
	 * Set the transaction manager. This will perform actual
	 * transaction management: This class is just a way of invoking it.
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	/**
	 * Return the transaction manager.
	 */
	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	/**
	 * Set the transaction attribute source which is used to find transaction
	 * attributes. If specifying a String property value, a PropertyEditor
	 * will create a MethodMapTransactionAttributeSource from the value.
	 * @see TransactionAttributeSourceEditor
	 * @see MethodMapTransactionAttributeSource
	 * @see NameMatchTransactionAttributeSource
	 */
	public void setTransactionAttributeSource(TransactionAttributeSource transactionAttributeSource) {
		this.transactionAttributeSource = transactionAttributeSource;
	}

	/**
	 * Return the transaction attribute source.
	 */
	public TransactionAttributeSource getTransactionAttributeSource() {
		return transactionAttributeSource;
	}

	public void afterPropertiesSet() {
		if (this.transactionManager == null) {
			throw new IllegalArgumentException("transactionManager is required");
		}
		if (this.transactionAttributeSource == null) {
			throw new IllegalArgumentException("transactionAttributeSource is required");
		}
	}

	public final Object invoke(MethodInvocation invocation) throws Throwable {
		// Work out the target class: may be null.
		// The TransactionAttributeSource should be passed the target class
		// as well as the method, which may be from an interface
		Class targetClass = (invocation.getThis() != null) ? invocation.getThis().getClass() : null;
		
		// if the transaction attribute is null, the method is non-transactional
		TransactionAttribute transAtt = this.transactionAttributeSource.getTransactionAttribute(invocation.getMethod(), targetClass);
		TransactionStatus status = null;
		TransactionStatus oldTransactionStatus = null;
		
		// create transaction if necessary
		if (transAtt != null) {
			// we need a transaction for this method
			if (logger.isDebugEnabled()) {
				logger.debug("Getting transaction for method '" + invocation.getMethod().getName() +
				             "' in class [" + invocation.getMethod().getDeclaringClass().getName() + "]");
			}
			
			// the transaction manager will flag an error if an incompatible tx already exists
			status = this.transactionManager.getTransaction(transAtt);
			
			// make the TransactionStatus available to callees
			oldTransactionStatus = (TransactionStatus) currentTransactionStatus.get();
			currentTransactionStatus.set(status);
		}
		else {
			// it isn't a transactional method
			if (logger.isDebugEnabled())
				logger.debug("Don't need to create transaction for method '" + invocation.getMethod().getName() +
				             "' in class [" + invocation.getMethod().getDeclaringClass().getName() +
				             "]: this method isn't transactional");
		}

		// Invoke the next interceptor in the chain.
		// This will normally result in a target object being invoked.
		Object retVal = null;
		try {
			retVal = invocation.proceed();
		}
		catch (Throwable ex) {
			// target invocation exception
			if (status != null) {
				onThrowable(invocation, transAtt, status, ex);
			}
			throw ex;
		}
		finally {
			if (transAtt != null) {
				// use stack to restore old transaction status if one was set
				currentTransactionStatus.set(oldTransactionStatus);
			}
		}
		if (status != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking commit for transaction on method '" + invocation.getMethod().getName() +
				             "' in class [" + invocation.getMethod().getDeclaringClass().getName() + "]");
			}
			this.transactionManager.commit(status);
		}
		return retVal;
	}

	/**
	 * Handle a throwable.
	 * We may commit or roll back, depending on our configuration.
	 */
	private void onThrowable(MethodInvocation invocation, TransactionAttribute txAtt,
	                         TransactionStatus status, Throwable ex) {
		if (txAtt.rollbackOn(ex)) {
			logger.info("Invoking rollback for transaction on method '" + invocation.getMethod().getName() +
									"' in class [" + invocation.getMethod().getDeclaringClass().getName() +
			            "] due to throwable [" + ex + "]");
			try {
				this.transactionManager.rollback(status);
			}
			catch (TransactionException tex) {
				logger.error("Application exception overridden by rollback exception", ex);
				throw tex;
			}
		}
		else {
			if (logger.isDebugEnabled())
				logger.debug("Method '"	+ invocation.getMethod().getName()+ "' in class [" +
				             invocation.getMethod().getDeclaringClass().getName() +
				             "] threw throwable [" + ex +	"] but this does not force transaction rollback");
			// Will still roll back if rollbackOnly is true
			this.transactionManager.commit(status);
		}
	}

}
