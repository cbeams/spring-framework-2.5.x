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

package org.springframework.jdbc.core;

import java.sql.CallableStatement;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;

/**
 * Generic callback interface for code that operates on a CallableStatement.
 * Allows to execute any number of operations on a single CallableStatement,
 * for example a single execute call or repeated execute calls with varying
 * parameters.
 *
 * <p>Used internally by JdbcTemplate, but also useful for application code.
 * Note that the passed-in CallableStatement can have been created by the
 * framework or by a custom CallableStatementCreator. However, the latter is
 * hardly ever necessary, as most custom callback actions will perform updates
 * in which case a standard CallableStatement is fine. Custom actions will
 * always set parameter values themselves, so that CallableStatementCreator
 * capability is not needed either.
 *
 * @author Juergen Hoeller
 * @since 16.03.2004
 * @see JdbcTemplate#execute(String, CallableStatementCallback)
 * @see JdbcTemplate#execute(CallableStatementCreator, CallableStatementCallback)
 */
public interface CallableStatementCallback {

	/**
	 * Gets called by JdbcTemplate.execute with an active JDBC CallableStatement.
	 * Does not need to care about activating or closing the Connection,
	 * or handling transactions.
	 *
	 * <p>If called without a thread-bound JDBC transaction (initiated by
	 * DataSourceTransactionManager), the code will simply get executed on the
	 * JDBC connection with its transactional semantics. If JdbcTemplate is
	 * configured to use a JTA-aware DataSource, the JDBC connection and thus
	 * the callback code will be transactional if a JTA transaction is active.
	 *
	 * <p>Allows for returning a result object created within the callback, i.e.
	 * a domain object or a collection of domain objects. A thrown RuntimeException
	 * is treated as application exception, it gets propagated to the caller of
	 * the template.
	 *
	 * @param cs active JDBC CallableStatement
	 * @return a result object, or null if none
	 * @throws SQLException if thrown by a JDBC method, to be auto-converted
	 * into a DataAccessException by a SQLExceptionTranslator
	 * @throws DataAccessException in case of custom exceptions
	 */
	Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException;

}
