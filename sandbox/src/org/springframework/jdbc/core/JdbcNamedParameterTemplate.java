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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.command.SqlNamedParameterHolder;
import org.springframework.jdbc.command.SqlNamedParameterValues;
import org.springframework.jdbc.command.NamedParameterUtils;
import org.springframework.jdbc.command.SqlNamedParameterTypes;
import org.springframework.jdbc.support.ParsedSql;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * <b>This is the central class in the JDBC core package.</b>
 * It simplifies the use of JDBC and helps to avoid common errors.
 * It executes core JDBC workflow, leaving application code to provide SQL
 * and extract results. This class executes SQL queries or updates, initiating
 * iteration over ResultSets and catching JDBC exceptions and translating
 * them to the generic, more informative exception hierarchy defined in the
 * <code>org.springframework.dao</code> package.
 *
 * <p>Code using this class need only implement callback interfaces, giving
 * them a clearly defined contract. The PreparedStatementCreator callback
 * interface creates a prepared statement given a Connection provided by this
 * class, providing SQL and any necessary parameters. The RowCallbackHandler
 * interface extracts values from each row of a ResultSet.
 *
 * <p>Can be used within a service implementation via direct instantiation
 * with a DataSource reference, or get prepared in an application context
 * and given to services as bean reference. Note: The DataSource should
 * always be configured as a bean in the application context, in the first case
 * given to the service directly, in the second case to the prepared template.
 *
 * <p>The motivation and design of this class is discussed
 * in detail in
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 *
 * <p>Because this class is parameterizable by the callback interfaces and
 * the SQLExceptionTranslator interface, it isn't necessary to subclass it.
 * All operations performed by this class are logged at debug level.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Thomas Risberg
 * @since May 3, 2001
 * @see org.springframework.jdbc.core.ResultSetExtractor
 * @see org.springframework.jdbc.core.RowCallbackHandler
 * @see org.springframework.jdbc.core.RowMapper
 * @see org.springframework.dao
 * @see org.springframework.jdbc.datasource
 * @see org.springframework.jdbc.object
 */
public class JdbcNamedParameterTemplate extends JdbcTemplate implements JdbcNamedParameterOperations {

//	/** Custom NativeJdbcExtractor */
//	private NativeJdbcExtractor nativeJdbcExtractor;
//
//	/** If this variable is false, we will throw exceptions on SQL warnings */
//	private boolean ignoreWarnings = true;
//
//	/**
//	 * If this variable is set to a non-zero value, it will be used for setting the
//	 * fetchSize property on statements used for query processing.
//	 */
//	private int fetchSize = 0;
//
//	/**
//	 * If this variable is set to a non-zero value, it will be used for setting the
//	 * maxRows property on statements used for query processing.
//	 */
//	private int maxRows = 0;
//

	/**
	 * Construct a new JdbcTemplate for bean usage.
	 * Note: The DataSource has to be set before using the instance.
	 * This constructor can be used to prepare a JdbcTemplate via a BeanFactory,
	 * typically setting the DataSource via setDataSource.
	 * @see #setDataSource
	 */
	public JdbcNamedParameterTemplate() {
	}

	/**
	 * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
	 * Note: This will not trigger initialization of the exception translator.
	 * @param dataSource JDBC DataSource to obtain connections from
	 */
	public JdbcNamedParameterTemplate(DataSource dataSource) {
		setDataSource(dataSource);
		afterPropertiesSet();
	}

	/**
	 * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
	 * Note: Depending on the "lazyInit" flag, initialization of the exception translator
	 * will be triggered.
	 * @param dataSource JDBC DataSource to obtain connections from
	 * @param lazyInit whether to lazily initialize the SQLExceptionTranslator
	 */
	public JdbcNamedParameterTemplate(DataSource dataSource, boolean lazyInit) {
		setDataSource(dataSource);
		setLazyInit(lazyInit);
		afterPropertiesSet();
	}


	public List query(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes, RowCallbackHandler rch)
			throws DataAccessException {
		ArgMapPreparedStatementCreator ampsc = new ArgMapPreparedStatementCreator(sql, namedParameters, namedTypes);
		// ToDo
		return (List) query(ampsc, ampsc, new RowCallbackHandlerResultSetExtractor(rch));
	}

	public List query(String sql, Map argMap, RowCallbackHandler rch)
			throws DataAccessException {
		ArgMapPreparedStatementCreator ampsc = new ArgMapPreparedStatementCreator(sql, argMap);
		// ToDo
		return (List) query(ampsc, ampsc, new RowCallbackHandlerResultSetExtractor(rch));
	}

