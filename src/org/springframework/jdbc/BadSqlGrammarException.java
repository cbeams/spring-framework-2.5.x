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

package org.springframework.jdbc;

import java.sql.SQLException;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Exception thrown when SQL specified is invalid. Such exceptions always have
 * a java.sql.SQLException root cause.
 *
 * <p>It would be possible to have subclasses for no such table, no such column etc.
 * A custom SQLExceptionTranslator could create such more specific exceptions,
 * without affecting code using this class.
 *
 * @author Rod Johnson
 */
public class BadSqlGrammarException extends InvalidDataAccessResourceUsageException {
	
	/** Root cause: underlying JDBC exception. */ 
	private final SQLException ex;
	
	/** The offending SQL. */
	private final String sql;

	/**
	 * Constructor for BadSqlGrammarException.
	 * @param task name of current task (may be null)
	 * @param sql the offending SQL statement
	 * @param ex the root cause
	 */
	public BadSqlGrammarException(String task, String sql, SQLException ex) {
		super("Bad SQL grammar [" + sql + "]" + (task != null ? " in task '" + task + "'" : ""), ex);
		this.ex = ex;
		this.sql = sql;
	}
	
	/**
	 * Return the wrapped SQLException.
	 * @return the wrapped SQLException
	 */
	public SQLException getSQLException() {
		return ex;
	}
	
	/**
	 * Return the SQL that caused the problem.
	 * @return the offdending SQL
	 */
	public String getSql() {
		return sql;
	}

}
