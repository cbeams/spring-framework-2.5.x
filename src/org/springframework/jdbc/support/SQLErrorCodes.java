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
 * @version $Id: SQLErrorCodes.java,v 1.3 2004-02-14 19:17:39 trisberg Exp $
 */
public class SQLErrorCodes {

	private String databaseProductName = null;

	private String[] badSqlGrammarCodes = new String[0];

	private String[] dataIntegrityViolationCodes = new String[0];
	
	private String[] dataRetrievalFailureCodes = new String[0];
	
	private String[] optimisticLockingFailureCodes = new String[0];
	
	private String[] dataAccessResourceFailureCodes = new String[0];

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
	 * @return Returns the dataRetrievalFailureCodes.
	 */
	public String[] getDataRetrievalFailureCodes() {
		return dataRetrievalFailureCodes;
	}
	/**
	 * @param dataRetrievalFailureCodes The dataRetrievalFailureCodes to set.
	 */
	public void setDataRetrievalFailureCodes(String[] dataRetrievalFailureCodes) {
		this.dataRetrievalFailureCodes = dataRetrievalFailureCodes;
	}

	/**
	 * @return Returns the dataAccessResourceFailureCodes.
	 */
	public String[] getDataAccessResourceFailureCodes() {
		return dataAccessResourceFailureCodes;
	}

	/**
	 * @param dataAccessResourceFailureCodes The dataAccessResourceFailureCodes to set.
	 */
	public void setDataAccessResourceFailureCodes(
			String[] dataAccessResourceFailureCodes) {
		this.dataAccessResourceFailureCodes = dataAccessResourceFailureCodes;
	}

	/**
	 * @return Returns the optimisticLockingFailureCodes.
	 */
	public String[] getOptimisticLockingFailureCodes() {
		return optimisticLockingFailureCodes;
	}
	
	/**
	 * @param optimisticLockingFailureCodes The optimisticLockingFailureCodes to set.
	 */
	public void setOptimisticLockingFailureCodes(
			String[] optimisticLockingFailureCodes) {
		this.optimisticLockingFailureCodes = optimisticLockingFailureCodes;
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
