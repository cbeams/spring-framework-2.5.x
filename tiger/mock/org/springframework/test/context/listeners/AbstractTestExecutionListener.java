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

import org.springframework.test.context.TestContext;

/**
 * Abstract implementation of the {@link TestExecutionListener} interface which
 * provides empty method stubs. Subclasses can extend this class and override
 * only those methods suitable for the task at hand.
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
public class AbstractTestExecutionListener implements TestExecutionListener {

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * The default implementation is <em>empty</em>. Can be overridden by
	 * subclasses as necessary.
	 *
	 * @see org.springframework.test.context.listeners.TestExecutionListener#prepareTestInstance(java.lang.Object)
	 */
	@Override
	public void prepareTestInstance(final TestContext<?> testContext) throws Exception {

		/* no-op */
	}

	// ------------------------------------------------------------------------|

	/**
	 * The default implementation is <em>empty</em>. Can be overridden by
	 * subclasses as necessary.
	 *
	 * @see org.springframework.test.context.listeners.TestExecutionListener#beforeTestMethod(org.springframework.test.context.TestContext)
	 */
	@Override
	public void beforeTestMethod(final TestContext<?> testContext) {

		/* no-op */
	}

	// ------------------------------------------------------------------------|

	/**
	 * The default implementation is <em>empty</em>. Can be overridden by
	 * subclasses as necessary.
	 *
	 * @see org.springframework.test.context.listeners.TestExecutionListener#afterTestMethod(org.springframework.test.context.TestContext,
	 *      java.lang.Throwable)
	 */
	@Override
	public void afterTestMethod(final TestContext<?> testContext, final Throwable t) {

		/* no-op */
	}

	// ------------------------------------------------------------------------|

}
