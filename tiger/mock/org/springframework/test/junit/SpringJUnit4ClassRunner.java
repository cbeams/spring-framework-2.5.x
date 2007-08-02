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
package org.springframework.test.junit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.internal.runners.MethodRoadie;
import org.junit.internal.runners.TestMethod;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.springframework.test.TestExecutionManager;

/**
 * SpringJUnit4ClassRunner is a custom extension of
 * {@link SpringJUnit4ClassRunner} which provides Spring testing functionality
 * to standard JUnit tests by means of the {@link TestExecutionManager} and
 * associated support classes and annotations.
 *
 * @author Sam Brannen
 * @version $Revision: 1.2 $
 * @since 2.2
 */
public class SpringJUnit4ClassRunner<T> extends JUnit4ClassRunner {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	private static final Log LOG = LogFactory.getLog(SpringJUnit4ClassRunner.class);

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final TestExecutionManager<T> testExecutionManager;

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Constructs a new <code>SpringJUnit4ClassRunner</code> and initializes a
	 * {@link TestExecutionManager} to provide Spring testing functionality to
	 * standard JUnit tests.
	 *
	 * @param clazz
	 * @throws InitializationError
	 */
	public SpringJUnit4ClassRunner(final Class<T> clazz) throws InitializationError {

		super(clazz);

		if (LOG.isDebugEnabled()) {
			LOG.debug("SpringJUnit4ClassRunner constructor called with [" + clazz + "].");
		}

		try {
			this.testExecutionManager = new TestExecutionManager<T>(clazz);
		}
		catch (final Exception e) {
			LOG.error("Caught an exception while attempting to instantiate a new TestExecutionManager for test class ["
					+ clazz + "].", e);
			throw new InitializationError(e);
		}
	}

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Overloads JUnit 4.4's {@link JUnit4ClassRunner#createTest() createTest()}
	 * method with a {@link Method} argument. Delegates to a
	 * {@link TestExecutionManager} to prepare the test instance for Spring
	 * testing functionality.
	 *
	 * @param method
	 * @see JUnit4ClassRunner#createTest()
	 * @return
	 * @throws Exception
	 */
	protected Object createTest(final Method method) throws Exception {

		// Note: 'method' is currently not used but will likely be necessary for
		// future functionality (e.g., @Transactional, etc.).

		@SuppressWarnings("unchecked")
		final T testInstance = (T) super.createTest();

		this.testExecutionManager.prepareTestInstance(testInstance);

		return testInstance;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Overrides invokeTestMethod().
	 *
	 * @see #createTest(Method)
	 * @see org.junit.internal.runners.JUnit4ClassRunner#invokeTestMethod(java.lang.reflect.Method,
	 *      org.junit.runner.notification.RunNotifier)
	 */
	@Override
	protected void invokeTestMethod(final Method method, final RunNotifier notifier) {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Invoking test method [" + method.toGenericString() + "].");
		}

		// ---------------------------------------------------------------------
		// XXX Optional: create a custom MethodRoadie extension that ...
		//
		// 1) overrides run(): by calling a custom runWithTimeout() method; also
		// need to provide support for Spring's @Repeat.
		//
		// 2) overrides runWithTimeout(): by reimplementing (copy-n-paste) to
		// provide support for Spring's @Timed.

		// ---------------------------------------------------------------------
		// XXX Optional: create a custom TestMethod extension that ...
		//
		// 1) overrides expectsException(): by adding support for Spring's
		// @ExpectedException.

		// ---------------------------------------------------------------------
		// The following is a direct copy of the original JUnit 4.4 code, except
		// that we call createTest(Method) instead of createTest().

		final Description description = methodDescription(method);
		Object test;
		try {
			test = createTest(method);
		}
		catch (final InvocationTargetException e) {
			notifier.testAborted(description, e.getCause());
			return;
		}
		catch (final Exception e) {
			notifier.testAborted(description, e);
			return;
		}
		final TestMethod testMethod = wrapMethod(method);

		new MethodRoadie(test, testMethod, notifier, description).run();
	}

	// ------------------------------------------------------------------------|

}
