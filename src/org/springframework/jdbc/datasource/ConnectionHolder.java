package org.springframework.jdbc.datasource;

import java.sql.Connection;

import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * Connection holder, wrapping a JDBC Connection.
 * Features rollback-only support for nested JDBC transactions.
 *
 * <p>DataSourceTransactionManager binds instances of this class
 * to the thread, for a given DataSource.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 06.05.2003
 * @see DataSourceTransactionManager
 * @see DataSourceTransactionObject
 * @see DataSourceUtils
 */
public class ConnectionHolder extends ResourceHolderSupport {

	private final Connection connection;

	public ConnectionHolder(Connection connection) {
		this.connection = connection;
	}

	public Connection getConnection() {
		return connection;
	}

}
