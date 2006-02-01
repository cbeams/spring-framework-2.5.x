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

package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.NamedParameterUtils;
import org.springframework.jdbc.support.ParsedSql;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * This class provides basic set of JDBC operations allowing the use of
 * named parameters rather than the traditional '?' placeholders.
 *
 * <p>It delegates to the JdbcTemplate once the substitution from named parameters
 * to JDBC style '?' placeholders is done at execution time. It also allows for
 * expanding a List of values to the appropriate number of placeholders.
 *
 * <p>The underlying JdbcTemplate is exposed to allow for convenient access
 * to the traditional JdbcTemplate methods.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 * @see NamedParameterJdbcOperations
 * @see JdbcTemplate
 */
public class NamedParameterJdbcTemplate implements NamedParameterJdbcOperations {

	/** The JdbcTemplate we are wrapping */
	private final JdbcOperations classicJdbcTemplate;


	/**
	 * Create a new NamedParameterJdbcTemplate for the given DataSource.
	 * <p>Creates a classic Spring JdbcTemplate and wraps it.
	 * @param dataSource the JDBC DataSource to access
	 */
	public NamedParameterJdbcTemplate(DataSource dataSource) {
		this.classicJdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * Create a new SimpleJdbcTemplate for the given classic Spring JdbcTemplate.
	 * @param classicJdbcTemplate the classic Spring JdbcTemplate to wrap
	 */
	public NamedParameterJdbcTemplate(JdbcOperations classicJdbcTemplate) {
		this.classicJdbcTemplate = classicJdbcTemplate;
	}

	/**
	 * Expose the classic Spring JdbcTemplate to allow invocation of
	 * less commonly used methods.
	 */
	public JdbcOperations getJdbcOperations() {
		return this.classicJdbcTemplate;
	}


	public void query(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes, RowCallbackHandler rch)
			throws DataAccessException {
		ArgMapPreparedStatementSetter ampss = new ArgMapPreparedStatementSetter(sql, namedParameters, namedTypes);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, namedParameters.getValues());
		getJdbcOperations().query(sqlToUse, ampss, rch);
	}

