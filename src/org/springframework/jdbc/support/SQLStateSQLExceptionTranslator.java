/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
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
 * code in the SQL exception. Can't diagnose all problems, but is
 * portable between databases.
 * @author Rod Johnson
 * @version $Id: SQLStateSQLExceptionTranslator.java,v 1.2 2004-02-25 19:57:14 trisberg Exp $
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
	
	/**
	 * @see org.springframework.jdbc.support.SQLExceptionTranslator#translate(java.lang.String,java.lang.String,java.sql.SQLException)
	 */
	public DataAccessException translate(String task, String sql, SQLException sqlex) {
		logger.warn("Translating SQLException with SQLState '" + sqlex.getSQLState() + "' and errorCode '" + sqlex.getErrorCode() +
						"' and message [" + sqlex.getMessage() + "]; SQL was [" + sql + "] for task [" + task + "]");

		String sqlState = sqlex.getSQLState();
		// Some JDBC drivers nest the actual exception from a batched update - need to get the nested one
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
				return new DataIntegrityViolationException("(" + task + "): data integrity violated by SQL '" + sql + "'", sqlex);
			}
		}
		
		// We couldn't identify it more precisely
		return new UncategorizedSQLException("(" + task + "): encountered SQLException [" + sqlex.getMessage() + "]", sql, sqlex);
	}

}
