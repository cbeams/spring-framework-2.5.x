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

package org.springframework.test.annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.util.Assert;

/**
 * <p>
 * Java 5 specific subclass of
 * {@link AbstractTransactionalDataSourceSpringContextTests}, exposing a
 * {@link SimpleJdbcTemplate} and obeying annotations for transaction control.
 * </p>
 * <p>
 * For example, test methods can be annotated with the regular Spring
 * {@link org.springframework.transaction.annotation.Transactional @Transactional}
 * annotation (e.g., to force execution in a read-only transaction) or with the
 * {@link NotTransactional @NotTransactional} annotation to prevent any
 * transaction being created at all. In addition, individual test methods can be
 * annotated with {@link Rollback @Rollback} to override the
 * {@link #isDefaultRollback() default rollback} settings.
 * </p>
 * <p>
 * The following list constitutes all annotations currently supported by
 * AbstractAnnotationAwareTransactionalTests:
 * </p>
 * <ul>
 * <li>{@link DirtiesContext @DirtiesContext}</li>
 * <li>{@link IfProfileValue @IfProfileValue}</li>
 * <li>{@link ExpectedException @ExpectedException}</li>
 * <li>{@link Timed @Timed}</li>
 * <li>{@link Repeat @Repeat}</li>
 * <li>{@link org.springframework.transaction.annotation.Transactional @Transactional}</li>
 * <li>{@link NotTransactional @NotTransactional}</li>
 * <li>{@link Rollback @Rollback}</li>
 * </ul>
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @since 2.0
 */
