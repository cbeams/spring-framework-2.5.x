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

package org.springframework.orm.ibatis;

import java.sql.SQLException;

import com.ibatis.sqlmap.client.SqlMapExecutor;

/**
 * Callback interface for data access code that works on an iBATIS Database Layer
 * SqlMapSession. To be used with SqlMapClientTemplate's execute method,
 * assumably often as anonymous classes within a method implementation.
 *
 * <p>NOTE: The SqlMapClient/SqlMapSession API is the API of iBATIS SQL Maps 2.
 * With SQL Maps 1.x, the SqlMap/MappedStatement API has to be used.
 *
 * @author Juergen Hoeller
 * @since 24.02.2004
 */
public interface SqlMapClientCallback {

	/**
	 * Gets called by SqlMapClientTemplate.execute with an active SqlMapSession.
	 * Does not need to care about activating or closing the session,
	 * or handling transactions.
	 *
	 * <p>If called without a thread-bound JDBC transaction (initiated by
	 * DataSourceTransactionManager), the code will simply get executed on the
	 * underlying JDBC connection with its transactional semantics. If using
	 * a JTA-aware DataSource, the JDBC connection and thus the callback code
	 * will be transactional if a JTA transaction is active.
	 *
	 * <p>Allows for returning a result object created within the callback, i.e.
	 * a domain object or a collection of domain objects. Note that there's
	 * special support for single step actions: see SqlMapClientTemplate.
	 * A thrown RuntimeException is treated as application exception, it gets
	 * propagated to the caller of the template.
	 *
	 * @param executor an active iBATIS SqlMapSession, passed-in as
	 * SqlMapExecutor interface here to avoid manual lifecycle handling
	 * @return a result object, or null if none
	 * @throws SQLException if throw my the iBATIS SQL Maps API
	 * @see SqlMapClientTemplate#execute
	 * @see SqlMapClientTemplate#queryForList
	 * @see SqlMapClientTemplate#queryForMap
	 * @see SqlMapClientTemplate#queryForObject
	 * @see SqlMapClientTemplate#insert
	 * @see SqlMapClientTemplate#update
	 * @see SqlMapClientTemplate#delete
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
	 */
	Object doInSqlMapClient(SqlMapExecutor executor) throws SQLException;

}
