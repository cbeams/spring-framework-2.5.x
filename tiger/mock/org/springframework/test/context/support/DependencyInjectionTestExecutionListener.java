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

package org.springframework.test.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.Conventions;
import org.springframework.test.context.TestContext;

/**
 * TestExecutionListener which provides support for dependency injection and
 * initialization of test instances.
 *
 * @author Sam Brannen
 * @since 2.5
 */
public class DependencyInjectionTestExecutionListener extends AbstractTestExecutionListener {

	/**
	 * <p>
	 * Attribute name for a {@link TestContext TestContext} attribute which
	 * indicates whether or not the dependencies of a test instance should be
	 * <em>reinjected</em> in
	 * {@link #beforeTestMethod(TestContext) beforeTestMethod()}. Note that
	 * dependencies will be injected in
	 * {@link #prepareTestInstance(TestContext) prepareTestInstance()} in any
	 * case.
	 * </p>
	 * <p>
	 * Clients of a {@link TestContext} (e.g., other
	 * {@link org.springframework.test.context.TestExecutionListener TestExecutionListeners}) may therefore
	 * choose to set this attribute if it is desirable for dependencies to be
	 * reinjected <em>between</em> execution of individual test methods.
	 * </p>
	 * <p>
	 * Permissible values include {@link Boolean#TRUE} and {@link Boolean#FALSE}.
	 * </p>
	 */
	public static final String REINJECT_DEPENDENCIES_ATTRIBUTE = Conventions.getQualifiedAttributeName(
			DependencyInjectionTestExecutionListener.class, "reinjectDependencies");

	private static final Log logger = LogFactory.getLog(DependencyInjectionTestExecutionListener.class);


	/**
	 * <p>
	 * Injects dependencies into the test instance of the supplied
	 * {@link TestContext test context}.
	 * </p>
	 * <p>
	 * {@link AutowireCapableBeanFactory#autowireBeanProperties(Object, int, boolean) Autowires}
	 * the test instance via the application context of the supplied
	 * {@link TestContext}, without checking dependencies. The resulting bean
	 * will also be
	 * {@link AutowireCapableBeanFactory#initializeBean(Object, String) initialized}.
	 * </p>
	 */
	@Override
	public void prepareTestInstance(final TestContext testContext) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Performing dependency injection for test context [" + testContext + "].");
		}
		injectDependencies(testContext);
	}

	/**
	 * <p>
	 * If the {@link #REINJECT_DEPENDENCIES_ATTRIBUTE} in the supplied
	 * {@link TestContext test context} is {@value Boolean#TRUE}, this method
	 * will have the same effect as
	 * {@link #prepareTestInstance(TestContext) prepareTestInstance()} and will
	 * subsequently remove the {@link #REINJECT_DEPENDENCIES_ATTRIBUTE} from the
	 * test context; otherwise, this method will have no effect.
	 * </p>
	 */
	@Override
	public void beforeTestMethod(final TestContext testContext) throws Exception {
		if (Boolean.TRUE.equals(testContext.getAttribute(REINJECT_DEPENDENCIES_ATTRIBUTE))) {
			if (logger.isDebugEnabled()) {
				logger.debug("Reinjecting dependencies for test context [" + testContext + "].");
			}
			injectDependencies(testContext);
			testContext.removeAttribute(REINJECT_DEPENDENCIES_ATTRIBUTE);
		}
	}

	/**
	 * Performs the actual dependency injection and bean initialization.
	 *
	 * @param testContext The test context for which dependency injection should
	 *        be performed; may not be <code>null</code>.
	 * @throws Exception Allows any exception to propagate.
	 * @see #prepareTestInstance(TestContext)
	 * @see #beforeTestMethod(TestContext)
	 */
	private void injectDependencies(final TestContext testContext) throws Exception {
		final Object bean = testContext.getTestInstance();
		final AutowireCapableBeanFactory beanFactory = testContext.getApplicationContext().getAutowireCapableBeanFactory();
		beanFactory.autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
		beanFactory.initializeBean(bean, testContext.getTestClass().getName());
	}

}
