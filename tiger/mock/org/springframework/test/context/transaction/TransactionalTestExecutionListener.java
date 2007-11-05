/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.test.context.transaction;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * <p>
 * <code>TestExecutionListener</code> which provides support for executing
 * tests within transactions by using
 * {@link org.springframework.transaction.annotation.Transactional @Transactional}
 * and {@link NotTransactional @NotTransactional} annotations.
 * </p>
 * <p>
 * Changes to the database during a test run with &#064;Transactional will be
 * run within a transaction that will, by default, be automatically
 * <em>rolled back</em> after completion of the test; whereas, changes to the
 * database during a test run with &#064;NotTransactional will <strong>not</strong>
 * be run within a transaction. Similarly, test methods that are not annotated
 * with either &#064;Transactional (at the class or method level) or
 * &#064;NotTransactional will not be run within a transaction.
 * </p>
 * <p>
 * Transactional commit and rollback behavior can be configured via the
 * class-level {@link TransactionConfiguration @TransactionConfiguration} and
 * method-level {@link Rollback @Rollback} annotations.
 * {@link TransactionConfiguration @TransactionConfiguration} also provides
 * configuration of the bean name of the {@link PlatformTransactionManager} that
 * is to be used to drive transactions.
 * </p>
 * <p>
 * When executing transactional tests, it is sometimes useful to be able execute
 * certain <em>set up</em> or <em>tear down</em> code outside of a
 * transaction. <code>TransactionalTestExecutionListener</code> provides such
 * support for methods annotated with
 * {@link BeforeTransaction @BeforeTransaction} and
 * {@link AfterTransaction @AfterTransaction}.
 * </p>
 *
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @since 2.5
 * @see TransactionConfiguration
 * @see org.springframework.transaction.annotation.Transactional
 * @see org.springframework.test.annotation.NotTransactional
 * @see org.springframework.test.annotation.Rollback
 * @see BeforeTransaction
 * @see AfterTransaction
 */
public class TransactionalTestExecutionListener extends AbstractTestExecutionListener {

	private static final Log logger = LogFactory.getLog(TransactionalTestExecutionListener.class);

	/**
	 * TransactionAttributeSource for the current test. Typical subclasses won't
	 * need to use it.
	 */
	protected final TransactionAttributeSource transactionAttributeSource = new AnnotationTransactionAttributeSource();

	/**
	 * Transaction definition used by this
	 * <code>TransactionalTestExecutionListener</code>.
	 */
	protected TransactionDefinition transactionDefinition;

	private PlatformTransactionManager transactionManager = null;

	/**
	 * Number of transactions started.
	 */
	private int transactionsStarted = 0;

	/**
	 * TransactionStatus for the current test. Typical subclasses won't need to
	 * use it.
	 */
	protected TransactionStatus transactionStatus;

	/**
	 * Has the current transaction been flagged for 'commit'? In other words,
	 * should we commit the current transaction upon completion of the current
	 * test?
	 */
	private boolean transactionFlaggedForCommit = false;


	/**
	 * <p>
	 * If the test method of the supplied {@link TestContext test context} is
	 * configured to run within a transaction, this method will run
	 * {@link BeforeTransaction @BeforeTransaction methods} and start a new
	 * transaction.
	 * </p>
	 * <p>
	 * Note that if a {@link BeforeTransaction @BeforeTransaction method} fails,
	 * remaining {@link BeforeTransaction @BeforeTransaction methods} will not
	 * be invoked, and a transaction will not be started.
	 * </p>
	 *
	 * @see org.springframework.transaction.annotation.Transactional
	 * @see org.springframework.test.annotation.NotTransactional
	 */
	@Override
	public void beforeTestMethod(final TestContext testContext) throws Exception {

		this.transactionDefinition = null;
		final Method testMethod = testContext.getTestMethod();
		Assert.notNull(testMethod, "The test method of the supplied TestContext must not be null.");

		// Abort early?
		if (testMethod.isAnnotationPresent(NotTransactional.class)) {
			this.transactionStatus = null;
			return;
		}

		// else...

		final TransactionDefinition explicitTransactionDefinition = this.transactionAttributeSource.getTransactionAttribute(
				testMethod, testContext.getTestClass());
		if (explicitTransactionDefinition != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Explicit transaction definition [" + explicitTransactionDefinition
						+ "] found for test context [" + testContext + "].");
			}
			this.transactionDefinition = explicitTransactionDefinition;
		}

