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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Properties;

import org.aopalliance.aop.AspectException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * Superclass for transactional aspects, such as the AOP Alliance-compatible
 * TransactionInterceptor, or an AspectJ aspect. This enables the underlying
 * Spring transaction infrastructure to be used easily to implement an aspect
 * for any aspect system.
 * <p>
 * Subclasses are responsible for calling methods in this class in the correct order.
 *
 * <p>Uses the <b>Strategy</b> design pattern. A PlatformTransactionManager
 * implementation will perform the actual transaction management.
 *
 * <p>This class could set JTA as default transaction manager as that
 * implementation does not need any specific configuration. JTA is
 * <i>not</i> the default though to avoid unnecessary dependencies.
 * 
 * <p>A transaction aspect is serializable if its 
 * PlatformTransactionManager and TransactionAttributeSource
 * are serializable.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class TransactionAspectSupport implements InitializingBean, Serializable {

	/**
	 * Holder to support the currentTransactionStatus() method, and communication
	 * between different cooperating advices (e.g. before and after advice)
	 * if the aspect involves more than a single method (as will be the case for
	 * around advice).
	 */
	private static ThreadLocal currentTransactionInfo = new ThreadLocal();

	/**
	 * Return the transaction transactionStatus of the current method invocation.
	 * Mainly intended for code that wants to set the current transaction
	 * rollback-only but not throw an application exception.
	 * @throws NoTransactionException
	 * if the transaction info cannot be found, because the method was invoked
	 * outside an AOP invocation context
	 */
	public static TransactionStatus currentTransactionStatus() throws NoTransactionException {
		return currentTransactionInfo().transactionStatus;
	}

	/**
	 * Subclasses can use this to return the current TransactionInfo.
	 * Only subclasses that cannot handle all operations in one method,
	 * such as an AspectJ aspect involving distinct before and after
	 * advice, need to use this mechanism to get at the current
	 * TransactionInfo. An around advice such as an AOP Alliance
	 * MethodInterceptor can hold a reference to the TransactionInfo
	 * throughout the aspect method.
	 * A TransactionInfo will be returned even if no transaction was
	 * created. The TransactionInfo.hasTransaction() method can be used
	 * to query this.
	 * @return TransactionInfo bound to this thread
	 * @throws NoTransactionException if no transaction has been created
	 * by an aspect
	 */
	protected static TransactionInfo currentTransactionInfo() throws NoTransactionException {
		TransactionInfo info = (TransactionInfo) currentTransactionInfo.get();
		if (info == null) {
			throw new NoTransactionException("No transaction aspect-managed TransactionStatus in scope");
		}
		return info;
	}

	/**
	 * Transient to avoid serialization. Not static as we want it
	 * to be the correct logger for subclasses. Reconstituted in
	 * readObject().
	 */
	protected transient Log logger = LogFactory.getLog(getClass());

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

	/**
	 * Check that required properties were set.
	 */
	public void afterPropertiesSet() {
		if (this.transactionManager == null) {
			throw new IllegalArgumentException("transactionManager is required");
		}
		if (this.transactionAttributeSource == null) {
			throw new IllegalArgumentException(
					"Either 'transactionAttributeSource' or 'transactionAttributes' is required: " +
					"If there are no transactional methods, don't use a TransactionInterceptor " +
					"respectively a transactional proxy.");
		}
	}

	/**
	 * Create a transaction if necessary
	 * @param method method about to execute
	 * @param targetClass class the method is on
	 * @return a TransactionInfo object, whether or not a transaction was created.
	 * The hasTransaction() method on TransactionInfo can be used to tell if there
	 * was a transaction created.
	 */
	protected TransactionInfo createTransactionIfNecessary(Method method, Class targetClass) {
		// If the transaction attribute is null, the method is non-transactional
		TransactionAttribute transAtt = this.transactionAttributeSource.getTransactionAttribute(method, targetClass);
		TransactionInfo txInfo = new TransactionInfo(transAtt, method);
		if (transAtt != null) {
			// We need a transaction for this method
			if (logger.isDebugEnabled()) {
				logger.debug("Getting transaction for " + txInfo.joinpointIdentification());
			}

			// The transaction manager will flag an error if an incompatible tx already exists
			txInfo.newTransactionStatus(this.transactionManager.getTransaction(transAtt));
		}
		else {
			// The TransactionInfo.hasTransaction() method will return
			// false. We created it only to preserve the integrity of
			// the ThreadLocal stack maintained in this class.
			if (logger.isDebugEnabled())
				logger.debug("Don't need to create transaction for " + methodIdentification(method) +
						": this method isn't transactional");
		}

		// We always bind the TransactionInfo to the thread, even if
		// we didn't create a new transaction here.
		// This guarantees that the TransactionInfo stack will be
		// managed correctly even if no transaction was created by
		// this aspect.
		txInfo.bindToThread();
		return txInfo;
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
	 * Execute after successful completion of call, but not
	 * after an exception was handled.
	 * Do nothing if we didn't create a transaction.
	 * @param txInfo information about the current transaction
	 */
	protected void doCommitTransactionAfterReturning(TransactionInfo txInfo) {
		if (txInfo != null && txInfo.hasTransaction()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking commit for transaction on " + txInfo.joinpointIdentification());
			}
			this.transactionManager.commit(txInfo.getTransactionStatus());
		}
	}

	/**
	 * Handle a throwable, closing out the transaction.
	 * We may commit or roll back, depending on our configuration.
	 * @param txInfo information about the current transaction
	 * @param ex throwable encountered
	 */
	protected void doCloseTransactionAfterThrowing(TransactionInfo txInfo, Throwable ex) {
		if (txInfo.hasTransaction()) {
			if (txInfo.transactionAttribute.rollbackOn(ex)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Invoking rollback for transaction on " + txInfo.joinpointIdentification() +
							" due to throwable [" + ex + "]");
				}
				try {
					this.transactionManager.rollback(txInfo.getTransactionStatus());
				}
				catch (RuntimeException ex2) {
					logger.error("Application exception overridden by rollback exception", ex);
					throw ex2;
				}
				catch (Error err) {
					logger.error("Application exception overridden by rollback error", ex);
					throw err;
				}
			}
			else {
				// we don't roll back on this exception
				if (logger.isDebugEnabled()) {
					logger.debug(txInfo.joinpointIdentification() + " threw throwable [" + ex +
							"] but this does not force transaction rollback");
				}
				// will still roll back if TransactionStatus.rollbackOnly is true
				this.transactionManager.commit(txInfo.getTransactionStatus());
			}
		}
	}

	/**
	 * Call this in all cases: exception or normal return. Resets
	 * the TransactionInfo ThreadLocal
	 * @param txInfo information about the current transaction. May be null.
	 */
	protected void doFinally(TransactionInfo txInfo) {
		if (txInfo != null) {
			txInfo.restoreThreadLocalStatus();
		}
	}

	
	//---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------
	
	private void readObject(ObjectInputStream ois) throws IOException {
		// Rely on default serialization, just initialize state after deserialization.
		try {
			ois.defaultReadObject();
		}
		catch (ClassNotFoundException ex) {
			throw new AspectException("Failed to deserialize Spring AOP transaction aspect:" +
					"Check that Spring AOP libraries are available on the client side", ex);
		}
		
		// Initialize transient fields
		this.logger = LogFactory.getLog(getClass());
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

		private TransactionInfo oldTransactionInfo;

		private TransactionInfo(TransactionAttribute transactionAttribute, Method method) {
			this.transactionAttribute = transactionAttribute;
			this.method = method;
		}

		/**
		 * @return whether a transaction was created by this aspect,
		 * or whether we just have a placeholder to keep ThreadLocal
		 * stack integrity
		 */
		public boolean hasTransaction() {
			return transactionStatus != null;
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
		}

		private void bindToThread() {
			// Expose current TransactionStatus, preserving any existing transactionStatus for
			// restoration after this transaction is complete.
			oldTransactionInfo = (TransactionInfo) currentTransactionInfo.get();
			currentTransactionInfo.set(this);
		}

		private void restoreThreadLocalStatus() {
			// Use stack to restore old transaction TransactionInfo.
			// Will be null if none was set.
			currentTransactionInfo.set(oldTransactionInfo);
		}

		public TransactionStatus getTransactionStatus() {
			return this.transactionStatus;
		}

		public TransactionAttribute getTransactionAttribute() {
			return this.transactionAttribute;
		}
	}

}
