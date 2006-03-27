package org.springframework.jdbc.datasource;

import java.sql.PreparedStatement;

/**
 * Subinterface of  PreparedStatement to be implemented by prepared statement proxies.
 * Allows access to the target statement.
 *
 * <p>Can be checked for when needing to cast to a native PreparedStatement
 * like OraclePreparedStatement. Spring's NativeJdbcExtractorAdapter automatically
 * detects such proxies before delegating to the actual unwrapping for a
 * specific connection pool.
 *
 * @author Thomas Risberg
 * @since 2.0
 * @see org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractorAdapter#getNativePreparedStatement
 */
public interface PreparedStatementProxy extends PreparedStatement {

    /**
     * Return the target statement of this proxy.
     * <p>This will typically either be the native JDBC Statement
     * or a wrapper from a connection pool.
     */
    PreparedStatement getTargetPreparedStatement();

}
