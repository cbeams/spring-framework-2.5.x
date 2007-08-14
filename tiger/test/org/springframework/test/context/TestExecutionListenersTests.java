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

import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.ContextConfiguration;
import org.springframework.test.annotation.TestExecutionListeners;
import org.springframework.test.context.listeners.AbstractTestExecutionListener;
import org.springframework.test.context.listeners.TestExecutionListener;

/**
 * JUnit 4 based unit test for the {@link TestExecutionListeners} annotation.
 * This test verifies proper registering of
 * {@link TestExecutionListener listeners} in conjunction with the
 * {@link ContextConfiguration} annotation and a {@link TestContextManager}.
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
@RunWith(JUnit4ClassRunner.class)
public class TestExecutionListenersTests {

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {

		return new JUnit4TestAdapter(TestExecutionListenersTests.class);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	@Test
	public void verifyNumListenersRegistered() throws Exception {

		final TestContextManager<?> testContextManager = new TestContextManager<ExampleTest>(ExampleTest.class);
		assertEquals("Verifying the number of registered TestExecutionListeners", 4,
				testContextManager.getTestExecutionListeners().size());
	}

	// ------------------------------------------------------------------------|
	// --- TYPES --------------------------------------------------------------|
	// ------------------------------------------------------------------------|

	@ContextConfiguration
	@TestExecutionListeners( { FooTestExecutionListener.class, BarTestExecutionListener.class,
			BazTestExecutionListener.class, QuuxTestExecutionListener.class })
	private static class ExampleTest {
	}

	static class FooTestExecutionListener extends AbstractTestExecutionListener {
	}

	static class BarTestExecutionListener extends AbstractTestExecutionListener {
	}

	static class BazTestExecutionListener extends AbstractTestExecutionListener {
	}

	static class QuuxTestExecutionListener extends AbstractTestExecutionListener {
	}

	// ------------------------------------------------------------------------|

}
