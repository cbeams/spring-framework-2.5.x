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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.internal.runners.MethodRoadie;
import org.junit.internal.runners.TestMethod;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.springframework.test.context.TestExecutionManager;

/**
 * SpringJUnit4ClassRunner is a custom extension of {@link JUnit4ClassRunner}
 * which provides Spring testing functionality to standard JUnit tests by means
 * of the {@link TestExecutionManager} and associated support classes and
 * annotations.
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
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
	 * Delegates to {@link JUnit4ClassRunner#createTest()} to create the test
	 * instance and then to a {@link TestExecutionManager} to prepare the test
	 * instance for Spring testing functionality.
	 *
	 * @see JUnit4ClassRunner#createTest()
	 * @see TestExecutionManager#prepareTestInstance(Object)
	 * @return A new test instance.
	 * @throws Exception if an error occurs while creating or preparing the test
	 *         instance.
	 */
	@Override
	protected Object createTest() throws Exception {

		@SuppressWarnings("unchecked")
		final T testInstance = (T) super.createTest();
		this.testExecutionManager.prepareTestInstance(testInstance);
		return testInstance;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Overrides invokeTestMethod().
	 *
	 * @see #createTest()
	 * @see JUnit4ClassRunner#invokeTestMethod(Method, RunNotifier)
	 */
	@Override
	protected void invokeTestMethod(final Method method, final RunNotifier notifier) {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Invoking test method [" + method.toGenericString() + "].");
		}

		// TODO Add test support for @DirtiesContext
		// TODO Add test support for @Transactional and @NotTransactional

		// XXX Optional: add test support for @IfProfileValue & @Repeat
		// XXX Optional: add test support for @Timed & @ExpectedException

		// ---------------------------------------------------------------------
		// --- DEVELOPMENT NOTES -----------------------------------------------
		// ---------------------------------------------------------------------
		// create a custom MethodRoadie that ...
		//
		// 1) overrides run(): by adding support for Spring's @Repeat.
		//
		// ---------------------------------------------------------------------
		// create a custom TestMethod that ...
		//
		// 1) overrides getExpectedException(): by adding support for Spring's
		// @ExpectedException.
		//
		// 2) overrides getTimeout(): by detecting Spring's @Timed and handling
		// it the same as the standard JUnit @Test(timeout=1) feature.

		// ---------------------------------------------------------------------
		// The following is a direct copy of the original JUnit 4.4 code, except
		// that we ... ??? (currently do nothing special)

		final Description description = methodDescription(method);
		Object test;
		try {
			test = createTest();
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
