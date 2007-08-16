/*
 * Copyright 2007 the original author or authors.
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
package org.springframework.test.context.listeners;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.TestContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * TODO Add comments for TransactionalTestExecutionListener.
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
public class TransactionalTestExecutionListener extends AbstractTestExecutionListener {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	private static final Log LOG = LogFactory.getLog(TransactionalTestExecutionListener.class);

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Should we commit the current transaction? */
	private boolean complete = false;

	/** Should we roll back by default? */
	private final boolean defaultRollback = true;

	private final TransactionAttributeSource transactionAttributeSource = new AnnotationTransactionAttributeSource();

	/**
	 * Transaction definition used by this test class: by default, a plain
	 * DefaultTransactionDefinition. Subclasses can change this to cause
	 * different behavior.
	 */
	protected TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();

	/** The transaction manager to use */
	protected PlatformTransactionManager transactionManager;

	/** Number of transactions started */
	private int transactionsStarted = 0;

	/**
	 * TransactionStatus for this test. Typical subclasses won't need to use it.
	 */
	protected TransactionStatus transactionStatus;

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * TODO Update JavaDoc.
	 * <p>
	 * Overrides afterTestMethod(): This implementation ends the transaction
	 * after test execution.
	 * <p>
	 * Override {@link #onTearDownInTransaction()} and/or
	 * {@link #onTearDownAfterTransaction()} to add custom tear-down behavior
	 * for transactional execution. Alternatively, override this method for
	 * general tear-down behavior, calling <code>super.onTearDown()</code> as
	 * part of your method implementation.
	 * <p>
	 * Note that {@link #onTearDownInTransaction()} will only be called if a
	 * transaction is still active at the time of the test shutdown. In
	 * particular, it will <i>not</i> be called if the transaction has been
	 * completed with an explicit {@link #endTransaction()} call before.
	 *
	 * @throws Exception simply let any exception propagate
	 * @see org.springframework.test.context.listeners.AbstractTestExecutionListener#afterTestMethod(org.springframework.test.context.TestContext,
	 *      java.lang.Throwable)
	 */
	@Override
	public void afterTestMethod(final TestContext<?> testContext, final Throwable t) {

		super.afterTestMethod(testContext, t);

		final Method testMethod = testContext.getTestMethod();
		if (LOG.isDebugEnabled()) {
			LOG.debug("afterTestMethodExecution(): method [" + testMethod + "].");
		}

		// End transaction if the transaction is still active.
		if (this.transactionStatus != null && !this.transactionStatus.isCompleted()) {
			endTransaction();
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * TODO Update JavaDoc.
	 * <p>
	 * Overrides beforeTestMethod(): this implementation creates a transaction
	 * before test execution.
	 * <p>
	 * Override {@link #onSetUpBeforeTransaction()} and/or
	 * {@link #onSetUpInTransaction()} to add custom set-up behavior for
	 * transactional execution. Alternatively, override this method for general
	 * set-up behavior, calling <code>super.onSetUp()</code> as part of your
	 * method implementation.
	 *
	 * @throws Exception simply let any exception propagate
	 * @see org.springframework.test.context.listeners.AbstractTestExecutionListener#beforeTestMethod(org.springframework.test.context.TestContext)
	 */
	@Override
	public void beforeTestMethod(final TestContext<?> testContext) {

		super.beforeTestMethod(testContext);

		final Method testMethod = testContext.getTestMethod();

		if (LOG.isDebugEnabled()) {
			LOG.debug("beforeTestMethodExecution(): method [" + testMethod + "].");
		}

		// ---------------------------------------------------------------------

		this.complete = !isDefaultRollback();

		if (getTransactionManager() == null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("No transaction manager set: test will NOT run within a transaction");
			}
		}
		else if (this.transactionDefinition == null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("No transaction definition set: test will NOT run within a transaction");
			}
		}
		else {
			startNewTransaction();
		}

		// ---------------------------------------------------------------------

		final TransactionDefinition explicitTransactionDefinition = this.transactionAttributeSource.getTransactionAttribute(
				testMethod, testContext.getTestClass());
		if (explicitTransactionDefinition != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Custom transaction definition [" + explicitTransactionDefinition + " for test method ["
						+ testMethod + "].");
			}
			setTransactionDefinition(explicitTransactionDefinition);
		}
		else if (testMethod.isAnnotationPresent(NotTransactional.class)) {
			// Don't have any transaction...
			preventTransaction();
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * Immediately force a commit or rollback of the transaction, according to
	 * the complete flag.
	 * <p>
	 * Can be used to explicitly let the transaction end early, for example to
	 * check whether lazy associations of persistent objects work outside of a
	 * transaction (that is, have been initialized properly).
	 *
	 * @see #setComplete()
	 */
	protected void endTransaction() {

		if (this.transactionStatus != null) {
			try {
				if (!this.complete) {
					getTransactionManager().rollback(this.transactionStatus);
					if (LOG.isInfoEnabled()) {
						LOG.info("Rolled back transaction after test execution");
					}
				}
				else {
					getTransactionManager().commit(this.transactionStatus);
					if (LOG.isInfoEnabled()) {
						LOG.info("Committed transaction after test execution");
					}
				}
			}
			finally {
				this.transactionStatus = null;
			}
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * @return The PlatformTransactionManager.
	 */
	protected final PlatformTransactionManager getTransactionManager() {

		return this.transactionManager;
	}

	// ------------------------------------------------------------------------|

	/**
	 * @return Returns the defaultRollback.
	 */
	public final boolean isDefaultRollback() {

		// TODO Parse the new @Rollback annotation...

		return this.defaultRollback;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Call this method in an overridden {@link #runBare()} method to prevent
	 * transactional execution.
	 */
	protected void preventTransaction() {

		this.transactionDefinition = null;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Cause the transaction to commit for this test method, even if default is
	 * set to rollback.
	 *
	 * @throws IllegalStateException if the operation cannot be set to complete
	 *         as no transaction manager was provided
	 */
	protected final void setComplete() {

		if (getTransactionManager() == null) {
			throw new IllegalStateException("No transaction manager set");
		}
		this.complete = true;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Call this method in an overridden {@link #runBare()} method to override
	 * the transaction attributes that will be used, so that {@link #setUp()}
	 * and {@link #tearDown()} behavior is modified.
	 *
	 * @param customDefinition the custom transaction definition
	 */
	protected final void setTransactionDefinition(final TransactionDefinition customDefinition) {

		this.transactionDefinition = customDefinition;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Specify the transaction manager to use. No transaction management will be
	 * available if this is not set. Populated through dependency injection by
	 * the superclass.
	 * <p>
	 * This mode works only if dependency checking is turned off in the
	 * {@link AbstractDependencyInjectionSpringContextTests} superclass.
	 */
	public final void setTransactionManager(final PlatformTransactionManager transactionManager) {

		this.transactionManager = transactionManager;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Start a new transaction. Only call this method if
	 * {@link #endTransaction()} has been called. {@link #setComplete()} can be
	 * used again in the new transaction. The fate of the new transaction, by
	 * default, will be the usual rollback.
	 *
	 * @throws TransactionException if starting the transaction failed
	 */
	protected void startNewTransaction() throws TransactionException {

		if (this.transactionStatus != null) {
			throw new IllegalStateException("Cannot start new transaction without ending existing transaction: "
					+ "Invoke endTransaction() before startNewTransaction()");
		}
		if (getTransactionManager() == null) {
			throw new IllegalStateException("No transaction manager set");
		}

		this.transactionStatus = getTransactionManager().getTransaction(this.transactionDefinition);
		++this.transactionsStarted;
		this.complete = !isDefaultRollback();

		if (LOG.isInfoEnabled()) {
			LOG.info("Began transaction (" + this.transactionsStarted + "): transaction manager ["
					+ getTransactionManager() + "]; default rollback = " + isDefaultRollback());
		}
	}

	// ------------------------------------------------------------------------|

}
