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

import junit.framework.JUnit4TestAdapter;

import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

/**
 * <p>
 * Verifies proper handling of {@link IfProfileValue @IfProfileValue} in
 * conjunction with {@link AbstractJUnit38SpringContextTests}.
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
@RunWith(JUnit38ClassRunner.class)
@ContextConfiguration
@TestExecutionListeners( {})
public class ProfileValueJUnit38SpringContextTests extends AbstractJUnit38SpringContextTests {

	private static final String NAME = "test.if_profile_value.name";
	private static final String VALUE = "enigma";


	public ProfileValueJUnit38SpringContextTests() throws Exception {
		this(null);
	}

	public ProfileValueJUnit38SpringContextTests(final String name) throws Exception {
		super(name);
		System.setProperty(NAME, VALUE);
	}

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ProfileValueJUnit38SpringContextTests.class);
	}

	@IfProfileValue(name = NAME, value = VALUE + "X")
	public void testIfProfileValueDisabled() {
		fail("The body of a disabled test should never be executed!");
	}

	@IfProfileValue(name = NAME, value = VALUE)
	public void testIfProfileValueEnabled() {
		assertTrue(true);
	}

	public void testIfProfileValueNotConfigured() {
		assertTrue(true);
	}

}
