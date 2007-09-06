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

import junit.framework.JUnit4TestAdapter;

import org.junit.runner.RunWith;
import org.springframework.test.context.TestExecutionListeners;

/**
 * <p>
 * Simple unit test to verify that {@link SpringJUnit4ClassRunner} does not
 * hinder correct functionality of standard JUnit 4.4+ testing features.
 * </p>
 * <p>
 * Note that {@link TestExecutionListeners @TestExecutionListeners} is
 * explicitly configured with an empty list, thus disabling all default
 * listeners.
 * </p>
 *
 * @author Sam Brannen
 * @since 2.1
 * @see StandardJUnit4FeaturesTests
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( {})
public class StandardJUnit4FeaturesSpringRunnerTests extends StandardJUnit4FeaturesTests {

	/* all tests are in the parent class. */

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {

		return new JUnit4TestAdapter(StandardJUnit4FeaturesSpringRunnerTests.class);
	}

}
