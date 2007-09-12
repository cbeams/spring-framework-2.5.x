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

package org.springframework.test.context.junit38;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.ProfileValueSource;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.annotation.SystemProfileValueSource;
import org.springframework.test.annotation.Timed;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.util.Assert;

/**
 * <p>
 * Abstract base {@link TestCase} which integrates the
 * <em>Spring TestContext Framework</em> with explicit
 * {@link ApplicationContext} testing support in a <strong>JUnit 3.8</strong>
 * environment.
 * </p>
 * <p>
 * Concrete subclasses must:
 * </p>
 * <ul>
 * <li>Declare a class-level {@link ContextConfiguration @ContextConfiguration}
 * annotation to configure the {@link ApplicationContext application context}
 * {@link ContextConfiguration#locations() resource locations}.</li>
 * <li>Declare public constructors which match the signatures of
 * {@link #AbstractJUnit38SpringContextTests() AbstractJUnit38SpringContextTests()}
 * and
 * {@link #AbstractJUnit38SpringContextTests(String) AbstractJUnit38SpringContextTests(String)}
 * and delegate to <code>super();</code> and <code>super(name);</code>
 * respectively.</li>
 * </ul>
 * <p>
 * The following list constitutes all annotations currently supported by
 * AbstractJUnit38SpringContextTests:
 * </p>
 * <ul>
 * <li>{@link org.springframework.test.annotation.DirtiesContext @DirtiesContext}
 * (via the {@link DirtiesContextTestExecutionListener})</li>
 * <li>{@link IfProfileValue @IfProfileValue}</li>
 * <li>{@link ExpectedException @ExpectedException}</li>
 * <li>{@link Timed @Timed}</li>
 * <li>{@link Repeat @Repeat}</li>
 * </ul>
 *
 * @author Sam Brannen
 * @since 2.5
 * @see TestContext
 * @see TestContextManager
 * @see TestExecutionListeners
 * @see AbstractTransactionalJUnit38SpringContextTests
 * @see org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests
 * @see org.springframework.test.context.testng.AbstractTestNGSpringContextTests
 */
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
public class AbstractJUnit38SpringContextTests extends TestCase implements ApplicationContextAware {

	private static int disabledTestCount = 0;


	/**
	 * Return the number of tests disabled in this environment.
	 */
	public static int getDisabledTestCount() {
		return disabledTestCount;
	}


	/**
	 * Logger available to subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The {@link ApplicationContext} that was injected into this test instance
	 * via {@link #setApplicationContext(ApplicationContext)}.
	 */
	protected ApplicationContext applicationContext;

	/**
	 * Source of profile values available to subclasses.
	 *
	 * @see SystemProfileValueSource
	 */
	protected ProfileValueSource profileValueSource = SystemProfileValueSource.getInstance();

	private final TestContextManager testContextManager;


	/**
	 * Default <em>no argument</em> constructor which delegates to
	 * {@link AbstractJUnit38SpringContextTests#AbstractJUnit38SpringContextTests(String) AbstractJUnit38SpringContextTests(String)},
	 * passing a value of <code>null</code> for the test name.
	 */
	public AbstractJUnit38SpringContextTests() {
		this(null);
	}

