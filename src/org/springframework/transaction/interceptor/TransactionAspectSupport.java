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
import java.util.Properties;

import org.aopalliance.aop.AspectException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * Superclass for transactional aspects, such as the AOP Alliance-compatible
 * TransactionInterceptor, or an AspectJ aspect.
 * Subclasses are responsible for calling methods in this class in the correct order.
 * 
 * <p>Uses the <b>Strategy</b> design pattern. A PlatformTransactionManager
 * implementation will perform the actual transaction management.
 *
 * <p>This class could set JTA as default transaction manager as that
 * implementation does not need any specific configuration. JTA is
 * <i>not</i> the default though to avoid unnecessary dependencies.
 *  
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: TransactionAspectSupport.java,v 1.2 2004-06-30 14:42:22 jhoeller Exp $
 */
public class TransactionAspectSupport implements InitializingBean {
	
	/** Holder to support the currentTransactionStatus() method */
	private static ThreadLocal currentTransactionStatus = new ThreadLocal();

	/**
	 * Return the transaction transactionStatus of the current method invocation.
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
	protected PlatformTransactionManager transactionManager;
	
	/** Helper used to find transaction attributes */
	protected TransactionAttributeSource transactionAttributeSource;


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
	 * Set properties with method names as keys and transaction attribute
	 * descriptors (parsed via TransactionAttributeEditor) as values:
	 * e.g. key = "myMethod", value = "PROPAGATION_REQUIRED,readOnly".
	 * <p>Note: Method names are always applied to the target class,
	 * no matter if defined in an interface or the class itself.
	 * <p>Internally, a NameMatchTransactionAttributeSource will be
	 * created from the given properties.
	 * @see #setTransactionAttributeSource
	 * @see TransactionAttributeEditor
	 * @see NameMatchTransactionAttributeSource
	 */
	public void setTransactionAttributes(Properties transactionAttributes) {
		NameMatchTransactionAttributeSource tas = new NameMatchTransactionAttributeSource();
		tas.setProperties(transactionAttributes);
		this.transactionAttributeSource = tas;
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
			throw new IllegalArgumentException("Either 'transactionAttributeSource' or 'transactionAttributes' " +
			                                   "is required: If there are no transactional methods, don't use " +
			                                   "a TransactionInterceptor respectively a transactional proxy.");
		}
	}
	
	/**
	 * Create a transaction if necessary
	 * @param method method about to execute
	 * @param targetClass class the method is on
	 * @return a TransactionInfo object if a transaction was created, else
	 * null
	 */
	protected TransactionInfo createTransactionIfNecessary(Method method, Class targetClass) {
		// If the transaction attribute is null, the method is non-transactional
		TransactionAttribute transAtt = this.transactionAttributeSource.getTransactionAttribute(method, targetClass);
		
		if (transAtt != null) {
			// We need a transaction for this method
			TransactionInfo txInfo = new TransactionInfo(transAtt, method);
			
			if (logger.isDebugEnabled()) {
				logger.debug("Getting transaction for " + txInfo.joinpointIdentification());
			}
								
			// The transaction manager will flag an error if an incompatible tx already exists
			txInfo.newTransactionStatus(this.transactionManager.getTransaction(transAtt));
			
			return txInfo;
		}
		else {	
			if (logger.isDebugEnabled())
				logger.debug("Don't need to create transaction for " + methodIdentification(method) +
				             ": this method isn't transactional");
			return null;
		}
	}
	
	/**
	 * Convenience method to return a String representation of this Method
	 * for use in logging.
	 * @param method method we're interested in
	 * @return log message identifying this method
	 */
	protected String methodIdentification(Method method) {
		return "method '" + method.getName() + "' in class [" + method.getDeclaringClass().getName() + "]";
	}

	/**
	 * Handle a throwable.
	 * We may commit or roll back, depending on our configuration.
	 * @param txInfo information about the current transaction
	 * @param t throwable encountered
	 */
	protected void doAfterThrowing(TransactionInfo txInfo, Throwable t) {
		if (txInfo.transactionAttribute.rollbackOn(t)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking rollback for transaction on " + txInfo.joinpointIdentification() +
				            " due to throwable [" + t + "]");
			}
			try {
				this.transactionManager.rollback(txInfo.getTransactionStatus());
			}
			catch (TransactionException tex) {
				logger.error("Application exception overridden by rollback exception", t);
				throw tex;
			}
		}
		else {
			// we don't roll back on this exception
			if (logger.isDebugEnabled()) {
				logger.debug(txInfo.joinpointIdentification() + " threw throwable [" + t +
				             "] but this does not force transaction rollback");
			}
			// will still roll back if rollbackOnly is true
			this.transactionManager.commit(txInfo.getTransactionStatus());
		}
	}
	
	/**
	 * Execution after successful completion of call, or after an
	 * exception was thrown that didn't trigger rollback.
	 * Do nothing if we didn't create a transaction.
	 * @param txInfo information about the current transaction
	 */
	protected void doAfterReturningOrNonRollbackThrowable(TransactionInfo txInfo) {
		if (txInfo != null) {			
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking commit for transaction on " + txInfo.joinpointIdentification());
			}
			this.transactionManager.commit(txInfo.getTransactionStatus());
		}
	}
	
	/**
	 * Call this in all cases: exception or normal return. Resets
	 * the TransactionStatus ThreadLocal
	 * @param txInfo information about the current transaction. May be null.
	 */
	protected void doFinally(TransactionInfo txInfo) {
		if (txInfo != null) {
			txInfo.restoreThreadLocalStatus();
		}
	}


	/**
	 * Opaque object used to hold Transaction information. Subclasses
	 * must pass it back to methods on this class, but not see its internals.
	 */
	protected class TransactionInfo {

		private final TransactionAttribute transactionAttribute;

		// TODO: Could open up to other kinds of joinpoint?
		private final Method method;

		private TransactionStatus transactionStatus;

		private TransactionStatus oldTransactionStatus;

		private TransactionInfo(TransactionAttribute transactionAttribute, Method method) {
			this.transactionAttribute = transactionAttribute;
			this.method = method;
		}

		/**
		 * Return a String representation of this joinpoint (usually a Method call)
		 * for use in logging.
		 */
		public String joinpointIdentification() {
			return methodIdentification(this.method);
		}

		public void newTransactionStatus(TransactionStatus status) {
			this.transactionStatus = status;
			// Expose current TransactionStatus, preserving any existing transactionStatus for
			// restoration after this transaction is complete.
			oldTransactionStatus = (TransactionStatus) currentTransactionStatus.get();
			currentTransactionStatus.set(status);
		}

		public void restoreThreadLocalStatus() {
			// Use stack to restore old transaction transactionStatus if one was set.
			currentTransactionStatus.set(oldTransactionStatus);
		}

		public TransactionStatus getTransactionStatus() {
			return this.transactionStatus;
		}

		public TransactionAttribute transactionAttribute() {
			return this.transactionAttribute;
		}
	}

}
