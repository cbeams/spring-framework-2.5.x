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

package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.command.NamedParameterUtils;
import org.springframework.jdbc.command.SqlNamedParameterHolder;
import org.springframework.jdbc.command.SqlNamedParameterTypes;
import org.springframework.jdbc.command.SqlNamedParameterValues;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.ParsedSql;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * @author Thomas Risberg
 * @author Juergen Hoeller
 */
public class JdbcNamedParameterTemplate implements JdbcNamedParameterOperations {

	/** The JdbcTemplate we are wrapping */
	private final JdbcOperations classicJdbcTemplate;


	/**
	 * Create a new SimpleJdbcTemplate for the given DataSource.
	 * <p>Creates a classic Spring JdbcTemplate and wraps it.
	 * @param dataSource the JDBC DataSource to access
	 */
	public JdbcNamedParameterTemplate(DataSource dataSource) {
		this.classicJdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * Create a new SimpleJdbcTemplate for the given classic Spring JdbcTemplate.
	 * @param classicJdbcTemplate the classic Spring JdbcTemplate to wrap
	 */
	public JdbcNamedParameterTemplate(JdbcOperations classicJdbcTemplate) {
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
		ArgMapPreparedStatementSetter ampsc = new ArgMapPreparedStatementSetter(sql, namedParameters, namedTypes);
		// ToDo
		getJdbcOperations().query(sql, ampsc, rch);
	}

	public void query(String sql, Map argMap, RowCallbackHandler rch)
			throws DataAccessException {
		ArgMapPreparedStatementSetter ampsc = new ArgMapPreparedStatementSetter(sql, argMap);
		// ToDo
		getJdbcOperations().query(sql, ampsc, rch);
	}

	public List query(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes, RowMapper rowMapper)
			throws DataAccessException {
		ArgMapPreparedStatementSetter ampsc = new ArgMapPreparedStatementSetter(sql, namedParameters, namedTypes);
		return (List) getJdbcOperations().query(sql, ampsc, new RowMapperResultSetExtractor(rowMapper));
	}

	public List query(String sql, Map argMap, RowMapper rowMapper)
			throws DataAccessException {
		ArgMapPreparedStatementSetter ampsc = new ArgMapPreparedStatementSetter(sql, argMap);
		return (List) getJdbcOperations().query(sql, ampsc, new RowMapperResultSetExtractor(rowMapper));
	}

	public Object queryForObject(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes, RowMapper rowMapper)
			throws DataAccessException {
		ArgMapPreparedStatementSetter ampsc = new ArgMapPreparedStatementSetter(sql, namedParameters, namedTypes);
		List results = (List) getJdbcOperations().query(sql, ampsc, new RowMapperResultSetExtractor(rowMapper, 1));
		return DataAccessUtils.requiredUniqueResult(results);
	}

	public Object queryForObject(String sql, Map argMap, RowMapper rowMapper) throws DataAccessException {
		ArgMapPreparedStatementSetter ampsc = new ArgMapPreparedStatementSetter(sql, argMap);
		List results = (List) getJdbcOperations().query(sql, ampsc, new RowMapperResultSetExtractor(rowMapper, 1));
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
		ArgMapPreparedStatementSetter ampsc = new ArgMapPreparedStatementSetter(sql, namedParameters, namedTypes);
		// ToDo
		return (SqlRowSet) getJdbcOperations().query(sql, ampsc, new SqlRowSetResultSetExtractor());
	}

	public SqlRowSet queryForRowSet(String sql, final Map argMap) throws DataAccessException {
		ArgMapPreparedStatementSetter ampsc = new ArgMapPreparedStatementSetter(sql, argMap);
		// ToDo
		return (SqlRowSet) getJdbcOperations().query(sql, ampsc, new SqlRowSetResultSetExtractor());
	}

	public int update(String sql, final SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes) throws DataAccessException {
		ArgMapPreparedStatementSetter ampsc = new ArgMapPreparedStatementSetter(sql, namedParameters, namedTypes);
		return getJdbcOperations().update(sql, ampsc);
	}

	public int update(String sql, final Map argMap) throws DataAccessException {
		ArgMapPreparedStatementSetter ampsc = new ArgMapPreparedStatementSetter(sql, argMap);
		return getJdbcOperations().update(sql, ampsc);
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
			//this.argTypes = new int[] {};
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
