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

package org.springframework.test.context.junit38;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.TestExecutionListeners;

/**
 * <p>
 * Verifies proper handling of {@link IfProfileValue @IfProfileValue} in
 * conjunction with {@link AbstractJUnit38SpringContextTests}.
 * </p>
 *
 * @author Sam Brannen
 * @since 2.5
 */
public class ProfileValueJUnit38SpringContextTests extends TestCase {

	public ProfileValueJUnit38SpringContextTests() throws Exception {
		this(null);
	}

	public ProfileValueJUnit38SpringContextTests(final String name) throws Exception {
		super(name);
	}

	private void assertInvocationCount(final String testName, final int expectedInvocationCount,
			final int expectedErrorCount, final int expectedFailureCount) throws Exception {
		final ProfileValueTestCase profileValueTestCase = new ProfileValueTestCase(testName);
		final TestResult testResult = profileValueTestCase.run();
		assertEquals("Verifying number of invocations for test method [" + testName + "].", expectedInvocationCount,
				profileValueTestCase.invocationCount);
		assertEquals("Verifying number of errors for test method [" + testName + "].", expectedErrorCount,
				testResult.errorCount());
		assertEquals("Verifying number of failures for test method [" + testName + "].", expectedFailureCount,
				testResult.failureCount());
	}

	public void testIfProfileValueAnnotationSupport() throws Exception {
		assertInvocationCount("testIfProfileValueEmpty", 0, 1, 0);
		assertInvocationCount("testIfProfileValueDisabled", 0, 0, 0);
		assertInvocationCount("testIfProfileValueEnabledViaSingleValue", 1, 0, 0);
		assertInvocationCount("testIfProfileValueEnabledViaMultipleValues", 1, 0, 0);
		assertInvocationCount("testIfProfileValueNotConfigured", 1, 0, 0);
	}


	/**
	 * <p>
	 * Note that {@link TestExecutionListeners @TestExecutionListeners} is
	 * explicitly configured with an empty list, thus disabling all default
	 * listeners.
	 * </p>
	 */
	@TestExecutionListeners( {})
	protected static class ProfileValueTestCase extends AbstractJUnit38SpringContextTests {

		private static final String NAME = "ProfileValueJUnit38SpringContextTests.profile_value.name";
		private static final String VALUE = "enigma";

		int invocationCount = 0;


		public ProfileValueTestCase(final String name) throws Exception {
			super(name);
			System.setProperty(NAME, VALUE);
		}

		@IfProfileValue(name = NAME, value = "")
		public void testIfProfileValueEmpty() {
			this.invocationCount++;
			fail("An empty profile value should throw an IllegalArgumentException.");
		}

		@IfProfileValue(name = NAME, value = VALUE + "X")
		public void testIfProfileValueDisabled() {
			this.invocationCount++;
			fail("The body of a disabled test should never be executed!");
		}

		@IfProfileValue(name = NAME, value = VALUE)
		public void testIfProfileValueEnabledViaSingleValue() {
			this.invocationCount++;
		}

		@IfProfileValue(name = NAME, values = { "foo", VALUE, "bar" })
		public void testIfProfileValueEnabledViaMultipleValues() {
			this.invocationCount++;
		}

		public void testIfProfileValueNotConfigured() {
			this.invocationCount++;
		}
	}
}
