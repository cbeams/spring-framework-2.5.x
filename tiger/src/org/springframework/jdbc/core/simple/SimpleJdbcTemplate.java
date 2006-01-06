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

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Java 5 and above wrapper for the classic Spring JdbcTemplate,
 * taking advantage of varargs and autoboxing, and exposing only the
 * most commonly required operations to simplify JdbcTemplate usage.
 *
 * <p>Use the <code>getJdbcOperations()</code> method if you need to invoke
 * less commonly used methods. This includes any methods specifying SQL types,
 * methods using less commonly used callbacks such as RowCallbackHandler,
 * updates with PreparedStatementSetters, rather than arg lists, stored
 * procedures and batch operations.
 * 
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see ParameterizedRowMapper
 * @see SimpleJdbcDaoSupport
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public class SimpleJdbcTemplate implements SimpleJdbcOperations {
	
	/** The JdbcTemplate we are wrapping */
	private final JdbcOperations classicJdbcTemplate;


	/**
	 * Create a new SimpleJdbcTemplate for the given DataSource.
	 * <p>Creates a classic Spring JdbcTemplate and wraps it.
	 * @param dataSource the JDBC DataSource to access
	 */
	public SimpleJdbcTemplate(DataSource dataSource) {
		this.classicJdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * Create a new SimpleJdbcTemplate for the given classic Spring JdbcTemplate.
	 * @param classicJdbcTemplate the classic Spring JdbcTemplate to wrap
	 */
	public SimpleJdbcTemplate(JdbcOperations classicJdbcTemplate) {
		this.classicJdbcTemplate = classicJdbcTemplate;
	}

	/**
	 * Expose the classic Spring JdbcTemplate to allow invocation of
	 * less commonly used methods.
	 */
	public JdbcOperations getJdbcOperations() {
		return this.classicJdbcTemplate;
	}
	
	
	public int queryForInt(String sql, Object... args) throws DataAccessException {
		return args == null || args.length == 0 ?
					getJdbcOperations().queryForInt(sql) :
					getJdbcOperations().queryForInt(sql, args);
	}
	
	public long queryForLong(String sql, Object... args) throws DataAccessException {
		return (args == null || args.length == 0) ?
					getJdbcOperations().queryForLong(sql) :
					getJdbcOperations().queryForLong(sql, args);
	}

	@SuppressWarnings("unchecked")
	public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) throws DataAccessException {
		return (T)((args == null || args.length == 0) ?
				getJdbcOperations().queryForObject(sql, requiredType) :
				getJdbcOperations().queryForObject(sql, args, requiredType));
	}

	@SuppressWarnings("unchecked")
	public <T> T queryForObject(String sql, ParameterizedRowMapper<T> rm, Object... args) throws DataAccessException {
		return (T)((args == null || args.length == 0)?
				getJdbcOperations().queryForObject(sql, rm):
				getJdbcOperations().queryForObject(sql, args, rm));
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> query(String sql, ParameterizedRowMapper<T> rm, Object... args) throws DataAccessException {
		return (List<T>)((args == null || args.length == 0) ?
				getJdbcOperations().query(sql, rm) :
				getJdbcOperations().query(sql, args, rm));
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> queryForMap(String sql, Object... args) throws DataAccessException {
		return (args == null || args.length == 0) ?
				getJdbcOperations().queryForMap(sql) :
				getJdbcOperations().queryForMap(sql, args);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> queryForList(String sql, Object... args) throws DataAccessException {
		return (args == null || args.length == 0) ?
				getJdbcOperations().queryForList(sql) :
				getJdbcOperations().queryForList(sql, args);
	}

	public int update(String sql, Object ... args) throws DataAccessException {
		return (args == null || args.length == 0) ?
				getJdbcOperations().update(sql) :
				getJdbcOperations().update(sql, args);
	}

}
