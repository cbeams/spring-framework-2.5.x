/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Implementation of SQLExceptionTranslator that uses specific vendor codes
 * defined in a configuration file named "sql-error-codes.xml". More precise
 * than SQLState implementation, but vendor specific. The JdbcTemplate class
 * enables error handling to be parameterized without making application's
 * dependent on a particular RDBMS.
 * @author Rod Johnson
 * @author Thomas Risberg
 */
public class SQLErrorCodeSQLExceptionTranslator implements SQLExceptionTranslator {

	protected final Log logger = LogFactory.getLog(getClass());

	/** Error codes available to subclasses */
	protected SQLErrorCodes sqlErrorCodes;

	public SQLErrorCodeSQLExceptionTranslator() {
		this.sqlErrorCodes = new SQLErrorCodes();		
	}

	public SQLErrorCodeSQLExceptionTranslator(SQLErrorCodes sec) {
		this.sqlErrorCodes = sec;		
	}

	/**
	 * set the error codes to be used for translation
	 */
	public void setSqlErrorCodes(SQLErrorCodes sec) {
		this.sqlErrorCodes = sec;		
	}

	/**
	 * @see SQLExceptionTranslator#translate(String, String, SQLException)
	 */
	public DataAccessException translate(String task, String sql, SQLException sqlex) {

		String errorCode = Integer.toString(sqlex.getErrorCode());
		if (java.util.Arrays.binarySearch(sqlErrorCodes.getBadSqlGrammarCodes(), errorCode) >= 0) {
			logTranslation(task, sql, sqlex);
			return new BadSqlGrammarException(task, sql, sqlex);
		}
		if (java.util.Arrays.binarySearch(sqlErrorCodes.getDataIntegrityViolationCodes() , errorCode) >= 0) {
			logTranslation(task, sql, sqlex);
			return new DataIntegrityViolationException(task + ": " + sqlex.getMessage(), sqlex);
		}

		// We couldn't identify it more precisely - let's hand it over to the SQLState translator.
		logger.warn("Unable to translate SQLException with errorCode=" + sqlex.getErrorCode() + 
						", will now try the SQLState translator");
		SQLStateSQLExceptionTranslator lastResort = new SQLStateSQLExceptionTranslator();
		return lastResort.translate(task, sql, sqlex);
	}

	private void logTranslation(String task, String sql, SQLException sqlex) {
		logger.warn("Translating SQLException with SQLState='" + sqlex.getSQLState() + "' and errorCode=" + sqlex.getErrorCode() + 
						" and message=" + sqlex.getMessage() + "; sql was '" + sql + "'");
	}
}