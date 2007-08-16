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

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;

/**
 * TestExecutionListener which processes methods configured with the
 * &#064;DirtiesContext annotation.
 *
 * @see DirtiesContext
 * @author Sam Brannen
 * @version $Revision: 1.2 $
 * @since 2.1
 */
public class DirtiesContextTestExecutionListener extends AbstractTestExecutionListener {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	private static final Log LOG = LogFactory.getLog(DirtiesContextTestExecutionListener.class);

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * If the current test method of the supplied
	 * {@link TestContext test context} has been annotated with
	 * {@link DirtiesContext}, the
	 * {@link ApplicationContext application context} of the test context will
	 * be {@link TestContext#markApplicationContextDirty() marked as dirty}.
	 * </p>
	 * <p>
	 * Note that this implementation allows for <em>annotation inheritance</em>
	 * for methods annotated with {@link DirtiesContext}.
	 * </p>
	 *
	 * @see AnnotationUtils#findAnnotation(Method, Class)
	 * @see org.springframework.test.context.listeners.AbstractTestExecutionListener#afterTestMethod(TestContext)
	 */
	@Override
	public void afterTestMethod(final TestContext<?> testContext) {

		final boolean dirtiesContext = (AnnotationUtils.findAnnotation(testContext.getTestMethod(),
				DirtiesContext.class) != null);
		if (LOG.isDebugEnabled()) {
			LOG.debug("After test method: context [" + testContext + "], dirtiesContext [" + dirtiesContext + "].");
		}

		if (dirtiesContext) {
			testContext.markApplicationContextDirty();
		}
	}

	// ------------------------------------------------------------------------|

}
