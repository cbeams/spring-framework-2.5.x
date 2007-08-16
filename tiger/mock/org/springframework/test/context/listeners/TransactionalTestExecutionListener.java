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
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.TestContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * TestExecutionListener which provides support for executing tests within
 * transactions by using the &#064;Transactional and &#064;NotTransactional
 * annotations. Changes to the database during a test run with
 * &#064;Transactional will automatically be rolled back after completion of the
 * test; whereas, changes to the database during a test run with
 * &#064;NotTransactional will be committed after completion of the test.
 *
 * @see Transactional
 * @see NotTransactional
 * @author Sam Brannen
 * @version $Revision: 1.3 $
 * @since 2.1
 */
public class TransactionalTestExecutionListener extends AbstractTestExecutionListener {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	private static final Log					LOG							= LogFactory.getLog(TransactionalTestExecutionListener.class);

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Should we commit the current transaction? */
	private boolean								complete					= false;

	/** Should we roll back by default? */
	private final boolean						rollbackByDefault			= true;

	private final TransactionAttributeSource	transactionAttributeSource	= new AnnotationTransactionAttributeSource();

	/**
	 * Transaction definition used by this test class: by default, a plain
	 * DefaultTransactionDefinition. Subclasses can change this to cause
	 * different behavior.
	 */
	protected TransactionDefinition				transactionDefinition		= new DefaultTransactionDefinition();

	/** Number of transactions started */
	private int									transactionsStarted			= 0;

	/**
	 * TransactionStatus for this test. Typical subclasses won't need to use it.
	 */
	protected TransactionStatus					transactionStatus;

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Creates a transaction before test method execution.
	 * </p>
	 *
	 * @see org.springframework.test.context.listeners.AbstractTestExecutionListener#beforeTestMethod(TestContext)
	 * @throws Exception simply let any exception propagate
	 */
	@Override
	public void beforeTestMethod(final TestContext<?> testContext) {

		final Method testMethod = testContext.getTestMethod();

		if (testMethod.isAnnotationPresent(NotTransactional.class)) {
			this.transactionDefinition = null;
		}
		else {

			final TransactionDefinition explicitTransactionDefinition = this.transactionAttributeSource.getTransactionAttribute(
					testMethod, testContext.getTestClass());
			if (explicitTransactionDefinition != null) {
				if (LOG.isInfoEnabled()) {
					LOG.info("Explicit transaction definition [" + explicitTransactionDefinition
							+ "] found for test context [" + testContext + "].");
				}
				this.transactionDefinition = explicitTransactionDefinition;
			}

			// -----------------------------------------------------------------

			this.complete = !isRollbackByDefault();

			// -----------------------------------------------------------------

			if (getTransactionManager(testContext) == null) {
				if (LOG.isInfoEnabled()) {
					LOG.info("No transaction manager set for test context [" + testContext
							+ "]: test will NOT run within a transaction.");
				}
			}
			else if (this.transactionDefinition == null) {
				if (LOG.isInfoEnabled()) {
					LOG.info("No transaction definition set for test context [" + testContext
							+ "]: test will NOT run within a transaction.");
				}
			}
			else {
				startNewTransaction(testContext);
			}
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Ends the transaction after test method execution if necessary.
	 * </p>
	 *
	 * @see org.springframework.test.context.listeners.AbstractTestExecutionListener#afterTestMethod(TestContext)
	 * @throws Exception simply let any exception propagate
	 */
	@Override
	public void afterTestMethod(final TestContext<?> testContext) {

		// End transaction if the transaction is still active.
		if (this.transactionStatus != null && !this.transactionStatus.isCompleted()) {
			endTransaction(testContext);
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Immediately force a commit or rollback of the transaction, according to
	 * the <code>complete</code> flag.
	 * </p>
	 *
	 * @see #setComplete()
	 */
	protected void endTransaction(final TestContext<?> testContext) {

		if (this.transactionStatus != null) {
			try {
				if (!this.complete) {
					getTransactionManager(testContext).rollback(this.transactionStatus);
					if (LOG.isInfoEnabled()) {
						LOG.info("Rolled back transaction after test execution.");
					}
				}
				else {
					getTransactionManager(testContext).commit(this.transactionStatus);
					if (LOG.isInfoEnabled()) {
						LOG.info("Committed transaction after test execution.");
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
	 * Gets the {@link PlatformTransactionManager transaction manager} to use
	 * for the supplied {@link TestContext test context}.
	 *
	 * @param testContext The test context for which the transaction manager
	 *        should be retrieved.
	 * @return The transaction manager to use, or <code>null</code> if not
	 *         found.
	 */
	protected final PlatformTransactionManager getTransactionManager(final TestContext<?> testContext) {

		// TODO Make transactionManagerName configurable via an annotation
		// attribute.
		final String transactionManagerName = "transactionManager";
		PlatformTransactionManager transactionManager = null;

		try {
			transactionManager = (PlatformTransactionManager) testContext.getApplicationContext().getBean(
					transactionManagerName, PlatformTransactionManager.class);
		}
		catch (final Exception e) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Caught exception while retrieving transaction manager with bean name ["
						+ transactionManagerName + "] for test context [" + testContext + "].", e);
			}
		}

		return transactionManager;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Whether or not to rollback transactions by default.
	 *
	 * @return The default <em>rollback</em> flag.
	 */
	protected final boolean isRollbackByDefault() {

		// TODO Parse the new @Rollback annotation... (?)

		return this.rollbackByDefault;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Cause the transaction to commit for this test method, even if default is
	 * set to rollback.
	 *
	 * @throws IllegalStateException if the operation cannot be set to complete
	 *         as no transaction manager was provided
	 */
	protected final void setComplete(final TestContext<?> testContext) {

		if (getTransactionManager(testContext) == null) {
			throw new IllegalStateException("No transaction manager set");
		}
		this.complete = true;
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
	protected void startNewTransaction(final TestContext<?> testContext) throws TransactionException {

		if (this.transactionStatus != null) {
			throw new IllegalStateException("Cannot start new transaction without ending existing transaction: "
					+ "Invoke endTransaction() before startNewTransaction().");
		}
		if (getTransactionManager(testContext) == null) {
			throw new IllegalStateException("No transaction manager set.");
		}

		this.transactionStatus = getTransactionManager(testContext).getTransaction(this.transactionDefinition);
		++this.transactionsStarted;
		this.complete = !isRollbackByDefault();

		if (LOG.isInfoEnabled()) {
			LOG.info("Began transaction (" + this.transactionsStarted + "): transaction manager ["
					+ getTransactionManager(testContext) + "]; default rollback [" + isRollbackByDefault() + "].");
		}
	}

	// ------------------------------------------------------------------------|

}
