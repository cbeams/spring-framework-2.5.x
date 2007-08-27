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
package org.springframework.test.context;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * <p>
 * TestContextManager is the central entry point into the
 * <em>Spring Test Context Framework</em>, which provides support for loading
 * and accessing {@link ApplicationContext application contexts}, dependency
 * injection of test instances, and {@link Transactional transactional}
 * execution of test methods.
 * </p>
 * <p>
 * Specifically, a TestContextManager is responsible for managing a single
 * {@link TestContext} and signaling events to all registered
 * {@link TestExecutionListener TestExecutionListeners} at well defined test
 * execution points:
 * </p>
 * <ul>
 * <li>{@link #prepareTestInstance(Object) test instance preparation}:
 * immediately following instantiation of the test instance</li>
 * <li>{@link #beforeTestMethod(Object, Method) before test method execution}:
 * prior to any <em>before methods</em> of a particular testing framework
 * (e.g., JUnit 4's {@link org.junit.Before @Before})</li>
 * <li>{@link #afterTestMethod(Object, Method, Throwable) after test method execution}:
 * after any <em>after methods</em> of a particular testing framework (e.g.,
 * JUnit 4's {@link org.junit.After @After})</li>
 * </ul>
 *
 * @see TestContext
 * @see TestExecutionListeners
 * @see ContextConfiguration
 * @see org.springframework.test.context.transaction.TransactionConfiguration
 * @author Sam Brannen
 * @version $Revision: 1.17 $
 * @since 2.1
 */
public class TestContextManager {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Cache of Spring application contexts. This needs to be static, as tests
	 * may be destroyed and recreated between running individual test methods,
	 * for example with JUnit.
	 */
	private static final ContextCache<ContextConfigurationAttributes, ApplicationContext>	CONTEXT_CACHE			= new ContextCache<ContextConfigurationAttributes, ApplicationContext>();

	/** Class Logger. */
	private static final Log																LOG						= LogFactory.getLog(TestContextManager.class);

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final TestContext																testContext;

	private final Set<TestExecutionListener>												testExecutionListeners	= new LinkedHashSet<TestExecutionListener>();

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Constructs a new {@link TestContextManager} for the specified
	 * {@link Class test class} and automatically
	 * {@link #registerTestExecutionListeners(TestExecutionListener...) registers}
	 * the {@link TestExecutionListener TestExecutionListeners} configured for
	 * the test class via the
	 * {@link TestExecutionListeners @TestExecutionListeners} annotation.
	 * </p>
	 *
	 * @see #registerTestExecutionListeners(TestExecutionListener...)
	 * @see #retrieveTestExecutionListeners(Class)
	 * @param testClass the Class object corresponding to the test class to be
	 *        managed.
	 * @throws Exception if an error occurs while processing the test class
	 */
	public TestContextManager(final Class<?> testClass) throws Exception {

		this.testContext = new TestContext(testClass, getContextCache());
		registerTestExecutionListeners(retrieveTestExecutionListeners(testClass));
	}

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Retrieves an array of newly instantiated
	 * {@link TestExecutionListener TestExecutionListeners} for the specified
	 * {@link Class class}.
	 * </p>
	 *
	 * @param clazz The Class object corresponding to the test class for which
	 *        the listeners should be retrieved.
	 * @return an array of TestExecutionListeners for the specified class.
	 * @throws IllegalArgumentException if the supplied class is
	 *         <code>null</code>.
	 * @throws Exception if an error occurs while retrieving the listeners.
	 */
	@SuppressWarnings("unchecked")
	protected static TestExecutionListener[] retrieveTestExecutionListeners(final Class<?> clazz)
			throws IllegalArgumentException, Exception {

		Assert.notNull(clazz, "Can not retrieve TestExecutionListeners for a NULL class.");
		final TestExecutionListeners testExecutionListeners = clazz.getAnnotation(TestExecutionListeners.class);
		Class<? extends TestExecutionListener>[] classes;

		if (testExecutionListeners != null) {
			classes = testExecutionListeners.value();
		}
		else {
			if (LOG.isInfoEnabled()) {
				LOG.info("@TestExecutionListeners is not present for class [" + clazz + "]: using defaults.");
			}
			classes = (Class<? extends TestExecutionListener>[]) AnnotationUtils.getDefaultValue(TestExecutionListeners.class);
		}

		final TestExecutionListener[] listeners = new TestExecutionListener[classes.length];

		for (int i = 0; i < classes.length; i++) {
			final Class<? extends TestExecutionListener> listenerClass = classes[i];
			if (LOG.isDebugEnabled()) {
				LOG.debug("Retrieved TestExecutionListener class [" + listenerClass + "] for annotated class [" + clazz
						+ "].");
			}
			listeners[i] = listenerClass.newInstance();
		}

		return listeners;
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Prepares the supplied test instance (e.g., injecting dependencies, etc.).
	 * </p>
	 * <p>
	 * The managed {@link TestContext} will be updated with the supplied
	 * <code>testInstance</code>.
	 * </p>
	 * <p>
	 * The default implementation attempts to give each registered
	 * {@link TestExecutionListener} a chance to prepare the test instance. If a
	 * listener throws an exception, however, the remaining registered listeners
	 * will not be called.
	 * </p>
	 *
	 * @param testInstance The test instance to prepare, not <code>null</code>.
	 * @throws Exception if an error occurs while preparing the test instance.
	 */
	public void prepareTestInstance(final Object testInstance) throws Exception {

		Assert.notNull(testInstance, "The testInstance can not be null.");
		if (LOG.isDebugEnabled()) {
			LOG.debug("prepareTestInstance(): instance [" + testInstance + "].");
		}

		getTestContext().updateState(testInstance, null, null);

		for (final TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
			try {
				testExecutionListener.prepareTestInstance(getTestContext());
			}
			catch (final Exception e) {
				// log and rethrow.
				if (LOG.isInfoEnabled()) {
					LOG.info("Caught exception while allowing TestExecutionListener [" + testExecutionListener
							+ "] to prepare test instance [" + testInstance + "].", e);
				}
				throw e;
			}
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Hook for pre-processing a test just <em>before</em> the execution of
	 * the supplied {@link Method test method}, for example for setting up test
	 * fixtures, transaction handling, etc.
	 * </p>
	 * <p>
	 * The managed {@link TestContext} will be updated with the supplied
	 * <code>testInstance</code> and <code>testMethod</code>.
	 * </p>
	 * <p>
	 * The default implementation attempts to give each registered
	 * {@link TestExecutionListener} a chance to pre-process the test method
	 * execution. If a listener throws an exception, however, the remaining
	 * registered listeners will not be called.
	 * </p>
	 *
	 * @see #getTestExecutionListeners()
	 * @param testInstance The current test instance, not <code>null</code>.
	 * @param testMethod The test method which is about to be executed on the
	 *        test instance, not <code>null</code>.
	 * @throws Exception if a registered TestExecutionListener throws an
	 *         exception.
	 */
	public void beforeTestMethod(final Object testInstance, final Method testMethod) throws Exception {

		Assert.notNull(testInstance, "The testInstance can not be null.");
		Assert.notNull(testMethod, "The testMethod can not be null.");
		if (LOG.isDebugEnabled()) {
			LOG.debug("beforeTestMethod(): instance [" + testInstance + "], method [" + testMethod + "].");
		}

		getTestContext().updateState(testInstance, testMethod, null);

		for (final TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
			try {
				testExecutionListener.beforeTestMethod(getTestContext());
			}
			catch (final Exception e) {
				// log and rethrow.
				if (LOG.isInfoEnabled()) {
					LOG.info("Caught exception while allowing TestExecutionListener [" + testExecutionListener
							+ "] to process 'before' method execution of test method [" + testMethod
							+ "] for test instance [" + testInstance + "].", e);
				}
				throw e;
			}
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Hook for post-processing a test just <em>after</em> the execution of
	 * the supplied {@link Method test method}, for example for tearing down
	 * test fixtures, transaction handling, etc.
	 * </p>
	 * <p>
	 * The managed {@link TestContext} will be updated with the supplied
	 * <code>testInstance</code> and <code>testMethod</code>.
	 * </p>
	 * <p>
	 * The default implementation gives each registered
	 * {@link TestExecutionListener} a chance to post-process the test method
	 * execution. Note that registered listeners will be executed in the
	 * opposite order in which they were registered.
	 * </p>
	 *
	 * @see #getTestExecutionListeners()
	 * @param testInstance The current test instance, not <code>null</code>.
	 * @param testMethod The test method which has just been executed on the
	 *        test instance, not <code>null</code>.
	 * @param exception The exception that was thrown during execution of the
	 *        test method, or <code>null</code> if none was thrown.
	 */
	public void afterTestMethod(final Object testInstance, final Method testMethod, final Throwable exception) {

		Assert.notNull(testInstance, "The testInstance can not be null.");
		Assert.notNull(testMethod, "The testMethod can not be null.");
		if (LOG.isDebugEnabled()) {
			LOG.debug("afterTestMethod(): instance [" + testInstance + "], method [" + testMethod + "], exception ["
					+ exception + "].");
		}

		getTestContext().updateState(testInstance, testMethod, exception);

		// Traverse the TestExecutionListeners in reverse order to ensure proper
		// "wrapper"-style execution ordering of listeners.
		final ArrayList<TestExecutionListener> listenersList = new ArrayList<TestExecutionListener>(
				getTestExecutionListeners());
		Collections.reverse(listenersList);

		for (final TestExecutionListener testExecutionListener : listenersList) {
			try {
				testExecutionListener.afterTestMethod(getTestContext());
			}
			catch (final Exception e) {
				// log and continue in order to let all listeners have a chance
				// to process the event.
				if (LOG.isInfoEnabled()) {
					LOG.info("Caught exception while allowing TestExecutionListener [" + testExecutionListener
							+ "] to process 'after' method execution for test: method [" + testMethod + "], instance ["
							+ testInstance + "], exception [" + exception + "].", e);
				}
			}
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets the {@link ContextCache} used by this {@link TestContextManager}.
	 * </p>
	 * <p>
	 * The default implementation returns a reference to a static cache shared
	 * by all {@link TestContextManager TestContextManagers}.
	 * </p>
	 *
	 * @return The context cache.
	 */
	protected ContextCache<ContextConfigurationAttributes, ApplicationContext> getContextCache() {

		return TestContextManager.CONTEXT_CACHE;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets the {@link TestContext} managed by this {@link TestContextManager}.
	 * </p>
	 *
	 * @return The test context.
	 */
	public final TestContext getTestContext() {

		return this.testContext;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets an {@link Collections#unmodifiableSet(Set) unmodifiable} copy of the
	 * {@link TestExecutionListener TestExecutionListeners} registered for this
	 * {@link TestContextManager}.
	 * </p>
	 *
	 * @return A copy of the TestExecutionListeners.
	 */
	public final Set<TestExecutionListener> getTestExecutionListeners() {

		return Collections.unmodifiableSet(this.testExecutionListeners);
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Registers the supplied
	 * {@link TestExecutionListener TestExecutionListeners} by appending them to
	 * the set of listeners used by this {@link TestContextManager}.
	 * </p>
	 */
	public void registerTestExecutionListeners(final TestExecutionListener... testExecutionListeners) {

		for (final TestExecutionListener listener : testExecutionListeners) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Registering TestExecutionListener [" + listener + "].");
			}
			this.testExecutionListeners.add(listener);
		}
	}

	// ------------------------------------------------------------------------|

}
