/*
 * Copyright 2002-2004 the original author or authors.
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

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;

/**
 * Implementation of SQLExceptionTranslator that uses the SQLState
 * code in the SQLException. Can't diagnose all problems, but is
 * portable between databases.
 * @author Rod Johnson
 * @see java.sql.SQLException#getSQLState
 */
public class SQLStateSQLExceptionTranslator implements SQLExceptionTranslator {
	
	protected final Log logger = LogFactory.getLog(getClass());

	/** Set of String 2-digit codes that indicate bad SQL */
	private static Set BAD_SQL_CODES = new HashSet();
	
	/** Set of String 2-digit codes that indicate RDBMS integrity violation */
	private static Set INTEGRITY_VIOLATION_CODES = new HashSet();
	
	// Populate reference data
	static {
		BAD_SQL_CODES.add("07");
		BAD_SQL_CODES.add("42");
		BAD_SQL_CODES.add("65");	// Oracle throws on unknown identifier
		BAD_SQL_CODES.add("S0");  // MySQL uses this - from ODBC error codes?
		
		INTEGRITY_VIOLATION_CODES.add("22");	// Integrity constraint violation
		INTEGRITY_VIOLATION_CODES.add("23");	// Integrity constraint violation
		INTEGRITY_VIOLATION_CODES.add("27");	// Triggered data change violation
		INTEGRITY_VIOLATION_CODES.add("44");	// With check violation
	}
	
	public DataAccessException translate(String task, String sql, SQLException sqlex) {
		if (task == null) {
			task = "";
		}
		if (sql == null) {
			sql = "";
		}

		if (logger.isInfoEnabled()) {
			logger.info("Translating SQLException with SQLState '" + sqlex.getSQLState() +
			            "' and errorCode '" + sqlex.getErrorCode() + "' and message [" +
			            sqlex.getMessage() + "]; SQL was [" + sql + "] for task [" + task + "]");
		}

		String sqlState = sqlex.getSQLState();
		// some JDBC drivers nest the actual exception from a batched update - need to get the nested one
		if (sqlState == null) {
			SQLException nestedEx = sqlex.getNextException();
			if (nestedEx != null)
				sqlState = nestedEx.getSQLState();
		}
		if (sqlState != null && sqlState.length() >= 2) {
			String classCode = sqlState.substring(0, 2);
			if (BAD_SQL_CODES.contains(classCode)) {
				return new BadSqlGrammarException(task, sql, sqlex);
			}
			else if (INTEGRITY_VIOLATION_CODES.contains(classCode)) {
				return new DataIntegrityViolationException("(" + task + "): data integrity violated by SQL '" +
				                                           sql + "'", sqlex);
			}
		}
		
		// we couldn't identify it more precisely
		return new UncategorizedSQLException("(" + task + "): encountered SQLException [" +
		                                     sqlex.getMessage() + "]", sql, sqlex);
	}

}
