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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;

/**
 * DataSource implementation that delegates all calls to a given target DataSource.
 * Abstract because it is meant to be to be subclasses, overriding specific methods
 * that should not simply delegate to the target.
 * @author Juergen Hoeller
 * @since 19.07.2004
 */
public abstract class DelegatingDataSource implements DataSource, InitializingBean {

	private DataSource targetDataSource;

	/**
	 * Set the target DataSource that this DataSource should delegate to.
	 */
	public void setTargetDataSource(DataSource targetDataSource) {
		this.targetDataSource = targetDataSource;
	}

	/**
	 * Return the target DataSource that this DataSource should delegate to.
	 */
	public DataSource getTargetDataSource() {
		return targetDataSource;
	}

	public void afterPropertiesSet() {
		if (this.targetDataSource == null) {
			throw new IllegalArgumentException("targetDataSource is required");
		}
	}


	public Connection getConnection() throws SQLException {
		return getTargetDataSource().getConnection();
	}

	public Connection getConnection(String username, String password) throws SQLException {
		return getTargetDataSource().getConnection(username, password);
	}

	public int getLoginTimeout() throws SQLException {
		return this.targetDataSource.getLoginTimeout();
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		this.targetDataSource.setLoginTimeout(seconds);
	}

	public PrintWriter getLogWriter() throws SQLException {
		return this.targetDataSource.getLogWriter();
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		this.targetDataSource.setLogWriter(out);
	}

}
