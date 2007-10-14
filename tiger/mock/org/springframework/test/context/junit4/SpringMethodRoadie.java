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

package org.springframework.test.context.junit4;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assume.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import org.springframework.test.annotation.Repeat;
import org.springframework.test.annotation.Timed;
import org.springframework.test.context.TestContextManager;

/**
 * <p>
 * <code>SpringMethodRoadie</code> is a custom implementation of JUnit 4.4's
 * {@link org.junit.internal.runners.MethodRoadie MethodRoadie}, which provides
 * the following enhancements:
 * </p>
 * <ul>
 * <li>Notifies a {@link TestContextManager} of
 * {@link TestContextManager#beforeTestMethod(Object,Method) before} and
 * {@link TestContextManager#afterTestMethod(Object,Method,Throwable) after}
 * events.</li>
 * <li>Uses a {@link SpringTestMethod} instead of JUnit 4.4's
 * {@link org.junit.internal.runners.TestMethod TestMethod}.</li>
 * <li>Tracks the exception thrown during execution of the test method.</li>
 * </ul>
 * <p>
 * Due to method and field visibility constraints, the code of
 * <code>MethodRoadie</code> has been duplicated here instead of subclassing
 * <code>MethodRoadie</code> directly.
 * </p>
 *
 * @author Sam Brannen
 * @since 2.5
 */
class SpringMethodRoadie {

	protected static final Log logger = LogFactory.getLog(SpringMethodRoadie.class);

	private final TestContextManager testContextManager;

	private final Object testInstance;

	private final SpringTestMethod testMethod;

	private Throwable testException;

	private final RunNotifier notifier;

	private final Description description;


	/**
	 * Constructs a <code>SpringMethodRoadie</code> with the supplied
	 * parameters.
	 *
	 * @param testContextManager the TestContextManager to notify.
	 * @param testInstance the test instance upon which to invoke the test
	 *        method.
	 * @param testMethod the test method to invoke.
	 * @param notifier the RunNotifier to notify.
	 * @param description the test description.
	 */
	public SpringMethodRoadie(final TestContextManager testContextManager, final Object testInstance,
			final SpringTestMethod testMethod, final RunNotifier notifier, final Description description) {
		this.testContextManager = testContextManager;
		this.testInstance = testInstance;
		this.testMethod = testMethod;
		this.notifier = notifier;
		this.description = description;
	}

	/**
	 * Runs the <em>test</em>, including notification of events to the
	 * {@link RunNotifier} and {@link TestContextManager} as well as proper
	 * handling of {@link org.junit.Ignore @Ignore},
	 * {@link org.junit.Test#expected() expected exceptions},
	 * {@link org.junit.Test#timeout() test timeouts}, and
	 * {@link org.junit.Assume.AssumptionViolatedException assumptions}.
	 */
	public void run() {

		if (getTestMethod().isIgnored()) {
			getNotifier().fireTestIgnored(getDescription());
			return;
		}
		getNotifier().fireTestStarted(getDescription());
		try {
			final Timed timedAnnotation = getTestMethod().getMethod().getAnnotation(Timed.class);
			final long springTimeout = ((timedAnnotation != null) && (timedAnnotation.millis() > 0)) ? timedAnnotation.millis()
					: 0;
			final long junitTimeout = getTestMethod().getTimeout();

			if ((springTimeout > 0) && (junitTimeout > 0)) {
				final String msg = "Test method [" + getTestMethod().getMethod()
						+ "] has been configured with Spring's @Timed(millis=" + springTimeout
						+ ") and JUnit's @Test(timeout=" + junitTimeout
						+ ") annotations. Only one declaration of a 'timeout' is permitted per test method.";
				logger.error(msg);
				throw new IllegalStateException(msg);
			}
			else if (springTimeout > 0) {
				final long startTime = System.currentTimeMillis();
				try {
					runTest();
				}
				finally {
					final long elapsed = System.currentTimeMillis() - startTime;
					if (elapsed > springTimeout) {
						addFailure(new Exception("Took " + elapsed + " ms; limit was " + springTimeout));
					}
				}
			}
			else if (junitTimeout > 0) {
				runWithTimeout(junitTimeout);
			}
			else {
				runTest();
			}
		}
		finally {
			getNotifier().fireTestFinished(getDescription());
		}
	}

