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

/**
 * JavaBean for holding JDBC error codes for a particular database.
 * Normally loaded through a BeanFactory implementation.
 * Used by the SQLErrorCodeSQLExceptionTranslator.
 * @author Thomas Risberg
 * @see SQLErrorCodeSQLExceptionTranslator
 * @see SQLErrorCodesFactory
 */
public class SQLErrorCodes {

	private String databaseProductName = null;

	private boolean useSqlStateForTranslation = false;

	private String[] badSqlGrammarCodes = new String[0];

	private String[] dataIntegrityViolationCodes = new String[0];
	
	private String[] dataRetrievalFailureCodes = new String[0];
	
	private String[] optimisticLockingFailureCodes = new String[0];
	
	private String[] cannotAcquireLockCodes = new String[0];
	
	private String[] dataAccessResourceFailureCodes = new String[0];
	
	private List customTranslations = new LinkedList();


	/**
	 * Set this property if the database name contains spaces, in which case
	 * we can not use the bean name for lookup.
	 */
	public void setDatabaseProductName(String databaseProductName) {
		this.databaseProductName = databaseProductName;
	}

	public String getDatabaseProductName() {
		return databaseProductName;
	}

	/**
	 * Set this propert to true for databases that do not provide an error code
	 * but that do provide SQL State (this includes PostgreSQL).
	 */
	public void setUseSqlStateForTranslation(boolean useStateCodeForTranslation) {
		this.useSqlStateForTranslation = useStateCodeForTranslation;
	}

	public boolean isUseSqlStateForTranslation() {
		return useSqlStateForTranslation;
	}

	public void setBadSqlGrammarCodes(String[] badSqlGrammarCodes) {
		this.badSqlGrammarCodes = badSqlGrammarCodes;
	}

	public String[] getBadSqlGrammarCodes() {
		return badSqlGrammarCodes;
	}

	public void setDataIntegrityViolationCodes(String[] dataIntegrityViolationCodes) {
		this.dataIntegrityViolationCodes = dataIntegrityViolationCodes;
	}

	public String[] getDataIntegrityViolationCodes() {
		return dataIntegrityViolationCodes;
	}

	public void setDataRetrievalFailureCodes(String[] dataRetrievalFailureCodes) {
		this.dataRetrievalFailureCodes = dataRetrievalFailureCodes;
	}

	public String[] getDataRetrievalFailureCodes() {
		return dataRetrievalFailureCodes;
	}

	public void setOptimisticLockingFailureCodes(String[] optimisticLockingFailureCodes) {
		this.optimisticLockingFailureCodes = optimisticLockingFailureCodes;
	}

	public String[] getOptimisticLockingFailureCodes() {
		return optimisticLockingFailureCodes;
	}

	public void setCannotAcquireLockCodes(String[] cannotAcquireLockCodes) {
		this.cannotAcquireLockCodes = cannotAcquireLockCodes;
	}

	public String[] getCannotAcquireLockCodes() {
		return cannotAcquireLockCodes;
	}

	public void setDataAccessResourceFailureCodes(String[] dataAccessResourceFailureCodes) {
		this.dataAccessResourceFailureCodes = dataAccessResourceFailureCodes;
	}

	public String[] getDataAccessResourceFailureCodes() {
		return dataAccessResourceFailureCodes;
	}

	public void setCustomTranslations(List customExceptions) {
		this.customTranslations = customExceptions;
	}

	public List getCustomTranslations() {
		return customTranslations;
	}

}
