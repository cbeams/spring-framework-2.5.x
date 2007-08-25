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
package org.springframework.test.context.junit4;

import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;

import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * JUnit 4 based unit test which verifies support of Spring's
 * {@link Transactional @Transactional},
 * {@link TestExecutionListeners @TestExecutionListeners}, and
 * {@link ContextConfiguration @ContextConfiguration} annotations in conjunction
 * with the {@link SpringJUnit4ClassRunner} and the following
 * {@link TestExecutionListener TestExecutionListeners}:
 * </p>
 * <ul>
 * <li>{@link DependencyInjectionTestExecutionListener}</li>
 * <li>{@link DirtiesContextTestExecutionListener}</li>
 * <li>{@link TransactionalTestExecutionListener}</li>
 * </ul>
 * <p>
 * This class specifically tests usage of <code>&#064;Transactional</code>
 * defined at the <strong>method level</strong>. In contrast to
 * {@link ClassLevelTransactionalSpringRunnerTests}, this class omits usage of
 * <code>&#064;NotTransactional</code>.
 * </p>
 *
 * @see ClassLevelTransactionalSpringRunnerTests
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class })
public class MethodLevelTransactionalSpringRunnerTests extends AbstractTransactionalSpringRunnerTests {

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected static SimpleJdbcTemplate	simpleJdbcTemplate;

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {

		return new JUnit4TestAdapter(MethodLevelTransactionalSpringRunnerTests.class);
	}

	@AfterClass
	public static void verifyFinalTestData() {

		assertEquals("Verifying the final number of rows in the person table after all tests.", 4,
				countRowsInPersonTable(simpleJdbcTemplate));
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	@Before
	public void verifyInitialTestData() {

		clearPersonTable(simpleJdbcTemplate);
		assertEquals("Adding bob", 1, addPerson(simpleJdbcTemplate, BOB));
		assertEquals("Verifying the initial number of rows in the person table.", 1,
				countRowsInPersonTable(simpleJdbcTemplate));
	}

	@Test
	@Transactional
	public void modifyTestDataWithinTransaction() {

		assertEquals("Deleting bob", 1, deletePerson(simpleJdbcTemplate, BOB));
		assertEquals("Adding jane", 1, addPerson(simpleJdbcTemplate, JANE));
		assertEquals("Adding sue", 1, addPerson(simpleJdbcTemplate, SUE));
		assertEquals("Verifying the number of rows in the person table within a transaction.", 2,
				countRowsInPersonTable(simpleJdbcTemplate));
	}

	@Test
	public void modifyTestDataWithoutTransaction() {

		assertEquals("Adding luke", 1, addPerson(simpleJdbcTemplate, LUKE));
		assertEquals("Adding leia", 1, addPerson(simpleJdbcTemplate, LEIA));
		assertEquals("Adding yoda", 1, addPerson(simpleJdbcTemplate, YODA));
		assertEquals("Verifying the number of rows in the person table without a transaction.", 4,
				countRowsInPersonTable(simpleJdbcTemplate));
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
