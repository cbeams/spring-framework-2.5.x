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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.ibatis.db.sqlmap.MappedStatement;
import com.ibatis.db.sqlmap.RowHandler;
import com.ibatis.db.sqlmap.SqlMap;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcAccessor;

/**
 * Helper class that simplifies data access via the MappedStatement API of the
 * iBATIS Database Layer, and converts checked SQLExceptions into unchecked
 * DataAccessExceptions, compatible to the org.springframework.dao exception
 * hierarchy. Uses the same SQLExceptionTranslator mechanism as JdbcTemplate.
 *
 * <p>The main method is execute, taking the name of a mapped statement defined
 * in the iBATIS SqlMap config file and a callback that implements a data access
 * action on the specified statement.
 *
 * <p>This class provides numerous convenience methods that mirror MappedStatement's
 * executeXXX methods. See the MappedStatement javadocs for details on those methods.
 *
 * <p>NOTE: The SqlMap/MappedStatement API is the one to use with iBATIS SQL Maps 1.x.
 * The SqlMapClient/SqlMapSession API is only available with SQL Maps 2.
 *
 * @author Juergen Hoeller
 * @since 28.11.2003
 * @see #execute
 * @see #setSqlMap
 * @see #setDataSource
 * @see #setExceptionTranslator
 * @see com.ibatis.db.sqlmap.MappedStatement
 */
public class SqlMapTemplate extends JdbcAccessor implements SqlMapOperations {

	private SqlMap sqlMap;


	/**
	 * Create a new SqlMapTemplate.
	 */
	public SqlMapTemplate() {
	}

	/**
	 * Create a new SqlMapTemplate.
	 * @param dataSource JDBC DataSource to obtain connections from
	 * @param sqlMap iBATIS SqlMap that defines the mapped statements
	 */
	public SqlMapTemplate(DataSource dataSource, SqlMap sqlMap) {
		setDataSource(dataSource);
		setSqlMap(sqlMap);
		afterPropertiesSet();
	}

	/**
	 * Set the iBATIS Database Layer SqlMap that defines the mapped statements.
	 */
	public void setSqlMap(SqlMap sqlMap) {
		this.sqlMap = sqlMap;
	}

	/**
	 * Return the iBATIS Database Layer SqlMap that this template works with.
	 */
	public SqlMap getSqlMap() {
		return sqlMap;
	}

	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		if (this.sqlMap == null) {
			throw new IllegalArgumentException("sqlMap is required");
		}
	}


	/**
	 * Execute the given data access action on the given iBATIS mapped statement.
	 * @param statementName name of the statement mapped in the iBATIS SqlMap config file
	 * @param action callback object that specifies the data access action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException in case of SQL Maps errors
	 */
	public Object execute(String statementName, SqlMapCallback action) throws DataAccessException {
		MappedStatement stmt = this.sqlMap.getMappedStatement(statementName);
		Connection con = DataSourceUtils.getConnection(getDataSource());
		try {
			return action.doInMappedStatement(stmt, con);
		}
		catch (SQLException ex) {
			throw getExceptionTranslator().translate("SqlMapTemplate", "(mapped statement)", ex);
		}
		finally {
			DataSourceUtils.closeConnectionIfNecessary(con, getDataSource());
		}
	}

	/**
	 * Execute the given data access action on the given mapped statement,
	 * expecting a List result.
	 * @param statementName name of the statement mapped in the iBATIS SqlMap config file
	 * @param action callback object that specifies the data access action
	 * @return a List result
	 * @throws DataAccessException in case of SQL Maps errors
	 */
	public List executeWithListResult(String statementName, SqlMapCallback action)
	    throws DataAccessException {
		return (List) execute(statementName, action);
	}

	/**
	 * Execute the given data access action on the given mapped statement,
	 * expecting a Map result.
	 * @param statementName name of the statement mapped in the iBATIS SqlMap config file
	 * @param action callback object that specifies the data access action
	 * @return a Map result
	 * @throws DataAccessException in case of SQL Maps errors
	 */
	public Map executeWithMapResult(String statementName, SqlMapCallback action)
	    throws DataAccessException {
		return (Map) execute(statementName, action);
	}


	public Object executeQueryForObject(String statementName, final Object parameterObject)
			throws DataAccessException {
		return execute(statementName, new SqlMapCallback() {
			public Object doInMappedStatement(MappedStatement stmt, Connection con) throws SQLException {
				return stmt.executeQueryForObject(con, parameterObject);
			}
		});
	}

	public Object executeQueryForObject(String statementName, final Object parameterObject,
																			final Object resultObject) throws DataAccessException {
		return execute(statementName, new SqlMapCallback() {
			public Object doInMappedStatement(MappedStatement stmt, Connection con) throws SQLException {
				return stmt.executeQueryForObject(con, parameterObject, resultObject);
			}
		});
	}

	public List executeQueryForList(String statementName, final Object parameterObject)
			throws DataAccessException {
		return executeWithListResult(statementName, new SqlMapCallback() {
			public Object doInMappedStatement(MappedStatement stmt, Connection con) throws SQLException {
				return stmt.executeQueryForList(con, parameterObject);
			}
		});
	}

	public List executeQueryForList(String statementName, final Object parameterObject,
																	final int skipResults, final int maxResults)
			throws DataAccessException {
		return executeWithListResult(statementName, new SqlMapCallback() {
			public Object doInMappedStatement(MappedStatement stmt, Connection con) throws SQLException {
				return stmt.executeQueryForList(con, parameterObject, skipResults, maxResults);
			}
		});
	}

	public Map executeQueryForMap(String statementName, final Object parameterObject,
																final String keyProperty) throws DataAccessException {
		return executeWithMapResult(statementName, new SqlMapCallback() {
			public Object doInMappedStatement(MappedStatement stmt, Connection con) throws SQLException {
				return stmt.executeQueryForMap(con, parameterObject, keyProperty);
			}
		});
	}

	public Map executeQueryForMap(String statementName, final Object parameterObject,
																final String keyProperty, final String valueProperty)
			throws DataAccessException {
		return executeWithMapResult(statementName, new SqlMapCallback() {
			public Object doInMappedStatement(MappedStatement stmt, Connection con) throws SQLException {
				return stmt.executeQueryForMap(con, parameterObject, keyProperty, valueProperty);
			}
		});
	}

	public void executeQueryWithRowHandler(String statementName, final Object parameterObject,
																				 final RowHandler rowHandler) throws DataAccessException {
		execute(statementName, new SqlMapCallback() {
			public Object doInMappedStatement(MappedStatement stmt, Connection con) throws SQLException {
				stmt.executeQueryWithRowHandler(con, parameterObject, rowHandler);
				return null;
			}
		});
	}

	public int executeUpdate(String statementName, final Object parameterObject)
			throws DataAccessException {
		Integer result = (Integer) execute(statementName, new SqlMapCallback() {
			public Object doInMappedStatement(MappedStatement stmt, Connection con) throws SQLException {
				return new Integer(stmt.executeUpdate(con, parameterObject));
			}
		});
		return result.intValue();
	}

}
