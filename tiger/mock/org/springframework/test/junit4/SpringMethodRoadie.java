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
package org.springframework.test.junit4;

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
import org.springframework.test.context.TestContextManager;

/**
 * TODO Add comments for SpringMethodRoadie.
 *
 * @author Sam Brannen
 * @version $Revision: 1.4 $
 * @since 2.1
 */
class SpringMethodRoadie<T> {

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

	private final TestContextManager<T>	testContextManager;

	private final T						testInstance;

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

	public SpringMethodRoadie(final TestContextManager<T> testContextManager, final T testInstance,
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

	protected void runTest() {

		runBeforesThenTestThenAfters(new Runnable() {

			public void run() {

				runTestMethod();
			}
		});
	}

	protected void runBeforesThenTestThenAfters(final Runnable test) {

		try {
			runBefores();
			test.run();
		}
		catch (final SpringFailedBefore e) {
		}
		catch (final Exception e) {
			throw new RuntimeException("test should never throw an exception to this level");
		}
		finally {
			runAfters();
		}
	}

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
				LOG.debug("Test method [" + getTestMethod().getMethod() + "] threw exception [" + getTestException()
						+ "].");
			}
		}
	}

	protected void runBefores() throws SpringFailedBefore {

		try {
			getTestContextManager().beforeTestMethod(getTestInstance(), getTestMethod().getMethod());

			final List<Method> befores = getTestMethod().getBefores();
			for (final Method before : befores) {
				before.invoke(getTestInstance());
			}
		}
		catch (final InvocationTargetException e) {
			addFailure(e.getTargetException());
			throw new SpringFailedBefore();
		}
		catch (final Throwable e) {
			addFailure(e);
			throw new SpringFailedBefore();
		}
	}

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

	protected void addFailure(final Throwable e) {

		getNotifier().fireTestFailure(new Failure(getDescription(), e));
	}

	/**
	 * @return Returns the testContext.
	 */
	protected final TestContextManager<T> getTestContextManager() {

		return this.testContextManager;
	}

	/**
	 * @return Returns the testInstance.
	 */
	protected final T getTestInstance() {

		return this.testInstance;
	}

	/**
	 * @return Returns the testMethod.
	 */
	protected final SpringTestMethod getTestMethod() {

		return this.testMethod;
	}

	/**
	 * Gets the exception thrown during execution of the test method, or
	 * <code>null</code> if no exception was thrown.
	 *
	 * @return The test exception.
	 */
	protected final Throwable getTestException() {

		return this.testException;
	}

	/**
	 * @return Returns the notifier.
	 */
	protected final RunNotifier getNotifier() {

		return this.notifier;
	}

	/**
	 * @return Returns the description.
	 */
	protected final Description getDescription() {

		return this.description;
	}

	/**
	 * Sets the exception thrown during execution of the test method, or
	 * <code>null</code> if no exception was thrown.
	 *
	 * @param testException The thrown test exception, or <code>null</code>.
	 */
	protected final void setTestException(final Throwable testException) {

		this.testException = testException;
	}

}
