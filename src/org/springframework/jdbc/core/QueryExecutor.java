package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Interface for custom query execution. Used by JdbcTemplate.
 *
 * <p>Useful to override ResultSet creation in the case of a connection
 * pool that uses ResultSet wrappers, to still return the underlying
 * native ResultSet to application code. The application can then
 * safely cast the ResultSet to e.g. OracleResultSet.
 *
 * <p>Note: Setting a custom query executor is just necessary if you
 * want to cast the ResultSets to database-specific implementations
 * like OracleResultSet. Else, any wrapped ResultSet will be fine too.
 *
 * @author Juergen Hoeller
 * @since 25.08.2003
 * @see JdbcTemplate#setQueryExecutor
 */
public interface QueryExecutor {

	/**
	 * Execute the given SQL query on the given statement.
	 * @param stmt the JDBC Statement as returned by the Connection
	 * @param sql the SQL query string
	 * @return the created ResultSet
	 * @throws SQLException thrown by Statement.executeQuery calls
	 * @see java.sql.Statement#executeQuery
	 */
	ResultSet executeQuery(Statement stmt, String sql) throws SQLException;

	/**
	 * Execute the given prepared statement.
	 * @param ps the JDBC PreparedStatement as returned by the Connection
	 * @return the created ResultSet
	 * @throws SQLException thrown by PreparedStatement.executeQuery calls
	 * @see java.sql.PreparedStatement#executeQuery
	 */
	ResultSet executeQuery(PreparedStatement ps) throws SQLException;

}
