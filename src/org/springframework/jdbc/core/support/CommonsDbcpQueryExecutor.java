package org.springframework.jdbc.core.support;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp.DelegatingPreparedStatement;
import org.apache.commons.dbcp.DelegatingStatement;

import org.springframework.jdbc.core.QueryExecutor;

/**
 * Implementation of the QueryExecutor interface for the Jakarta Commons
 * DBCP connection pool. Returns the underlying native ResultSet to
 * application code instead of DBCP's wrapper implementations.
 * The returned ResultSets can then safely be cast to e.g. OracleResultSet.
 *
 * <p>This query executor can be set just to allow working with a
 * Commons DBCP DataSource: If a given Statement or PreparedStatement
 * object is not a Commons DBCP wrapper, it will be returned as is.
 *
 * <p>Note: Setting a custom query executor is just necessary if you
 * want to cast the ResultSets to database-specific implementations
 * like OracleResultSet. Else, any wrapped ResultSet will be fine too.
 *
 * @author Juergen Hoeller
 * @since 25.08.2003
 * @see org.springframework.jdbc.core.JdbcTemplate#setQueryExecutor
 */
public class CommonsDbcpQueryExecutor implements QueryExecutor {

	public ResultSet executeQuery(Statement stmt, String sql) throws SQLException {
		if (stmt instanceof DelegatingStatement) {
			stmt = ((DelegatingStatement) stmt).getInnermostDelegate();
		}
		return stmt.executeQuery(sql);
	}

	public ResultSet executeQuery(PreparedStatement ps) throws SQLException {
		if (ps instanceof DelegatingPreparedStatement) {
			ps = ((DelegatingPreparedStatement) ps).getInnermostDelegate();
		}
		return ps.executeQuery();
	}

}