	/**
	 * Runs the test method on the test instance with the specified
	 * <code>timeout</code>.
	 *
	 * @param timeout the timeout in milliseconds.
	 * @see #runWithRepetitions(Runnable)
	 * @see #runTestMethod()
	 */
	protected void runWithTimeout(final long timeout) {
		runWithRepetitions(new Runnable() {

			public void run() {
				final ExecutorService service = Executors.newSingleThreadExecutor();
				final Callable<Object> callable = new Callable<Object>() {

					public Object call() throws Exception {

						runTestMethod();
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
					// throws the exception if one occurred during the
					// invocation
					result.get(0, TimeUnit.MILLISECONDS);
				}
				catch (final TimeoutException e) {
					addFailure(new Exception(String.format("test timed out after %d milliseconds", timeout)));
				}
				catch (final Exception e) {
					addFailure(e);
				}
			}
		});
	}

	/**
	 * Runs the test, including {@link #runBefores() @Before} and
	 * {@link #runAfters() @After} methods.
	 *
	 * @see #runWithRepetitions(Runnable)
	 * @see #runTestMethod()
	 */
	protected void runTest() {
		runWithRepetitions(new Runnable() {

			public void run() {
				runTestMethod();
			}
		});
	}

	/**
	 * <p>
	 * Runs the supplied <code>test</code> with repetitions. Checks for the
	 * presence of {@link Repeat @Repeat} to determine if the test should be run
	 * more than once and delegates to
	 * {@link #runBeforesThenTestThenAfters(Runnable)} for each repetition. The
	 * test will be run at least once.
	 * </p>
	 *
	 * @param test the runnable test.
	 * @see Repeat
	 * @see #runBeforesThenTestThenAfters(Runnable)
	 */
	protected void runWithRepetitions(final Runnable test) {
		final Method method = this.getTestMethod().getMethod();
		final Repeat repeat = method.getAnnotation(Repeat.class);
		final int runs = ((repeat != null) && (repeat.value() > 1)) ? repeat.value() : 1;

		for (int i = 0; i < runs; i++) {
			if ((runs > 1) && (logger != null) && (logger.isInfoEnabled())) {
				logger.info("Repetition " + (i + 1) + " of test " + method.getName());
			}
			runBeforesThenTestThenAfters(test);
		}
	}

	/**
	 * <p>
	 * Runs the following methods on the test instance, guaranteeing that
	 * {@link #runAfters() @After methods} will have a chance to execute:
	 * </p>
	 * <ul>
	 * <li>{@link #runBefores() @Before methods}</li>
	 * <li>The supplied, {@link Runnable} <code>test</code></li>
	 * <li>{@link #runAfters() @After methods}</li>
	 * </ul>
	 *
	 * @param test the runnable test.
	 */
	protected void runBeforesThenTestThenAfters(final Runnable test) {
		try {
			runBefores();
			test.run();
		}
		catch (final FailedBefore e) {
		}
		catch (final Exception e) {
			throw new RuntimeException("test should never throw an exception to this level");
		}
		finally {
			runAfters();
		}
	}

	/**
	 * Runs the test method on the test instance, processing exceptions (both
	 * expected and unexpected), assumptions, and registering failures as
	 * necessary.
	 */
	protected void runTestMethod() {
		this.testException = null;
		try {
			getTestMethod().invoke(getTestInstance());
			if (getTestMethod().expectsException()) {
				addFailure(new AssertionError("Expected exception: " + getTestMethod().getExpectedException().getName()));
			}
		}
		catch (final InvocationTargetException e) {
			this.testException = e.getTargetException();
			if (this.testException instanceof AssumptionViolatedException) {
				return;
			}
			else if (!getTestMethod().expectsException()) {
				addFailure(this.testException);
			}
			else if (getTestMethod().isUnexpected(this.testException)) {
				final String message = "Unexpected exception, expected<"
						+ getTestMethod().getExpectedException().getName() + "> but was<"
						+ this.testException.getClass().getName() + ">";
				addFailure(new Exception(message, this.testException));
			}
		}
		catch (final Throwable t) {
			addFailure(t);
		}
		finally {
			if (logger.isDebugEnabled()) {
				logger.debug("Test method [" + getTestMethod().getMethod() + "] threw exception [" + this.testException
						+ "].");
			}
		}
	}

	/**
	 * Calls {@link TestContextManager#beforeTestMethod(Object,Method)} and then
	 * runs {@link org.junit.Before @Before methods}, registering failures and
	 * throwing {@link FailedBefore} exceptions as necessary.
	 *
	 * @throws FailedBefore If an error occurs while executing a <em>before</em>
	 *         method.
	 */
	protected void runBefores() throws FailedBefore {
		try {
			getTestContextManager().beforeTestMethod(getTestInstance(), getTestMethod().getMethod());
			final List<Method> befores = getTestMethod().getBefores();
			for (final Method before : befores) {
				before.invoke(getTestInstance());
			}
		}
		catch (final InvocationTargetException e) {
			addFailure(e.getTargetException());
			throw new FailedBefore();
		}
		catch (final Throwable t) {
			addFailure(t);
			throw new FailedBefore();
		}
	}

	/**
	 * Runs {@link org.junit.After @After methods}, registering failures as
	 * necessary, and then calls
	 * {@link TestContextManager#afterTestMethod(Object,Method,Throwable)}.
	 */
	protected void runAfters() {
		final List<Method> afters = getTestMethod().getAfters();
		for (final Method after : afters) {
			try {
				after.invoke(getTestInstance());
			}
			catch (final InvocationTargetException e) {
				addFailure(e.getTargetException());
			}
			catch (final Throwable t) {
				addFailure(t); // Untested, but seems impossible
			}
		}

		try {
			getTestContextManager().afterTestMethod(getTestInstance(), getTestMethod().getMethod(), getTestException());
		}
		catch (final Throwable t) {
			addFailure(t);
		}
	}

	/**
	 * Fires a failure for the supplied <code>exception</code> with the
	 * {@link #getNotifier() RunNotifier}.
	 *
	 * @param exception the exception upon which to base the failure.
	 */
	protected void addFailure(final Throwable exception) {
		getNotifier().fireTestFailure(new Failure(getDescription(), exception));
	}

	/**
	 * Gets the {@link TestContextManager}.
	 *
	 * @return the test context manager.
	 */
	protected final TestContextManager getTestContextManager() {
		return this.testContextManager;
	}

	/**
	 * Gets the test instance.
	 *
	 * @return the test instance.
	 */
	protected final Object getTestInstance() {
		return this.testInstance;
	}

	/**
	 * Gets the {@link SpringTestMethod}.
	 *
	 * @return the test method.
	 */
	protected final SpringTestMethod getTestMethod() {
		return this.testMethod;
	}

	/**
	 * Gets the exception thrown during execution of the test method, or
	 * <code>null</code> if no exception was thrown.
	 *
	 * @return the test exception.
	 */
	protected final Throwable getTestException() {
		return this.testException;
	}

	/**
	 * Gets the {@link RunNotifier}.
	 *
	 * @return the notifier.
	 */
	protected final RunNotifier getNotifier() {
		return this.notifier;
	}

	/**
	 * Gets the test description.
	 *
	 * @return the description.
	 */
	protected final Description getDescription() {
		return this.description;
	}

	/**
	 * Sets the exception thrown during execution of the test method, or
	 * <code>null</code> if no exception was thrown.
	 *
	 * @param testException the thrown test exception, or <code>null</code>.
	 */
	protected final void setTestException(final Throwable testException) {
		this.testException = testException;
	}


	/**
	 * Marker exception to signal that an exception was encountered while
	 * executing an {@link org.junit.Before @Before} method.
	 */
	private static class FailedBefore extends Exception {

		private static final long serialVersionUID = 8054300181079811763L;
	}

}
