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
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.annotation.ContextConfiguration;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.annotation.TestExecutionListeners;
import org.springframework.test.context.listeners.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.listeners.DirtiesContextTestExecutionListener;
import org.springframework.test.context.listeners.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * JUnit 4 based unit test, which verifies support of Spring's
 * {@link Transactional}, {@link NotTransactional}, and
 * {@link ContextConfiguration} annotations in conjunction with
 * {@link SpringJUnit4ClassRunner}.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.2 $
 * @since 2.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class })
@Transactional
public class TransactionalSpringRunnerTests {

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected static int				numRowsInPersonTable;

	protected static SimpleJdbcTemplate	simpleJdbcTemplate;

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected boolean					noTestsRun	= true;

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected static int countRowsInPersonTable() {

		return simpleJdbcTemplate.queryForInt("SELECT COUNT(0) FROM person");
	}

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {

		return new JUnit4TestAdapter(TransactionalSpringRunnerTests.class);
	}

	@AfterClass
	public static void verifyFinalTestData() {

		assertEquals("Verifying the final number of rows in the person table.", 4, numRowsInPersonTable);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected void addPerson(final String name, final int age) {

		simpleJdbcTemplate.update("INSERT INTO person VALUES(?,?)", name, age);
	}

	@Before
	public void verifyInitialTestData() {

		if (this.noTestsRun) {
			simpleJdbcTemplate.update("DELETE FROM person");
			addPerson("bob", 35);
			assertEquals("Verifying the initial number of rows in the person table.", 1, countRowsInPersonTable());
		}
	}

	@Test
	public void modifyTestDataWithinTransaction() {

		addPerson("jane", 25);
		addPerson("sue", 24);

		final int numRows = countRowsInPersonTable();
		assertEquals("Verifying the number of rows in the person table.", 3, numRows);

		this.noTestsRun = false;
		TransactionalSpringRunnerTests.numRowsInPersonTable = numRows;
	}

	@Test
	@NotTransactional
	public void modifyTestDataWithoutTransaction() {

		addPerson("luke", 25);
		addPerson("leia", 25);
		addPerson("yoda", 900);

		final int numRows = countRowsInPersonTable();
		assertEquals("Verifying the number of rows in the person table.", 4, numRows);

		this.noTestsRun = false;
		TransactionalSpringRunnerTests.numRowsInPersonTable = numRows;
	}

	// ------------------------------------------------------------------------|
	// --- TYPES --------------------------------------------------------------|
	// ------------------------------------------------------------------------|

	public static class DatabaseSetup {

		@Autowired
		void setDataSource(final DataSource dataSource) {

			TransactionalSpringRunnerTests.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
			try {
				TransactionalSpringRunnerTests.simpleJdbcTemplate.update("CREATE TABLE person (name VARCHAR(20) NOT NULL, age INTEGER NOT NULL, PRIMARY KEY(name))");
			}
			catch (final BadSqlGrammarException bsge) {
				/* ignore */
			}
		}
	}

	// ------------------------------------------------------------------------|

}
