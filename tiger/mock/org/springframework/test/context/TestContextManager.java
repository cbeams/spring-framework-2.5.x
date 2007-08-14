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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.listeners.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.listeners.DirtiesContextTestExecutionListener;
import org.springframework.test.context.listeners.TestExecutionListener;
import org.springframework.test.context.support.MapBackedContextCache;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * <p>
 * TestContextManager is the central entry point into the Spring testing support
 * API, which serves as a facade and encapsulates support for loading and
 * accessing {@link ConfigurableApplicationContext application contexts},
 * dependency injection of test instances, and
 * {@link Transactional transactional} execution of test methods.
 * </p>
 * <p>
 * Specifically, a TestContextManager is responsible for managing a single
 * {@link TestContext} and ensuring that all registered
 * {@link TestExecutionListener TestExecutionListeners} are given a chance to
 * process the test context at well defined test lifecycle execution points
 * (e.g., {@link #prepareTestInstance(Object)},
 * {@link #beforeTestMethod(Object, Method)},
 * {@link #afterTestMethod(Object, Method)}, etc.).
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.5 $
 * @since 2.1
 */
public class TestContextManager<T> {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Cache of Spring application contexts. This needs to be static, as tests
	 * may be destroyed and recreated between running individual test methods,
	 * for example with JUnit.
	 */
	private static final ContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext> CONTEXT_CACHE = new MapBackedContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext>();

	/** Class Logger. */
	private static final Log LOG = LogFactory.getLog(TestContextManager.class);

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final TestContext<T> testContext;

	private final Set<TestExecutionListener> testExecutionListeners = new LinkedHashSet<TestExecutionListener>();

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Constructs a new {@link TestContextManager} for the specified
	 * {@link Class test class}.
	 * </p>
	 *
	 * @see #registerDefaultTestExecutionListeners()
	 * @see #registerTestExecutionListeners(TestExecutionListener...)
	 * @param testClass the Class object corresponding to the test class to be
	 *        managed.
	 * @throws Exception if an error occurs while processing the test class
	 */
	public TestContextManager(final Class<T> testClass) throws Exception {

		this.testContext = new TestContext<T>(testClass, getContextCache());
		registerDefaultTestExecutionListeners();
		// TODO Add TestExecutionListeners to ContextConfig, and
		// TODO Register custom TestExecutionListeners from config
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Registers the default set of
	 * {@link TestExecutionListener TestExecutionListeners} to be used by this
	 * {@link TestContextManager}.
	 * </p>
	 * <p>
	 * The default implementation registers the following standard listeners:
	 * </p>
	 * <ol>
	 * <li>{@link DependencyInjectionTestExecutionListener}</li>
	 * <li>{@link DirtiesContextTestExecutionListener}</li>
	 * </ol>
	 *
	 * @see #registerTestExecutionListeners(TestExecutionListener...)
	 */
	public void registerDefaultTestExecutionListeners() {

		registerTestExecutionListeners(new DependencyInjectionTestExecutionListener(),
				new DirtiesContextTestExecutionListener());
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

	/**
	 * <p>
	 * Prepares the supplied test instance (e.g., injecting dependencies, etc.).
	 * </p>
	 * <p>
	 * The managed {@link TestContext} will be updated with the supplied
	 * <code>testInstance</code>.
	 * </p>
	 *
	 * @param testInstance The test instance to prepare, not <code>null</code>.
	 * @throws Exception if an error occurs while preparing the test instance.
	 */
	public void prepareTestInstance(final T testInstance) throws Exception {

		Assert.notNull(testInstance, "The testInstance can not be null.");
		if (LOG.isDebugEnabled()) {
			LOG.debug("prepareTestInstance(): instance [" + testInstance + "].");
		}

		// Update test instance
		getTestContext().setTestInstanceAndMethod(testInstance, null);

		for (final TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
			try {
				testExecutionListener.prepareTestInstance(getTestContext());
			}
			catch (final Exception e) {
				// log and continue in order to let all listeners have a chance
				if (LOG.isInfoEnabled()) {
					LOG.info("Caught exception while allowing TestExecutionListener [" + testExecutionListener
							+ "] to prepare test instance [" + testInstance + "].", e);
				}
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
	 * The default implementation gives each registered
	 * {@link TestExecutionListener} a chance to pre-process the test method
	 * execution.
	 * </p>
	 *
	 * @param testInstance The current test instance, not <code>null</code>.
	 * @param testMethod The test method which is about to be executed on the
	 *        test instance, not <code>null</code>.
	 * @see #getTestExecutionListeners()
	 */
	public void beforeTestMethod(final Object testInstance, final Method testMethod) {

		Assert.notNull(testInstance, "The testInstance can not be null.");
		Assert.notNull(testMethod, "The testMethod can not be null.");
		if (LOG.isDebugEnabled()) {
			LOG.debug("beforeTestMethod(): instance [" + testInstance + "], method [" + testMethod + "].");
		}

		// Update test instance
		getTestContext().setTestInstanceAndMethod(testInstance, testMethod);

		for (final TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
			try {
				testExecutionListener.beforeTestMethod(getTestContext());
			}
			catch (final Exception e) {
				// log and continue in order to let all listeners have a chance
				if (LOG.isInfoEnabled()) {
					LOG.info("Caught exception while allowing TestExecutionListener [" + testExecutionListener
							+ "] to process 'before' method execution of test method [" + testMethod
							+ "] for test instance [" + testInstance + "].", e);
				}
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
	 * @param testInstance The current test instance, not <code>null</code>.
	 * @param testMethod The test method which has just been executed on the
	 *        test instance, not <code>null</code>.
	 * @see #getTestExecutionListeners()
	 */
	public void afterTestMethod(final Object testInstance, final Method testMethod) {

		Assert.notNull(testInstance, "The testInstance can not be null.");
		Assert.notNull(testMethod, "The testMethod can not be null.");
		if (LOG.isDebugEnabled()) {
			LOG.debug("afterTestMethod(): instance [" + testInstance + "], method [" + testMethod + "].");
		}

		// Update test instance.
		getTestContext().setTestInstanceAndMethod(testInstance, testMethod);

		// Traverse the TestExecutionListeners in reverse order.
		final ArrayList<TestExecutionListener> listenersList = new ArrayList<TestExecutionListener>(
				getTestExecutionListeners());
		Collections.reverse(listenersList);

		for (final TestExecutionListener testExecutionListener : listenersList) {
			try {
				// TODO Relocate call to afterTestMethod() & pass in Throwable!
				testExecutionListener.afterTestMethod(getTestContext(), null);
			}
			catch (final Exception e) {
				// log and continue in order to let all listeners have a chance
				if (LOG.isInfoEnabled()) {
					LOG.info("Caught exception while allowing TestExecutionListener [" + testExecutionListener
							+ "] to process 'after' method execution of test method [" + testMethod
							+ "] for test instance [" + testInstance + "].", e);
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
	protected ContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext> getContextCache() {

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
	public final TestContext<T> getTestContext() {

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

}
