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

package org.springframework.test.context.testng;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.BeforeClass;

/**
 * <p>
 * Abstract base test class which integrates the
 * <em>Spring TestContext Framework</em> with explicit
 * {@link ApplicationContext} testing support in a <strong>TestNG</strong>
 * environment.
 * </p>
 * <p>
 * Concrete subclasses must:
 * </p>
 * <ul>
 * <li>Declare a class-level {@link ContextConfiguration @ContextConfiguration}
 * annotation to configure the {@link ApplicationContext application context}
 * {@link ContextConfiguration#locations() resource locations}.</li>
 * <li>Declare a <code>public</code> no-args constructor which either
 * implicitly or explicitly delegates to <code>super();</code>.</li>
 * </ul>
 *
 * @author Sam Brannen
 * @see TestContext
 * @see TestContextManager
 * @see TestExecutionListeners
 * @see AbstractTransactionalTestNGSpringContextTests
 * @see org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests
 * @see org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests
 * @since 2.1
 */
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
public abstract class AbstractTestNGSpringContextTests implements IHookable, ApplicationContextAware {

	/**
	 * Logger available to subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The {@link ApplicationContext} that was injected into this test instance
	 * via {@link #setApplicationContext(ApplicationContext)}.
	 */
	protected ApplicationContext applicationContext;

	private final TestContextManager testContextManager;


	/**
	 * Constructs a new AbstractTestNGSpringContextTests instance and
	 * initializes the internal {@link TestContextManager} for the current test.
	 *
	 * @throws RuntimeException If an error occurs while initializing the
	 *         TestContextManager.
	 */
	public AbstractTestNGSpringContextTests() {
		try {
			this.testContextManager = new TestContextManager(getClass());
		}
		catch (Exception e) {
			final String msg = "Exception caught while attempting to instantiate a new TestContextManager for test class ["
					+ getClass() + "].";
			this.logger.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * <p>
	 * Delegates to the configured {@link TestContextManager} to
	 * {@link TestContextManager#prepareTestInstance(Object) prepare} this test
	 * instance prior to execution of any individual test methods, for example
	 * for injecting dependencies, etc.
	 * </p>
	 *
	 * @throws Exception if a registered TestExecutionListener throws an
	 *         exception.
	 */
	@BeforeClass(alwaysRun = true)
	public void prepareTestInstance() throws Exception {
		this.testContextManager.prepareTestInstance(this);
	}

	/**
	 * Calls {@link TestContextManager#beforeTestMethod(Object,Method)} and
	 * {@link TestContextManager#afterTestMethod(Object,Method,Throwable)} at
	 * the appropriate test execution points and delegates to the
	 * {@link IHookCallBack#runTestMethod(ITestResult) test method} in the
	 * supplied <code>callback</code> to execute the actual test.
	 *
	 * @see org.testng.IHookable#run(org.testng.IHookCallBack,
	 *      org.testng.ITestResult)
	 */
	public void run(final IHookCallBack callBack, final ITestResult testResult) {

		Throwable exception = null;
		try {
			this.testContextManager.beforeTestMethod(this, testResult.getMethod().getMethod());
			callBack.runTestMethod(testResult);
		}
		catch (final Throwable t) {
			exception = t;
		}

		this.testContextManager.afterTestMethod(this, testResult.getMethod().getMethod(), exception);

		if (exception != null) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Sets the {@link ApplicationContext} to be used by this test instance,
	 * provided via {@link ApplicationContextAware} semantics.
	 *
	 * @param applicationContext The applicationContext to set.
	 */
	public final void setApplicationContext(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

}
