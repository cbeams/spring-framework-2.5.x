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
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

/**
 * Java 5 specific subclass of
 * {@link AbstractTransactionalDataSourceSpringContextTests}, exposing a
 * {@link SimpleJdbcTemplate} and obeying annotations for transaction control.
 *
 * <p>For example, test methods can be annotated with the regular Spring
 * {@link org.springframework.transaction.annotation.Transactional} annotation
 * (for example, to force execution in a read-only transaction) or with the
 * {@link NotTransactional} annotation to prevent any transaction being created
 * at all.
 *
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class AbstractAnnotationAwareTransactionalTests
		extends AbstractTransactionalDataSourceSpringContextTests {

	protected SimpleJdbcTemplate simpleJdbcTemplate;

	private TransactionAttributeSource transactionAttributeSource = new AnnotationTransactionAttributeSource();

	protected ProfileValueSource profileValueSource = SystemProfileValueSource.getInstance();


	@Override
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(dataSource);
		// JdbcTemplate will be identically configured
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(this.jdbcTemplate);
	}

	protected void findUniqueProfileValueSourceFromContext(ApplicationContext ac) {
		Map beans = ac.getBeansOfType(ProfileValueSource.class);
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

		// Use same algorithm as JUnit itself to retrieve the test method
		// about to be executed (the method name is returned by getName).
		// It has to be public so we can retrieve it
		final Method testMethod = getClass().getMethod(getName(), (Class[]) null);

		if (isDisabledInThisEnvironment(testMethod)) {
			logger.info("**** " + getClass().getName() + "." + getName() + " disabled in this environment: " +
					"Total disabled tests=" + getDisabledTestCount());
			recordDisabled();
			return;
		}

		TransactionDefinition explicitTransactionDefinition =
				this.transactionAttributeSource.getTransactionAttribute(testMethod, getClass());
		if (explicitTransactionDefinition != null) {
			logger.info("Custom transaction definition [" + explicitTransactionDefinition + " for test method " + getName());
			setTransactionDefinition(explicitTransactionDefinition);
		}
		else if (testMethod.isAnnotationPresent(NotTransactional.class)) {
			// Don't have any transaction...
			preventTransaction();
		}

		// Let JUnit handle execution. We're just changing the state of the
		// test class first.
		runTestTimed(
				new TestExecutionCallback() {
					public void run() throws Throwable {
						try {
							AbstractAnnotationAwareTransactionalTests.super.runBare();
						}
						finally {
							// Mark the context to be blown away if the test was annotated to result
							// in setDirty being invoked automatically.
							if (testMethod.isAnnotationPresent(DirtiesContext.class)) {
								AbstractAnnotationAwareTransactionalTests.this.setDirty();
							}
						}
					}
				},
				testMethod,
				logger);
	}

	protected boolean isDisabledInThisEnvironment(Method testMethod) {
		IfProfileValue inProfile = testMethod.getAnnotation(IfProfileValue.class);
		if (inProfile == null) {
			inProfile = getClass().getAnnotation(IfProfileValue.class);
		}

		if (inProfile != null) {
			// May be true
			return !this.profileValueSource.get(inProfile.name()).equals(inProfile.value());
		}
		else {
			return false;
		}

		// TODO IfNotProfileValue
	}


	private static void runTestTimed(TestExecutionCallback tec, Method testMethod, Log logger) throws Throwable {
		Timed timed = testMethod.getAnnotation(Timed.class);

		if (timed == null) {
			runTest(tec, testMethod, logger);
		}
		else {
			long startTime = System.currentTimeMillis();
			try {
				runTest(tec, testMethod, logger);
			}
			finally {
				long elapsed = System.currentTimeMillis() - startTime;
				if (elapsed > timed.millis()) {
					fail("Took " + elapsed + " ms; limit was " + timed.millis());
				}
			}
		}
	}

	private static void runTest(TestExecutionCallback tec, Method testMethod, Log logger) throws Throwable {
		ExpectedException ee = testMethod.getAnnotation(ExpectedException.class);
		Repeat repeat = testMethod.getAnnotation(Repeat.class);

		int runs = (repeat != null) ? repeat.value() : 1;
		for (int i = 0; i < runs; i++) {
			try {
				if (i > 0 && logger != null) {
					logger.info("Repetition " + i + " of test " + testMethod.getName());
				}
				tec.run();
				if (ee != null) {
					fail("Expected throwable of class " + ee.value());
				}
			}
			catch (Throwable ex) {
				if (ee == null) {
					throw ex;
				}
				if (ee.value().isAssignableFrom(ex.getClass())) {
					// OK
				}
				else {
					// fail("Expected throwable of class " + ee.value() + "; got " + t);
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
