
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
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * @author Rod Johnson
 */
public class SQLErrorCodeSQLExceptionTranslatorTests extends TestCase {
	
	private static SQLErrorCodes ERROR_CODES = new SQLErrorCodes();
	static {
		ERROR_CODES.setBadSqlGrammarCodes(new String[] { "1", "2" });
		ERROR_CODES.setDataIntegrityViolationCodes(new String[] { "3", "4" });
	}
	
	static SQLException BAD_SQL_EX = new SQLException() {
		public int getErrorCode() {
			return 1;
		}
	};
	
	static SQLException INTEG_VIOLATION_EX = new SQLException() {
		public int getErrorCode() {
			return 3;
		}
	};

	/**
	 * Constructor for SQLErrorCodeSQLExceptionTranslatorTests.
	 * @param arg0
	 */
	public SQLErrorCodeSQLExceptionTranslatorTests(String arg0) {
		super(arg0);
	}
	
	/**
	 * Run tests on this translator
	 * @param sext
	 */
	private void runTests(SQLExceptionTranslator sext) {
		BadSqlGrammarException ex = (BadSqlGrammarException) sext.translate("task", "SQL", BAD_SQL_EX);
		assertEquals("SQL", ex.getSql());
		assertEquals(BAD_SQL_EX, ex.getSQLException());
		DataIntegrityViolationException diex = (DataIntegrityViolationException) sext.translate("task", "SQL", INTEG_VIOLATION_EX);
		assertEquals(INTEG_VIOLATION_EX, diex.getCause());
		
		// Test fallback. We assume that no database will ever return this error code,
		// but 07xxx will be bad grammar picked up by the fallback SQLState translator
		SQLException sex = new SQLException("", "07xxx") {
			public int getErrorCode() {
				return 666666666;
			}
		};
		ex = (BadSqlGrammarException) sext.translate("task", "SQL2", sex);
		assertEquals("SQL2", ex.getSql());
		assertEquals(sex, ex.getSQLException());

	}
	
	public void testErrorCodesProvidedInConstructor() {
		runTests(new SQLErrorCodeSQLExceptionTranslator(ERROR_CODES));
	}
	
	public void testCustomTranslateMethodTranslation() {
		final String TASK = "TASK";
		final String SQL = "SQL SELECT *";
		final DataAccessException customDex = new DataAccessException("") {};
		SQLErrorCodeSQLExceptionTranslator sext = new SQLErrorCodeSQLExceptionTranslator() {

			protected DataAccessException customTranslate(String task, String sql, SQLException sqlex) {
				assertEquals(TASK, task);
				assertEquals(SQL, sql);
				return (sqlex == BAD_SQL_EX) ? customDex : null;
			}
		};
		sext.setSqlErrorCodes(ERROR_CODES);
		
		// Shouldn't custom translate this
		assertEquals(customDex, sext.translate(TASK, SQL, BAD_SQL_EX));
		DataIntegrityViolationException diex = (DataIntegrityViolationException) sext.translate(TASK, SQL, INTEG_VIOLATION_EX);
		assertEquals(INTEG_VIOLATION_EX, diex.getCause());
	}
	
	public void testCustomExceptionTranslation() {
		final String TASK = "TASK";
		final String SQL = "SQL SELECT *";
		final DataAccessException customDex = new CustomErrorCodeException("") {};
		final SQLErrorCodes customErrorCodes = new SQLErrorCodes();
		final CustomSQLErrorCodesTranslation customTranslation = new CustomSQLErrorCodesTranslation();
		
		customErrorCodes.setBadSqlGrammarCodes(new String[] { "1", "2" });
		customErrorCodes.setDataIntegrityViolationCodes(new String[] { "3", "4" });
		customTranslation.setErrorCodes(new String[] { "1" });
		customTranslation.setExceptionClass("org.springframework.jdbc.support.CustomErrorCodeException");
		List l = new LinkedList();
		l.add(customTranslation);
		customErrorCodes.setCustomTranslations(l);

		SQLErrorCodeSQLExceptionTranslator sext = new SQLErrorCodeSQLExceptionTranslator();
		sext.setSqlErrorCodes(customErrorCodes);
		
		// Should custom translate this
		assertEquals(CustomErrorCodeException.class, sext.translate(TASK, SQL, BAD_SQL_EX).getClass());
		assertEquals(BAD_SQL_EX, sext.translate(TASK, SQL, BAD_SQL_EX).getCause());
		// Shouldn't custom translate this
		DataIntegrityViolationException diex = (DataIntegrityViolationException) sext.translate(TASK, SQL, INTEG_VIOLATION_EX);
		assertEquals(INTEG_VIOLATION_EX, diex.getCause());
		// Shouldn't custom translate this - class not found
		customTranslation.setExceptionClass("org.springframework.jdbc.support.NoSuchException");
		assertEquals(BadSqlGrammarException.class, sext.translate(TASK, SQL, BAD_SQL_EX).getClass());
	}

}
