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
package org.springframework.test.junit38;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.Employee;
import org.springframework.beans.Pet;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.annotation.AfterTransaction;
import org.springframework.test.annotation.BeforeTransaction;
import org.springframework.test.annotation.ContextConfiguration;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.jdbc.SimpleJdbcTestUtils;

/**
 * Combined unit test for {@link AbstractJUnit38SpringContextTests} and
 * {@link AbstractTransactionalJUnit38SpringContextTests}.
 *
 * @author Sam Brannen
 * @version $Revision: 1.4 $
 * @since 2.1
 */
@ContextConfiguration
public class ConcreteTransactionalJUnit38SpringContextTests extends AbstractTransactionalJUnit38SpringContextTests
		implements BeanNameAware, InitializingBean {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected static final String	BOB				= "bob";

	protected static final String	JANE			= "jane";

	protected static final String	SUE				= "sue";

	protected static final String	LUKE			= "luke";

	protected static final String	LEIA			= "leia";

	protected static final String	YODA			= "yoda";

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private boolean					beanInitialized	= false;

	private String					beanName		= "replace me with null";

	private Employee				employee;

	@Autowired
	private Pet						pet;

	@Autowired(required = false)
	protected Long					nonrequiredLong;

	@Resource()
	protected String				foo;

	protected String				bar;

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	public ConcreteTransactionalJUnit38SpringContextTests() throws Exception {

		super();
	}

	public ConcreteTransactionalJUnit38SpringContextTests(final String name) throws Exception {

		super(name);
	}

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected static int clearPersonTable(final SimpleJdbcTemplate simpleJdbcTemplate) {

		return SimpleJdbcTestUtils.deleteFromTables(simpleJdbcTemplate, "person");
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

		return SimpleJdbcTestUtils.countRowsInTable(simpleJdbcTemplate, "person");
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
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public final void afterPropertiesSet() throws Exception {

		this.beanInitialized = true;
	}

	/**
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public final void setBeanName(final String beanName) {

		this.beanName = beanName;
	}

	@Autowired
	protected final void setEmployee(final Employee employee) {

		this.employee = employee;
	}

	@Resource
	protected final void setBar(final String bar) {

		this.bar = bar;
	}

	// ------------------------------------------------------------------------|

	@Test
	@NotTransactional
	public final void verifyApplicationContext() {

		assertNotNull("The application context should have been set due to ApplicationContextAware semantics.",
				getApplicationContext());
	}

	@Test
	@NotTransactional
	public final void verifyBeanInitialized() {

		assertTrue("This test bean should have been initialized due to InitializingBean semantics.",
				this.beanInitialized);
	}

	@Test
	@NotTransactional
	public final void verifyBeanNameSet() {

		assertNull("The bean name of this test instance should have been set to NULL "
				+ "due to BeanNameAware semantics, since the testing support classes "
				+ "currently do not provide a bean name for dependency injected test instances.", this.beanName);
	}

	@Test
	@NotTransactional
	public final void verifyAnnotationAutowiredFields() {

		assertNull("The nonrequiredLong property should NOT have been autowired.", this.nonrequiredLong);
		assertNotNull("The pet field should have been autowired.", this.pet);
		assertEquals("Fido", this.pet.getName());
	}

	@Test
	@NotTransactional
	public final void verifyAnnotationAutowiredMethods() {

		assertNotNull("The employee setter method should have been autowired.", this.employee);
		assertEquals("John Smith", this.employee.getName());
	}

	@Test
	@NotTransactional
	public final void verifyResourceAnnotationWiredFields() {

		assertEquals("The foo field should have been wired via @Resource.", "Foo", this.foo);
	}

	@Test
	@NotTransactional
	public final void verifyResourceAnnotationWiredMethods() {

		assertEquals("The bar method should have been wired via @Resource.", "Bar", this.bar);
	}

	// ------------------------------------------------------------------------|

	@BeforeTransaction
	public void beforeTransaction() {

		assertEquals("Verifying the number of rows in the person table before a transactional test method.", 1,
				countRowsInPersonTable(getSimpleJdbcTemplate()));
		assertEquals("Adding yoda", 1, addPerson(getSimpleJdbcTemplate(), YODA));
	}

	@Override
	protected void setUp() throws Exception {

		assertEquals("Verifying the number of rows in the person table before a test method.", 2,
				countRowsInPersonTable(getSimpleJdbcTemplate()));
	}

	public void testModifyTestDataWithinTransaction() {

		assertEquals("Adding jane", 1, addPerson(getSimpleJdbcTemplate(), JANE));
		assertEquals("Adding sue", 1, addPerson(getSimpleJdbcTemplate(), SUE));
		assertEquals("Verifying the number of rows in the person table within transactionalMethod2().", 4,
				countRowsInPersonTable(getSimpleJdbcTemplate()));
	}

	@Override
	protected void tearDown() throws Exception {

		assertEquals("Verifying the number of rows in the person table after a test method.", 4,
				countRowsInPersonTable(getSimpleJdbcTemplate()));
	}

	@AfterTransaction
	public void afterTransaction() {

		assertEquals("Deleting yoda", 1, deletePerson(getSimpleJdbcTemplate(), YODA));
		assertEquals("Verifying the number of rows in the person table after a transactional test method.", 1,
				countRowsInPersonTable(getSimpleJdbcTemplate()));
	}

	// ------------------------------------------------------------------------|
	// --- TYPES --------------------------------------------------------------|
	// ------------------------------------------------------------------------|

	public static class DatabaseSetup {

		@Autowired
		void setDataSource(final DataSource dataSource) {

			final SimpleJdbcTemplate simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
			createPersonTable(simpleJdbcTemplate);
			clearPersonTable(simpleJdbcTemplate);
			addPerson(simpleJdbcTemplate, BOB);
		}
	}

	// ------------------------------------------------------------------------|

}
