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

import java.util.LinkedList;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 * JavaBean for holding JDBC error codes for a particular database.
 * Normally loaded through a BeanFactory implementation.
 * Used by the SQLErrorCodeSQLExceptionTranslator.
 * @author Thomas Risberg
 * @see SQLErrorCodeSQLExceptionTranslator
 * @see SQLErrorCodesFactory
 */
public class SQLErrorCodes {

	private String[] databaseProductNames;

	private boolean useSqlStateForTranslation = false;

	private String[] badSqlGrammarCodes = new String[0];

	private String[] dataIntegrityViolationCodes = new String[0];
	
	private String[] dataRetrievalFailureCodes = new String[0];
	
	private String[] optimisticLockingFailureCodes = new String[0];
	
	private String[] cannotAcquireLockCodes = new String[0];
	
	private String[] dataAccessResourceFailureCodes = new String[0];
	
	private List customTranslations = new LinkedList();


	/**
	 * Set this property if the database name contains spaces,
	 * in which case we can not use the bean name for lookup.
	 */
	public void setDatabaseProductName(String databaseProductName) {
		this.databaseProductNames = new String[] {databaseProductName};
	}

	public String getDatabaseProductName() {
		return (this.databaseProductNames != null && this.databaseProductNames.length > 0 ?
				this.databaseProductNames[0] : null);
	}

	/**
	 * Set this property to specify multiple database names that contains spaces,
	 * in which case we can not use bean names for lookup.
	 */
	public void setDatabaseProductNames(String[] databaseProductNames) {
		this.databaseProductNames = databaseProductNames;
	}

	public String[] getDatabaseProductNames() {
		return databaseProductNames;
	}

	/**
	 * Set this property to true for databases that do not provide an error code
	 * but that do provide SQL State (this includes PostgreSQL).
	 */
	public void setUseSqlStateForTranslation(boolean useStateCodeForTranslation) {
		this.useSqlStateForTranslation = useStateCodeForTranslation;
	}

	public boolean isUseSqlStateForTranslation() {
		return useSqlStateForTranslation;
	}

	public void setBadSqlGrammarCodes(String[] badSqlGrammarCodes) {
		this.badSqlGrammarCodes = StringUtils.sortStringArray(badSqlGrammarCodes);
	}

	public String[] getBadSqlGrammarCodes() {
		return badSqlGrammarCodes;
	}

	public void setDataIntegrityViolationCodes(String[] dataIntegrityViolationCodes) {
		this.dataIntegrityViolationCodes = StringUtils.sortStringArray(dataIntegrityViolationCodes);
	}

	public String[] getDataIntegrityViolationCodes() {
		return dataIntegrityViolationCodes;
	}

	public void setDataRetrievalFailureCodes(String[] dataRetrievalFailureCodes) {
		this.dataRetrievalFailureCodes = StringUtils.sortStringArray(dataRetrievalFailureCodes);
	}

	public String[] getDataRetrievalFailureCodes() {
		return dataRetrievalFailureCodes;
	}

	public void setOptimisticLockingFailureCodes(String[] optimisticLockingFailureCodes) {
		this.optimisticLockingFailureCodes = StringUtils.sortStringArray(optimisticLockingFailureCodes);
	}

	public String[] getOptimisticLockingFailureCodes() {
		return optimisticLockingFailureCodes;
	}

	public void setCannotAcquireLockCodes(String[] cannotAcquireLockCodes) {
		this.cannotAcquireLockCodes = StringUtils.sortStringArray(cannotAcquireLockCodes);
	}

	public String[] getCannotAcquireLockCodes() {
		return cannotAcquireLockCodes;
	}

	public void setDataAccessResourceFailureCodes(String[] dataAccessResourceFailureCodes) {
		this.dataAccessResourceFailureCodes = StringUtils.sortStringArray(dataAccessResourceFailureCodes);
	}

	public String[] getDataAccessResourceFailureCodes() {
		return dataAccessResourceFailureCodes;
	}

	/**
	 * @param customTranslations List of CustomSQLErrorCodesTranslation objects
	 * @see CustomSQLErrorCodesTranslation
	 */
	public void setCustomTranslations(List customTranslations) {
		this.customTranslations = customTranslations;
	}

	/**
	 * @return List of CustomSQLErrorCodesTranslation objects
	 * @see CustomSQLErrorCodesTranslation
	 */
	public List getCustomTranslations() {
		return customTranslations;
	}

}
