package org.springframework.jdbc.support.nativejdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Interface for extracting native JDBC objects from wrapped objects coming from
 * connection pools. This is necessary to be able to case to native implementations
 * like OracleConnection or OracleResultSet in application code, for example to
 * create Blobs or access other vendor-specific features.
 *
 * <p>Note: Setting a custom NativeJdbcExtractor is just necessary if you want to
 * cast to database-specific implementations, like OracleConnection/OracleResultSet.
 * Else, any wrapped JDBC object will be fine too.
 *
 * <p>Note: To be able to support any pool's strategy of native ResultSet wrapping,
 * it is advisable to get both the native Statement <i>and</i> the native ResultSet
 * via this extractor. Some pools just allow to unwrap the Statement, some just to
 * unwrap the ResultSet - the above strategy will cover both. It is typically
 * <i>not</i> necessary to unwrap the Connection to retrieve the native ResultSet.
 *
 * @author Juergen Hoeller
 * @since 25.08.2003
 * @see org.springframework.jdbc.core.JdbcTemplate#setNativeJdbcExtractor
 */
public interface NativeJdbcExtractor {

	/**
	 * Return whether it is necessary to work on the native Connection to
	 * receive native Statements and ResultSets.
	 * <p>This should be true if the connection pool does not allow to extract
	 * the native JDBC objects from its Statement/ResultSet wrappers but
	 * supports a way to retrieve the native JDBC Connection. This way,
	 * applications can still receive native Statements and ResultSet via
	 * working on the native JDBC Connection.
	 */
	boolean isNativeConnectionNecessaryForNativeStatements();

	/**
	 * Retrieve the underlying native JDBC Connection for the given Connection.
	 * Supposed to return the given Connection if not capable of unwrapping.
	 * @param con the Connection handle, potentially wrapped by a connection pool
	 * @return the underlying native JDBC Connection, if possible
	 * @throws SQLException if thrown by JDBC methods
	 */
	Connection getNativeConnection(Connection con) throws SQLException;

	/**
	 * Retrieve the underlying native JDBC Statement for the given Statement.
	 * Supposed to return the given Statement if not capable of unwrapping.
	 * @param stmt the Statement handle, potentially wrapped by a connection pool
	 * @return the underlying native JDBC Statement, if possible
	 * @throws SQLException if thrown by JDBC methods
	 */
	Statement getNativeStatement(Statement stmt) throws SQLException;

	/**
	 * Retrieve the underlying native JDBC PreparedStatement for the given statement.
	 * Supposed to return the given PreparedStatement if not capable of unwrapping.
	 * @param ps the PreparedStatement handle, potentially wrapped by a connection pool
	 * @return the underlying native JDBC PreparedStatement, if possible
	 * @throws SQLException if thrown by JDBC methods
	 */
	PreparedStatement getNativePreparedStatement(PreparedStatement ps) throws SQLException;

	/**
	 * Retrieve the underlying native JDBC CallableStatement for the given statement.
	 * Supposed to return the given CallableStatement if not capable of unwrapping.
	 * @param cs the CallableStatement handle, potentially wrapped by a connection pool
	 * @return the underlying native JDBC CallableStatement, if possible
	 * @throws SQLException if thrown by JDBC methods
	 */
	CallableStatement getNativeCallableStatement(CallableStatement cs) throws SQLException;

	/**
	 * Retrieve the underlying native JDBC ResultSet for the given statement.
	 * Supposed to return the given ResultSet if not capable of unwrapping.
	 * @param rs the ResultSet handle, potentially wrapped by a connection pool
	 * @return the underlying native JDBC ResultSet, if possible
	 * @throws SQLException if thrown by JDBC methods
	 */
	ResultSet getNativeResultSet(ResultSet rs) throws SQLException;

}
