package org.springframework.jdbc.datasource;

import java.sql.Connection;

/**
 * Subinterface of Connection to be implemented by connection proxies.
 * Allows access to the target connection.
 *
 * <p>Can be checked for when needing to cast to a native Connection
 * like OracleConnection. Spring's NativeJdbcExtractorAdapter automatically
 * detects such proxies before delegating to the actual unwrapping for a
 * specific connection pool.
 *
 * @author Juergen Hoeller
 * @since 22.07.2004
 * @see org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractorAdapter#getNativeConnection
 */
public interface ConnectionProxy extends Connection {

	/**
	 * Return the target connection of this proxy.
	 * <p>This will typically either be the native JDBC Connection
	 * or a wrapper from a connection pool.
	 */
	Connection getTargetConnection();

}