	/**
	 * Constructs a new AbstractJUnit38SpringContextTests instance with the
	 * supplied <code>name</code> and initializes the internal
	 * {@link TestContextManager} for the current test.
	 *
	 * @param name The name of the current test to execute.
	 * @throws RuntimeException If an error occurs while initializing the
	 *         TestContextManager.
	 * @see TestCase#TestCase(String)
	 */
	public AbstractJUnit38SpringContextTests(final String name) {

		super(name);

		try {
			this.testContextManager = new TestContextManager(getClass());
		}
		catch (Exception e) {
			final String msg = "Exception caught while attempting to instantiate a new TestContextManager for test class ["
					+ getClass() + "].";
			this.logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * <p>
	 * Runs the <em>Spring Test Context Framework</em> test sequence.
	 * </p>
	 * <p>
	 * In addition to standard {@link TestCase#runBare()} semantics, this
	 * implementation performs the following:
	 * </p>
	 * <ul>
	 * <li>Calls
	 * {@link TestContextManager#prepareTestInstance(Object) prepareTestInstance()},
	 * {@link TestContextManager#beforeTestMethod(Object,Method) beforeTestMethod()},
	 * and
	 * {@link TestContextManager#afterTestMethod(Object,Method,Throwable) afterTestMethod()}
	 * on this test's {@link TestContextManager} at the appropriate test
	 * execution points.</li>
	 * <li>Provides support for {@link IfProfileValue @IfProfileValue}.</li>
	 * <li>Provides support for {@link Repeat @Repeat}.</li>
	 * <li>Provides support for {@link Timed @Timed}.</li>
	 * <li>Provides support for {@link ExpectedException @ExpectedException}.</li>
	 * </ul>
	 *
	 * @see junit.framework.TestCase#runBare()
	 */
	@Override
	public void runBare() throws Throwable {

		this.testContextManager.prepareTestInstance(this);

		final Method testMethod = getTestMethod();

		if (isDisabledInThisEnvironment(testMethod)) {
			recordDisabled(testMethod);
			return;
		}

		runTestTimed(new TestExecutionCallback() {

			public void run() throws Throwable {

				runManaged(testMethod);
			}
		}, testMethod);
	}

	/**
	 * Get the current test method.
	 *
	 * @return The current test method.
	 */
	private Method getTestMethod() {

		assertNotNull("TestCase.getName() cannot be null", getName());
		Method testMethod = null;
		try {
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
	 * Runs a <em>timed</em> test via the supplied
	 * {@link TestExecutionCallback}, providing support for the
	 * {@link Timed @Timed} annotation.
	 *
	 * @param tec The test execution callback to run.
	 * @param testMethod The actual test method: used to retrieve the
	 *        <code>timeout</code>.
	 * @throws Throwable if any exception is thrown.
	 * @see Timed
	 * @see #runTest(org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests.TestExecutionCallback,
	 *      Method,Log)
	 */
	private void runTestTimed(final TestExecutionCallback tec, final Method testMethod) throws Throwable {

		final Timed timed = testMethod.getAnnotation(Timed.class);

		if (timed == null) {
			runTest(tec, testMethod);
		}
		else {
			final long startTime = System.currentTimeMillis();
			try {
				runTest(tec, testMethod);
			}
			finally {
				final long elapsed = System.currentTimeMillis() - startTime;
				if (elapsed > timed.millis()) {
					fail("Took " + elapsed + " ms; limit was " + timed.millis());
				}
			}
		}
	}

	/**
	 * Runs a test via the supplied {@link TestExecutionCallback}, providing
	 * support for the {@link ExpectedException @ExpectedException} and
	 * {@link Repeat @Repeat} annotations.
	 *
	 * @param tec The test execution callback to run.
	 * @param testMethod The actual test method: used to retrieve the
	 *        {@link ExpectedException @ExpectedException} and
	 *        {@link Repeat @Repeat} annotations.
	 * @throws Throwable if any exception is thrown.
	 * @see ExpectedException
	 * @see Repeat
	 */
	private void runTest(final TestExecutionCallback tec, final Method testMethod) throws Throwable {

		final ExpectedException expectedExceptionAnnotation = testMethod.getAnnotation(ExpectedException.class);
		final boolean exceptionIsExpected = (expectedExceptionAnnotation != null)
				&& (expectedExceptionAnnotation.value() != null);
		final Class<? extends Throwable> expectedException = exceptionIsExpected ? expectedExceptionAnnotation.value()
				: null;

		final Repeat repeat = testMethod.getAnnotation(Repeat.class);
		final int runs = ((repeat != null) && (repeat.value() > 1)) ? repeat.value() : 1;

		for (int i = 0; i < runs; i++) {
			try {
				if ((runs > 1) && (this.logger.isInfoEnabled())) {
					this.logger.info("Repetition " + (i + 1) + " of test " + testMethod.getName());
				}
				tec.run();
				if (exceptionIsExpected) {
					fail("Expected exception: " + expectedException.getName());
				}
			}
			catch (final Throwable t) {
				if (!exceptionIsExpected) {
					throw t;
				}
				if (!expectedException.isAssignableFrom(t.getClass())) {
					// Wrap the unexpected throwable with an explicit message.
					throw new Exception(("Unexpected exception, expected<" + expectedException.getClass().getName()
							+ "> but was<" + t.getClass().getName() + ">"), t);
				}
			}
		}
	}

	/**
	 * Calls {@link TestContextManager#beforeTestMethod(Object,Method)} and
	 * {@link TestContextManager#afterTestMethod(Object,Method,Throwable)} at
	 * the appropriate test execution points.
	 *
	 * @param testMethod The test method to run.
	 * @throws Throwable If any exception is thrown.
	 * @see #runBare()
	 * @see TestCase#runTest()
	 */
	private void runManaged(final Method testMethod) throws Throwable {

		Throwable exception = null;
		this.testContextManager.beforeTestMethod(this, testMethod);
		setUp();
		try {
			super.runTest();
		}
		catch (final Throwable running) {
			exception = running;
		}
		finally {
			try {
				tearDown();
			}
			catch (final Throwable tearingDown) {
				if (exception == null) {
					exception = tearingDown;
				}
			}
		}
		this.testContextManager.afterTestMethod(this, testMethod, exception);
		if (exception != null) {
			throw exception;
		}
	}

	/**
	 * Sets the {@link ApplicationContext} to be used by this test instance,
	 * provided via {@link ApplicationContextAware} semantics.
	 *
	 * @param applicationContext The applicationContext to set.
	 */
	public final void setApplicationContext(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * Determines if the supplied test method is <em>disabled</em> in the
	 * current environment (i.e., whether or not the test should be executed) by
	 * evaluating the {@link IfProfileValue @IfProfileValue} annotation, if
	 * present.
	 *
	 * @param testMethod The test method to test against.
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
	 * Records the supplied test method as <em>disabled</em> in the current
	 * environment by incrementing the total number of disabled tests and
	 * logging a debug message.
	 *
	 * @param testMethod The test method that is disabled.
	 * @see #getDisabledTestCount()
	 */
	protected void recordDisabled(final Method testMethod) {
		disabledTestCount++;
		this.logger.info("**** " + getClass().getName() + "." + getName() + "() is disabled in this environment: "
				+ "Total disabled tests = " + getDisabledTestCount());
	}


	private static interface TestExecutionCallback {

		void run() throws Throwable;
	}

}
