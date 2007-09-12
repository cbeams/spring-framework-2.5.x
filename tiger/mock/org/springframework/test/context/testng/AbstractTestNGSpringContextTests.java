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
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * <p>
 * Abstract base test class which integrates the
 * <em>Spring TestContext Framework</em> with explicit
 * {@link ApplicationContext} testing support in a <strong>TestNG</strong>
 * environment.
 * </p>
 * <p>
 * Concrete subclasses:
 * </p>
 * <ul>
 * <li>Typically declare a class-level
 * {@link ContextConfiguration @ContextConfiguration} annotation to configure
 * the {@link ApplicationContext application context}
 * {@link ContextConfiguration#locations() resource locations}.
 * <em>If your test does not need to load an application context, you may choose
 * to omit the {@link ContextConfiguration @ContextConfiguration} declaration
 * and to configure the appropriate
 * {@link org.springframework.test.context.TestExecutionListener TestExecutionListeners}
 * manually.</em></li>
 * <li>Must declare a <code>public</code> no-args constructor which either
 * implicitly or explicitly delegates to <code>super();</code>.</li>
 * </ul>
 *
 * @author Sam Brannen
 * @since 2.5
 * @see TestContext
 * @see TestContextManager
 * @see TestExecutionListeners
 * @see AbstractTransactionalTestNGSpringContextTests
 * @see org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests
 * @see org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests
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

	private Throwable testException;


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
			final String msg = "Caught exception while attempting to instantiate a new TestContextManager for test class ["
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
	 * Delegates to the configured {@link TestContextManager} to
	 * {@link TestContextManager#beforeTestMethod(Object,Method) pre-process}
	 * the test method before the actual test is executed.
	 *
	 * @param testContext The current TestNG test context.
	 * @param testMethod The test method which is about to be executed.
	 * @throws Exception Allows all exceptions to propagate.
	 */
	@BeforeMethod(alwaysRun = true)
	public void beforeTestMethod(final ITestContext testContext, final Method testMethod) throws Exception {
		this.testContextManager.beforeTestMethod(this, testMethod);
	}

	/**
	 * Delegates to the
	 * {@link IHookCallBack#runTestMethod(ITestResult) test method} in the
	 * supplied <code>callback</code> to execute the actual test and then
	 * tracks the exception thrown during test execution, if any.
	 *
	 * @see org.testng.IHookable#run(org.testng.IHookCallBack,
	 *      org.testng.ITestResult)
	 */
	public void run(final IHookCallBack callBack, final ITestResult testResult) {
		callBack.runTestMethod(testResult);
		this.testException = testResult.getThrowable();
	}

	/**
	 * Delegates to the configured {@link TestContextManager} to
	 * {@link TestContextManager#afterTestMethod(Object, Method, Throwable) post-process}
	 * the test method after the actual test has executed.
	 *
	 * @param testContext The current TestNG test context.
	 * @param testMethod The test method which has just been executed on the
	 *        test instance.
	 * @throws Exception Allows all exceptions to propagate.
	 */
	@AfterMethod(alwaysRun = true)
	public void afterTestMethod(final ITestContext testContext, final Method testMethod) throws Exception {
		this.testContextManager.afterTestMethod(this, testMethod, this.testException);
		this.testException = null;
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
