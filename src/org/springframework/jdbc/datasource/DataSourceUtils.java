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
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jndi.JndiLocatorSupport;
import org.springframework.jndi.JndiTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
 
/**
 * Helper class that provides static methods to obtain connections from
 * JNDI and close connections if necessary. Has support for thread-bound
 * connections, e.g. for use with DataSourceTransactionManager.
 *
 * <p>Note: The <code>getDataSourceFromJndi</code> methods are targetted at
 * applications that do not use a Spring BeanFactory. With the latter, it is
 * preferable to preconfigure your beans or even JdbcTemplate instances in the
 * factory: JndiObjectFactoryBean can be used to fetch a DataSource from JNDI
 * and pass the DataSource bean reference to other beans. Switching to another
 * DataSource is just a matter of configuration then: For example, you can
 * replace the definition of the JndiObjectFactoryBean with a local DataSource
 * (like a Commons DBCP BasicDataSource).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getDataSourceFromJndi
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see #getConnection
 * @see DataSourceTransactionManager
 */
public abstract class DataSourceUtils {

	private static final Log logger = LogFactory.getLog(DataSourceUtils.class);

	/**
	 * Look up the specified DataSource in JNDI, assuming that the lookup
	 * occurs in a J2EE container, i.e. adding the prefix "java:comp/env/"
	 * to the JNDI name if it doesn't already contain it.
	 * <p>Use getDataSourceFromJndi(jndiName,false) in case of a custom JNDI name.
	 * @param jndiName jndiName of the DataSource
	 * @return the DataSource
	 * @throws org.springframework.jdbc.CannotGetJdbcConnectionException
	 * if the data source cannot be located
	 * @deprecated in favor of managing a DataSource via dependency injection,
	 * i.e. using a JndiObjectFactoryBean for a JNDI DataSource and pass a
	 * bean reference to a setDataSource(DataSource) method or the like
	 * @see #getDataSourceFromJndi(String, boolean)
	 * @see org.springframework.jndi.JndiObjectFactoryBean
	 */
	public static DataSource getDataSourceFromJndi(String jndiName)
	    throws CannotGetJdbcConnectionException {
		return getDataSourceFromJndi(jndiName, true);
	}

	/**
	 * Look up the specified DataSource in JNDI, explicitly specifying
	 * if the lookup occurs in a J2EE container.
	 * @param jndiName jndiName of the DataSource
	 * @param resourceRef if the lookup occurs in a J2EE container, i.e. if the prefix
	 * "java:comp/env/" needs to be added if the JNDI name doesn't already contain it.
	 * @return the DataSource
	 * @throws org.springframework.jdbc.CannotGetJdbcConnectionException
	 * if the data source cannot be located
	 * @deprecated in favor of managing a DataSource via dependency injection,
	 * i.e. using a JndiObjectFactoryBean for a JNDI DataSource and pass a
	 * bean reference to a setDataSource(DataSource) method or the like
	 * @see org.springframework.jndi.JndiObjectFactoryBean
	 */
	public static DataSource getDataSourceFromJndi(String jndiName, boolean resourceRef)
	    throws CannotGetJdbcConnectionException {
		if (jndiName == null || "".equals(jndiName)) {
			throw new IllegalArgumentException("jndiName must not be empty");
		}
		if (resourceRef && !jndiName.startsWith(JndiLocatorSupport.CONTAINER_PREFIX)) {
			jndiName = JndiLocatorSupport.CONTAINER_PREFIX + jndiName;
		}
		try {
			// Perform JNDI lookup to obtain resource manager connection factory
			return (DataSource) new JndiTemplate().lookup(jndiName);
		}
		catch (NamingException ex) {
			throw new CannotGetJdbcConnectionException(
					"Naming exception looking up JNDI data source [" + jndiName + "]", ex);
		}
	}

