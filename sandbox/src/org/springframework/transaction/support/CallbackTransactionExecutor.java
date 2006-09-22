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

package org.springframework.transaction.support;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.InvalidTimeoutException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * @author Juergen Hoeller
 * @since 19.09.2006
 */
public class CallbackTransactionExecutor {

	protected Log logger = LogFactory.getLog(getClass());

	private int transactionSynchronization;


	public CallbackTransactionExecutor(int transactionSynchronization) {
		this.transactionSynchronization = transactionSynchronization;
	}


	public Object execute(
			TransactionDefinition definition, TransactionCallback callback, CallbackTransactionObject transaction) {

		DefaultTransactionStatus status = prepareTransactionStatusForCallback(definition, transaction);
		try {
			Object result = callback.doInTransaction(status);
			if (status.isLocalRollbackOnly()) {
				transaction.setRollbackOnly();
			}
			else {
				triggerBeforeCommit(status);
			}
			return result;
		}
		finally {
			triggerBeforeCompletion(status);
			if (status.isNewSynchronization()) {
				List synchronizations = TransactionSynchronizationManager.getSynchronizations();
				cleanupAfterCompletionForCallback(status);
				transaction.registerAfterCompletion(synchronizations);
			}
		}
	}

	private DefaultTransactionStatus prepareTransactionStatusForCallback(
			TransactionDefinition definition, CallbackTransactionObject transaction) {

		// Cache debug flag to avoid repeated checks.
		boolean debugEnabled = logger.isDebugEnabled();
		if (debugEnabled) {
			logger.debug("Using transaction object [" + transaction + "]");
		}

		if (definition == null) {
			// Use defaults if no transaction definition given.
			definition = new DefaultTransactionDefinition();
		}

		DefaultTransactionStatus status = null;

		if (transaction.isExistingTransaction()) {
			// Existing transaction found -> check propagation behavior to find out how to behave.
			status = handleExistingTransactionForCallback(definition, transaction, debugEnabled);
		}

		else {
			// Check definition settings for new transaction.
			if (definition.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) {
				throw new InvalidTimeoutException("Invalid transaction timeout", definition.getTimeout());
			}

			// No existing transaction found -> check propagation behavior to find out how to behave.
			if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
				throw new IllegalTransactionStateException(
						"Transaction propagation 'mandatory' but no existing transaction found");
			}
			else if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
					definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
					definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
				if (debugEnabled) {
					logger.debug("Creating new transaction with name [" + definition.getName() + "]");
				}
				boolean newSynchronization = (this.transactionSynchronization != AbstractPlatformTransactionManager.SYNCHRONIZATION_NEVER);
				status = newTransactionStatus(definition, transaction, true, newSynchronization, debugEnabled, null);
			}
			else {
				// Create "empty" transaction: no actual transaction, but potentially synchronization.
				boolean newSynchronization = (this.transactionSynchronization == AbstractPlatformTransactionManager.SYNCHRONIZATION_ALWAYS);
				status = newTransactionStatus(definition, null, false, newSynchronization, debugEnabled, null);
			}
		}

		return status;
	}

	/**
	 * Create a TransactionStatus for an existing transaction.
	 */
	private DefaultTransactionStatus handleExistingTransactionForCallback(
			TransactionDefinition definition, Object transaction, boolean debugEnabled)
			throws TransactionException {

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER) {
			throw new IllegalTransactionStateException(
					"Transaction propagation 'never' but existing transaction found");
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED) {
			if (debugEnabled) {
				logger.debug("Suspending current transaction");
			}
			Object suspendedResources = suspendForCallback();
			boolean newSynchronization = (this.transactionSynchronization == AbstractPlatformTransactionManager.SYNCHRONIZATION_ALWAYS);
			return newTransactionStatus(
					definition, null, false, newSynchronization, debugEnabled, suspendedResources);
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW) {
			if (debugEnabled) {
				logger.debug("Suspending current transaction, creating new transaction with name [" +
						definition.getName() + "]");
			}
			Object suspendedResources = suspendForCallback();
			boolean newSynchronization = (this.transactionSynchronization != AbstractPlatformTransactionManager.SYNCHRONIZATION_NEVER);
			return newTransactionStatus(
					definition, transaction, true, newSynchronization, debugEnabled, suspendedResources);
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
			if (debugEnabled) {
				logger.debug("Creating nested transaction with name [" + definition.getName() + "]");
			}
			boolean newSynchronization = (this.transactionSynchronization != AbstractPlatformTransactionManager.SYNCHRONIZATION_NEVER);
			return newTransactionStatus(definition, transaction, true, newSynchronization, debugEnabled, null);
		}

		// Assumably PROPAGATION_SUPPORTS.
		if (debugEnabled) {
			logger.debug("Participating in existing transaction");
		}
		boolean newSynchronization = (this.transactionSynchronization != AbstractPlatformTransactionManager.SYNCHRONIZATION_NEVER);
		return newTransactionStatus(definition, transaction, false, newSynchronization, debugEnabled, null);
	}

	/**
	 * Create a new TransactionStatus for the given arguments,
	 * initializing transaction synchronization if appropriate.
	 */
	protected DefaultTransactionStatus newTransactionStatus(
			TransactionDefinition definition, Object transaction, boolean newTransaction,
			boolean newSynchronization, boolean debug, Object suspendedResources) {

		boolean actualNewSynchronization = newSynchronization &&
				!TransactionSynchronizationManager.isSynchronizationActive();
		if (actualNewSynchronization) {
			if (newTransaction) {
				TransactionSynchronizationManager.setActualTransactionActive(true);
			}
			TransactionSynchronizationManager.setCurrentTransactionReadOnly(definition.isReadOnly());
			TransactionSynchronizationManager.setCurrentTransactionName(definition.getName());
			TransactionSynchronizationManager.initSynchronization();
		}
		return new DefaultTransactionStatus(
				transaction, newTransaction, actualNewSynchronization,
				definition.isReadOnly(), debug, suspendedResources);
	}

	/**
	 * Clean up after completion, clearing synchronization if necessary,
	 * and invoking doCleanupAfterCompletion.
	 * @param status object representing the transaction
	 */
	private void cleanupAfterCompletionForCallback(DefaultTransactionStatus status) {
		status.setCompleted();
		if (status.isNewSynchronization()) {
			TransactionSynchronizationManager.clearSynchronization();
			TransactionSynchronizationManager.setCurrentTransactionName(null);
			TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
			if (status.isNewTransaction()) {
				TransactionSynchronizationManager.setActualTransactionActive(false);
			}
		}
		if (status.getSuspendedResources() != null) {
			if (status.isDebug()) {
				logger.debug("Resuming suspended transaction");
			}
			resumeForCallback((SuspendedResourcesHolder) status.getSuspendedResources());
		}
	}

	/**
	 * Suspend the given transaction. Suspends transaction synchronization first,
	 * then delegates to the <code>doSuspend</code> template method.
	 * @return an object that holds suspended resources
	 */
	private SuspendedResourcesHolder suspendForCallback() throws TransactionException {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			List suspendedSynchronizations = TransactionSynchronizationManager.getSynchronizations();
			for (Iterator it = suspendedSynchronizations.iterator(); it.hasNext();) {
				((TransactionSynchronization) it.next()).suspend();
			}
			TransactionSynchronizationManager.clearSynchronization();
			String name = TransactionSynchronizationManager.getCurrentTransactionName();
			TransactionSynchronizationManager.setCurrentTransactionName(null);
			boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
			TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
			TransactionSynchronizationManager.setActualTransactionActive(false);
			return new SuspendedResourcesHolder(suspendedSynchronizations, name, readOnly);
		}
		else {
			return new SuspendedResourcesHolder(null, null, false);
		}
	}

	/**
	 * Resume the given transaction. Delegates to the <code>doResume</code>
	 * template method first, then resuming transaction synchronization.
	 * @param resourcesHolder the object that holds suspended resources,
	 * as returned by suspend
	 */
	private void resumeForCallback(SuspendedResourcesHolder resourcesHolder) throws TransactionException {
		if (resourcesHolder.getSuspendedSynchronizations() != null) {
			TransactionSynchronizationManager.setActualTransactionActive(true);
			TransactionSynchronizationManager.setCurrentTransactionReadOnly(resourcesHolder.isReadOnly());
			TransactionSynchronizationManager.setCurrentTransactionName(resourcesHolder.getName());
			TransactionSynchronizationManager.initSynchronization();
			for (Iterator it = resourcesHolder.getSuspendedSynchronizations().iterator(); it.hasNext();) {
				TransactionSynchronization synchronization = (TransactionSynchronization) it.next();
				synchronization.resume();
				TransactionSynchronizationManager.registerSynchronization(synchronization);
			}
		}
	}
	/**
	 * Trigger beforeCommit callbacks.
	 * @param status object representing the transaction
	 */
	private void triggerBeforeCommit(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			if (status.isDebug()) {
				logger.debug("Triggering beforeCommit synchronization");
			}
			for (Iterator it = TransactionSynchronizationManager.getSynchronizations().iterator(); it.hasNext();) {
				TransactionSynchronization synchronization = (TransactionSynchronization) it.next();
				synchronization.beforeCommit(status.isReadOnly());
			}
		}
	}

	/**
	 * Trigger beforeCompletion callbacks.
	 * @param status object representing the transaction
	 */
	private void triggerBeforeCompletion(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			if (status.isDebug()) {
				logger.debug("Triggering beforeCompletion synchronization");
			}
			for (Iterator it = TransactionSynchronizationManager.getSynchronizations().iterator(); it.hasNext();) {
				TransactionSynchronization synchronization = (TransactionSynchronization) it.next();
				try {
					synchronization.beforeCompletion();
				}
				catch (Throwable tsex) {
					logger.error("TransactionSynchronization.beforeCompletion threw exception", tsex);
				}
			}
		}
	}


	public interface CallbackTransactionObject {

		boolean isExistingTransaction();

		void setRollbackOnly();

		void registerAfterCompletion(List synchronizations);
	}


	/**
	 * Holder for suspended resources.
	 * Used internally by <code>suspend</code> and <code>resume</code>.
	 */
	private static class SuspendedResourcesHolder {

		private final List suspendedSynchronizations;

		private final String name;

		private final boolean readOnly;

		public SuspendedResourcesHolder(List suspendedSynchronizations, String name, boolean readOnly) {
			this.suspendedSynchronizations = suspendedSynchronizations;
			this.name = name;
			this.readOnly = readOnly;
		}

		public List getSuspendedSynchronizations() {
			return suspendedSynchronizations;
		}

		public String getName() {
			return name;
		}

		public boolean isReadOnly() {
			return readOnly;
		}
	}

}