	public void query(String sql, Map argMap, RowCallbackHandler rch)
			throws DataAccessException {
		ArgMapPreparedStatementSetter ampss = new ArgMapPreparedStatementSetter(sql, argMap);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, argMap);
		getJdbcOperations().query(sqlToUse, ampss, rch);
	}

	public List query(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes, RowMapper rowMapper)
			throws DataAccessException {
		ArgMapPreparedStatementSetter ampss = new ArgMapPreparedStatementSetter(sql, namedParameters, namedTypes);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, namedParameters.getValues());
		return (List) getJdbcOperations().query(sqlToUse, ampss, new RowMapperResultSetExtractor(rowMapper));
	}

	public List query(String sql, Map argMap, RowMapper rowMapper)
			throws DataAccessException {
		ArgMapPreparedStatementSetter ampss = new ArgMapPreparedStatementSetter(sql, argMap);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, argMap);
		return (List) getJdbcOperations().query(sqlToUse, ampss, new RowMapperResultSetExtractor(rowMapper));
	}

	public Object queryForObject(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes, RowMapper rowMapper)
			throws DataAccessException {
		ArgMapPreparedStatementSetter ampss = new ArgMapPreparedStatementSetter(sql, namedParameters, namedTypes);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, namedParameters.getValues());
		List results = (List) getJdbcOperations().query(sqlToUse, ampss, new RowMapperResultSetExtractor(rowMapper, 1));
		return DataAccessUtils.requiredUniqueResult(results);
	}

	public Object queryForObject(String sql, Map argMap, RowMapper rowMapper) throws DataAccessException {
		ArgMapPreparedStatementSetter ampss = new ArgMapPreparedStatementSetter(sql, argMap);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, argMap);
		List results = (List) getJdbcOperations().query(sqlToUse, ampss, new RowMapperResultSetExtractor(rowMapper, 1));
		return DataAccessUtils.requiredUniqueResult(results);
	}

	public Object queryForObject(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes, Class requiredType)
			throws DataAccessException {
		return queryForObject(sql, namedParameters, namedTypes, new SingleColumnRowMapper(requiredType));
	}

	public Object queryForObject(String sql, Map argMap, Class requiredType) throws DataAccessException {
		return queryForObject(sql, argMap, new SingleColumnRowMapper(requiredType));
	}

	public Map queryForMap(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes) throws DataAccessException {
		return (Map) queryForObject(sql, namedParameters, namedTypes, new ColumnMapRowMapper());
	}

	public Map queryForMap(String sql, Map argMap) throws DataAccessException {
		return (Map) queryForObject(sql, argMap, new ColumnMapRowMapper());
	}

	public long queryForLong(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes) throws DataAccessException {
		Number number = (Number) queryForObject(sql, namedParameters, namedTypes, Number.class);
		return (number != null ? number.longValue() : 0);
	}

	public long queryForLong(String sql, Map argMap) throws DataAccessException {
		Number number = (Number) queryForObject(sql, argMap, Number.class);
		return (number != null ? number.longValue() : 0);
	}

	public int queryForInt(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes) throws DataAccessException {
		Number number = (Number) queryForObject(sql, namedParameters, namedTypes, Number.class);
		return (number != null ? number.intValue() : 0);
	}

	public int queryForInt(String sql, Map argMap) throws DataAccessException {
		Number number = (Number) queryForObject(sql, argMap, Number.class);
		return (number != null ? number.intValue() : 0);
	}

	public List queryForList(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes, Class elementType)
			throws DataAccessException {
		return query(sql, namedParameters, namedTypes, new SingleColumnRowMapper(elementType));
	}

	public List queryForList(String sql, Map argMap, Class elementType) throws DataAccessException {
		return query(sql, argMap, new SingleColumnRowMapper(elementType));
	}

	public List queryForList(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes) throws DataAccessException {
		return query(sql, namedParameters, namedTypes, new ColumnMapRowMapper());
	}

	public List queryForList(String sql, final Map argMap) throws DataAccessException {
		return query(sql, argMap, new ColumnMapRowMapper());
	}

	public SqlRowSet queryForRowSet(String sql, final SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes) throws DataAccessException {
		ArgMapPreparedStatementSetter ampss = new ArgMapPreparedStatementSetter(sql, namedParameters, namedTypes);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, namedParameters.getValues());
		return (SqlRowSet) getJdbcOperations().query(sqlToUse, ampss, new SqlRowSetResultSetExtractor());
	}

	public SqlRowSet queryForRowSet(String sql, final Map argMap) throws DataAccessException {
		ArgMapPreparedStatementSetter ampss = new ArgMapPreparedStatementSetter(sql, argMap);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, argMap);
		return (SqlRowSet) getJdbcOperations().query(sqlToUse, ampss, new SqlRowSetResultSetExtractor());
	}

	public int update(String sql, final SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes) throws DataAccessException {
		ArgMapPreparedStatementSetter ampss = new ArgMapPreparedStatementSetter(sql, namedParameters, namedTypes);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, namedParameters.getValues());
		return getJdbcOperations().update(sqlToUse, ampss);
	}

	public int update(String sql, final Map argMap) throws DataAccessException {
		ArgMapPreparedStatementSetter ampss = new ArgMapPreparedStatementSetter(sql, argMap);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, argMap);
		return getJdbcOperations().update(sqlToUse, ampss);
	}

	public int update(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes, KeyHolder keyHolder, String[] keyColumnNames) {
		ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		int[] types = NamedParameterUtils.convertTypeMapToArray(namedTypes.getTypes(), parsedSql);
		Object[] values = NamedParameterUtils.convertArgMapToArray(namedParameters.getValues(), parsedSql);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, namedParameters.getValues());
		PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sqlToUse, types);
		pscf.setReturnGeneratedKeys(true);
		if (keyColumnNames != null) {
			pscf.setGeneratedKeysColumnNames(keyColumnNames);
		}
		return getJdbcOperations().update(pscf.newPreparedStatementCreator(values), keyHolder);
	}


	/**
	 * Simple adapter for PreparedStatementSetter that applies
	 * a given map of arguments.
	 */
	private static class ArgMapPreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {

		private final Object[] args;
		private final int[] argTypes;

		public ArgMapPreparedStatementSetter(String sql, Map argMap) {
			this(sql, new SqlNamedParameterValues(argMap), new SqlNamedParameterTypes());
		}

		public ArgMapPreparedStatementSetter(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes) {
			ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
			this.args = NamedParameterUtils.convertArgMapToArray(namedParameters.getValues(), parsedSql);
			this.argTypes = NamedParameterUtils.convertTypeMapToArray(namedTypes.getTypes(), parsedSql);
		}

		public void setValues(PreparedStatement ps) throws SQLException {
			if (this.args != null) {
				int placeholder = 1;
				for (int i = 0; i < this.args.length; i++) {
					Object o = this.args[i];
					if (o instanceof List) {
						for (int j = 0; j < ((List)o).size(); j++) {
							StatementCreatorUtils.setParameterValue(ps, placeholder++, this.argTypes[i], null, ((List)o).get(j));
						}
					}
					else {
						StatementCreatorUtils.setParameterValue(ps, placeholder++, this.argTypes[i], null, this.args[i]);
					}
				}
			}
		}

		public void cleanupParameters() {
			StatementCreatorUtils.cleanupParameters(this.args);
		}

	}

}
