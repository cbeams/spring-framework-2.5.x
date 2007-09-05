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
package org.springframework.test.context.junit38;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

/**
 * <p>
 * Abstract {@link TestCase} which integrates the
 * <em>Spring TestContext Framework</em> with explicit
 * {@link ApplicationContext} testing support in a JUnit 3.8 environment.
 * </p>
 * <p>
 * Concrete subclasses must:
 * </p>
 * <ul>
 * <li>Declare a class-level {@link ContextConfiguration @ContextConfiguration}
 * annotation to configure the {@link ApplicationContext application context}
 * {@link ContextConfiguration#locations() resource locations}.</li>
 * <li>Declare public constructors which match the signatures of
 * {@link AbstractJUnit38SpringContextTests#AbstractJUnit38SpringContextTests() AbstractJUnit38SpringContextTests()}
 * and
 * {@link AbstractJUnit38SpringContextTests#AbstractJUnit38SpringContextTests(String) AbstractJUnit38SpringContextTests(String)}
 * and delegate to <code>super();</code> and <code>super(name);</code>
 * respectively.</li>
 * </ul>
 *
 * @see TestContext
 * @see TestContextManager
 * @see TestExecutionListeners
 * @author Sam Brannen
 * @version $Revision: 1.7 $
 * @since 2.1
 */
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
public class AbstractJUnit38SpringContextTests extends TestCase implements ApplicationContextAware {

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	private static int					disabledTestCount	= 0;

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * The {@link ApplicationContext} that was injected into this test instance
	 * via {@link #setApplicationContext(ApplicationContext)}.
	 */
	protected ApplicationContext		applicationContext;

	/** Logger available to subclasses. */
	protected final Log					logger				= LogFactory.getLog(getClass());

	protected ProfileValueSource		profileValueSource	= SystemProfileValueSource.getInstance();

	private final TestContextManager	testContextManager;

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Default <em>no argument</em> constructor which delegates to
	 * {@link AbstractJUnit38SpringContextTests#AbstractJUnit38SpringContextTests(String) AbstractJUnit38SpringContextTests(String)},
	 * passing a value of <code>null</code> for the test name.
	 *
	 * @see TestCase#TestCase()
	 * @throws Exception If an error occurs while initializing the test
	 *         instance.
	 */
	public AbstractJUnit38SpringContextTests() throws Exception {

		this(null);
	}

	// ------------------------------------------------------------------------|

	/**
	 * Constructs a new AbstractJUnit38SpringContextTests instance with the
	 * supplied <code>name</code>.
	 *
	 * @see TestCase#TestCase(String)
	 * @param name The name of the current test to execute.
	 * @throws Exception If an error occurs while initializing the test
	 *         instance.
	 */
	public AbstractJUnit38SpringContextTests(final String name) throws Exception {

		super(name);
		this.testContextManager = new TestContextManager(getClass());
	}

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Return the number of tests disabled in this environment.
	 */
	public static int getDisabledTestCount() {

		return disabledTestCount;
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

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

	// ------------------------------------------------------------------------|

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
	 * {@link TestContextManager#beforeTestMethod(Object, Method) beforeTestMethod()},
	 * and
	 * {@link TestContextManager#afterTestMethod(Object, Method, Throwable) afterTestMethod()}
	 * on this test's {@link TestContextManager} at the appropriate test
	 * execution points.</li>
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

		final Repeat repeat = testMethod.getAnnotation(Repeat.class);
		final long runs = ((repeat != null) && (repeat.value() > 1)) ? repeat.value() : 1;
		for (int i = 0; i < runs; i++) {
			if (this.logger.isInfoEnabled()) {
				this.logger.info("Repetition " + (i + 1) + " of test " + testMethod.getName());
			}
			runManaged(testMethod);
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * Calls {@link TestContextManager#beforeTestMethod(Object, Method)} and
	 * {@link TestContextManager#afterTestMethod(Object, Method, Throwable)} at
	 * the appropriate test execution points. Also provides support for
	 * {@link Timed @Timed}.
	 *
	 * @see #runTimed(Method, long)
	 * @see #runTest(Method)
	 * @param testMethod The test method to run.
	 * @throws Throwable If any exception is thrown.
	 */
	private void runManaged(final Method testMethod) throws Throwable {

		Throwable exception = null;
		this.testContextManager.beforeTestMethod(this, testMethod);
		setUp();
		try {

			final Timed timed = testMethod.getAnnotation(Timed.class);
			final long timeout = ((timed != null) && (timed.millis() > 0)) ? timed.millis() : 0;

			if (timeout > 0) {
				runTimed(testMethod, timeout);
			}
			else {
				runTest(testMethod);
			}
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

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Runs the test with the supplied <code>timeout</code>.
	 * </p>
	 * <p>
	 * The timeout functionality is based on JUnit 4.4's
	 * {@link org.junit.internal.runners.MethodRoadie#runWithTimeout(long)}
	 * method.
	 * </p>
	 *
	 * @see Timed
	 * @see #runTest(Method)
	 * @param testMethod The test method to run.
	 * @param timeout The timeout in milliseconds.
	 * @throws Throwable if any exception is thrown.
	 */
	private void runTimed(final Method testMethod, final long timeout) throws Throwable {

		final ExecutorService service = Executors.newSingleThreadExecutor();
		final Callable<Object> callable = new Callable<Object>() {

			public Object call() throws Exception {

				try {
					runTest(testMethod);
				}
				catch (final Throwable t) {
					if (t instanceof Exception) {
						throw (Exception) t;
					}
					throw new Exception(t);
				}
				return null;
			}
		};
		final Future<Object> result = service.submit(callable);
		service.shutdown();
		try {
			boolean terminated = service.awaitTermination(timeout, TimeUnit.MILLISECONDS);
			if (!terminated) {
				service.shutdownNow();
			}
			// throws the exception if one occurred during the invocation
			result.get(0, TimeUnit.MILLISECONDS);
		}
		catch (final TimeoutException e) {
			fail(String.format("test timed out after %d milliseconds", timeout));
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * Runs the atomic test with support for
	 * {@link ExpectedException @ExpectedException}.
	 *
	 * @see ExpectedException
	 * @see junit.framework.TestCase#runTest()
	 * @param testMethod The method on which to run the test.
	 * @throws Throwable if any exception is thrown.
	 */
	private void runTest(final Method testMethod) throws Throwable {

		final ExpectedException expectedExceptionAnnotation = testMethod.getAnnotation(ExpectedException.class);
		final boolean exceptionIsExpected = (expectedExceptionAnnotation != null)
				&& (expectedExceptionAnnotation.value() != null);
		final Class<? extends Throwable> expectedException = exceptionIsExpected ? expectedExceptionAnnotation.value()
				: null;

		try {
			super.runTest();
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

	// ------------------------------------------------------------------------|

	/**
	 * Sets the {@link ApplicationContext} to be used by this test instance,
	 * provided via {@link ApplicationContextAware} semantics.
	 *
	 * @param applicationContext The applicationContext to set.
	 */
	public final void setApplicationContext(final ApplicationContext applicationContext) {

		this.applicationContext = applicationContext;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Determines if the supplied test method is <em>disabled</em> in the
	 * current environment (i.e., whether or not the test should be executed) by
	 * evaluating the {@link IfProfileValue @IfProfileValue} annotation, if
	 * present.
	 *
	 * @see #isIgnored()
	 * @param testMethod The test method to test against.
	 * @return whether the test method should execute in the current environment
	 */
	protected boolean isDisabledInThisEnvironment(final Method testMethod) {

		IfProfileValue ifProfileValue = testMethod.getAnnotation(IfProfileValue.class);
		if (ifProfileValue == null) {
			ifProfileValue = getClass().getAnnotation(IfProfileValue.class);
		}

		if (ifProfileValue != null) {
			// May be true
			return !this.profileValueSource.get(ifProfileValue.name()).equals(ifProfileValue.value());
		}

		// else
		return false;

		// XXX Optional: add support for @IfNotProfileValue.
	}

	// ------------------------------------------------------------------------|

	/**
	 * Records the supplied test method as <em>disabled</em> in the current
	 * environment by incrementing the total number of disabled tests and
	 * logging a debug message.
	 *
	 * @see #getDisabledTestCount()
	 * @param testMethod The test method that is disabled.
	 */
	protected void recordDisabled(final Method testMethod) {

		disabledTestCount++;
		this.logger.info("**** " + getClass().getName() + "." + getName() + "() is disabled in this environment: "
				+ "Total disabled tests = " + getDisabledTestCount());
	}

	// ------------------------------------------------------------------------|

}
