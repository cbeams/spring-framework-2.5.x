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
package org.springframework.test.context;

import java.lang.reflect.Method;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
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
 * JUnit38TestContextManagerTests serves as a proof-of-concept for manually
 * using a {@link TestContextManager} to instrument a JUnit 3.8 based test.
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
@RunWith(JUnit38ClassRunner.class)
@ContextConfiguration
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class })
@Transactional
public class JUnit38TestContextManagerTests extends TestCase {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected static final String										BOB		= "bob";

	protected static final String										JANE	= "jane";

	protected static final String										SUE		= "sue";

	protected static final String										LUKE	= "luke";

	protected static final String										LEIA	= "leia";

	protected static final String										YODA	= "yoda";

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected static SimpleJdbcTemplate									simpleJdbcTemplate;

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final TestContextManager<JUnit38TestContextManagerTests>	testContextManager;

	private final Method												testMethod;

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|

	/**
	 * Default no-args constructor which delegates to
	 * {@link #JUnit38TestContextManagerTests(String)}, passing a value of
	 * <code>null</code> for the name.
	 *
	 * @throws Exception
	 */
	public JUnit38TestContextManagerTests() throws Exception {

		this(null);
	}

	// ------------------------------------------------------------------------|

	/**
	 * TODO Add comments for constructor.
	 *
	 * @param name
	 * @throws Exception
	 */
	public JUnit38TestContextManagerTests(final String name) throws Exception {

		super(name);
		this.testContextManager = new TestContextManager<JUnit38TestContextManagerTests>(
				JUnit38TestContextManagerTests.class);
		this.testMethod = getClass().getMethod(getName(), (Class[]) null);
		this.testContextManager.prepareTestInstance(this);
	}

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected static int clearPersonTable(final SimpleJdbcTemplate simpleJdbcTemplate) {

		return simpleJdbcTemplate.update("DELETE FROM person");
	}

	protected static void createPersonTable(final SimpleJdbcTemplate simpleJdbcTemplate) {

		try {
			simpleJdbcTemplate.update("CREATE TABLE person (name VARCHAR(20) NOT NULL, PRIMARY KEY(name))");
		}
		catch (final BadSqlGrammarException bsge) {
			/* ignore */
		}
	}

	protected static int countRowsInPersonTable(final SimpleJdbcTemplate simpleJdbcTemplate) {

		return simpleJdbcTemplate.queryForInt("SELECT COUNT(0) FROM person");
	}

	protected static int addPerson(final SimpleJdbcTemplate simpleJdbcTemplate, final String name) {

		return simpleJdbcTemplate.update("INSERT INTO person VALUES(?)", name);
	}

	protected static int deletePerson(final SimpleJdbcTemplate simpleJdbcTemplate, final String name) {

		return simpleJdbcTemplate.update("DELETE FROM person WHERE name=?", name);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * TODO Add comments for overridden runBare().
	 *
	 * @see junit.framework.TestCase#runBare()
	 */
	@Override
	public void runBare() throws Throwable {

		Throwable exception = null;
		this.testContextManager.beforeTestMethod(this, this.testMethod);
		setUp();
		try {
			runTest();
		}
		catch (final Throwable running) {
			exception = running;
		}
		finally {
			try {
				tearDown();
			}
			catch (final Throwable tearingDown) {
				if (exception == null) {
					exception = tearingDown;
				}
			}
		}
		this.testContextManager.afterTestMethod(this, this.testMethod, exception);
		if (exception != null) {
			throw exception;
		}
	}

	// ------------------------------------------------------------------------|

	@BeforeTransaction
	public void beforeTransaction() {

		assertEquals("Verifying the number of rows in the person table before a transactional test method.", 1,
				countRowsInPersonTable(simpleJdbcTemplate));
		assertEquals("Adding yoda", 1, addPerson(simpleJdbcTemplate, YODA));
	}

	@Override
	protected void setUp() throws Exception {

		assertEquals("Verifying the number of rows in the person table before a test method.", 2,
				countRowsInPersonTable(simpleJdbcTemplate));
	}

	public void testModifyTestDataWithinTransaction() {

		assertEquals("Adding jane", 1, addPerson(simpleJdbcTemplate, JANE));
		assertEquals("Adding sue", 1, addPerson(simpleJdbcTemplate, SUE));
		assertEquals("Verifying the number of rows in the person table within transactionalMethod2().", 4,
				countRowsInPersonTable(simpleJdbcTemplate));
	}

	@Override
	protected void tearDown() throws Exception {

		assertEquals("Verifying the number of rows in the person table after a test method.", 4,
				countRowsInPersonTable(simpleJdbcTemplate));
	}

	@AfterTransaction
	public void afterTransaction() {

		assertEquals("Deleting yoda", 1, deletePerson(simpleJdbcTemplate, YODA));
		assertEquals("Verifying the number of rows in the person table after a transactional test method.", 1,
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
			clearPersonTable(simpleJdbcTemplate);
			addPerson(simpleJdbcTemplate, BOB);
		}
	}

	// ------------------------------------------------------------------------|

}
