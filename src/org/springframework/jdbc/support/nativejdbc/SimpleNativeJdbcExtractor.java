package org.springframework.jdbc.support.nativejdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Simple implementation of the NativeJdbcExtractor interface.
 * Assumes a pool that just wraps Connections but not Statements/ResultSets:
 * In this case, the underlying native Connection can be retrieved by simply
 * opening a Statement and invoking Statement.getConnection. All other JDBC
 * objects will be returned as passed in.
 *
 * <p>Known to work with Resin 2.1 and C3P0, but should work with any pool
 * that does not wrap Statements and ResultSets, or at least returns the
 * native Connection on Statement.getConnection.
 *
 * @author Juergen Hoeller
 * @since 05.12.2003
 */
public class SimpleNativeJdbcExtractor implements NativeJdbcExtractor {

	private boolean nativeConnectionNecessaryForNativeStatements = false;

	/**
	 * Set whether it is necessary to work on the native Connection to
	 * receive native Statements and ResultSets. Default is false.
	 * <p>This makes sense if you need to work with native Statements/ResultSets
	 * from a pool that does not allow to extract the native JDBC objects from
	 * its wrappers but returns the native Connection on Statement.getConnection,
	 * like C3P0.
	 */
	public void setNativeConnectionNecessaryForNativeStatements(boolean nativeConnectionNecessary) {
		this.nativeConnectionNecessaryForNativeStatements = nativeConnectionNecessary;
	}

	/**
	 * Return whether it is necessary to work on the native Connection to
	 * receive native Statements and ResultSets.
	 */
	public boolean isNativeConnectionNecessaryForNativeStatements() {
		return nativeConnectionNecessaryForNativeStatements;
	}

	public Connection getNativeConnection(Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		try {
			return stmt.getConnection();
		}
		finally {
			stmt.close();
		}
	}

	public Connection getNativeConnectionFromStatement(Statement stmt) throws SQLException {
		return stmt.getConnection();
	}

	public Statement getNativeStatement(Statement stmt) {
		return stmt;
	}

	public PreparedStatement getNativePreparedStatement(PreparedStatement ps) {
		return ps;
	}

	public CallableStatement getNativeCallableStatement(CallableStatement cs) {
		return cs;
	}

	public ResultSet getNativeResultSet(ResultSet rs) throws SQLException {
		return rs;
	}

}
