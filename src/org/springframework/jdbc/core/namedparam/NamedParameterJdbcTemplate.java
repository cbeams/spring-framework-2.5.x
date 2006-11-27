/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.jdbc.core.namedparam;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.SqlRowSetResultSetExtractor;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.util.Assert;

/**
 * This class provides basic set of JDBC operations allowing the use of
 * named parameters rather than the traditional '?' placeholders.
 *
 * <p>It delegates to a wrapped {@link #getJdbcOperations() JdbcTemplate} once
 * the substitution from named parameters to JDBC style '?' placeholders is done
 * at execution time. It also allows for expanding a {@link java.util.List} of
 * values to the appropriate number of placeholders.
 *
 * <p>The underlying {@link org.springframework.jdbc.core.JdbcTemplate} is
 * exposed to allow for convenient access to the traditional
 * {@link org.springframework.jdbc.core.JdbcTemplate} methods.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 * @see NamedParameterJdbcOperations
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public class NamedParameterJdbcTemplate implements NamedParameterJdbcOperations {

	/**
	 * The JdbcTemplate we are wrapping
	 */
	private final JdbcOperations classicJdbcTemplate;


	/**
	 * Create a new NamedParameterJdbcTemplate for the given {@link DataSource}.
	 * <p>Creates a classic Spring {@link org.springframework.jdbc.core.JdbcTemplate} and wraps it.
	 * @param dataSource the JDBC {@link DataSource} to access
	 * @throws IllegalArgumentException if the given {@link DataSource} is <code>null</code>
	 */
	public NamedParameterJdbcTemplate(DataSource dataSource) {
		Assert.notNull(dataSource, "The [dataSource] argument cannot be null.");
		this.classicJdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * Create a new SimpleJdbcTemplate for the given classic Spring {@link org.springframework.jdbc.core.JdbcTemplate}.
	 * @param classicJdbcTemplate the classic Spring {@link org.springframework.jdbc.core.JdbcTemplate} to wrap
	 * @throws IllegalArgumentException if the given {@link org.springframework.jdbc.core.JdbcTemplate} is <code>null</code>
	 */
	public NamedParameterJdbcTemplate(JdbcOperations classicJdbcTemplate) {
		Assert.notNull(classicJdbcTemplate, "The [classicJdbcTemplate] argument cannot be null.");
		this.classicJdbcTemplate = classicJdbcTemplate;
	}

	/**
	 * Expose the classic Spring JdbcTemplate to allow invocation of
	 * less commonly used methods.
	 */
	public JdbcOperations getJdbcOperations() {
		return this.classicJdbcTemplate;
	}


	//-------------------------------------------------------------------------
	// Query operations
	//-------------------------------------------------------------------------

	public void query(String sql, SqlParameterSource paramSource, RowCallbackHandler rch)
			throws DataAccessException {

		ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		Object[] args = NamedParameterUtils.buildValueArray(parsedSql, paramSource);
		int[] argTypes = NamedParameterUtils.buildSqlTypeArray(parsedSql, paramSource);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, paramSource);
		getJdbcOperations().query(sqlToUse, args, argTypes, rch);
	}

	public void query(String sql, Map paramMap, RowCallbackHandler rch) throws DataAccessException {
		query(sql, new MapSqlParameterSource(paramMap), rch);
	}

	public List query(String sql, SqlParameterSource paramSource, RowMapper rowMapper)
			throws DataAccessException {

		ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		Object[] args = NamedParameterUtils.buildValueArray(parsedSql, paramSource);
		int[] argTypes = NamedParameterUtils.buildSqlTypeArray(parsedSql, paramSource);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, paramSource);
		return (List) getJdbcOperations().query(
				sqlToUse, args, argTypes, new RowMapperResultSetExtractor(rowMapper));
	}

	public List query(String sql, Map paramMap, RowMapper rowMapper) throws DataAccessException {
		return query(sql, new MapSqlParameterSource(paramMap), rowMapper);
	}

	public Object queryForObject(String sql, SqlParameterSource paramSource, RowMapper rowMapper)
			throws DataAccessException {

		ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		Object[] args = NamedParameterUtils.buildValueArray(parsedSql, paramSource);
		int[] argTypes = NamedParameterUtils.buildSqlTypeArray(parsedSql, paramSource);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, paramSource);
		List results = (List) getJdbcOperations().query(
				sqlToUse, args, argTypes, new RowMapperResultSetExtractor(rowMapper, 1));
		return DataAccessUtils.requiredSingleResult(results);
	}

	public Object queryForObject(String sql, Map paramMap, RowMapper rowMapper) throws DataAccessException {
		return queryForObject(sql, new MapSqlParameterSource(paramMap), rowMapper);
	}

	public Object queryForObject(String sql, SqlParameterSource paramSource, Class requiredType)
			throws DataAccessException {
		return queryForObject(sql, paramSource, new SingleColumnRowMapper(requiredType));
	}

	public Object queryForObject(String sql, Map paramMap, Class requiredType) throws DataAccessException {
		return queryForObject(sql, paramMap, new SingleColumnRowMapper(requiredType));
	}

	public Map queryForMap(String sql, SqlParameterSource paramSource) throws DataAccessException {
		return (Map) queryForObject(sql, paramSource, new ColumnMapRowMapper());
	}

	public Map queryForMap(String sql, Map paramMap) throws DataAccessException {
		return (Map) queryForObject(sql, paramMap, new ColumnMapRowMapper());
	}

	public long queryForLong(String sql, SqlParameterSource paramSource) throws DataAccessException {
		Number number = (Number) queryForObject(sql, paramSource, Number.class);
		return (number != null ? number.longValue() : 0);
	}

	public long queryForLong(String sql, Map paramMap) throws DataAccessException {
		return queryForLong(sql, new MapSqlParameterSource(paramMap));
	}

	public int queryForInt(String sql, SqlParameterSource paramSource) throws DataAccessException {
		Number number = (Number) queryForObject(sql, paramSource, Number.class);
		return (number != null ? number.intValue() : 0);
	}

	public int queryForInt(String sql, Map paramMap) throws DataAccessException {
		return queryForInt(sql, new MapSqlParameterSource(paramMap));
	}

	public List queryForList(String sql, SqlParameterSource paramSource, Class elementType)
			throws DataAccessException {
		return query(sql, paramSource, new SingleColumnRowMapper(elementType));
	}

	public List queryForList(String sql, Map paramMap, Class elementType) throws DataAccessException {
		return queryForList(sql, new MapSqlParameterSource(paramMap), elementType);
	}

	public List queryForList(String sql, SqlParameterSource paramSource) throws DataAccessException {
		return query(sql, paramSource, new ColumnMapRowMapper());
	}

	public List queryForList(String sql, Map paramMap) throws DataAccessException {
		return queryForList(sql, new MapSqlParameterSource(paramMap));
	}

	public SqlRowSet queryForRowSet(String sql, SqlParameterSource paramSource) throws DataAccessException {
		ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		Object[] args = NamedParameterUtils.buildValueArray(parsedSql, paramSource);
		int[] argTypes = NamedParameterUtils.buildSqlTypeArray(parsedSql, paramSource);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, paramSource);
		return (SqlRowSet) getJdbcOperations().query(sqlToUse, args, argTypes, new SqlRowSetResultSetExtractor());
	}

	public SqlRowSet queryForRowSet(String sql, Map paramMap) throws DataAccessException {
		return queryForRowSet(sql, new MapSqlParameterSource(paramMap));
	}


	//-------------------------------------------------------------------------
	// Update operations
	//-------------------------------------------------------------------------

	public int update(String sql, SqlParameterSource paramSource) throws DataAccessException {
		ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		Object[] args = NamedParameterUtils.buildValueArray(parsedSql, paramSource);
		int[] argTypes = NamedParameterUtils.buildSqlTypeArray(parsedSql, paramSource);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, paramSource);
		return getJdbcOperations().update(sqlToUse, args, argTypes);
	}

	public int update(String sql, Map paramMap) throws DataAccessException {
		return update(sql, new MapSqlParameterSource(paramMap));
	}

	public int update(String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder)
			throws DataAccessException {

		return update(sql, paramSource, generatedKeyHolder, null);
	}

	public int update(
			String sql, SqlParameterSource paramSource, KeyHolder generatedKeyHolder, String[] keyColumnNames)
			throws DataAccessException {

		ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		Object[] args = NamedParameterUtils.buildValueArray(parsedSql, paramSource);
		int[] argTypes = NamedParameterUtils.buildSqlTypeArray(parsedSql, paramSource);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, paramSource);
		PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sqlToUse, argTypes);
		if (keyColumnNames != null) {
			pscf.setGeneratedKeysColumnNames(keyColumnNames);
		}
		else {
			pscf.setReturnGeneratedKeys(true);
		}
		return getJdbcOperations().update(pscf.newPreparedStatementCreator(args), generatedKeyHolder);
	}

}
