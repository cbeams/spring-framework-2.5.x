/*
 * Copyright 2002-2004 the original author or authors.
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

	private final ConnectionHandle connectionHandle;

	public ConnectionHolder(ConnectionHandle connectionHandle) {
		this.connectionHandle = connectionHandle;
	}

	public ConnectionHolder(Connection connection) {
		this.connectionHandle = new SimpleConnectionHandle(connection);
	}

	public ConnectionHandle getConnectionHandle() {
		return connectionHandle;
	}

	public Connection getConnection() {
		return this.connectionHandle.getConnection();
	}

}