	/**
	 * Get a Connection from the given DataSource. Changes any SQL exception into
	 * the Spring hierarchy of unchecked generic data access exceptions, simplifying
	 * calling code and making any exception that is thrown more meaningful.
	 * <p>Is aware of a corresponding Connection bound to the current thread, for example
	 * when using DataSourceTransactionManager. Will bind a Connection to the thread
	 * if transaction synchronization is active (e.g. if in a JTA transaction).
	 * @param ds DataSource to get Connection from
	 * @return a JDBC Connection from this DataSource
	 * @throws org.springframework.jdbc.CannotGetJdbcConnectionException
	 * if the attempt to get a Connection failed
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager
	 * @see DataSourceTransactionManager
	 */
	public static Connection getConnection(DataSource ds) throws CannotGetJdbcConnectionException {
		return getConnection(ds, true);
	}

	/**
	 * Get a Connection from the given DataSource. Changes any SQL exception into
	 * the Spring hierarchy of unchecked generic data access exceptions, simplifying
	 * calling code and making any exception that is thrown more meaningful.
	 * <p>Is aware of a corresponding Connection bound to the current thread, for example
	 * when using DataSourceTransactionManager. Will bind a Connection to the thread
	 * if transaction synchronization is active (e.g. if in a JTA transaction).
	 * @param ds DataSource to get Connection from
	 * @param allowSynchronization if a new JDBC Connection is supposed to be
	 * registered with transaction synchronization (if synchronization is active).
	 * This will always be true for typical data access code.
	 * @return a JDBC Connection from this DataSource
	 * @throws org.springframework.jdbc.CannotGetJdbcConnectionException
	 * if the attempt to get a Connection failed
	 * @see #doGetConnection
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager
	 * @see DataSourceTransactionManager
	 */
	public static Connection getConnection(DataSource ds, boolean allowSynchronization)
	    throws CannotGetJdbcConnectionException {
		try {
			return doGetConnection(ds, allowSynchronization);
		}
		catch (SQLException ex) {
			throw new CannotGetJdbcConnectionException("Could not get JDBC connection", ex);
		}
	}

	/**
	 * Actually get a JDBC Connection for the given DataSource.
	 * Same as getConnection, but throwing the original SQLException.
	 * <p>Directly accessed by TransactionAwareDataSourceProxy.
	 * @throws SQLException if thrown by JDBC methods
	 * @see #getConnection(DataSource, boolean)
	 * @see TransactionAwareDataSourceProxy
	 */
	protected static Connection doGetConnection(DataSource ds, boolean allowSynchronization)
			throws SQLException {
		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(ds);
		if (conHolder != null) {
			return conHolder.getConnection();
		}
		Connection con = ds.getConnection();
		if (allowSynchronization && TransactionSynchronizationManager.isSynchronizationActive()) {
			logger.debug("Registering transaction synchronization for JDBC connection");
			// use same Connection for further JDBC actions within the transaction
			// thread object will get removed by synchronization at transaction completion
			conHolder = new ConnectionHolder(con);
			TransactionSynchronizationManager.bindResource(ds, conHolder);
			TransactionSynchronizationManager.registerSynchronization(new ConnectionSynchronization(conHolder, ds));
		}
		return con;
	}

