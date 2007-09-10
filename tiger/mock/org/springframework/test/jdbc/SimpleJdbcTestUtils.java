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

package org.springframework.test.jdbc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.util.StringUtils;

/**
 * <p>
 * A Java-5-based collection of JDBC related utility functions intended to
 * simplify standard database testing scenarios.
 * </p>
 *
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @since 2.5
 */
public abstract class SimpleJdbcTestUtils {

	private static final Log logger = LogFactory.getLog(SimpleJdbcTestUtils.class);


	/**
	 * Count the rows in the given table.
	 *
	 * @param simpleJdbcTemplate The SimpleJdbcTemplate with which to perform
	 * JDBC operations.
	 * @param tableName table name to count rows in
	 * @return the number of rows in the table
	 */
	public static final int countRowsInTable(final SimpleJdbcTemplate simpleJdbcTemplate, final String tableName) {

		return simpleJdbcTemplate.queryForInt("SELECT COUNT(0) FROM " + tableName);
	}

	/**
	 * Deletes all rows from the specified tables.
	 *
	 * @param simpleJdbcTemplate The SimpleJdbcTemplate with which to perform
	 * JDBC operations.
	 * @param tableNames The names of the tables from which to delete.
	 * @return The total number of rows deleted from all specified tables.
	 */
	public static final int deleteFromTables(final SimpleJdbcTemplate simpleJdbcTemplate, final String... tableNames) {

		int totalRowCount = 0;
		for (int i = 0; i < tableNames.length; i++) {
			final int rowCount = simpleJdbcTemplate.update("DELETE FROM " + tableNames[i]);
			totalRowCount += rowCount;
			if (logger.isInfoEnabled()) {
				logger.info("Deleted " + rowCount + " rows from table " + tableNames[i]);
			}
		}
		return totalRowCount;
	}

	/**
	 * <p>
	 * Execute the given SQL script.
	 * </p>
	 *
	 * @param simpleJdbcTemplate The SimpleJdbcTemplate with which to perform
	 * JDBC operations.
	 * @param resourceLoader The resource loader (e.g., an
	 * {@link ApplicationContextException}) with which to load the SQL
	 * script.
	 * @param sqlResourcePath Spring resource path for the SQL script. Should
	 * normally be loaded by classpath. There should be one statement per
	 * line. Any semicolons will be removed. <b>Do not use this method to
	 * execute DDL if you expect rollback.</b>
	 * @param continueOnError whether or not to continue without throwing an
	 * exception in the event of an error.
	 * @throws DataAccessException if there is an error executing a statement
	 * and continueOnError was <code>false</code>.
	 */
	public static final void executeSqlScript(final SimpleJdbcTemplate simpleJdbcTemplate,
			final ResourceLoader resourceLoader, final String sqlResourcePath, final boolean continueOnError)
			throws DataAccessException {

		if (logger.isInfoEnabled()) {
			logger.info("Executing SQL script '" + sqlResourcePath + "'");
		}

		final long startTime = System.currentTimeMillis();
		final List<String> statements = new LinkedList<String>();
		final Resource res = resourceLoader.getResource(sqlResourcePath);
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
					final int rowsAffected = simpleJdbcTemplate.update(statement);
					if (logger.isDebugEnabled()) {
						logger.debug(rowsAffected + " rows affected by SQL: " + statement);
					}
				}
				catch (final DataAccessException ex) {
					if (continueOnError) {
						if (logger.isWarnEnabled()) {
							logger.warn("SQL: " + statement + " failed", ex);
						}
					}
					else {
						throw ex;
					}
				}
			}
			final long elapsedTime = System.currentTimeMillis() - startTime;
			if (logger.isInfoEnabled()) {
				logger.info("Done executing SQL script '" + sqlResourcePath + "' in " + elapsedTime + " ms.");
			}
		}
		catch (final IOException ex) {
			throw new DataAccessResourceFailureException("Failed to open SQL script '" + sqlResourcePath + "'.", ex);
		}
	}

}
