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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.util.Assert;

/**
 * Proxy for a target DataSource, adding awareness of Spring-managed transactions.
 * Similar to a transactional JNDI DataSource as provided by a J2EE server.
 *
 * <p>Data access code that should remain unaware of Spring's data access support
 * can work with this proxy to seamlessly participate in Spring-managed transactions.
 * Note that the transaction manager, for example DataSourceTransactionManager,
 * still needs to work with underlying DataSource, <i>not</i> with this proxy.
 *
 * <p><b>Make sure that TransactionAwareDataSourceProxy is the outermost DataSource
 * of a chain of DataSource proxies/adapters.</b> TransactionAwareDataSourceProxy
 * can delegate either directly to the target connection pool or to some intermediate
 * proxy/adapter like LazyConnectionDataSourceProxy or UserCredentialsDataSourceAdapter.
 *
 * <p>Delegates to DataSourceUtils for automatically participating in thread-bound
 * transactions, for example managed by DataSourceTransactionManager.
 * <code>getConnection</code> calls and <code>close<code> calls on returned Connections
 * will behave properly within a transaction, i.e. always work on the transactional
 * Connection. If not within a transaction, normal DataSource behavior applies.
 *
 * <p>This proxy allows data access code to work with the plain JDBC API and still
 * participate in Spring-managed transactions, similar to JDBC code in a J2EE/JTA
 * environment. However, if possible, use Spring's DataSourceUtils, JdbcTemplate or
 * JDBC operation objects to get transaction participation even without a proxy for
 * the target DataSource, avoiding the need to define such a proxy in the first place.
 *
 * <p>As a further effect, using a transaction-aware DataSource will apply remaining
 * transaction timeouts to all created JDBC (Prepared/Callable)Statement. This means
 * that all operations performed through standard JDBC will automatically participate
 * in Spring-managed transaction timeouts.
 *
 * <p><b>NOTE:</b> This DataSource proxy needs to return wrapped Connections to
 * handle close calls on them properly. Therefore, the returned Connections cannot
 * be cast to a native JDBC Connection type like OracleConnection, or to a
 * connection pool implementation type. Use a corresponding NativeJdbcExtractor
 * to retrieve the native JDBC Connection.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see javax.sql.DataSource#getConnection
 * @see java.sql.Connection#close
 * @see DataSourceUtils#doGetConnection
 * @see DataSourceUtils#doReleaseConnection
 * @see DataSourceUtils#applyTransactionTimeout
 * @see ConnectionProxy
 * @see org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor
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
	 * <p>The returned Connection handle implements the ConnectionProxy interface,
	 * allowing to retrieve the underlying target Connection.
	 * @return a transactional Connection if any, a new one else
	 * @see DataSourceUtils#doGetConnection
	 * @see ConnectionProxy#getTargetConnection
	 */
	public Connection getConnection() throws SQLException {
		Assert.state(getTargetDataSource() != null, "targetDataSource is required");
		Connection con = DataSourceUtils.doGetConnection(getTargetDataSource());
		return getTransactionAwareConnectionProxy(con, getTargetDataSource());
	}

	/**
	 * Wrap the given Connection with a proxy that delegates every method call to it
	 * but delegates <code>close</code> calls to DataSourceUtils.
	 * @param target the original Connection to wrap
	 * @param dataSource DataSource that the Connection came from
	 * @return the wrapped Connection
	 * @see java.sql.Connection#close()
	 * @see DataSourceUtils#doReleaseConnection
	 */
	protected Connection getTransactionAwareConnectionProxy(Connection target, DataSource dataSource) {
		return (Connection) Proxy.newProxyInstance(
				ConnectionProxy.class.getClassLoader(),
				new Class[] {ConnectionProxy.class},
				new TransactionAwareInvocationHandler(target, dataSource));
	}


	/**
	 * Invocation handler that delegates close calls on JDBC Connections
	 * to DataSourceUtils for being aware of thread-bound transactions.
	 */
	private static class TransactionAwareInvocationHandler implements InvocationHandler {

		private final Connection target;

		private final DataSource dataSource;

		public TransactionAwareInvocationHandler(Connection target, DataSource dataSource) {
			this.target = target;
			this.dataSource = dataSource;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on ConnectionProxy interface coming in...

			if (method.getName().equals("getTargetConnection")) {
				// Handle getTargetConnection method: return underlying Connection.
				return this.target;
			}
			else if (method.getName().equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			}
			else if (method.getName().equals("hashCode")) {
				// Use hashCode of Connection proxy.
				return new Integer(hashCode());
			}
			else if (method.getName().equals("close")) {
				// Handle close method: only close if not within a transaction.
				if (this.dataSource != null) {
					DataSourceUtils.doReleaseConnection(this.target, this.dataSource);
				}
				return null;
			}

			// Invoke method on target Connection.
			try {
				Object retVal = method.invoke(this.target, args);

				// If return value is a Statement, apply transaction timeout.
				// Applies to createStatement, prepareStatement, prepareCall.
				if (retVal instanceof Statement) {
					DataSourceUtils.applyTransactionTimeout((Statement) retVal, this.dataSource);
				}

				return retVal;
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
