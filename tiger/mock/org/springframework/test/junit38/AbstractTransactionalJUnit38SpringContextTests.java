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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.annotation.TestExecutionListeners;
import org.springframework.test.context.listeners.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.listeners.DirtiesContextTestExecutionListener;
import org.springframework.test.context.listeners.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * <p>
 * Abstract {@link Transactional transactional} extension of
 * {@link AbstractJUnit38SpringContextTests} that also adds some convenience
 * functionality for JDBC access. Expects a {@link javax.sql.DataSource} bean to
 * be defined in the Spring {@link ApplicationContext application context}.
 * </p>
 * <p>
 * This class exposes a {@link SimpleJdbcTemplate} and provides an easy way to
 * delete from the database in a new transaction.
 * </p>
 * <p>
 * Concrete subclasses must fulfill the same requirements outlined in
 * {@link AbstractJUnit38SpringContextTests}.
 * </p>
 *
 * @see AbstractJUnit38SpringContextTests
 * @see TestExecutionListeners
 * @see Transactional
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
		TransactionalTestExecutionListener.class })
@Transactional
public class AbstractTransactionalJUnit38SpringContextTests extends AbstractJUnit38SpringContextTests {

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	protected SimpleJdbcTemplate	jdbcTemplate;

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Default <em>no argument</em> constructor which delegates to
	 * {@link AbstractTransactionalJUnit38SpringContextTests#AbstractTransactionalJUnit38SpringContextTests(String) AbstractTransactionalJUnit38SpringContextTests(String)},
	 * passing a value of <code>null</code> for the test name.
	 *
	 * @see AbstractTransactionalJUnit38SpringContextTests#AbstractTransactionalJUnit38SpringContextTests(String)
	 * @throws Exception If an error occurs while initializing the test
	 *         instance.
	 */
	public AbstractTransactionalJUnit38SpringContextTests() throws Exception {

		this(null);
	}

	// ------------------------------------------------------------------------|

	/**
	 * Delegates to
	 * {@link AbstractJUnit38SpringContextTests#AbstractJUnit38SpringContextTests(String) AbstractJUnit38SpringContextTests(String)}.
	 *
	 * @see AbstractJUnit38SpringContextTests#AbstractJUnit38SpringContextTests(String)
	 * @param name The name of the current test to execute.
	 * @throws Exception If an error occurs while initializing the test
	 *         instance.
	 */
	@SuppressWarnings("unchecked")
	public AbstractTransactionalJUnit38SpringContextTests(final String name) throws Exception {

		super(name);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Setter: DataSource is provided by Dependency Injection.
	 */
	@Autowired
	public void setDataSource(final DataSource dataSource) {

		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	// ------------------------------------------------------------------------|

	/**
	 * Return the SimpleJdbcTemplate that this base class manages.
	 */
	public final SimpleJdbcTemplate getSimpleJdbcTemplate() {

		return this.jdbcTemplate;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Convenient method to delete all rows from these tables. Use with caution
	 * outside of a transaction!
	 */
	protected void deleteFromTables(final String[] names) {

		for (int i = 0; i < names.length; i++) {
			final int rowCount = this.jdbcTemplate.update("DELETE FROM " + names[i]);
			if (this.logger.isInfoEnabled()) {
				this.logger.info("Deleted " + rowCount + " rows from table " + names[i]);
			}
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * Count the rows in the given table.
	 *
	 * @param tableName table name to count rows in
	 * @return the number of rows in the table
	 */
	protected int countRowsInTable(final String tableName) {

		return this.jdbcTemplate.queryForInt("SELECT COUNT(0) FROM " + tableName);
	}

	// ------------------------------------------------------------------------|

	/**
	 * Execute the given SQL script. Will be rolled back by default, according
	 * to the fate of the current transaction.
	 *
	 * @param sqlResourcePath Spring resource path for the SQL script. Should
	 *        normally be loaded by classpath. There should be one statement per
	 *        line. Any semicolons will be removed. <b>Do not use this method to
	 *        execute DDL if you expect rollback.</b>
	 * @param continueOnError whether or not to continue without throwing an
	 *        exception in the event of an error
	 * @throws DataAccessException if there is an error executing a statement
	 *         and continueOnError was false
	 */
	protected void executeSqlScript(final String sqlResourcePath, final boolean continueOnError)
			throws DataAccessException {

		if (this.logger.isInfoEnabled()) {
			this.logger.info("Executing SQL script '" + sqlResourcePath + "'");
		}

		final long startTime = System.currentTimeMillis();
		final List<String> statements = new LinkedList<String>();
		final Resource res = getApplicationContext().getResource(sqlResourcePath);
		try {
			final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(res.getInputStream()));
			String currentStatement = lnr.readLine();
			while (currentStatement != null) {
				currentStatement = StringUtils.replace(currentStatement, ";", "");
				statements.add(currentStatement);
				currentStatement = lnr.readLine();
			}

			for (final Iterator<String> itr = statements.iterator(); itr.hasNext();) {
				final String statement = itr.next();
				try {
					final int rowsAffected = this.jdbcTemplate.update(statement);
					if (this.logger.isDebugEnabled()) {
						this.logger.debug(rowsAffected + " rows affected by SQL: " + statement);
					}
				}
				catch (final DataAccessException ex) {
					if (continueOnError) {
						if (this.logger.isWarnEnabled()) {
							this.logger.warn("SQL: " + statement + " failed", ex);
						}
					}
					else {
						throw ex;
					}
				}
			}
			final long elapsedTime = System.currentTimeMillis() - startTime;
			this.logger.info("Done executing SQL script '" + sqlResourcePath + "' in " + elapsedTime + " ms");
		}
		catch (final IOException ex) {
			throw new DataAccessResourceFailureException("Failed to open SQL script '" + sqlResourcePath + "'", ex);
		}
	}

	// ------------------------------------------------------------------------|

}
