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
package org.springframework.test.context.junit38;

import junit.framework.TestCase;

import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;

/**
 * Unit test for {@link AbstractJUnit38SpringContextTests} which focuses on
 * proper support of the {@link Repeat @Repeat} annotation.
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
public class RepeatedJUnit38SpringContextTests extends TestCase {

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	public RepeatedJUnit38SpringContextTests() throws Exception {

		super();
	}

	public RepeatedJUnit38SpringContextTests(final String name) throws Exception {

		super(name);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	private void assertRepetitions(final String testName, final int expectedNumInvocations) throws Exception {

		final RepeatedTestCase repeatedTestCase = new RepeatedTestCase(testName);
		repeatedTestCase.run();
		assertEquals("Verifying number of invocations for test method [" + testName + "].", expectedNumInvocations,
				repeatedTestCase.invocationCount);
	}

	public void testRepeatAnnotationSupport() throws Exception {

		assertRepetitions("testNonAnnotated", 1);
		assertRepetitions("testNegativeRepeatValue", 1);
		assertRepetitions("testDefaultRepeatValue", 1);
		assertRepetitions("testRepeatedFiveTimes", 5);
	}

	// ------------------------------------------------------------------------|
	// --- TYPES --------------------------------------------------------------|
	// ------------------------------------------------------------------------|

	@ContextConfiguration(generateDefaultLocations = false)
	public static class RepeatedTestCase extends AbstractJUnit38SpringContextTests {

		int	invocationCount	= 0;

		public RepeatedTestCase(final String name) throws Exception {

			super(name);
		}

		@Override
		protected void setUp() throws Exception {

			this.invocationCount++;
		}

		public void testNonAnnotated() {

			/* no-op */
		}

		@Repeat(-5)
		public void testNegativeRepeatValue() {

			/* no-op */
		}

		@Repeat
		public void testDefaultRepeatValue() {

			/* no-op */
		}

		@Repeat(5)
		public void testRepeatedFiveTimes() {

			/* no-op */
		}
	}

}
