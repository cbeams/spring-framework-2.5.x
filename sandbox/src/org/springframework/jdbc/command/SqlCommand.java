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

package org.springframework.jdbc.command;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcNamedParameterOperations;
import org.springframework.jdbc.core.JdbcNamedParameterTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.ParsedSql;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * Implementation providing a basic set of JDBC operations expressed as a command object.
 * This class is loosely modelled after the .NET SqlCommand class.
 * All database access is done via Spring's JdbcTemplate.
 *
 * @author Thomas Risberg
 * @see org.springframework.jdbc.command.SqlCommand
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public class SqlCommand implements SqlCommandOperations {

	private JdbcNamedParameterOperations namedParameterOperations;

	private String sql;

	private ParsedSql parsedSql;

	private SqlNamedParameterTypes sqlTypes = new SqlNamedParameterTypes();


	public SqlCommand(DataSource dataSource, String sql) {
		this.namedParameterOperations = new JdbcNamedParameterTemplate(dataSource);
		this.sql = sql;
		this.parsedSql = NamedParameterUtils.parseSqlStatement(sql);
	}

	public void setSqlTypes(Map types) {
		this.sqlTypes = new SqlNamedParameterTypes(types);
	}

	public void setSqlTypes(SqlNamedParameterTypes namedTypes) {
		this.sqlTypes = namedTypes;
	}


	public Object executeScalar() {
		return namedParameterOperations.getJdbcOperations().queryForObject(sql, Object.class);
	}

	public Object executeScalar(Map parameters) {
		return namedParameterOperations.queryForObject(sql, parameters, Object.class);
	}

	public Object executeScalar(SqlNamedParameterHolder parameterHolder) {
		return namedParameterOperations.queryForObject(sql, parameterHolder, sqlTypes, Object.class);
	}

	public Object executeObject(RowMapper rowMapper) {
		return namedParameterOperations.getJdbcOperations().queryForObject(parsedSql.getNewSql(), rowMapper);
	}

	public Object executeObject(RowMapper rowMapper, Map parameters) {
		return namedParameterOperations.queryForObject(sql, parameters, rowMapper);
	}

	public Object executeObject(RowMapper rowMapper, SqlNamedParameterHolder parameterHolder) {
		return namedParameterOperations.queryForObject(sql, parameterHolder, sqlTypes, rowMapper);
	}

	public List executeQuery() {
		return namedParameterOperations.getJdbcOperations().queryForList(parsedSql.getNewSql());
	}

	public List executeQuery(Map parameters) {
		return namedParameterOperations.queryForList(sql, parameters);
	}

	public List executeQuery(SqlNamedParameterHolder parameterHolder) {
		return namedParameterOperations.queryForList(sql, parameterHolder, sqlTypes);
	}

	public List executeQuery(RowMapper rowMapper) {
		return namedParameterOperations.getJdbcOperations().query(parsedSql.getNewSql(), rowMapper);
	}

	public List executeQuery(RowMapper rowMapper, Map parameters) {
		return namedParameterOperations.query(sql, parameters, rowMapper);
	}

	public List executeQuery(RowMapper rowMapper, SqlNamedParameterHolder parameterHolder) {
		return namedParameterOperations.query(sql, parameterHolder, sqlTypes, rowMapper);
	}

	public SqlRowSet executeRowSet() {
		return namedParameterOperations.getJdbcOperations().queryForRowSet(parsedSql.getNewSql());
	}

	public SqlRowSet executeRowSet(Map parameters) {
		return namedParameterOperations.queryForRowSet(sql, parameters);
	}

	public SqlRowSet executeRowSet(SqlNamedParameterHolder parameterHolder) {
		return namedParameterOperations.queryForRowSet(sql, parameterHolder, sqlTypes);
	}

	public int executeUpdate() {
		return namedParameterOperations.getJdbcOperations().update(parsedSql.getNewSql());
	}

	public int executeUpdate(Map parameters) {
		return namedParameterOperations.update(sql, parameters);
	}

	public int executeUpdate(SqlNamedParameterHolder parameterHolder) {
		return namedParameterOperations.update(sql, parameterHolder, sqlTypes);
	}

	public int executeUpdate(SqlNamedParameterHolder parameterHolder, KeyHolder keyHolder) {
		return executeUpdate(parameterHolder, keyHolder, null);
	}

	public int executeUpdate(SqlNamedParameterHolder parameterHolder, KeyHolder keyHolder, String[] keyColumnNames) {
		return namedParameterOperations.update(sql, parameterHolder, sqlTypes, keyHolder, keyColumnNames);
	}

}
