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
package org.springframework.test.junit4;

import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;

import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.annotation.ContextConfiguration;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.annotation.TestExecutionListeners;
import org.springframework.test.context.listeners.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.listeners.DirtiesContextTestExecutionListener;
import org.springframework.test.context.listeners.TestExecutionListener;
import org.springframework.test.context.listeners.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * JUnit 4 based unit test which verifies support of Spring's
 * &#064;Transactional, &#064;NotTransactional, &#064;TestExecutionListeners,
 * and &#064;ContextConfiguration annotations in conjunction with the
 * {@link SpringJUnit4ClassRunner} and the following
 * {@link TestExecutionListener TestExecutionListeners}:
 * {@link DependencyInjectionTestExecutionListener},
 * {@link DirtiesContextTestExecutionListener}, and
 * {@link TransactionalTestExecutionListener}.
 * </p>
 * <p>
 * This class specifically tests usage of &#064;Transactional defined at the
 * <strong>class level</strong>.
 * </p>
 *
 * @see MethodLevelTransactionalSpringRunnerTests
 * @see Transactional
 * @see NotTransactional
 * @see TestExecutionListeners
 * @see ContextConfiguration
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class })
@Transactional
public class ClassLevelTransactionalSpringRunnerTests extends AbstractTransactionalSpringRunnerTests {

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected static int				numRowsInPersonTable;

	protected static SimpleJdbcTemplate	simpleJdbcTemplate;

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {

		return new JUnit4TestAdapter(ClassLevelTransactionalSpringRunnerTests.class);
	}

	@AfterClass
	public static void verifyFinalTestData() {

		assertEquals("Verifying the final number of rows in the person table after all tests.", 4, numRowsInPersonTable);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	@Before
	public void verifyInitialTestData() {

		if (this.noTestsRun) {
			simpleJdbcTemplate.update("DELETE FROM person");
		}
		assertEquals("Adding bob", 1, addPerson(simpleJdbcTemplate, BOB));
		assertEquals("Verifying the initial number of rows in the person table.", 1,
				countRowsInPersonTable(simpleJdbcTemplate));
	}

	@Test
	public void modifyTestDataWithinTransaction() {

		// TODO How can we verify this method IS executing in a transaction?

		assertEquals("Deleting bob", 1, deletePerson(simpleJdbcTemplate, BOB));
		assertEquals("Adding jane", 1, addPerson(simpleJdbcTemplate, JANE));
		assertEquals("Adding sue", 1, addPerson(simpleJdbcTemplate, SUE));

		final int numRows = countRowsInPersonTable(simpleJdbcTemplate);
		assertEquals("Verifying the number of rows in the person table within a transaction.", 2, numRows);

		this.noTestsRun = false;
		ClassLevelTransactionalSpringRunnerTests.numRowsInPersonTable = numRows;
	}

	@Test
	@NotTransactional
	public void modifyTestDataWithoutTransaction() {

		// TODO How can we verify this method is NOT executing in a transaction?

		assertEquals("Adding luke", 1, addPerson(simpleJdbcTemplate, LUKE));
		assertEquals("Adding leia", 1, addPerson(simpleJdbcTemplate, LEIA));
		assertEquals("Adding yoda", 1, addPerson(simpleJdbcTemplate, YODA));

		final int numRows = countRowsInPersonTable(simpleJdbcTemplate);
		assertEquals("Verifying the number of rows in the person table without a transaction.", 4, numRows);

		this.noTestsRun = false;
		ClassLevelTransactionalSpringRunnerTests.numRowsInPersonTable = numRows;
	}

	// ------------------------------------------------------------------------|
	// --- TYPES --------------------------------------------------------------|
	// ------------------------------------------------------------------------|

	public static class DatabaseSetup {

		@Autowired
		void setDataSource(final DataSource dataSource) {

			simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
			createPersonTable(simpleJdbcTemplate);
		}
	}

	// ------------------------------------------------------------------------|

}
