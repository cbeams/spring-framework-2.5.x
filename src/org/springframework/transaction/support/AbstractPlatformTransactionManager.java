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

package org.springframework.transaction.support;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.Constants;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.InvalidTimeoutException;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;

/**
 * Abstract base class that allows for easy implementation of concrete
 * platform transaction managers like JtaTransactionManager and
 * HibernateTransactionManager.
 *
 * <p>Provides the following workflow handling:
 * <ul>
 * <li>determines if there is an existing transaction;
 * <li>applies the appropriate propagation behavior;
 * <li>suspends and resumes transactions if necessary;
 * <li>checks the rollback-only flag on commit;
 * <li>applies the appropriate modification on rollback
 * (actual rollback or setting rollback-only);
 * <li>triggers registered synchronization callbacks
 * (if transaction synchronization is active).
 * </ul>
 *
 * <p>Transaction synchronization is a generic mechanism for registering
 * callbacks that get invoked at transaction completion time. This is mainly
 * used internally by the data access support classes for JDBC, Hibernate,
 * and JDO: They register resources that are opened within the transaction
 * for closing at transaction completion time, allowing e.g. for reuse of
 * the same Hibernate Session within the transaction. The same mechanism
 * can also be used for custom synchronization efforts.
 * 
 * <p>The state of this class is serializable. It's up to subclasses if
 * they wish to make their state to be serializable.
 * They should implement readObject() methods if they need
 * to restore any transient state.
 *
 * @author Juergen Hoeller
 * @since 28.03.2003
 * @see #setTransactionSynchronization
 * @see TransactionSynchronizationManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.orm.hibernate.HibernateTransactionManager
 */
public abstract class AbstractPlatformTransactionManager implements PlatformTransactionManager, Serializable {

	/**
	 * Always activate transaction synchronization, even for "empty" transactions
	 * that result from PROPAGATION_SUPPORTS with no existing backend transaction.
	 */
	public static final int SYNCHRONIZATION_ALWAYS = 0;

	/**
	 * Activate transaction synchronization only for actual transactions,
	 * i.e. not for empty ones that result from PROPAGATION_SUPPORTS with no
	 * existing backend transaction.
	 */
	public static final int SYNCHRONIZATION_ON_ACTUAL_TRANSACTION = 1;

	/**
	 * Never active transaction synchronization.
	 */
	public static final int SYNCHRONIZATION_NEVER = 2;

	/** Transient to optimize serialization */
	protected transient Log logger = LogFactory.getLog(getClass());

	/** Constants instance for AbstractPlatformTransactionManager */
	private static final Constants constants = new Constants(AbstractPlatformTransactionManager.class);

	private int transactionSynchronization = SYNCHRONIZATION_ALWAYS;

	private boolean nestedTransactionAllowed = false;

	private boolean rollbackOnCommitFailure = false;


	/**
	 * Set the transaction synchronization by the name of the corresponding constant
	 * in this class, e.g. "SYNCHRONIZATION_ALWAYS".
	 * @param constantName name of the constant
	 * @see #SYNCHRONIZATION_ALWAYS
	 */
	public void setTransactionSynchronizationName(String constantName) {
		setTransactionSynchronization(constants.asNumber(constantName).intValue());
	}

	/**
	 * Set when this transaction manager should activate the thread-bound
	 * transaction synchronization support. Default is "always".
	 * <p>Note that transaction synchronization isn't supported for
	 * multiple concurrent transactions by different transaction managers.
	 * Only one transaction manager is allowed to activate it at any time.
	 * @see #SYNCHRONIZATION_ALWAYS
	 * @see #SYNCHRONIZATION_ON_ACTUAL_TRANSACTION
	 * @see #SYNCHRONIZATION_NEVER
	 * @see TransactionSynchronizationManager
	 * @see TransactionSynchronization
	 */
	public void setTransactionSynchronization(int transactionSynchronization) {
		this.transactionSynchronization = transactionSynchronization;
	}

	/**
	 * Return if this transaction manager should activate the thread-bound
	 * transaction synchronization support.
	 */
	public int getTransactionSynchronization() {
		return transactionSynchronization;
	}

