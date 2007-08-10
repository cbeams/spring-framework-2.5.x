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
package org.springframework.test.context.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.TestContext;

/**
 * TestExecutionListener which provides support for dependency injection of test
 * instances.
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
public class DependencyInjectionTestExecutionListener extends AbstractTestExecutionListener {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	private static final Log LOG = LogFactory.getLog(DependencyInjectionTestExecutionListener.class);

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Injects dependencies into the test instance of the supplied
	 * {@link TestContext test context}.
	 * </p>
	 * <p>
	 * The default implementation
	 * {@link AutowireCapableBeanFactory#autowireBeanProperties(Object, int, boolean) autowires}
	 * the test instance via the supplied application context, using the
	 * {@link ContextConfigurationAttributes#getAutowireMode() autowire mode}
	 * and
	 * {@link ContextConfigurationAttributes#isCheckDependencies() dependency check}
	 * attributes in the {@link ContextConfigurationAttributes configuration} of
	 * the supplied test context. The resulting bean will also be
	 * {@link AutowireCapableBeanFactory#initializeBean(Object, String) initialized}.
	 * </p>
	 * <p>
	 * Override this method if you need full control over how dependencies are
	 * injected into the test instance.
	 * </p>
	 *
	 * @see org.springframework.test.context.listeners.AbstractTestExecutionListener#prepareTestInstance(java.lang.Object)
	 */
	@Override
	public void prepareTestInstance(final TestContext<?> testContext) throws Exception {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Performing dependency injection for test context [" + testContext + "].");
		}

		final ConfigurableApplicationContext context = testContext.getApplicationContext();
		final Object bean = testContext.getTestInstance();

		synchronized (context) {

			final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
			beanFactory.autowireBeanProperties(bean,
					testContext.getConfigurationAttributes().getAutowireMode().value(),
					testContext.getConfigurationAttributes().isCheckDependencies());
			beanFactory.initializeBean(bean, null);

			if (!context.isActive()) {
				context.refresh();
			}
		}

	}

	// ------------------------------------------------------------------------|

}
