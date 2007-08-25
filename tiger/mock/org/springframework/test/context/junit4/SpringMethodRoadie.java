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
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assume.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.springframework.test.context.TestContextManager;

/**
 * <p>
 * SpringMethodRoadie is a custom, parameterized implementation of JUnit 4.4's
 * {@link org.junit.internal.runners.MethodRoadie}, which provides the
 * following enhancements:
 * </p>
 * <ul>
 * <li>Notifies a {@link TestContextManager} of
 * {@link TestContextManager#beforeTestMethod(Object, Method) before} and
 * {@link TestContextManager#afterTestMethod(Object, Method, Throwable) after}
 * events.</li>
 * <li>Uses a {@link SpringTestMethod} instead of JUnit 4.4's
 * {@link org.junit.internal.runners.TestMethod}.</li>
 * <li>Tracks the exception thrown during execution of the test method.</li>
 * </ul>
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
class SpringMethodRoadie {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	protected static final Log			LOG	= LogFactory.getLog(SpringMethodRoadie.class);

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final TestContextManager	testContextManager;

	private final Object				testInstance;

	private final SpringTestMethod		testMethod;

	private Throwable					testException;

	private final RunNotifier			notifier;

	private final Description			description;

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Constructs a SpringMethodRoadie with the supplied parameters.
	 *
	 * @param testContextManager The TestContextManager to notify.
	 * @param testInstance The test instance upon which to invoke the test
	 *        method.
	 * @param testMethod The test method to invoke.
	 * @param notifier The RunNotifier to notify.
	 * @param description The test description.
	 */
	public SpringMethodRoadie(final TestContextManager testContextManager, final Object testInstance,
			final SpringTestMethod testMethod, final RunNotifier notifier, final Description description) {

		this.testContextManager = testContextManager;
		this.testInstance = testInstance;
		this.testMethod = testMethod;
		this.notifier = notifier;
		this.description = description;
	}

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Runs the <em>test</em>, including notification of events to the
	 * {@link RunListener} and {@link TestContextManager} as well as proper
	 * handling of {@link Ignore @Ignore},
	 * {@link Test#expected() expected exceptions},
	 * {@link Test#timeout() test timeouts}, and
	 * {@link Assume.AssumptionViolatedException assumptions}.
	 */
	public void run() {

		// XXX Optional: add support for Spring's @IfProfileValue & @Repeat

		if (getTestMethod().isIgnored()) {
			getNotifier().fireTestIgnored(getDescription());
			return;
		}
		getNotifier().fireTestStarted(getDescription());
		try {
			final long timeout = getTestMethod().getTimeout();
			if (timeout > 0) {
				runWithTimeout(timeout);
			}
			else {
				runTest();
			}
		}
		finally {
			getNotifier().fireTestFinished(getDescription());
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * Runs the test method on the test instance with the specified
	 * <code>timeout</code>.
	 *
	 * @see #runTestMethod()
	 * @param timeout The timeout in milliseconds.
	 */
	protected void runWithTimeout(final long timeout) {

		runBeforesThenTestThenAfters(new Runnable() {

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

	// ------------------------------------------------------------------------|

	/**
	 * Runs the test, including {@link #runBefores() @Before} and
	 * {@link #runAfters() @After} methods.
	 *
	 * @see #runBeforesThenTestThenAfters(Runnable)
	 * @see #runTestMethod()
	 */
	protected void runTest() {

		runBeforesThenTestThenAfters(new Runnable() {

			public void run() {

				runTestMethod();
			}
		});
	}

	// ------------------------------------------------------------------------|

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
	 * @param test The runnable test.
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

	// ------------------------------------------------------------------------|

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
		catch (final Throwable e) {
			addFailure(e);
		}
		finally {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Test method [" + getTestMethod().getMethod() + "] threw exception [" + this.testException
						+ "].");
			}
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * Calls {@link TestContextManager#beforeTestMethod(Object, Method)} and
	 * then runs {@link org.junit.Before @Before methods}, registering failures
	 * and throwing {@link FailedBefore} exceptions as necessary.
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
		catch (final Throwable e) {
			addFailure(e);
			throw new FailedBefore();
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * Runs {@link org.junit.After @After methods}, registering failures and
	 * throwing {@link FailedBefore} exceptions as necessary, and then calls
	 * {@link TestContextManager#afterTestMethod(Object, Method, Throwable)}.
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
			catch (final Throwable e) {
				addFailure(e); // Untested, but seems impossible
			}
		}
		getTestContextManager().afterTestMethod(getTestInstance(), getTestMethod().getMethod(), getTestException());
	}

	// ------------------------------------------------------------------------|

	/**
	 * Fires a failure for the supplied <code>exception</code> with the
	 * {@link #getNotifier() RunNotifier}.
	 *
	 * @param exception The exception upon which to base the failure.
	 */
	protected void addFailure(final Throwable exception) {

		getNotifier().fireTestFailure(new Failure(getDescription(), exception));
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the {@link TestContextManager}.
	 *
	 * @return The test context manager.
	 */
	protected final TestContextManager getTestContextManager() {

		return this.testContextManager;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the test instance.
	 *
	 * @return The test instance.
	 */
	protected final Object getTestInstance() {

		return this.testInstance;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the {@link SpringTestMethod}.
	 *
	 * @return The test method.
	 */
	protected final SpringTestMethod getTestMethod() {

		return this.testMethod;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the exception thrown during execution of the test method, or
	 * <code>null</code> if no exception was thrown.
	 *
	 * @return The test exception.
	 */
	protected final Throwable getTestException() {

		return this.testException;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the {@link RunNotifier}.
	 *
	 * @return The notifier.
	 */
	protected final RunNotifier getNotifier() {

		return this.notifier;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the test description.
	 *
	 * @return The description.
	 */
	protected final Description getDescription() {

		return this.description;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Sets the exception thrown during execution of the test method, or
	 * <code>null</code> if no exception was thrown.
	 *
	 * @param testException The thrown test exception, or <code>null</code>.
	 */
	protected final void setTestException(final Throwable testException) {

		this.testException = testException;
	}

	// ------------------------------------------------------------------------|
	// --- TYPES --------------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Marker exception to signal that an exception was encountered while
	 * executing an {@link Before @Before} method.
	 */
	protected static final class FailedBefore extends Exception {

		private static final long	serialVersionUID	= 1L;

	}

	// ------------------------------------------------------------------------|

}
