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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.ProfileValueSource;
import org.springframework.test.annotation.ProfileValueUtils;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.annotation.Timed;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

/**
 * <p>
 * Abstract base {@link TestCase} which integrates the
 * <em>Spring TestContext Framework</em> with explicit
 * {@link ApplicationContext} testing support in a <strong>JUnit 3.8</strong>
 * environment.
 * </p>
 * <p>
 * Concrete subclasses:
 * </p>
 * <ul>
 * <li>Typically declare a class-level
 * {@link org.springframework.test.context.ContextConfiguration @ContextConfiguration}
 * annotation to configure the {@link ApplicationContext application context}
 * {@link org.springframework.test.context.ContextConfiguration#locations() resource locations}.
 * <em>If your test does not need to load an
 * application context, you may choose to omit the
 * {@link org.springframework.test.context.ContextConfiguration @ContextConfiguration} declaration and configure
 * the appropriate {@link org.springframework.test.context.TestExecutionListener TestExecutionListeners}
 * manually.</em></li>
 * <li>Must declare public constructors which match the signatures of
 * {@link #AbstractJUnit38SpringContextTests() AbstractJUnit38SpringContextTests()}
 * and
 * {@link #AbstractJUnit38SpringContextTests(String) AbstractJUnit38SpringContextTests(String)}
 * and delegate to <code>super();</code> and <code>super(name);</code>
 * respectively.</li>
 * </ul>
 * <p>
 * The following list constitutes all annotations currently supported directly
 * by <code>AbstractJUnit38SpringContextTests</code>.
 * <em>(Note that additional annotations may be supported by various
 * {@link org.springframework.test.context.TestExecutionListener TestExecutionListeners})</em>
 * </p>
 * <ul>
 * <li>{@link org.springframework.test.annotation.DirtiesContext @DirtiesContext}
 * (via the configured {@link DirtiesContextTestExecutionListener})</li>
 * <li>{@link org.springframework.test.annotation.ProfileValueSourceConfiguration @ProfileValueSourceConfiguration}</li>
 * <li>{@link IfProfileValue @IfProfileValue}</li>
 * <li>{@link ExpectedException @ExpectedException}</li>
 * <li>{@link Timed @Timed}</li>
 * <li>{@link Repeat @Repeat}</li>
 * </ul>
 *
 * @author Sam Brannen
 * @since 2.5
 * @see org.springframework.test.context.TestContext
 * @see org.springframework.test.context.TestContextManager
 * @see org.springframework.test.context.TestExecutionListeners
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
	 * <p>
	 * {@link ProfileValueSource} available to subclasses but primarily intended
	 * for internal use to provide support for
	 * {@link IfProfileValue @IfProfileValue}.
	 * </p>
	 * <p>
	 * Set in the {@link #AbstractJUnit38SpringContextTests(String)}
	 * constructor.
	 * </p>
	 */
	protected final ProfileValueSource profileValueSource;

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
	 * supplied <code>name</code>; initializes the internal
	 * {@link TestContextManager} for the current test; and retrieves the
	 * configured (or default) {@link ProfileValueSource}.
	 *
	 * @param name the name of the current test to execute.
	 * @see TestCase#TestCase(String)
	 */
	public AbstractJUnit38SpringContextTests(final String name) {
		super(name);
		this.testContextManager = new TestContextManager(getClass());
		this.profileValueSource = ProfileValueUtils.retrieveProfileValueSource(getClass());
	}

	/**
	 * <p>
	 * Runs the <em>Spring TestContext Framework</em> test sequence.
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
	 * @see ProfileValueUtils#isTestEnabledInThisEnvironment(ProfileValueSource,
	 *      Method)
	 * @see junit.framework.TestCase#runBare()
	 */
	@Override
	public void runBare() throws Throwable {

		this.testContextManager.prepareTestInstance(this);

		final Method testMethod = getTestMethod();

		if (!ProfileValueUtils.isTestEnabledInThisEnvironment(this.profileValueSource, testMethod)) {
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
	 * @return the current test method.
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
	 * @param tec the test execution callback to run.
	 * @param testMethod the actual test method: used to retrieve the
	 *        <code>timeout</code>.
	 * @throws Throwable if any exception is thrown.
	 * @see Timed
	 * @see #runTest
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
	 * @param tec the test execution callback to run.
	 * @param testMethod the actual test method: used to retrieve the
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
					AssertionFailedError assertionError = new AssertionFailedError("Unexpected exception, expected<"
							+ expectedException.getName() + "> but was<" + t.getClass().getName() + ">");
					assertionError.initCause(t);
					throw assertionError;
				}
			}
		}
	}

	/**
	 * Calls {@link TestContextManager#beforeTestMethod(Object,Method)} and
	 * {@link TestContextManager#afterTestMethod(Object,Method,Throwable)} at
	 * the appropriate test execution points.
	 *
	 * @param testMethod the test method to run.
	 * @throws Throwable if any exception is thrown.
	 * @see #runBare()
	 * @see TestCase#runTest()
	 */
	private void runManaged(final Method testMethod) throws Throwable {

		Throwable exception = null;

		try {
			this.testContextManager.beforeTestMethod(this, testMethod);
		}
		catch (Throwable t) {
			if (t.getCause() instanceof AssertionError) {
				t = t.getCause();
			}
			throw t;
		}

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

		try {
			this.testContextManager.afterTestMethod(this, testMethod, exception);
		}
		catch (final Throwable t) {
			if (exception == null) {
				exception = t;
			}
		}

		if (exception != null) {
			if (exception.getCause() instanceof AssertionError) {
				exception = exception.getCause();
			}
			throw exception;
		}
	}

	/**
	 * Sets the {@link ApplicationContext} to be used by this test instance,
	 * provided via {@link ApplicationContextAware} semantics.
	 *
	 * @param applicationContext the application context to set.
	 */
	public final void setApplicationContext(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * Records the supplied test method as <em>disabled</em> in the current
	 * environment by incrementing the total number of disabled tests and
	 * logging a debug message.
	 *
	 * @param testMethod the test method that is disabled.
	 * @see #getDisabledTestCount()
	 */
	protected void recordDisabled(final Method testMethod) {
		disabledTestCount++;
		if (this.logger.isInfoEnabled()) {
			this.logger.info("**** " + getClass().getName() + "." + getName() + "() is disabled in this environment: "
					+ "Total disabled tests = " + getDisabledTestCount());
		}
	}


	private static interface TestExecutionCallback {

		void run() throws Throwable;
	}

}
