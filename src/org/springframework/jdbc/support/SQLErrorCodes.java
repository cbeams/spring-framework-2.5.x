/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.support;

/**
 * JavaBean for holding JDBC Error Codes for a particular database.
 * Normally loaded through a BeanFactory
 * implementation. Used by the SQLErrorCodeSQLExceptionTranslator.
 * @author Thomas Risberg
 * @version $Id: SQLErrorCodes.java,v 1.2 2003-12-10 02:15:17 trisberg Exp $
 */
public class SQLErrorCodes {

	private String databaseProductName = null;

	private String[] badSqlGrammarCodes = new String[0];

	private String[] dataIntegrityViolationCodes = new String[0];

	/**
	 * Sets the badSqlGrammarCodes.
	 */
	public void setBadSqlGrammarCodes(String[] badSqlGrammarCodes) {
		this.badSqlGrammarCodes = badSqlGrammarCodes;
	}

	/**
	 * Returns the badSqlGrammarCodes.
	 */
	public String[] getBadSqlGrammarCodes() {
		return badSqlGrammarCodes;
	}

	/**
	 * Sets the dataIntegrityViolationCodes.
	 */
	public void setDataIntegrityViolationCodes(String[] dataIntegrityViolationCodes) {
		this.dataIntegrityViolationCodes = dataIntegrityViolationCodes;
	}

	/**
	 * Returns the dataIntegrityViolationCodes.
	 */
	public String[] getDataIntegrityViolationCodes() {
		return dataIntegrityViolationCodes;
	}

	/**
	 * @return Returns the databaseProductName.
	 */
	public String getDatabaseProductName() {
		return databaseProductName;
	}

	/**
	 * @param databaseProductName The databaseProductName to set.
	 */
	public void setDatabaseProductName(String databaseProductName) {
		this.databaseProductName = databaseProductName;
	}

}
