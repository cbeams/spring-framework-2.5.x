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

/**
 * Simple implementation of the ConnectionHandle interface,
 * containing a given JDBC Connection.
 * @author Juergen Hoeller
 * @since 14.06.2004
 */
public class SimpleConnectionHandle implements ConnectionHandle {

	private final Connection connection;

	/**
	 * Create a new SimpleConnectionHandle for the given Connection.
	 * @param connection the JDBC Connection
	 */
	public SimpleConnectionHandle(Connection connection) {
		this.connection = connection;
	}

	public Connection getConnection() {
		return connection;
	}

}