		if (this.transactionDefinition != null) {
			if (getTransactionManager(testContext) == null) {
				if (logger.isWarnEnabled()) {
					logger.warn("Transaction definition was set for test context [" + testContext
							+ "], but no transaction manager was set: test will NOT run within a transaction.");
				}
			}
			else {
				runBeforeTransactionMethods(testContext);
				startNewTransaction(testContext);
			}
		}
	}

	/**
	 * <p>
	 * If a transaction is currently active for the test method of the supplied
	 * {@link TestContext test context}, this method will end the transaction
	 * and run {@link AfterTransaction @AfterTransaction methods}.
	 * </p>
	 * <p>
	 * {@link AfterTransaction @AfterTransaction methods} are guaranteed to be
	 * invoked even if an error occurs while ending the transaction.
	 * </p>
	 */
	@Override
	public void afterTestMethod(final TestContext testContext) throws Exception {

		// Note: the test method is not used directly in this method but is
		// required by endTransaction(), etc.
		Assert.notNull(testContext.getTestMethod(), "The test method of the supplied TestContext must not be null.");

		// If the transaction is still active...
		if (this.transactionStatus != null && !this.transactionStatus.isCompleted()) {
			try {
				endTransaction(testContext);
			}
			finally {
				runAfterTransactionMethods(testContext);
			}
		}
	}

	/**
	 * Runs all {@link BeforeTransaction @BeforeTransaction methods} for the
	 * specified {@link TestContext test context}. If one of the methods fails,
	 * however, the caught exception will be rethrown in a wrapped
	 * {@link RuntimeException}, and the remaining methods will <strong>not</strong>
	 * be given a chance to execute.
	 *
	 * @param testContext the current test context.
	 */
	protected void runBeforeTransactionMethods(final TestContext testContext) throws Exception {
		try {
			final List<Method> methods = getAnnotatedMethods(testContext.getTestClass(), BeforeTransaction.class);
			Collections.reverse(methods);
			for (final Method method : methods) {
				if (logger.isDebugEnabled()) {
					logger.debug("Executing @BeforeTransaction method [" + method + "] for test context ["
							+ testContext + "].");
				}
				method.invoke(testContext.getTestInstance());
			}
		}
		catch (InvocationTargetException ex) {
			logger.error("Exception encountered while executing @BeforeTransaction methods for test context ["
					+ testContext + "].", ex.getTargetException());
			ReflectionUtils.rethrowException(ex.getTargetException());
		}
	}

	/**
	 * Runs all {@link AfterTransaction @AfterTransaction methods} for the
	 * specified {@link TestContext test context}. If one of the methods fails,
	 * the caught exception will be logged as an error, and the remaining
	 * methods will be given a chance to execute. After all methods have
	 * executed, the first caught exception, if any, will be rethrown.
	 *
	 * @param testContext the current test context.
	 */
	protected void runAfterTransactionMethods(final TestContext testContext) throws Exception {

		Throwable afterTransactionException = null;

		final List<Method> methods = getAnnotatedMethods(testContext.getTestClass(), AfterTransaction.class);
		for (final Method method : methods) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Executing @AfterTransaction method [" + method + "] for test context [" + testContext
							+ "].");
				}
				method.invoke(testContext.getTestInstance());
			}
			catch (InvocationTargetException ex) {
				final Throwable targetException = ex.getTargetException();
				if (afterTransactionException == null) {
					afterTransactionException = targetException;
				}
				logger.error("Exception encountered while executing @AfterTransaction method [" + method
						+ "] for test context [" + testContext + "].", targetException);
			}
			catch (Exception ex) {
				if (afterTransactionException == null) {
					afterTransactionException = ex;
				}
				logger.error("Exception encountered while executing @AfterTransaction method [" + method
						+ "] for test context [" + testContext + "].", ex);
			}
		}

		if (afterTransactionException != null) {
			ReflectionUtils.rethrowException(afterTransactionException);
		}
	}

	/**
	 * <p>
	 * Start a new transaction for the supplied {@link TestContext test context}.
	 * </p>
	 * <p>
	 * Only call this method if {@link #endTransaction} has been called or if no
	 * transaction has been previously started.
	 * </p>
	 * <p>
	 * {@link #flagTransactionForCommit(TestContext)} can be used in the new
	 * transaction. The fate of the new transaction will then, by default, be
	 * determined by the configured {@link #isRollback rollback flag}.
	 * </p>
	 *
	 * @param testContext the current test context.
	 * @throws TransactionException if starting the transaction fails.
	 * @throws Exception if an error occurs while retrieving the transaction
	 *         manager.
	 */
	protected void startNewTransaction(final TestContext testContext) throws TransactionException, Exception {

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
		this.transactionFlaggedForCommit = !isRollback(testContext);

		if (logger.isInfoEnabled()) {
			logger.info("Began transaction (" + this.transactionsStarted + "): transaction manager ["
					+ transactionManager + "]; rollback [" + isRollback(testContext) + "]; flagged for commmit ["
					+ this.transactionFlaggedForCommit + "].");
		}
	}

	/**
	 * <p>
	 * Immediately force a <em>commit</em> or <em>rollback</em> of the
	 * transaction for the supplied {@link TestContext test context}, according
	 * to the {@link #isTransactionFlaggedForCommit(TestContext) commit} and
	 * {@link #isRollback(TestContext) rollback} flags.
	 * </p>
	 *
	 * @param testContext the current test context.
	 * @throws TransactionException if ending the transaction fails.
	 * @throws Exception if an error occurs while retrieving the transaction
	 *         manager.
	 * @see #isTransactionFlaggedForCommit(TestContext)
	 */
	protected void endTransaction(final TestContext testContext) throws TransactionException, Exception {

		final boolean commit = isTransactionFlaggedForCommit(testContext) || !isRollback(testContext);

		if (logger.isTraceEnabled()) {
			logger.trace("Ending transaction for test context [" + testContext + "]; transactionStatus ["
					+ this.transactionStatus + "]; commit [" + commit + "].");
		}

		if (this.transactionStatus != null) {
			try {
				if (commit) {
					getTransactionManager(testContext).commit(this.transactionStatus);
					if (logger.isInfoEnabled()) {
						logger.info("Committed transaction after test execution for test context [" + testContext
								+ "].");
					}
				}
				else {
					getTransactionManager(testContext).rollback(this.transactionStatus);
					if (logger.isInfoEnabled()) {
						logger.info("Rolled back transaction after test execution for test context [" + testContext
								+ "].");
					}
				}
			}
			finally {
				this.transactionStatus = null;
			}
		}
	}

	/**
	 * Gets the {@link PlatformTransactionManager transaction manager} to use
	 * for the supplied {@link TestContext test context}.
	 *
	 * @param testContext the test context for which the transaction manager
	 * should be retrieved.
	 * @return the transaction manager to use, or <code>null</code> if not found.
	 * @throws Exception if an error occurs while retrieving the transaction manager.
	 */
	protected final PlatformTransactionManager getTransactionManager(final TestContext testContext) throws Exception {

		// Lazy load:
		if (this.transactionManager != null) {
			return this.transactionManager;
		}

		// else...

		final TransactionConfigurationAttributes configAttributes = retrieveTransactionConfigurationAttributes(testContext.getTestClass());
		final String transactionManagerName = configAttributes.getTransactionManagerName();

		try {
			this.transactionManager = (PlatformTransactionManager) testContext.getApplicationContext().getBean(
					transactionManagerName, PlatformTransactionManager.class);
		}
		catch (final Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Caught exception while retrieving transaction manager with bean name ["
						+ transactionManagerName + "] for test context [" + testContext + "].", e);
			}
			throw e;
		}

		return this.transactionManager;
	}

	/**
	 * Determines whether or not to rollback transactions by default for the
	 * supplied {@link TestContext test context}.
	 *
	 * @param testContext the test context for which the default rollback flag
	 * should be retrieved.
	 * @return the <em>default rollback</em> flag for the supplied test context.
	 * @throws Exception if an error occurs while determining the default rollback flag.
	 */
	protected final boolean isDefaultRollback(final TestContext testContext) throws Exception {
		return retrieveTransactionConfigurationAttributes(testContext.getTestClass()).isDefaultRollback();
	}

	/**
	 * Determines whether or not to rollback transactions for the supplied
	 * {@link TestContext test context} by taking into consideration the
	 * {@link #isDefaultRollback(TestContext) default rollback} flag and a
	 * possible method-level override via the {@link Rollback} annotation.
	 *
	 * @param testContext the test context for which the rollback flag
	 * should be retrieved.
	 * @return the <em>rollback</em> flag for the supplied test context.
	 * @throws Exception if an error occurs while determining the rollback flag.
	 */
	protected final boolean isRollback(final TestContext testContext) throws Exception {

		boolean rollback = isDefaultRollback(testContext);
		final Rollback rollbackAnnotation = testContext.getTestMethod().getAnnotation(Rollback.class);
		if (rollbackAnnotation != null) {

			final boolean rollbackOverride = rollbackAnnotation.value();
			if (logger.isDebugEnabled()) {
				logger.debug("Method-level @Rollback(" + rollbackOverride + ") overrides default rollback [" + rollback
						+ "] for test context [" + testContext + "].");
			}
			rollback = rollbackOverride;
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("No method-level @Rollback override: using default rollback [" + rollback
						+ "] for test context [" + testContext + "].");
			}
		}

		return rollback;
	}

	/**
	 * Has the current transaction for the supplied
	 * {@link TestContext test context} been <em>flagged for commit</em>? In
	 * other words, should the current transaction be committed upon completion
	 * of the current test regardless of the value of the
	 * {@link #isRollback(TestContext) rollback flag}?
	 *
	 * @param testContext the current test context.
	 * @return the <em>transactionFlaggedForCommit</em> flag.
	 * @see #flagTransactionForCommit(TestContext)
	 * @see #isRollback(TestContext)
	 */
	protected final boolean isTransactionFlaggedForCommit(final TestContext testContext) {
		return this.transactionFlaggedForCommit;
	}

	/**
	 * Flag the current transaction for <em>commit</em>, thus causing the
	 * transaction to be committed for the current test method, even if the
	 * {@link #isRollback(TestContext) rollback flag} is <code>true</code>.
	 *
	 * @throws IllegalStateException if the transaction cannot be flagged for
	 *         commit because no transaction manager was provided.
	 * @see #isRollback(TestContext)
	 */
	protected final void flagTransactionForCommit(final TestContext testContext) throws IllegalStateException {
		try {
			if (getTransactionManager(testContext) == null) {
				throw new IllegalStateException("No transaction manager set.");
			}
		}
		catch (final Exception e) {
			throw new IllegalStateException("No transaction manager set.", e);
		}
		this.transactionFlaggedForCommit = true;
	}

	/**
	 * <p>
	 * Gets all superclasses of the supplied {@link Class class}, including the
	 * class itself. The ordering of the returned list will begin with the
	 * supplied class and continue up the class hierarchy.
	 * </p>
	 * <p>
	 * Note: This code has been borrowed from
	 * {@link org.junit.internal.runners.TestClass#getSuperClasses(Class)} and
	 * adapted.
	 * </p>
	 *
	 * @param clazz the class for which to retrieve the superclasses.
	 * @return all superclasses of the supplied class.
	 */
	private List<Class<?>> getSuperClasses(final Class<?> clazz) {
		final ArrayList<Class<?>> results = new ArrayList<Class<?>>();
		Class<?> current = clazz;
		while (current != null) {
			results.add(current);
			current = current.getSuperclass();
		}
		return results;
	}

	/**
	 * <p>
	 * Gets all methods in the supplied {@link Class class} and its superclasses
	 * which are annotated with the supplied <code>annotationType</code> but
	 * which are not <em>shadowed</em> by methods overridden in subclasses.
	 * </p>
	 * <p>
	 * Note: This code has been borrowed from
	 * {@link org.junit.internal.runners.TestClass#getAnnotatedMethods(Class)}
	 * and adapted.
	 * </p>
	 *
	 * @param clazz the class for which to retrieve the annotated methods.
	 * @param annotationType the annotation type for which to search.
	 * @return All annotated methods in the supplied class and its superclasses.
	 */
	private List<Method> getAnnotatedMethods(final Class<?> clazz, final Class<? extends Annotation> annotationType) {
		final List<Method> results = new ArrayList<Method>();
		for (final Class<?> eachClass : getSuperClasses(clazz)) {
			final Method[] methods = eachClass.getDeclaredMethods();
			for (final Method eachMethod : methods) {
				final Annotation annotation = eachMethod.getAnnotation(annotationType);
				if (annotation != null && !isShadowed(eachMethod, results)) {
					results.add(eachMethod);
				}
			}
		}
		return results;
	}

	/**
	 * <p>
	 * Determines if the supplied {@link Method method} is <em>shadowed</em>
	 * by a method in supplied {@link List list} of previous methods.
	 * </p>
	 * <p>
	 * Note: This code has been borrowed from
	 * {@link org.junit.internal.runners.TestClass#isShadowed(Method,List)}.
	 * </p>
	 *
	 * @param method the method to check for shadowing.
	 * @param previousMethods the list of methods which have previously been
	 *        processed.
	 * @return <code>true</code> if the supplied method is shadowed by a
	 *         method in the <code>previousMethods</code> list.
	 */
	private boolean isShadowed(final Method method, final List<Method> previousMethods) {
		for (final Method each : previousMethods) {
			if (isShadowed(method, each)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * Determines if the supplied {@link Method current method} is
	 * <em>shadowed</em> by a {@link Method previous method}.
	 * </p>
	 * <p>
	 * Note: This code has been borrowed from
	 * {@link org.junit.internal.runners.TestClass#isShadowed(Method,Method)}.
	 * </p>
	 *
	 * @param current the current method.
	 * @param previous the previous method.
	 * @return <code>true</code> if the previous method shadows the current
	 *         one.
	 */
	private boolean isShadowed(final Method current, final Method previous) {
		if (!previous.getName().equals(current.getName())) {
			return false;
		}
		if (previous.getParameterTypes().length != current.getParameterTypes().length) {
			return false;
		}
		for (int i = 0; i < previous.getParameterTypes().length; i++) {
			if (!previous.getParameterTypes()[i].equals(current.getParameterTypes()[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>
	 * Retrieves the {@link TransactionConfigurationAttributes} for the
	 * specified {@link Class class} which may optionally declare or inherit a
	 * {@link TransactionConfiguration @TransactionConfiguration}. If a
	 * {@link TransactionConfiguration} annotation is not present for the
	 * supplied class, the <em>default values</em> for attributes defined in
	 * {@link TransactionConfiguration} will be used instead.
	 *
	 * @param clazz the Class object corresponding to the test class for which
	 *        the configuration attributes should be retrieved.
	 * @return a new TransactionConfigurationAttributes instance.
	 * @throws IllegalArgumentException if the supplied class is
	 *         <code>null</code>.
	 */
	private TransactionConfigurationAttributes retrieveTransactionConfigurationAttributes(final Class<?> clazz) {

		Assert.notNull(clazz, "Can not retrieve transaction configuration attributes for a NULL class.");
		final Class<TransactionConfiguration> annotationType = TransactionConfiguration.class;
		final TransactionConfiguration config = clazz.getAnnotation(annotationType);

		if (logger.isDebugEnabled()) {
			logger.debug("Retrieved @TransactionConfiguration [" + config + "] for test class [" + clazz + "].");
		}

		String transactionManagerName;
		boolean defaultRollback;

		if (config != null) {
			transactionManagerName = config.transactionManager();
			defaultRollback = config.defaultRollback();
		}
		else {
			transactionManagerName = (String) AnnotationUtils.getDefaultValue(annotationType, "transactionManager");
			defaultRollback = (Boolean) AnnotationUtils.getDefaultValue(annotationType, "defaultRollback");
		}

		final TransactionConfigurationAttributes configAttributes = new TransactionConfigurationAttributes(
				transactionManagerName, defaultRollback);
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieved TransactionConfigurationAttributes [" + configAttributes + "] for class [" + clazz
					+ "].");
		}

		return configAttributes;
	}

}