	/**
	 * Prepare the given Connection with the given transaction semantics.
	 * @param con Connection to prepare
	 * @param definition the transaction definition to apply
	 * @return the previous isolation level, if any
	 * @throws SQLException if thrown by JDBC methods
	 * @see #resetConnectionAfterTransaction
	 */
	public static Integer prepareConnectionForTransaction(Connection con, TransactionDefinition definition)
			throws SQLException {

		// apply read-only
		if (definition.isReadOnly()) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Setting JDBC connection [" + con + "] read-only");
				}
				con.setReadOnly(true);
			}
			catch (Exception ex) {
				// SQLException or UnsupportedOperationException
				// -> ignore, it's just a hint anyway
				logger.debug("Could not set JDBC connection read-only", ex);
			}
		}

		// apply isolation level
		Integer previousIsolationLevel = null;
		if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			if (logger.isDebugEnabled()) {
				logger.debug("Changing isolation level of JDBC connection [" + con + "] to " +
						definition.getIsolationLevel());
			}
			previousIsolationLevel = new Integer(con.getTransactionIsolation());
			con.setTransactionIsolation(definition.getIsolationLevel());
		}

		return previousIsolationLevel;
	}

	/**
	 * Reset the given Connection after a transaction,
	 * regarding read-only flag and isolation level.
	 * @param con Conneciton to reset
	 * @param previousIsolationLevel the isolation level to restore, if any
	 * @see #prepareConnectionForTransaction
	 */
	public static void resetConnectionAfterTransaction(Connection con, Integer previousIsolationLevel) {
		try {
			// reset transaction isolation to previous value, if changed for the transaction
			if (previousIsolationLevel != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting isolation level of connection [" + con + "] to " + previousIsolationLevel);
				}
				con.setTransactionIsolation(previousIsolationLevel.intValue());
			}

			// reset read-only
			if (con.isReadOnly()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting read-only flag of connection [" + con + "]");
				}
				con.setReadOnly(false);
			}
		}
		catch (Exception ex) {
			logger.info("Could not reset JDBC connection after transaction", ex);
		}
	}

	/**
	 * Apply the current transaction timeout, if any,
	 * to the given JDBC Statement object.
	 * @param stmt the JDBC Statement object
	 * @param ds DataSource that the Connection came from
	 */
	public static void applyTransactionTimeout(Statement stmt, DataSource ds) throws SQLException {
		ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(ds);
		if (holder != null && holder.hasTimeout()) {
			stmt.setQueryTimeout(holder.getTimeToLiveInSeconds());
		}
	}

	/**
	 * Close the given Connection if necessary, i.e. if it is not bound to the thread
	 * and it is not created by a SmartDataSource returning shouldClose=false.
	 * @param con Connection to close if necessary
	 * (if this is null, the call will be ignored)
	 * @param ds DataSource that the Connection came from
	 * @see SmartDataSource#shouldClose
	 */
	public static void closeConnectionIfNecessary(Connection con, DataSource ds) {
		try {
			doCloseConnectionIfNecessary(con, ds);
		}
		catch (SQLException ex) {
			logger.error("Could not close JDBC connection", ex);
		}
	}

	/**
	 * Actually close a JDBC Connection for the given DataSource.
	 * Same as closeConnectionIfNecessary, but throwing the original SQLException.
	 * <p>Directly accessed by TransactionAwareDataSourceProxy.
	 * @throws SQLException if thrown by JDBC methods
	 * @see #closeConnectionIfNecessary
	 * @see TransactionAwareDataSourceProxy
	 */
	protected static void doCloseConnectionIfNecessary(Connection con, DataSource ds) throws SQLException {
		if (con == null || TransactionSynchronizationManager.hasResource(ds)) {
			return;
		}
		// Leave the Connection open only if the DataSource is our
		// special data source, and it wants the Connection left open.
		if (!(ds instanceof SmartDataSource) || ((SmartDataSource) ds).shouldClose(con)) {
			con.close();
		}
	}


	/**
	 * Callback for resource cleanup at the end of a non-native-JDBC transaction
	 * (e.g. when participating in a JTA transaction).
	 */
	private static class ConnectionSynchronization extends TransactionSynchronizationAdapter {

		private final ConnectionHolder connectionHolder;

		private final DataSource dataSource;

		private ConnectionSynchronization(ConnectionHolder connectionHolder, DataSource dataSource) {
			this.connectionHolder = connectionHolder;
			this.dataSource = dataSource;
		}

		public void suspend() {
			TransactionSynchronizationManager.unbindResource(this.dataSource);
		}

		public void resume() {
			TransactionSynchronizationManager.bindResource(this.dataSource, this.connectionHolder);
		}

		public void beforeCompletion() {
			TransactionSynchronizationManager.unbindResource(this.dataSource);
			closeConnectionIfNecessary(this.connectionHolder.getConnection(), this.dataSource);
		}
	}

}
