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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Proxy for a target DataSource that is aware of Spring-managed transactions.
 * Similar to a transactional JNDI DataSource as provided by a J2EE server.
 *
 * <p>Delegates to DataSourceUtils for automatically participating in thread-bound
 * transactions, for example managed by DataSourceTransactionManager.
 * getConnection calls and close calls on returned Connections will behave properly
 * within a transaction, i.e. always work on the transactional Connection.
 * If not within a transaction, normal DataSource behavior applies.
 *
 * <p>This proxy allows data access code to work with the plain JDBC API and still
 * participate in Spring-managed transactions, similar to JDBC code in a J2EE/JTA
 * environment. However, if possible, use Spring's DataAccessUtils, JdbcTemplate or
 * JDBC operation objects to get transaction participation even without a proxy for
 * the target DataSource, avoiding the need to define such a proxy in the first place.
 *
 * <p><b>NOTE:</b> This DataSource proxy needs to return wrapped Connections to
 * handle close calls on them properly. Therefore, the returned Connections cannot
 * be cast to a native JDBC Connection type like OracleConnection, respectively to
 * a connection pool implementation type.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see DataSourceUtils#doGetConnection
 * @see DataSourceUtils#doCloseConnectionIfNecessary
 */
public class TransactionAwareDataSourceProxy extends DelegatingDataSource {

	/**
	 * Create a new TransactionAwareDataSourceProxy.
	 * @see #setTargetDataSource
	 */
	public TransactionAwareDataSourceProxy() {
	}

	/**
	 * Create a new TransactionAwareDataSourceProxy.
	 * @param targetDataSource the target DataSource
	 */
	public TransactionAwareDataSourceProxy(DataSource targetDataSource) {
		setTargetDataSource(targetDataSource);
		afterPropertiesSet();
	}

	/**
	 * Delegate to DataSourceUtils for automatically participating in Spring-managed
	 * transactions. Throws the original SQLException, if any.
	 * @return a transactional Connection if any, a new one else
	 * @see DataSourceUtils#doGetConnection
	 */
	public Connection getConnection() throws SQLException {
		Connection con = DataSourceUtils.doGetConnection(getTargetDataSource(), true);
		return getTransactionAwareConnectionProxy(con, getTargetDataSource());
	}

	/**
	 * Wrap the given Connection with a proxy that delegates every method call to it
	 * but delegates close calls to DataSourceUtils.
	 * @param target the original Connection to wrap
	 * @return the wrapped Connection
	 * @see DataSourceUtils#doCloseConnectionIfNecessary
	 */
	protected Connection getTransactionAwareConnectionProxy(Connection target, DataSource ds) {
		return (Connection) Proxy.newProxyInstance(
				ConnectionProxy.class.getClassLoader(),
				new Class[] {ConnectionProxy.class},
				new TransactionAwareInvocationHandler(target, ds));
	}


	/**
	 * Invocation handler that delegates close calls on JDBC Connections
	 * to DataSourceUtils for being aware of thread-bound transactions.
	 */
	private static class TransactionAwareInvocationHandler implements InvocationHandler {

		private static final String GET_TARGET_CONNECTION_METHOD_NAME = "getTargetConnection";

		private static final String CONNECTION_CLOSE_METHOD_NAME = "close";

		private final Connection target;

		private final DataSource dataSource;

		private TransactionAwareInvocationHandler(Connection target, DataSource dataSource) {
			this.target = target;
			this.dataSource = dataSource;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName().equals(GET_TARGET_CONNECTION_METHOD_NAME)) {
				return this.target;
			}
			if (method.getName().equals(CONNECTION_CLOSE_METHOD_NAME)) {
				if (this.dataSource != null) {
					DataSourceUtils.doCloseConnectionIfNecessary(this.target, this.dataSource);
				}
				return null;
			}
			try {
				return method.invoke(this.target, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
