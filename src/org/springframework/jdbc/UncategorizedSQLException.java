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

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * Exception thrown when we can't classify a SQLException into 
 * one of our generic data access exceptions.
 * @author Rod Johnson
 */
public class UncategorizedSQLException extends UncategorizedDataAccessException {
	
	/** SQL that led to the problem */
	private final String sql;

	/**
	 * Constructor for ConnectionFactoryException.
	 * @param msg message
	 * @param sql SQL we were tring to execute
	 * @param ex SQLException
	 */
	public UncategorizedSQLException(String msg, String sql, SQLException ex) {
		super(msg, ex);
		this.sql = sql;
	}
	
	/**
	 * Return the underlying SQLException.
	 */
	public SQLException getSQLException() {
		return (SQLException) getCause();
	}
	
	/**
	 * Return the SQL that led to the problem.
	 */
	public String getSql() {
		return sql;
	}

}
