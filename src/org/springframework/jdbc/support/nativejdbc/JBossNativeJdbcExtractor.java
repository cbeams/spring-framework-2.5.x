package org.springframework.jdbc.support.nativejdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jboss.resource.adapter.jdbc.WrappedCallableStatement;
import org.jboss.resource.adapter.jdbc.WrappedConnection;
import org.jboss.resource.adapter.jdbc.WrappedPreparedStatement;
import org.jboss.resource.adapter.jdbc.WrappedStatement;

/**
 * Implementation of the NativeJdbcExtractor interface for the JBoss 3.2
 * connection pool. Returns the underlying native Connection, Statement,
 * etc to application code instead of JBoss' wrapper implementations.
 * The returned JDBC classes can then safely be cast, e.g. to OracleResultSet.
 *
 * <p>This NativeJdbcExtractor can be set just to <i>allow</i> working with
 * a JBoss connection pool: If a given object is not a JBoss wrapper,
 * it will be returned as-is.
 *
 * @author Juergen Hoeller
 * @since 03.01.2004
 * @see org.springframework.jdbc.core.JdbcTemplate#setNativeJdbcExtractor
 */
public class JBossNativeJdbcExtractor implements NativeJdbcExtractor {

	public boolean isNativeConnectionNecessaryForNativeStatements() {
		return false;
	}

	public boolean isNativeConnectionNecessaryForNativePreparedStatements() {
		return false;
	}

	public boolean isNativeConnectionNecessaryForNativeCallableStatements() {
		return false;
	}

	public Connection getNativeConnection(Connection con) throws SQLException {
		if (con instanceof WrappedConnection) {
			return ((WrappedConnection) con).getUnderlyingConnection();
		};
		return con;
	}

	public Connection getNativeConnectionFromStatement(Statement stmt) throws SQLException {
		return getNativeConnection(stmt.getConnection());
	}

	public Statement getNativeStatement(Statement stmt) throws SQLException {
		if (stmt instanceof WrappedStatement) {
			return ((WrappedStatement) stmt).getUnderlyingStatement();
		}
		return stmt;
	}

	public PreparedStatement getNativePreparedStatement(PreparedStatement ps) throws SQLException {
		if (ps instanceof WrappedPreparedStatement) {
			return (PreparedStatement) ((WrappedPreparedStatement) ps).getUnderlyingStatement();
		}
		return ps;
	}

	public CallableStatement getNativeCallableStatement(CallableStatement cs) throws SQLException {
		if (cs instanceof WrappedStatement) {
			return (CallableStatement) ((WrappedCallableStatement) cs).getUnderlyingStatement();
		}
		return cs;
	}

	public ResultSet getNativeResultSet(ResultSet rs) throws SQLException {
		return rs;
	}

}
