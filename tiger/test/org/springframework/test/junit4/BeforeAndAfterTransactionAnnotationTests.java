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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.annotation.AfterTransaction;
import org.springframework.test.annotation.BeforeTransaction;
import org.springframework.test.annotation.ContextConfiguration;
import org.springframework.test.annotation.TestExecutionListeners;
import org.springframework.test.context.listeners.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.listeners.DirtiesContextTestExecutionListener;
import org.springframework.test.context.listeners.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * JUnit 4 based unit test which verifies
 * {@link BeforeTransaction @BeforeTransaction} and
 * {@link AfterTransaction @AfterTransaction} behavior.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class })
public class BeforeAndAfterTransactionAnnotationTests extends AbstractTransactionalSpringRunnerTests {

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected static SimpleJdbcTemplate	simpleJdbcTemplate;

	protected static int				numBeforeTransactionCalls	= 0;

	protected static int				numAfterTransactionCalls	= 0;

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected boolean					inTransaction				= false;

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {

		return new JUnit4TestAdapter(BeforeAndAfterTransactionAnnotationTests.class);
	}

	@BeforeClass
	public static void beforeClass() {

		BeforeAndAfterTransactionAnnotationTests.numBeforeTransactionCalls = 0;
		BeforeAndAfterTransactionAnnotationTests.numAfterTransactionCalls = 0;
	}

	@AfterClass
	public static void afterClass() {

		assertEquals("Verifying the final number of rows in the person table after all tests.", 3,
				countRowsInPersonTable(simpleJdbcTemplate));
		assertEquals("Verifying the total number of calls to beforeTransaction().", 2,
				BeforeAndAfterTransactionAnnotationTests.numBeforeTransactionCalls);
		assertEquals("Verifying the total number of calls to afterTransaction().", 2,
				BeforeAndAfterTransactionAnnotationTests.numAfterTransactionCalls);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	@BeforeTransaction
	public void beforeTransaction() {

		this.inTransaction = true;
		BeforeAndAfterTransactionAnnotationTests.numBeforeTransactionCalls++;
		clearPersonTable(simpleJdbcTemplate);
		assertEquals("Adding yoda", 1, addPerson(simpleJdbcTemplate, YODA));
	}

	@AfterTransaction
	public void afterTransaction() {

		this.inTransaction = false;
		BeforeAndAfterTransactionAnnotationTests.numAfterTransactionCalls++;
		assertEquals("Deleting yoda", 1, deletePerson(simpleJdbcTemplate, YODA));
		assertEquals("Verifying the number of rows in the person table after a transactional test method.", 0,
				countRowsInPersonTable(simpleJdbcTemplate));
	}

	@Before
	public void before() {

		assertEquals("Verifying the number of rows in the person table before a test method.", (this.inTransaction ? 1
				: 0), countRowsInPersonTable(simpleJdbcTemplate));
	}

	@Test
	@Transactional
	public void transactionalMethod1() {

		assertEquals("Adding jane", 1, addPerson(simpleJdbcTemplate, JANE));
		assertEquals("Verifying the number of rows in the person table within transactionalMethod1().", 2,
				countRowsInPersonTable(simpleJdbcTemplate));
	}

	@Test
	@Transactional
	public void transactionalMethod2() {

		assertEquals("Adding jane", 1, addPerson(simpleJdbcTemplate, JANE));
		assertEquals("Adding sue", 1, addPerson(simpleJdbcTemplate, SUE));
		assertEquals("Verifying the number of rows in the person table within transactionalMethod2().", 3,
				countRowsInPersonTable(simpleJdbcTemplate));
	}

	@Test
	public void nonTransactionalMethod() {

		assertEquals("Adding luke", 1, addPerson(simpleJdbcTemplate, LUKE));
		assertEquals("Adding leia", 1, addPerson(simpleJdbcTemplate, LEIA));
		assertEquals("Adding yoda", 1, addPerson(simpleJdbcTemplate, YODA));
		assertEquals("Verifying the number of rows in the person table without a transaction.", 3,
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
