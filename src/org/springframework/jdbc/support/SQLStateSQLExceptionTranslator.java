/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.jdbc.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.util.Assert;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link SQLExceptionTranslator} implementation that analyzes the SQL state
 * in the {@link SQLException}.
 * 
 * <p>Not able to diagnose all problems, but is portable between databases and
 * does not require special initialization (no database vendor detection, etc.).
 * For more precise translation, consider {@link SQLErrorCodeSQLExceptionTranslator}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.sql.SQLException#getSQLState()
 * @see SQLErrorCodeSQLExceptionTranslator
 */
public class SQLStateSQLExceptionTranslator implements SQLExceptionTranslator {

	/**
	 * Set of well-known String 2-digit codes that indicate bad SQL
	 */
	private static final Set BAD_SQL_CODES = new HashSet(6);

	/**
	 * Set of well-known String 2-digit codes that indicate RDBMS integrity violation
	 */
	private static final Set INTEGRITY_VIOLATION_CODES = new HashSet(4);

	/**
	 * Set of String 2-digit codes that indicate communication errors
	 */
	private static final Set RESOURCE_FAILURE_CODES = new HashSet(3);

	/**
	 * Set of String 2-digit codes that indicate concurrency errors
	 */
	private static final Set CONCURRENCY_CODES = new HashSet(1);


	// Populate reference data.
	static {
		BAD_SQL_CODES.add("07");
		BAD_SQL_CODES.add("37");
		BAD_SQL_CODES.add("42");
		BAD_SQL_CODES.add("2A");
		BAD_SQL_CODES.add("65");	// Oracle throws this on unknown identifier
		BAD_SQL_CODES.add("S0");	// MySQL uses this - from ODBC error codes?

		INTEGRITY_VIOLATION_CODES.add("22");	// Integrity constraint violation
		INTEGRITY_VIOLATION_CODES.add("23");	// Integrity constraint violation
		INTEGRITY_VIOLATION_CODES.add("27");	// Triggered data change violation
		INTEGRITY_VIOLATION_CODES.add("44");	// With check violation

		CONCURRENCY_CODES.add("40");	// Transaction rollback

		RESOURCE_FAILURE_CODES.add("08");	// Connection exception
		RESOURCE_FAILURE_CODES.add("53");	// PostgreSQL uses this - insufficient resources (e.g. disk full)
		RESOURCE_FAILURE_CODES.add("54");	// PostgreSQL uses this - program limit exceeded (e.g. statement too complex)
	}


	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());


	public DataAccessException translate(String task, String sql, SQLException ex) {
		Assert.notNull(ex, "Cannot translate a null SQLException.");
		if (task == null) {
			task = "";
		}
		if (sql == null) {
			sql = "";
		}
		String sqlState = getSqlState(ex);
		if (sqlState != null && sqlState.length() >= 2) {
			String classCode = sqlState.substring(0, 2);
			if (BAD_SQL_CODES.contains(classCode)) {
				return new BadSqlGrammarException(task, sql, ex);
			}
			else if (INTEGRITY_VIOLATION_CODES.contains(classCode)) {
				return new DataIntegrityViolationException(buildMessage(task, sql, ex), ex);
			}
			else if (RESOURCE_FAILURE_CODES.contains(classCode)) {
				return new DataAccessResourceFailureException(buildMessage(task, sql, ex), ex);
			}
			else if (CONCURRENCY_CODES.contains(classCode)) {
				return new ConcurrencyFailureException(buildMessage(task, sql, ex), ex);
			}
		}
		// We couldn't identify it more precisely.
		return new UncategorizedSQLException(task, sql, ex);
	}


	/**
	 * Build a message <code>String</code> for the given {@link SQLException}.
	 * <p>Called when creating an instance of a generic
	 * {@link DataAccessException} class.
	 * @param task readable text describing the task being attempted
	 * @param sql  the SQL statement that caused the problem. May be <code>null</code>.
	 * @param ex   the offending <code>SQLException</code>
	 * @return the message <code>String</code> to use
	 */
	protected String buildMessage(String task, String sql, SQLException ex) {
		return task + "; SQL [" + sql + "]; " + ex.getMessage();
	}


	/**
	 * Gets the SQL state code from the supplied {@link SQLException exception}.
	 * <p>Some JDBC drivers nest the actual exception from a batched update, so we
	 * might need to dig down into the nested exception.
	 * @param ex the exception from which the {@link SQLException#getSQLState() SQL state} is to be extracted
	 * @return the SQL state code
	 */
	private String getSqlState(SQLException ex) {
		String sqlState = ex.getSQLState();
		if (sqlState == null) {
			SQLException nestedEx = ex.getNextException();
			if (nestedEx != null) {
				sqlState = nestedEx.getSQLState();
			}
		}
		return sqlState;
	}

}