public abstract class AbstractAnnotationAwareTransactionalTests extends
		AbstractTransactionalDataSourceSpringContextTests {

	protected SimpleJdbcTemplate simpleJdbcTemplate;

	private final TransactionAttributeSource transactionAttributeSource = new AnnotationTransactionAttributeSource();

	protected ProfileValueSource profileValueSource = SystemProfileValueSource.getInstance();


	/**
	 * Default constructor for AbstractAnnotationAwareTransactionalTests.
	 */
	public AbstractAnnotationAwareTransactionalTests() {
	}

	/**
	 * Constructor for AbstractAnnotationAwareTransactionalTests with a JUnit
	 * name.
	 */
	public AbstractAnnotationAwareTransactionalTests(String name) {
		super(name);
	}

	@Override
	public void setDataSource(final DataSource dataSource) {

		super.setDataSource(dataSource);
		// JdbcTemplate will be identically configured
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(this.jdbcTemplate);
	}

	protected void findUniqueProfileValueSourceFromContext(final ApplicationContext ac) {

		final Map<?, ?> beans = ac.getBeansOfType(ProfileValueSource.class);
		if (beans.size() == 1) {
			this.profileValueSource = (ProfileValueSource) beans.values().iterator().next();
		}
	}

	/**
	 * Overridden to populate transaction definition from annotations.
	 */
	@Override
	public void runBare() throws Throwable {

		// getName will return the name of the method being run.
		if (isDisabledInThisEnvironment(getName())) {
			// Let superclass log that we didn't run the test.
			super.runBare();
			return;
		}

		final Method testMethod = getTestMethod();

		if (isDisabledInThisEnvironment(testMethod)) {
			recordDisabled();
			this.logger.info("**** " + getClass().getName() + "." + getName() + " disabled in this environment: "
					+ "Total disabled tests=" + getDisabledTestCount());
			return;
		}

		final TransactionDefinition explicitTransactionDefinition = this.transactionAttributeSource.getTransactionAttribute(
				testMethod, getClass());
		if (explicitTransactionDefinition != null) {
			this.logger.info("Custom transaction definition [" + explicitTransactionDefinition + "] for test method ["
					+ getName() + "].");
			setTransactionDefinition(explicitTransactionDefinition);
		}
		else if (testMethod.isAnnotationPresent(NotTransactional.class)) {
			// Don't have any transaction...
			preventTransaction();
		}

		// Let JUnit handle execution. We're just changing the state of the test
		// class first.
		runTestTimed(new TestExecutionCallback() {

			public void run() throws Throwable {

				try {
					AbstractAnnotationAwareTransactionalTests.super.runBare();
				}
				finally {
					// Mark the context to be blown away if the test was
					// annotated to result in setDirty being invoked
					// automatically.
					if (testMethod.isAnnotationPresent(DirtiesContext.class)) {
						AbstractAnnotationAwareTransactionalTests.this.setDirty();
					}
				}
			}
		}, testMethod, this.logger);
	}

	/**
	 * <p>
	 * Determines if the test for the supplied <code>testMethod</code> should
	 * run in the current environment.
	 * </p>
	 * <p>
	 * The default implementation is based on
	 * {@link IfProfileValue @IfProfileValue} semantics.
	 * </p>
	 *
	 * @param testMethod the test method
	 * @return <code>true</code> if the test should be <em>disabled</em> in
	 *         the current environment
	 */
	protected boolean isDisabledInThisEnvironment(final Method testMethod) {

		boolean disabled = false;

		IfProfileValue inProfile = testMethod.getAnnotation(IfProfileValue.class);
		if (inProfile == null) {
			inProfile = getClass().getAnnotation(IfProfileValue.class);
		}

		if (inProfile != null) {
			final String name = inProfile.name();
			Assert.hasText(name, "The name attribute supplied to @IfProfileValue must not be empty.");

			final String annotatedValue = inProfile.value();
			final String environmentValue = this.profileValueSource.get(name);
			final boolean bothValuesAreNull = (environmentValue == null) && (annotatedValue == null);

			final boolean enabled = bothValuesAreNull
					|| ((environmentValue != null) && environmentValue.equals(annotatedValue));
			disabled = !enabled;
		}

		return disabled;

		// XXX Optional: add support for @IfNotProfileValue.
	}

	/**
	 * Get the current test method.
	 *
	 * @return The current test method.
	 */
	protected Method getTestMethod() {

		assertNotNull("TestCase.getName() cannot be null", getName());
		Method testMethod = null;
		try {
			// Use same algorithm as JUnit itself to retrieve the test method
			// about to be executed (the method name is returned by getName). It
			// has to be public so we can retrieve it.
			testMethod = getClass().getMethod(getName(), (Class[]) null);
		}
		catch (final NoSuchMethodException e) {
			fail("Method \"" + getName() + "\" not found");
		}
		if (!Modifier.isPublic(testMethod.getModifiers())) {
			fail("Method \"" + getName() + "\" should be public");
		}
		return testMethod;
	}

	/**
	 * Determines whether or not to rollback transactions for the current test
	 * by taking into consideration the
	 * {@link #isDefaultRollback() default rollback} flag and a possible
	 * method-level override via the {@link Rollback @Rollback} annotation.
	 *
	 * @return The <em>rollback</em> flag for the current test.
	 * @throws Exception If an error occurs while determining the rollback flag.
	 */
	@Override
	protected boolean isRollback() {

		boolean rollback = isDefaultRollback();
		final Rollback rollbackAnnotation = getTestMethod().getAnnotation(Rollback.class);
		if (rollbackAnnotation != null) {

			final boolean rollbackOverride = rollbackAnnotation.value();
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Method-level @Rollback(" + rollbackOverride + ") overrides default rollback ["
						+ rollback + "] for test [" + getName() + "].");
			}
			rollback = rollbackOverride;
		}
		else {
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("No method-level @Rollback override: using default rollback [" + rollback
						+ "] for test [" + getName() + "].");
			}
		}

		return rollback;
	}

	private void runTestTimed(final TestExecutionCallback tec, final Method testMethod, final Log logger)
			throws Throwable {

		final Timed timed = testMethod.getAnnotation(Timed.class);

		if (timed == null) {
			runTest(tec, testMethod, logger);
		}
		else {
			final long startTime = System.currentTimeMillis();
			try {
				runTest(tec, testMethod, logger);
			}
			finally {
				final long elapsed = System.currentTimeMillis() - startTime;
				if (elapsed > timed.millis()) {
					fail("Took " + elapsed + " ms; limit was " + timed.millis());
				}
			}
		}
	}

	private void runTest(final TestExecutionCallback tec, final Method testMethod, final Log logger) throws Throwable {

		final ExpectedException ee = testMethod.getAnnotation(ExpectedException.class);
		final Repeat repeat = testMethod.getAnnotation(Repeat.class);

		final int runs = (repeat != null) ? repeat.value() : 1;
		for (int i = 0; i < runs; i++) {
			try {
				if ((runs > 1) && (logger != null) && (logger.isInfoEnabled())) {
					logger.info("Repetition " + (i + 1) + " of test " + testMethod.getName());
				}
				tec.run();
				if (ee != null) {
					fail("Expected throwable of class " + ee.value());
				}
			}
			catch (final Throwable ex) {
				if (ee == null) {
					throw ex;
				}
				if (ee.value().isAssignableFrom(ex.getClass())) {
					// OK
				}
				else {
					// Throw the unexpected problem throwable
					throw ex;
				}
			}
		}
	}


	private static interface TestExecutionCallback {

		void run() throws Throwable;
	}

}
