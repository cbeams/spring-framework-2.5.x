/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

/**
 * JavaBean for holding JDBC Error Codes - loaded through BeanFactory
 * implementation. Used by the SQLExceptionTranslator.
 * @author Thomas Risberg
 */
public class SQLErrorCodes {

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

}