	/**
	 * Set whether nested transactions are allowed. Default is false.
	 * <p>Typically initialized with an appropriate default by the
	 * concrete transaction manager subclass.
	 */
	public void setNestedTransactionAllowed(boolean nestedTransactionAllowed) {
		this.nestedTransactionAllowed = nestedTransactionAllowed;
	}

	/**
	 * Return whether nested transactions are allowed.
	 */
	public boolean isNestedTransactionAllowed() {
		return nestedTransactionAllowed;
	}

	/**
	 * Set if doRollback should be performed on failure of the doCommit call.
	 * Typically not necessary and thus to be avoided as it can override the
	 * commit exception with a subsequent rollback exception. Default is false.
	 * @see #doCommit
	 * @see #doRollback
	 */
	public void setRollbackOnCommitFailure(boolean rollbackOnCommitFailure) {
		this.rollbackOnCommitFailure = rollbackOnCommitFailure;
	}

	/**
	 * Return if a rollback should be performed on failure of the commit call.
	 */
	public boolean isRollbackOnCommitFailure() {
		return rollbackOnCommitFailure;
	}


	//---------------------------------------------------------------------
	// Implementation of PlatformTransactionManager
	//---------------------------------------------------------------------

	/**
	 * This implementation of getTransaction handles propagation behavior.
	 * Delegates to doGetTransaction, isExistingTransaction, doBegin.
	 * @see #doGetTransaction
	 * @see #isExistingTransaction
	 * @see #doBegin
	 */
	public final TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
		Object transaction = doGetTransaction();

		// cache to avoid repeated checks
		boolean debugEnabled = logger.isDebugEnabled();

		if (debugEnabled) {
			logger.debug("Using transaction object [" + transaction + "]");
		}

		if (definition == null) {
			// use defaults
			definition = new DefaultTransactionDefinition();
		}

