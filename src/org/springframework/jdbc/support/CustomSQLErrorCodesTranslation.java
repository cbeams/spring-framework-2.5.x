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

/**
 * JavaBean for holding Custom JDBC Error Codes translation for a particular
 * database. The exceptionClass property defines which exception will be
 * thrown for the list of error codes specified in the errorCodes property.
 *
 * <p>Normally loaded through a BeanFactory implementation.
 * Used by the SQLErrorCodeSQLExceptionTranslator.
 *
 * @author Thomas Risberg
 * @since 30.06.2004
 * @see SQLErrorCodeSQLExceptionTranslator
 */
public class CustomSQLErrorCodesTranslation {

	private String[] errorCodes = new String[0];

	private String exceptionClass = null;
	
	/**
	 * Set the SQL error codes to match.
	 */
	public void setErrorCodes(String[] errorCodes) {
		this.errorCodes = errorCodes;
	}

	/**
	 * Return the SQL error codes to match.
	 */
	public String[] getErrorCodes() {
		return errorCodes;
	}

	/**
	 * Set the exception class for the specified error codes.
	 */
	public void setExceptionClass(String exceptionClass) {
		this.exceptionClass = exceptionClass;
	}

	/**
	 * Return the exception class for the specified error codes.
	 */
	public String getExceptionClass() {
		return exceptionClass;
	}

}
