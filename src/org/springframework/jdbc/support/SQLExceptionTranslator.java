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

import org.springframework.dao.DataAccessException;

/**
 * Interface to be implemented by classes that can translate
 * between SQLExceptions and our data access strategy-agnostic
 * org.springframework.dao.DataAccessException.
 *
 * <p>Implementations can be generic (for example, using SQLState
 * codes for JDBC) or proprietary (for example, using Oracle
 * error codes) for greater precision.
 *
 * @author Rod Johnson
 * @see org.springframework.dao.DataAccessException
 */
public interface SQLExceptionTranslator {

	/** 
	 * Translate the given SQL exception into a generic
	 * data access exception.
	 * @param task readable text describing the task being attempted
	 * @param sql SQL query or update that caused the problem.
	 * May be null.
	 * @param sqlex SQLException encountered by JDBC implementation
	 */
	DataAccessException translate(String task, String sql, SQLException sqlex);

}
