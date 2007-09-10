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

package org.springframework.test.context.junit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.TestExecutionListeners;

/**
 * <p>
 * Verifies the correct handling of JUnit's {@link Ignore @Ignore} and Spring's
 * {@link IfProfileValue @IfProfileValue} annotations in conjunction with the
 * {@link SpringJUnit4ClassRunner}.
 * </p>
 * <p>
 * Note that {@link TestExecutionListeners @TestExecutionListeners} is
 * explicitly configured with an empty list, thus disabling all default
 * listeners.
 * </p>
 *
 * @author Sam Brannen
 * @since 2.5
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( {})
public class EnabledAndIgnoredSpringRunnerTests {

	// ------------------------------------------------------------------------|
	// --- CLASS VARIABLES ----------------------------------------------------|
	// ------------------------------------------------------------------------|

	private static int			numTestsExecuted	= 0;

	private static final String	NAME				= "test.if_profile_value.name";

	private static final String	VALUE				= "enigma";

	// ------------------------------------------------------------------------|
	// --- CLASS METHODS ------------------------------------------------------|
	// ------------------------------------------------------------------------|

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {

		return new JUnit4TestAdapter(EnabledAndIgnoredSpringRunnerTests.class);
	}

	// ------------------------------------------------------------------------|

	@BeforeClass
	public static void setProfileValue() {

		numTestsExecuted = 0;
		System.setProperty(NAME, VALUE);
	}

	@AfterClass
	public static void verifyNumTestsExecuted() {

		assertEquals("Verifying the number of tests executed.", 1, numTestsExecuted);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	@Test
	@IfProfileValue(name = NAME, value = VALUE + "X")
	public void testDisabledIfProfileValueAnnotation() {

		numTestsExecuted++;
		fail("The body of a disabled test should never be executed!");
	}

	@Test
	@IfProfileValue(name = NAME, value = VALUE)
	public void testEnabledIfProfileValueAnnotation() {

		numTestsExecuted++;
	}

	@Test
	@Ignore
	public void testJUnitIgnoreAnnotation() {

		numTestsExecuted++;
		fail("The body of an ignored test should never be executed!");
	}

}
