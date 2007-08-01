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
package org.springframework.test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.Employee;
import org.springframework.beans.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.ContextConfiguration;

/**
 * <p>
 * SpringJUnit4ClassRunnerApplicationContextTests serves as a
 * <em>proof of concept</em> JUnit 4 based test class, which verifies the
 * expected functionality of {@link SpringJUnit4ClassRunner} in conjunction with
 * {@link ContextConfiguration} and Spring's {@link Autowired} annotation.
 * </p>
 * <p>
 * Since no {@link ContextConfiguration#locations() locations} are explicitly
 * defined and
 * {@link ContextConfiguration#generateDefaultLocations() generateDefaultLocations}
 * is left set to its default value of <code>true</code>, this test class's
 * dependencies will be injected via
 * {@link Autowired annotation-based autowiring} from beans defined in the
 * {@link ApplicationContext} loaded from the default resource: &quot;<code>org/springframework/test/junit/SpringJUnit4ClassRunnerApplicationContextTests-context.xml</code>&quot;.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class SpringJUnit4ClassRunnerApplicationContextTests {

	// ------------------------------------------------------------------------|
	// --- CLASS VARIABLES ----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CLASS METHODS ------------------------------------------------------|
	// ------------------------------------------------------------------------|

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {

		return new JUnit4TestAdapter(SpringJUnit4ClassRunnerApplicationContextTests.class);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private Employee employee;

	@Autowired
	private Pet pet;

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected Employee getEmployee() {

		return this.employee;
	}

	// ------------------------------------------------------------------------|

	protected Pet getPet() {

		return this.pet;
	}

	// ------------------------------------------------------------------------|

	/**
	 * @param employee The employee to set.
	 */
	@Autowired
	protected void setEmployee(final Employee employee) {

		this.employee = employee;
	}

	// ------------------------------------------------------------------------|

	@Test
	public void verifyAnnotationAutowiredFields() {

		assertNotNull("The pet field should have been autowired.", getPet());
		assertEquals("Fido", getPet().getName());
	}

	// ------------------------------------------------------------------------|

	@Test
	public void verifyAnnotationAutowiredMethods() {

		assertNotNull("The employee setter method should have been autowired.", getEmployee());
		assertEquals("John Smith", getEmployee().getName());
	}

	// ------------------------------------------------------------------------|

}
