/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jdbc.datasource;

import java.sql.Connection;

import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;

/**
 * Connection holder, wrapping a JDBC Connection.
 * DataSourceTransactionManager binds instances of this class
 * to the thread, for a given DataSource.
 *
 * <p>Inherits rollback-only support for nested JDBC transactions
 * and reference count functionality from the base class.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 06.05.2003
 * @see DataSourceTransactionManager
 * @see DataSourceUtils
 */
public class ConnectionHolder extends ResourceHolderSupport {

	private ConnectionHandle connectionHandle;

	private Connection currentConnection;

	private boolean transactionActive = false;


	/**
	 * Create a new ConnectionHolder for the given ConnectionHandle.
	 * @param connectionHandle the ConnectionHandle to hold
	 */
	public ConnectionHolder(ConnectionHandle connectionHandle) {
		Assert.notNull(connectionHandle, "ConnectionHandle must not be null");
		this.connectionHandle = connectionHandle;
	}

	/**
	 * Create a new ConnectionHolder for the given JDBC Connection,
	 * wrapping it with a SimpleConnectionHandle.
	 * @param connection the JDBC Connection to hold
	 * @see SimpleConnectionHandle
	 */
	public ConnectionHolder(Connection connection) {
		this.connectionHandle = new SimpleConnectionHandle(connection);
	}


	/**
	 * Return the ConnectionHandle held by this ConnectionHolder.
	 */
	public ConnectionHandle getConnectionHandle() {
		return connectionHandle;
	}

	/**
	 * Return whether this holder currently has a Connection.
	 */
	protected boolean hasConnection() {
		return (this.connectionHandle != null);
	}

	/**
	 * Set whether this holder represents an active transaction.
	 */
	protected void setTransactionActive(boolean transactionActive) {
		this.transactionActive = transactionActive;
	}

	/**
	 * Return whether this holder represents an active transaction.
	 */
	protected boolean isTransactionActive() {
		return this.transactionActive;
	}


	/**
	 * Override the existing Connection handle with the given Connection.
	 * Reset the handle if given <code>null</code>.
	 * <p>Used for releasing the Connection on suspend (with a <code>null</code>
	 * argument) and setting a fresh Connection on resume.
	 */
	protected void setConnection(Connection connection) {
		if (this.currentConnection != null) {
			this.connectionHandle.releaseConnection(this.currentConnection);
			this.currentConnection = null;
		}
		if (connection != null) {
			this.connectionHandle = new SimpleConnectionHandle(connection);
		}
		else {
			this.connectionHandle = null;
		}
	}

	/**
	 * Return the current Connection held by this ConnectionHolder.
	 * <p>This will be the same Connection until <code>released</code>
	 * gets called on the ConnectionHolder, which will reset the
	 * held Connection, fetching a new Connection on demand.
	 * @see ConnectionHandle#getConnection()
	 * @see #released()
	 */
	public Connection getConnection() {
		Assert.notNull(this.connectionHandle, "Active Connection is required");
		if (this.currentConnection == null) {
			this.currentConnection = this.connectionHandle.getConnection();
		}
		return this.currentConnection;
	}

	/**
	 * Releases the current Connection held by this ConnectionHolder.
	 * <p>This is necessary for ConnectionHandles that expect "Connection borrowing",
	 * where each returned Connection is only temporarily leased and needs to be
	 * returned once the data operation is done, to make the Connection available
	 * for other operations within the same transaction. This is the case with
	 * JDO 2.0 DataStoreConnections, for example.
	 * @see org.springframework.orm.jdo.DefaultJdoDialect#getJdbcConnection
	 */
	public void released() {
		super.released();
		if (this.currentConnection != null) {
			this.connectionHandle.releaseConnection(this.currentConnection);
			this.currentConnection = null;
		}
	}


	public void clear() {
		super.clear();
		this.transactionActive = false;
	}

}
