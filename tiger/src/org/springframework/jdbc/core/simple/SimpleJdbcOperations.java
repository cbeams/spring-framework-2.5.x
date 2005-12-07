/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.jdbc.core.simple;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * JDBC operations nterface usable on Java 5 and above, exposing a
 * set of common JDBC operations, whose interface is simplified
 * through the use of varargs and autoboxing.
 * 
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 2.0
 * @see SimpleJdbcTemplate
 * @see org.springframework.jdbc.core.JdbcOperations
 */
public interface SimpleJdbcOperations {

	/**
	 * Expose the classic Spring JdbcTemplate to allow invocation of less
	 * commonly used methods.
	 */
	JdbcOperations getJdbcOperations();


	/**
	 * Query for an <code>int</code> passing in a SQL query and a variable
	 * number of arguments.
	 */
	int queryForInt(String sql, Object... args) throws DataAccessException;

	/**
	 * Query for an <code>long</code> passing in a SQL query and a variable
	 * number of arguments.
	 */
	long queryForLong(String sql, Object... args) throws DataAccessException;

	/**
	 * Query for an object of type <code>T</code> identified by the supplied @{@link Class}.
	 * @param sql the SQL query to run.
	 * @param requiredType the required type of the return value.
	 * @param args the args for the query.
	 * @see JdbcOperations#queryForObject(String, Class)
	 * @see JdbcOperations#queryForObject(String, Object[], Class)
	 */
	<T> T queryForObject(String sql, Class<T> requiredType, Object... args)
			throws DataAccessException;

	/**
	 * Query for an object of type <code>T</code> using the supplied
	 * {@link ParameterizedRowMapper} to the query results to the object.
	 * @param sql the SQL query to run.
	 * @param rm the @{@link ParameterizedRowMapper} to use for result mapping
	 * @param args the args for the query.
	 * @see JdbcOperations#queryForObject(String, org.springframework.jdbc.core.RowMapper)
	 * @see JdbcOperations#queryForObject(String, Object[], org.springframework.jdbc.core.RowMapper)
	 */
	<T> T queryForObject(String sql, ParameterizedRowMapper<T> rm, Object... args)
			throws DataAccessException;

	/**
	 * Query for a {@link List} of <code>Objects</code> of type <code>T</code> using
	 * the supplied {@link ParameterizedRowMapper} to the query results to the object.
	 * @param sql the SQL query to run.
	 * @param rm the @{@link ParameterizedRowMapper} to use for result mapping
	 * @param args the args for the query.
	 * @see JdbcOperations#queryForObject(String, org.springframework.jdbc.core.RowMapper)
	 * @see JdbcOperations#queryForObject(String, Object[], org.springframework.jdbc.core.RowMapper)
	 */
	<T> List<T> query(String sql, ParameterizedRowMapper<T> rm, Object... args)
			throws DataAccessException;

	/**
	 * Execute the supplied query with the (optional) supplied arguments.
	 * <p>The query is expected to be a single row query; the result row will be
	 * mapped to a Map (one entry for each column, using the column name as the key).
	 * @see JdbcOperations#queryForMap(String)
	 * @see JdbcOperations#queryForMap(String, Object[])
	 */
	Map<String, Object> queryForMap(String sql, Object... args)
			throws DataAccessException;

	/**
	 * Execute the supplied query with the (optional) supplied arguments.
	 * <p>Each element in the returned {@link List} is constructed as a {@link Map}
	 * as described in {@link #queryForMap}
	 * @see JdbcOperations#queryForList(String)
	 * @see JdbcOperations#queryForList(String, Object[]) 
	 */
	List<Map<String, Object>> queryForList(String sql, Object... args)
			throws DataAccessException;

	/**
	 * Executes the supplied SQL statement with (optional) supplied arguments.
	 * @return the numbers of rows affected by the update.
	 * @see JdbcOperations#update(String)
	 * @see JdbcOperations#update(String, Object[])
	 */
	int update(String sql, Object... args) throws DataAccessException;

}