		if (isExistingTransaction(transaction)) {
			if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER) {
				throw new IllegalTransactionStateException("Transaction propagation 'never' but existing transaction found");
			}
			if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED) {
				if (debugEnabled) {
					logger.debug("Suspending current transaction");
				}
				Object suspendedResources = suspend(transaction);
				boolean newSynchronization = (this.transactionSynchronization == SYNCHRONIZATION_ALWAYS);
				return newTransactionStatus(null, false, newSynchronization,
				                            definition.isReadOnly(), debugEnabled, suspendedResources);
			}
			else if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW) {
				if (debugEnabled) {
					logger.debug("Creating new transaction, suspending current one");
				}
				Object suspendedResources = suspend(transaction);
				doBegin(transaction, definition);
				boolean newSynchronization = (this.transactionSynchronization != SYNCHRONIZATION_NEVER);
				return newTransactionStatus(transaction, true, newSynchronization,
				                            definition.isReadOnly(), debugEnabled, suspendedResources);
			}
			else if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
				if (!isNestedTransactionAllowed()) {
					throw new NestedTransactionNotSupportedException(
							"Transaction manager does not allow nested transactions by default - " +
							"specify 'nestedTransactionAllowed' property with value 'true'");
				}
				if (debugEnabled) {
					logger.debug("Creating nested transaction");
				}
				boolean newSynchronization = (this.transactionSynchronization != SYNCHRONIZATION_NEVER);
				DefaultTransactionStatus status = newTransactionStatus(transaction, true, newSynchronization,
																															 definition.isReadOnly(), debugEnabled, null);
				if (useSavepointForNestedTransaction()) {
					status.createAndHoldSavepoint();
				}
				else {
					doBegin(transaction, definition);
				}
				return status;
			}
			else {
				if (debugEnabled) {
					logger.debug("Participating in existing transaction");
				}
				boolean newSynchronization = (this.transactionSynchronization != SYNCHRONIZATION_NEVER);
				return newTransactionStatus(transaction, false, newSynchronization,
				                            definition.isReadOnly(), debugEnabled, null);
			}
		}

		if (definition.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) {
			throw new InvalidTimeoutException("Invalid transaction timeout", definition.getTimeout());
		}
		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
			throw new IllegalTransactionStateException("Transaction propagation 'mandatory' but no existing transaction found");
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
				definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
		    definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
			if (debugEnabled) {
				logger.debug("Creating new transaction");
			}
			doBegin(transaction, definition);
			boolean newSynchronization = (this.transactionSynchronization != SYNCHRONIZATION_NEVER);
			return newTransactionStatus(transaction, true, newSynchronization,
			                            definition.isReadOnly(), debugEnabled, null);
		}
		else {
			// "empty" (-> no) transaction
			boolean newSynchronization = (this.transactionSynchronization == SYNCHRONIZATION_ALWAYS);
			return newTransactionStatus(null, false, newSynchronization,
			                            definition.isReadOnly(), debugEnabled, null);
		}
	}

	/**
	 * Create a new TransactionStatus for the given arguments,
	 * initializing transaction synchronization if appropriate.
	 */
	private DefaultTransactionStatus newTransactionStatus(Object transaction, boolean newTransaction,
																												boolean newSynchronization, boolean readOnly,
																												boolean debug, Object suspendedResources) {
		boolean actualNewSynchronization = newSynchronization &&
				!TransactionSynchronizationManager.isSynchronizationActive();
		if (actualNewSynchronization) {
			TransactionSynchronizationManager.initSynchronization();
		}
		return new DefaultTransactionStatus(transaction, newTransaction, actualNewSynchronization,
		                                    readOnly, debug, suspendedResources);
	}

	/**
	 * Suspend the given transaction. Suspends transaction synchronization first,
	 * then delegates to the doSuspend template method.
	 * @param transaction the current transaction object
	 * @return an object that holds suspended resources
	 * @see #doSuspend
	 * @see #resume
	 */
	private Object suspend(Object transaction) throws TransactionException {
		List suspendedSynchronizations = null;
		Object holder = doSuspend(transaction);
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			suspendedSynchronizations = TransactionSynchronizationManager.getSynchronizations();
			for (Iterator it = suspendedSynchronizations.iterator(); it.hasNext();) {
				((TransactionSynchronization) it.next()).suspend();
			}
			TransactionSynchronizationManager.clearSynchronization();
		}
		return new SuspendedResourcesHolder(suspendedSynchronizations, holder);
	}

	/**
	 * Resume the given transaction. Delegates to the doResume template method
	 * first, then resuming transaction synchronization.
	 * @param transaction the current transaction object
	 * @param suspendedResources the object that holds suspended resources,
	 * as returned by suspend
	 * @see #doResume
	 * @see #suspend
	 */
	private void resume(Object transaction, Object suspendedResources) throws TransactionException {
		SuspendedResourcesHolder resourcesHolder = (SuspendedResourcesHolder) suspendedResources;
		if (resourcesHolder.getSuspendedSynchronizations() != null) {
			TransactionSynchronizationManager.initSynchronization();
			for (Iterator it = resourcesHolder.getSuspendedSynchronizations().iterator(); it.hasNext();) {
				TransactionSynchronization synchronization = (TransactionSynchronization) it.next();
				synchronization.resume();
				TransactionSynchronizationManager.registerSynchronization(synchronization);
			}
		}
		doResume(transaction, resourcesHolder.getSuspendedResources());
	}

	/**
	 * This implementation of commit handles participating in existing
	 * transactions and programmatic rollback requests.
	 * Delegates to isRollbackOnly, doCommit and rollback.
	 * @see org.springframework.transaction.TransactionStatus#isRollbackOnly
	 * @see #doCommit
	 * @see #rollback
	 */
	public final void commit(TransactionStatus status) throws TransactionException {
		DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
		if (status.isRollbackOnly()) {
			if (defStatus.isDebug()) {
				logger.debug("Transactional code has requested rollback");
			}
			rollback(status);
		}

		else {
			try {
				boolean beforeCompletionInvoked = false;
				try {
					triggerBeforeCommit(defStatus);
					triggerBeforeCompletion(defStatus, null);
					beforeCompletionInvoked = true;
					if (defStatus.hasSavepoint()) {
						if (defStatus.isDebug()) {
							logger.debug("Releasing transaction savepoint");
						}
						defStatus.releaseHeldSavepoint();
					}
					else if (status.isNewTransaction()) {
						logger.debug("Initiating transaction commit");
						doCommit(defStatus);
					}
				}
				catch (UnexpectedRollbackException ex) {
					// can only be caused by doCommit
					triggerAfterCompletion(defStatus, TransactionSynchronization.STATUS_ROLLED_BACK, ex);
					throw ex;
				}
				catch (TransactionException ex) {
					// can only be caused by doCommit
					if (isRollbackOnCommitFailure()) {
						doRollbackOnCommitException(defStatus, ex);
					}
					else {
						triggerAfterCompletion(defStatus, TransactionSynchronization.STATUS_UNKNOWN, ex);
					}
					throw ex;
				}
				catch (RuntimeException ex) {
					if (!beforeCompletionInvoked) {
						triggerBeforeCompletion(defStatus, ex);
					}
					doRollbackOnCommitException(defStatus, ex);
					throw ex;
				}
				catch (Error err) {
					if (!beforeCompletionInvoked) {
						triggerBeforeCompletion(defStatus, err);
					}
					doRollbackOnCommitException(defStatus, err);
					throw err;
				}
				triggerAfterCompletion(defStatus, TransactionSynchronization.STATUS_COMMITTED, null);
			}
			finally {
				cleanupAfterCompletion(defStatus);
			}
		}
	}

	/**
	 * This implementation of rollback handles participating in existing
	 * transactions. Delegates to doRollback and doSetRollbackOnly.
	 * @see #doRollback
	 * @see #doSetRollbackOnly
	 */
	public final void rollback(TransactionStatus status) throws TransactionException {
		DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
		try {
			try {
				triggerBeforeCompletion(defStatus, null);
				if (defStatus.hasSavepoint()) {
					if (defStatus.isDebug()) {
						logger.debug("Rolling back transaction to savepoint");
					}
					defStatus.rollbackToHeldSavepoint();
				}
				else if (status.isNewTransaction()) {
					logger.debug("Initiating transaction rollback");
					doRollback(defStatus);
				}
				else if (defStatus.getTransaction() != null) {
					if (defStatus.isDebug()) {
						logger.debug("Setting existing transaction rollback-only");
					}
					doSetRollbackOnly(defStatus);
				}
				else {
					logger.warn("Should roll back transaction but cannot - no transaction available");
				}
			}
			catch (RuntimeException ex) {
				triggerAfterCompletion(defStatus, TransactionSynchronization.STATUS_UNKNOWN, ex);
				throw ex;
			}
			catch (Error err) {
				triggerAfterCompletion(defStatus, TransactionSynchronization.STATUS_UNKNOWN, err);
				throw err;
			}
			triggerAfterCompletion(defStatus, TransactionSynchronization.STATUS_ROLLED_BACK, null);
		}
		finally {
			cleanupAfterCompletion(defStatus);
		}
	}

	/**
	 * Invoke doRollback, handling rollback exceptions properly.
	 * @param status object representing the transaction
	 * @param ex the thrown application exception or error
	 * @throws TransactionException in case of a rollback error
	 * @see #doRollback
	 */
	private void doRollbackOnCommitException(DefaultTransactionStatus status, Throwable ex)
	    throws TransactionException {
		try {
			if (status.isNewTransaction()) {
				if (status.isDebug()) {
					logger.debug("Initiating transaction rollback on commit exception", ex);
				}
				doRollback(status);
			}
		}
		catch (RuntimeException rbex) {
			logger.error("Commit exception overridden by rollback exception", ex);
			triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN, rbex);
			throw rbex;
		}
		catch (Error rberr) {
			logger.error("Commit exception overridden by rollback exception", ex);
			triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN, rberr);
			throw rberr;
		}
		triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK, ex);
	}

	/**
	 * Trigger beforeCommit callback.
	 * @param status object representing the transaction
	 */
	private void triggerBeforeCommit(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			logger.debug("Triggering beforeCommit synchronization");
			for (Iterator it = TransactionSynchronizationManager.getSynchronizations().iterator(); it.hasNext();) {
				TransactionSynchronization synchronization = (TransactionSynchronization) it.next();
				synchronization.beforeCommit(status.isReadOnly());
			}
		}
	}

	/**
	 * Trigger beforeCompletion callback.
	 * @param status object representing the transaction
	 * @param ex the thrown application exception or error, or null
	 */
	private void triggerBeforeCompletion(DefaultTransactionStatus status, Throwable ex) {
		if (status.isNewSynchronization()) {
			logger.debug("Triggering beforeCompletion synchronization");
			try {
				for (Iterator it = TransactionSynchronizationManager.getSynchronizations().iterator(); it.hasNext();) {
					TransactionSynchronization synchronization = (TransactionSynchronization) it.next();
					synchronization.beforeCompletion();
				}
			}
			catch (RuntimeException tsex) {
				if (ex != null) {
					logger.error("Rollback exception overridden by synchronization exception", ex);
				}
				throw tsex;
			}
			catch (Error tserr) {
				if (ex != null) {
					logger.error("Rollback exception overridden by synchronization exception", ex);
				}
				throw tserr;
			}
		}
	}

	/**
	 * Trigger afterCompletion callback, handling exceptions properly.
	 * @param status object representing the transaction
	 * @param completionStatus completion status according to TransactionSynchronization constants
	 * @param ex the thrown application exception or error, or null
	 */
	private void triggerAfterCompletion(DefaultTransactionStatus status, int completionStatus, Throwable ex) {
		if (status.isNewSynchronization()) {
			logger.debug("Triggering afterCompletion synchronization");
			try {
				for (Iterator it = TransactionSynchronizationManager.getSynchronizations().iterator(); it.hasNext();) {
					TransactionSynchronization synchronization = (TransactionSynchronization) it.next();
					synchronization.afterCompletion(completionStatus);
				}
			}
			catch (RuntimeException tsex) {
				if (ex != null) {
					logger.error("Rollback exception overridden by synchronization exception", ex);
				}
				throw tsex;
			}
			catch (Error tserr) {
				if (ex != null) {
					logger.error("Rollback exception overridden by synchronization exception", ex);
				}
				throw tserr;
			}
		}
	}

	/**
	 * Clean up after completion, clearing synchronization if necessary,
	 * and invoking doCleanupAfterCompletion.
	 * @param status object representing the transaction
	 * @see #doCleanupAfterCompletion
	 */
	private void cleanupAfterCompletion(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
		if (status.isNewTransaction() && !status.hasSavepoint()) {
			doCleanupAfterCompletion(status.getTransaction());
		}
		if (status.getSuspendedResources() != null) {
			if (status.isDebug()) {
				logger.debug("Resuming suspended transaction");
			}
			resume(status.getTransaction(), status.getSuspendedResources());
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
			throw new IOException("Failed to deserialize [" + getClass().getName() + "] - " +
					"check that Spring transaction libraries are available on the client side: " + ex.getMessage());
		}

		// initialize transient fields
		this.logger = LogFactory.getLog(getClass());
	}


	//---------------------------------------------------------------------
	// Template methods to be implemented in subclasses
	//---------------------------------------------------------------------

	/**
	 * Return a current transaction object, i.e. a JTA UserTransaction.
	 * @return the current transaction object
	 * @throws org.springframework.transaction.CannotCreateTransactionException
	 * if transaction support is not available (e.g. no JTA UserTransaction retrievable from JNDI)
	 * @throws TransactionException in case of lookup or system errors
	 */
	protected abstract Object doGetTransaction() throws TransactionException;

	/**
	 * Check if the given transaction object indicates an existing,
	 * i.e. already begun, transaction.
	 * @param transaction transaction object returned by doGetTransaction
	 * @return if there is an existing transaction
	 * @throws TransactionException in case of system errors
	 */
	protected abstract boolean isExistingTransaction(Object transaction) throws TransactionException;

	/**
	 * Return whether to use a savepoint for a nested transaction. Default is true,
	 * which causes delegation to DefaultTransactionStatus for holding a savepoint.
	 * <p>Subclasses can override this to return false, causing a further
	 * invocation of doBegin despite an already existing transaction.
	 * @see DefaultTransactionStatus#createAndHoldSavepoint
	 * @see DefaultTransactionStatus#rollbackToHeldSavepoint
	 * @see DefaultTransactionStatus#releaseHeldSavepoint
	 * @see #doBegin
	 */
	protected boolean useSavepointForNestedTransaction() {
		return true;
	}

	/**
	 * Begin a new transaction with the given transaction definition.
	 * Does not have to care about applying the propagation behavior,
	 * as this has already been handled by this abstract manager.
	 * @param transaction transaction object returned by doGetTransaction
	 * @param definition TransactionDefinition instance, describing
	 * propagation behavior, isolation level, timeout etc.
	 * @throws TransactionException in case of creation or system errors
	 */
	protected abstract void doBegin(Object transaction, TransactionDefinition definition)
	    throws TransactionException;

	/**
	 * Suspend the resources of the current transaction.
	 * Transaction synchronization will already have been suspended.
	 * @param transaction transaction object returned by doGetTransaction
	 * @return an object that holds suspended resources
	 * (will be kept unexamined for passing it into doResume)
	 * @throws org.springframework.transaction.IllegalTransactionStateException
	 * if suspending is not supported by the transaction manager implementation
	 * @throws TransactionException in case of system errors
	 * @see #doResume
	 */
	protected abstract Object doSuspend(Object transaction) throws TransactionException;

	/**
	 * Resume the resources of the current transaction.
	 * Transaction synchronization will be resumed afterwards.
	 * @param transaction transaction object returned by doGetTransaction
	 * @param suspendedResources the object that holds suspended resources,
	 * as returned by doSuspend
	 * @throws org.springframework.transaction.IllegalTransactionStateException
	 * if resuming is not supported by the transaction manager implementation
	 * @throws TransactionException in case of system errors
	 * @see #doSuspend
	 */
	protected abstract void doResume(Object transaction, Object suspendedResources)
	    throws TransactionException;

	/**
	 * Perform an actual commit on the given transaction.
	 * An implementation does not need to check the rollback-only flag.
	 * @param status status representation of the transaction
	 * @throws TransactionException in case of commit or system errors
	 */
	protected abstract void doCommit(DefaultTransactionStatus status) throws TransactionException;

	/**
	 * Perform an actual rollback on the given transaction.
	 * An implementation does not need to check the new transaction flag.
	 * @param status status representation of the transaction
	 * @throws TransactionException in case of system errors
	 */
	protected abstract void doRollback(DefaultTransactionStatus status) throws TransactionException;

	/**
	 * Set the given transaction rollback-only. Only called on rollback
	 * if the current transaction takes part in an existing one.
	 * @param status status representation of the transaction
	 * @throws TransactionException in case of system errors
	 */
	protected abstract void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException;

	/**
	 * Cleanup resources after transaction completion.
	 * Called after doCommit and doRollback execution on any outcome.
	 * Should not throw any exceptions but just issue warnings on errors.
	 * <p>Default implementation does nothing.
	 * @param transaction transaction object returned by doGetTransaction
	 */
	protected void doCleanupAfterCompletion(Object transaction) {
	}


	/**
	 * Holder for suspended resources.
	 * Used internally by suspend and resume.
	 * @see #suspend
	 * @see #resume
	 */
	private static class SuspendedResourcesHolder {

		private final List suspendedSynchronizations;

		private final Object suspendedResources;

		private SuspendedResourcesHolder(List suspendedSynchronizations, Object suspendedResources) {
			this.suspendedSynchronizations = suspendedSynchronizations;
			this.suspendedResources = suspendedResources;
		}

		private List getSuspendedSynchronizations() {
			return suspendedSynchronizations;
		}

		private Object getSuspendedResources() {
			return suspendedResources;
		}
	}

}
