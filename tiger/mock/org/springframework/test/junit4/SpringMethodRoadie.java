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
 * @version $Revision: 1.1 $
 * @since 2.1
 */
public class SpringMethodRoadie<T> {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	protected static final Log LOG = LogFactory.getLog(SpringMethodRoadie.class);

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final Object testInstance;

	private final TestContextManager<T> testContextManager;

	private final RunNotifier notifier;

	private final Description description;

	private final SpringTestMethod testMethod;

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	public SpringMethodRoadie(final TestContextManager<T> testContextManager, final Object testInstance,
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

	public void runTest() {

		runBeforesThenTestThenAfters(new Runnable() {

			public void run() {

				runTestMethod();
			}
		});
	}

	public void runBeforesThenTestThenAfters(final Runnable test) {

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

		Throwable actual = null;

		try {
			getTestMethod().invoke(getTestInstance());
			if (getTestMethod().expectsException()) {
				addFailure(new AssertionError("Expected exception: " + getTestMethod().getExpectedException().getName()));
			}
		}
		catch (final InvocationTargetException e) {
			actual = e.getTargetException();
			if (actual instanceof AssumptionViolatedException) {
				return;
			}
			else if (!getTestMethod().expectsException()) {
				addFailure(actual);
			}
			else if (getTestMethod().isUnexpected(actual)) {
				final String message = "Unexpected exception, expected<"
						+ getTestMethod().getExpectedException().getName() + "> but was<" + actual.getClass().getName()
						+ ">";
				addFailure(new Exception(message, actual));
			}
		}
		catch (final Throwable e) {
			addFailure(e);
		}
		finally {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Test method [" + getTestMethod().getMethod() + "] threw exception [" + actual + "].");
			}
			getTestContextManager().afterTestMethod(getTestInstance(), getTestMethod().getMethod(), actual);
		}
	}

	protected void runBefores() throws SpringFailedBefore {

		try {
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
	}

	protected void addFailure(final Throwable e) {

		getNotifier().fireTestFailure(new Failure(getDescription(), e));
	}

	/**
	 * @return Returns the testContext.
	 */
	public final TestContextManager<T> getTestContextManager() {

		return this.testContextManager;
	}

	/**
	 * @return Returns the testInstance.
	 */
	public final Object getTestInstance() {

		return this.testInstance;
	}

	/**
	 * @return Returns the testMethod.
	 */
	public final SpringTestMethod getTestMethod() {

		return this.testMethod;
	}

	/**
	 * @return Returns the notifier.
	 */
	public final RunNotifier getNotifier() {

		return this.notifier;
	}

	/**
	 * @return Returns the description.
	 */
	public final Description getDescription() {

		return this.description;
	}

}
