package org.springframework.jdbc.datasource;

import java.sql.Connection;

import org.springframework.util.ExpiringObject;

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
public class ConnectionHolder extends ExpiringObject {

	private final Connection connection;

	private boolean rollbackOnly;

	public ConnectionHolder(Connection connection) {
		this.connection = connection;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setRollbackOnly() {
		this.rollbackOnly = true;
	}

	public boolean isRollbackOnly() {
		return rollbackOnly;
	}

}
