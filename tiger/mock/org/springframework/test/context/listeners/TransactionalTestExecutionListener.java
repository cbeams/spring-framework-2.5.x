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
import org.springframework.test.annotation.TransactionConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TransactionConfigurationAttributes;
import org.springframework.test.context.support.DefaultTransactionConfigurationAttributes;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;

/**
 * <p>
 * TestExecutionListener which provides support for executing tests within
 * transactions by using Spring's &#064;Transactional and &#064;NotTransactional
 * annotations.
 * </p>
 * <p>
 * Changes to the database during a test run with &#064;Transactional will be
 * run within a transaction that will be automatically <em>rolled back</em>
 * after completion of the test; whereas, changes to the database during a test
 * run with &#064;NotTransactional will <strong>not</strong> be run within a
 * transaction.
 * </p>
 * <p>
 * TODO Comment on configuration via {@link TransactionConfiguration}.
 * </p>
 *
 * @see Transactional
 * @see NotTransactional
 * @author Sam Brannen
 * @version $Revision: 1.4 $
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

	/**
	 * TransactionAttributeSource for the current test. Typical subclasses won't
	 * need to use it.
	 */
	protected final TransactionAttributeSource	transactionAttributeSource	= new AnnotationTransactionAttributeSource();

	/**
	 * Transaction definition used by this test class: by default, a plain
	 * DefaultTransactionDefinition. Subclasses can change this to cause
	 * different behavior.
	 */
	protected TransactionDefinition				transactionDefinition;

	/** Number of transactions started */
	private int									transactionsStarted			= 0;

	/**
	 * TransactionStatus for the current test. Typical subclasses won't need to
	 * use it.
	 */
	protected TransactionStatus					transactionStatus;

	/**
	 * Has the current transaction been marked as 'complete'? In other words,
	 * should we commit the current transaction upon completion of the current
	 * test?
	 */
	private boolean								transactionMarkedComplete	= false;

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Retrieves the {@link TransactionConfigurationAttributes} for the
	 * specified {@link Class class}.
	 * </p>
	 *
	 * @param clazz The Class object corresponding to the test class for which
	 *        the configuration attributes should be retrieved.
	 * @return a new TransactionConfigurationAttributes instance for the
	 *         specified class.
	 * @throws IllegalArgumentException if the supplied class is
	 *         <code>null</code>.
	 */
	protected static TransactionConfigurationAttributes retrieveConfigurationAttributes(final Class<?> clazz)
			throws IllegalArgumentException {

		Assert.notNull(clazz, "Can not retrieve transaction configuration attributes for a NULL class.");
		final TransactionConfigurationAttributes configAttributes = DefaultTransactionConfigurationAttributes.constructAttributes(clazz);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Retrieved transaction configuration attributes [" + configAttributes + "] for class [" + clazz
					+ "].");
		}

		return configAttributes;
	}

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

		// Abort early?
		if (testMethod.isAnnotationPresent(NotTransactional.class)) {
			this.transactionDefinition = null;
			return;
		}

		// ---------------------------------------------------------------------
		// else...

		this.transactionDefinition = new DefaultTransactionDefinition();
		final TransactionDefinition explicitTransactionDefinition = this.transactionAttributeSource.getTransactionAttribute(
				testMethod, testContext.getTestClass());
		if (explicitTransactionDefinition != null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Explicit transaction definition [" + explicitTransactionDefinition
						+ "] found for test context [" + testContext + "].");
			}
			this.transactionDefinition = explicitTransactionDefinition;
		}

		// ---------------------------------------------------------------------

		if (getTransactionManager(testContext) == null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("No transaction manager set for test context [" + testContext
						+ "]: test will NOT run within a transaction.");
			}
		}
		else if (this.transactionDefinition == null) {
			// This should theoretically never occur!
			if (LOG.isWarnEnabled()) {
				LOG.warn("No transaction definition set for test context [" + testContext
						+ "]: test will NOT run within a transaction.");
			}
		}
		else {
			startNewTransaction(testContext);
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
	 * Start a new transaction for the supplied {@link TestContext test context}.
	 * </p>
	 * <p>
	 * Only call this method if {@link #endTransaction()} has been called or if
	 * no transaction has been previously started.
	 * {@link #markTransactionComplete(TestContext)} can be used again in the
	 * new transaction. The fate of the new transaction, by default, will be the
	 * usual {@link #isDefaultRollback() rollback}.
	 * </p>
	 *
	 * @param testContext The current test context.
	 * @throws TransactionException If starting the transaction failed.
	 */
	protected void startNewTransaction(final TestContext<?> testContext) throws TransactionException {

		if (this.transactionStatus != null) {
			throw new IllegalStateException("Cannot start new transaction without ending existing transaction: "
					+ "Invoke endTransaction() before startNewTransaction().");
		}
		final PlatformTransactionManager transactionManager = getTransactionManager(testContext);
		if (transactionManager == null) {
			throw new IllegalStateException("No transaction manager set for test context [" + testContext + "].");
		}

		this.transactionStatus = transactionManager.getTransaction(this.transactionDefinition);
		++this.transactionsStarted;
		this.transactionMarkedComplete = !isDefaultRollback(testContext);

		if (LOG.isInfoEnabled()) {
			LOG.info("Began transaction (" + this.transactionsStarted + "): transaction manager [" + transactionManager
					+ "]; default rollback [" + isDefaultRollback(testContext) + "].");
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Immediately force a commit or rollback of the transaction for the
	 * supplied {@link TestContext test context}, according to the
	 * {@link #isTransactionMarkedComplete(TestContext) complete} flag.
	 * </p>
	 *
	 * @see #isTransactionMarkedComplete(TestContext)
	 * @param testContext The current test context.
	 * @throws TransactionException If starting the transaction failed.
	 */
	protected void endTransaction(final TestContext<?> testContext) throws TransactionException {

		// XXX Parse the yet-to-be-defined @Rollback annotation(?)

		if (this.transactionStatus != null) {
			try {
				if (!isTransactionMarkedComplete(testContext)) {
					getTransactionManager(testContext).rollback(this.transactionStatus);
					if (LOG.isInfoEnabled()) {
						LOG.info("Rolled back transaction after test execution for test context [" + testContext + "].");
					}
				}
				else {
					getTransactionManager(testContext).commit(this.transactionStatus);
					if (LOG.isInfoEnabled()) {
						LOG.info("Committed transaction after test execution for test context [" + testContext + "].");
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

		final TransactionConfigurationAttributes configAttributes = retrieveConfigurationAttributes(testContext.getTestClass());
		final String transactionManagerName = configAttributes.getTransactionManagerName();
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
	 * Determines whether or not to rollback transactions by default for the
	 * supplied {@link TestContext test context}.
	 *
	 * @param testContext The test context for which the default rollback flag
	 *        should be retrieved.
	 * @return The <em>default rollback</em> flag for the supplied test
	 *         context.
	 */
	protected final boolean isDefaultRollback(final TestContext<?> testContext) {

		return retrieveConfigurationAttributes(testContext.getTestClass()).isDefaultRollback();
	}

	// ------------------------------------------------------------------------|

	/**
	 * Has the current transaction for the supplied
	 * {@link TestContext test context} been marked as <em>complete</em>? In
	 * other words, should the current transaction be committed upon completion
	 * of the current test despite the value of {@link #isDefaultRollback()}?
	 *
	 * @see #markTransactionComplete(TestContext)
	 * @see #isDefaultRollback()
	 * @param testContext The current test context.
	 * @return The <em>transactionMarkedComplete</em> flag.
	 */
	protected final boolean isTransactionMarkedComplete(final TestContext<?> testContext) {

		return this.transactionMarkedComplete;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Mark the current transaction as <em>complete</em>, thus causing the
	 * transaction to <em>commit</em> for the current test method, even if the
	 * default is set to rollback.
	 *
	 * @see #isDefaultRollback()
	 * @throws IllegalStateException if the operation cannot be set to complete
	 *         as no transaction manager was provided
	 */
	protected final void markTransactionComplete(final TestContext<?> testContext) {

		if (getTransactionManager(testContext) == null) {
			throw new IllegalStateException("No transaction manager set.");
		}
		this.transactionMarkedComplete = true;
	}

	// ------------------------------------------------------------------------|

}
