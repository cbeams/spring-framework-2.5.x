package org.springframework.orm.ibatis;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapSession;
import com.ibatis.sqlmap.client.event.RowHandler;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcAccessor;

/**
 * Helper class that simplifies data access via the SqlMapClient API of the
 * iBATIS Database Layer, and converts checked SQLExceptions into unchecked
 * DataAccessExceptions, compatible to the org.springframework.dao exception
 * hierarchy. Uses the same SQLExceptionTranslator mechanism as JdbcTemplate.
 *
 * <p>The main method is execute, a callback that implements a data access action.
 * This class provides numerous convenience methods that mirror SqlMapSession's
 * execution methods. See the SqlMapClient javadocs for details on those methods.
 *
 * <p>NOTE: The SqlMapClient/SqlMapSession API is the API of iBATIS SQL Maps 2.
 * With SQL Maps 1.x, the SqlMap/MappedStatement has to be used.
 *
 * @author Juergen Hoeller
 * @since 24.02.2004
 * @see #execute
 * @see #setSqlMapClient
 * @see #setDataSource
 * @see #setExceptionTranslator
 * @see com.ibatis.sqlmap.client.SqlMapSession
 */
public class SqlMapClientTemplate extends JdbcAccessor implements SqlMapClientOperations {

	private SqlMapClient sqlMapClient;

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		if (this.sqlMapClient == null) {
			throw new IllegalArgumentException("sqlMapClient is required");
		}
	}


	public Object execute(SqlMapClientCallback action) throws DataAccessException {
		SqlMapSession sqlMapSession = this.sqlMapClient.getSession();
		Connection con = DataSourceUtils.getConnection(getDataSource());
		try {
			sqlMapSession.setUserConnection(con);
			return action.doInSqlMapSession(sqlMapSession);
		}
		catch (SQLException ex) {
			throw getExceptionTranslator().translate("SqlMapTemplate", "(mapped statement)", ex);
		}
		finally {
			DataSourceUtils.closeConnectionIfNecessary(con, getDataSource());
		}
	}

	public List executeWithListResult(SqlMapClientCallback action) throws DataAccessException {
		return (List) execute(action);
	}

	public Map executeWithMapResult(SqlMapClientCallback action) throws DataAccessException {
		return (Map) execute(action);
	}

	public Object queryForObject(final String statementName, final Object parameterObject)
			throws DataAccessException {
		return execute(new SqlMapClientCallback() {
			public Object doInSqlMapSession(SqlMapSession sqlMapSession) throws SQLException {
				return sqlMapSession.queryForObject(statementName, parameterObject);
			}
		});
	}

	public Object queryForObject(final String statementName, final Object parameterObject,
															 final Object resultObject) throws DataAccessException {
		return execute(new SqlMapClientCallback() {
			public Object doInSqlMapSession(SqlMapSession sqlMapSession) throws SQLException {
				return sqlMapSession.queryForObject(statementName, parameterObject, resultObject);
			}
		});
	}

	public List queryForList(final String statementName, final Object parameterObject)
			throws DataAccessException {
		return executeWithListResult(new SqlMapClientCallback() {
			public Object doInSqlMapSession(SqlMapSession sqlMapSession) throws SQLException {
				return sqlMapSession.queryForList(statementName, parameterObject);
			}
		});
	}

	public List queryForList(final String statementName, final Object parameterObject,
													 final int skipResults, final int maxResults)
			throws DataAccessException {
		return executeWithListResult(new SqlMapClientCallback() {
			public Object doInSqlMapSession(SqlMapSession sqlMapSession) throws SQLException {
				return sqlMapSession.queryForList(statementName, parameterObject, skipResults, maxResults);
			}
		});
	}

	public List queryForList(final String statementName, final Object parameterObject,
													 final RowHandler rowHandler) throws DataAccessException {
		return executeWithListResult(new SqlMapClientCallback() {
			public Object doInSqlMapSession(SqlMapSession sqlMapSession) throws SQLException {
				return sqlMapSession.queryForList(statementName, parameterObject, rowHandler);
			}
		});
	}

	public Map queryForMap(final String statementName, final Object parameterObject,
												 final String keyProperty) throws DataAccessException {
		return executeWithMapResult(new SqlMapClientCallback() {
			public Object doInSqlMapSession(SqlMapSession sqlMapSession) throws SQLException {
				return sqlMapSession.queryForMap(statementName, parameterObject, keyProperty);
			}
		});
	}

	public Map queryForMap(final String statementName, final Object parameterObject,
												 final String keyProperty, final String valueProperty)
			throws DataAccessException {
		return executeWithMapResult(new SqlMapClientCallback() {
			public Object doInSqlMapSession(SqlMapSession sqlMapSession) throws SQLException {
				return sqlMapSession.queryForMap(statementName, parameterObject, keyProperty, valueProperty);
			}
		});
	}

	public Object insert(final String statementName, final Object parameterObject)
			throws DataAccessException {
		return execute(new SqlMapClientCallback() {
			public Object doInSqlMapSession(SqlMapSession sqlMapSession) throws SQLException {
				return sqlMapSession.insert(statementName, parameterObject);
			}
		});
	}

	public int update(final String statementName, final Object parameterObject)
			throws DataAccessException {
		Integer result = (Integer) execute(new SqlMapClientCallback() {
			public Object doInSqlMapSession(SqlMapSession sqlMapSession) throws SQLException {
				return new Integer(sqlMapSession.update(statementName, parameterObject));
			}
		});
		return result.intValue();
	}

	public int delete(final String statementName, final Object parameterObject)
			throws DataAccessException {
		Integer result = (Integer) execute(new SqlMapClientCallback() {
			public Object doInSqlMapSession(SqlMapSession sqlMapSession) throws SQLException {
				return new Integer(sqlMapSession.delete(statementName, parameterObject));
			}
		});
		return result.intValue();
	}

}