	public List query(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes, RowMapper rowMapper)
			throws DataAccessException {
		return query(sql, namedParameters, namedTypes, new RowMapperResultReader(rowMapper));
	}

	public List query(String sql, Map argMap, RowMapper rowMapper)
			throws DataAccessException {
		return query(sql, argMap, new RowMapperResultReader(rowMapper));
	}

	public Object queryForObject(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes, RowMapper rowMapper)
			throws DataAccessException {
		List results = query(sql, namedParameters, namedTypes, new RowMapperResultReader(rowMapper, 1));
		return DataAccessUtils.requiredUniqueResult(results);
	}

	public Object queryForObject(String sql, Map argMap, RowMapper rowMapper) throws DataAccessException {
		List results = query(sql, argMap, new RowMapperResultReader(rowMapper, 1));
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
		ArgMapPreparedStatementCreator ampsc = new ArgMapPreparedStatementCreator(sql, namedParameters, namedTypes);
		// ToDo
		return (SqlRowSet) query(ampsc, ampsc, new SqlRowSetResultSetExtractor());
	}

	public SqlRowSet queryForRowSet(String sql, final Map argMap) throws DataAccessException {
		ArgMapPreparedStatementCreator ampsc = new ArgMapPreparedStatementCreator(sql, argMap);
		// ToDo
		return (SqlRowSet) query(ampsc, ampsc, new SqlRowSetResultSetExtractor());
	}

	public int update(String sql, final SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes) throws DataAccessException {
		ArgMapPreparedStatementCreator ampsc = new ArgMapPreparedStatementCreator(sql, namedParameters, namedTypes);
		return update(ampsc, ampsc);
	}

	public int update(String sql, final Map argMap) throws DataAccessException {
		ArgMapPreparedStatementCreator ampsc = new ArgMapPreparedStatementCreator(sql, argMap);
		return update(ampsc, ampsc);
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
        return update(pscf.newPreparedStatementCreator(values), keyHolder);
    }

    /**
	 * Simple adapter for PreparedStatementSetter that applies
	 * a given map of arguments.
	 */
	private static class ArgMapPreparedStatementCreator implements PreparedStatementCreator, PreparedStatementSetter, ParameterDisposer {

		private final Object[] args;
		private final String sql;
		private final String sqlToUse;
		//private final ParsedSql parsedSql;
		private final int[] argTypes;

		public ArgMapPreparedStatementCreator(String sql, Map argMap) {
			this(sql, new SqlNamedParameterValues(argMap), new SqlNamedParameterTypes());
		}

		public ArgMapPreparedStatementCreator(String sql, SqlNamedParameterHolder namedParameters, SqlNamedParameterTypes namedTypes) {
			this.sql = sql;
            sqlToUse = NamedParameterUtils.substituteNamedParameters(sql, namedParameters.getValues());
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

		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			return con.prepareStatement(sqlToUse);
		}

		public String getSql() {
			return sql;
		}
	}

	/**
	 * Simple adapter for PreparedStatementSetter that applies
	 * given arrays of arguments and JDBC argument types.
	 */
	private static class ArgTypePreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {

		private final Object[] args;

		private final int[] argTypes;

		public ArgTypePreparedStatementSetter(Object[] args, int[] argTypes) {
			if ((args != null && argTypes == null) || (args == null && argTypes != null) ||
					(args != null && args.length != argTypes.length)) {
				throw new InvalidDataAccessApiUsageException("args and argTypes parameters must match");
			}
			this.args = args;
			this.argTypes = argTypes;
		}

		public void setValues(PreparedStatement ps) throws SQLException {
			if (this.args != null) {
				for (int i = 0; i < this.args.length; i++) {
					StatementCreatorUtils.setParameterValue(ps, i + 1, this.argTypes[i], null, this.args[i]);
				}
			}
		}

		public void cleanupParameters() {
			StatementCreatorUtils.cleanupParameters(this.args);
		}
	}

	/**
	 * Adapter to enable use of a RowCallbackHandler inside a ResultSetExtractor.
	 * <p>Uses a regular ResultSet, so we have to be careful when using it:
	 * We don't use it for navigating since this could lead to unpredictable consequences.
	 */
	private static class RowCallbackHandlerResultSetExtractor implements ResultSetExtractor {

		private final RowCallbackHandler rch;

		public RowCallbackHandlerResultSetExtractor(RowCallbackHandler rch) {
			this.rch = rch;
		}

		public Object extractData(ResultSet rs) throws SQLException {
			while (rs.next()) {
				this.rch.processRow(rs);
			}
			if (this.rch instanceof ResultReader) {
				return ((ResultReader) this.rch).getResults();
			}
			else {
				return null;
			}
		}
	}
}
