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

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
 
/**
 * Helper class that provides static methods to obtain JDBC Connections from
 * a DataSource, and to close Connections if necessary. Has special support for
 * Spring-managed connections, e.g. for use with DataSourceTransactionManager.
 *
 * <p>Used internally by JdbcTemplate, JDBC operation objects and the JDBC
 * DataSourceTransactionManager. Can also be used directly in application code.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getConnection
 * @see #closeConnectionIfNecessary
 * @see DataSourceTransactionManager
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.object.RdbmsOperation
 */
public abstract class DataSourceUtils {

	/**
	 * Order value for TransactionSynchronization objects that clean up
	 * JDBC Connections.
	 */
	public static final int CONNECTION_SYNCHRONIZATION_ORDER = 1000;

	private static final Log logger = LogFactory.getLog(DataSourceUtils.class);


	/**
	 * Get a Connection from the given DataSource. Changes any SQL exception into
	 * the Spring hierarchy of unchecked generic data access exceptions, simplifying
	 * calling code and making any exception that is thrown more meaningful.
	 * <p>Is aware of a corresponding Connection bound to the current thread, for example
	 * when using DataSourceTransactionManager. Will bind a Connection to the thread
	 * if transaction synchronization is active (e.g. if in a JTA transaction).
	 * @param dataSource DataSource to get Connection from
	 * @return a JDBC Connection from this DataSource
	 * @throws org.springframework.jdbc.CannotGetJdbcConnectionException
	 * if the attempt to get a Connection failed
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager
	 * @see DataSourceTransactionManager
	 */
	public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
		return getConnection(dataSource, true);
	}

	/**
	 * Get a Connection from the given DataSource. Changes any SQL exception into
	 * the Spring hierarchy of unchecked generic data access exceptions, simplifying
	 * calling code and making any exception that is thrown more meaningful.
	 * <p>Is aware of a corresponding Connection bound to the current thread, for example
	 * when using DataSourceTransactionManager. Will bind a Connection to the thread
	 * if transaction synchronization is active (e.g. if in a JTA transaction).
	 * @param dataSource DataSource to get Connection from
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
	public static Connection getConnection(DataSource dataSource, boolean allowSynchronization)
	    throws CannotGetJdbcConnectionException {
		try {
			return doGetConnection(dataSource, allowSynchronization);
		}
		catch (SQLException ex) {
			throw new CannotGetJdbcConnectionException("Could not get JDBC connection", ex);
		}
	}

	/**
	 * Actually get a JDBC Connection for the given DataSource.
	 * Same as getConnection, but throwing the original SQLException.
	 * <p>Directly accessed by TransactionAwareDataSourceProxy.
	 * @param dataSource DataSource to get Connection from
	 * @param allowSynchronization if a new JDBC Connection is supposed to be
	 * registered with transaction synchronization (if synchronization is active).
	 * This will always be true for typical data access code.
	 * @return a JDBC Connection from this DataSource
	 * @throws SQLException if thrown by JDBC methods
	 * @see #getConnection(DataSource, boolean)
	 * @see TransactionAwareDataSourceProxy
	 */
	protected static Connection doGetConnection(DataSource dataSource, boolean allowSynchronization)
			throws SQLException {
		Assert.notNull(dataSource, "No DataSource specified");

		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		if (conHolder != null) {
			conHolder.requested();
			return conHolder.getConnection();
		}

		logger.debug("Opening JDBC connection");
		Connection con = dataSource.getConnection();
		if (allowSynchronization && TransactionSynchronizationManager.isSynchronizationActive()) {
			logger.debug("Registering transaction synchronization for JDBC connection");
			// Use same Connection for further JDBC actions within the transaction.
			// Thread-bound object will get removed by synchronization at transaction completion.
			conHolder = new ConnectionHolder(con);
			TransactionSynchronizationManager.bindResource(dataSource, conHolder);
			TransactionSynchronizationManager.registerSynchronization(new ConnectionSynchronization(conHolder, dataSource));
			conHolder.requested();
		}

		return con;
	}

	/**
	 * Prepare the given Connection with the given transaction semantics.
	 * @param con the Connection to prepare
	 * @param definition the transaction definition to apply
	 * @return the previous isolation level, if any
	 * @throws SQLException if thrown by JDBC methods
	 * @see #resetConnectionAfterTransaction
	 */
	public static Integer prepareConnectionForTransaction(Connection con, TransactionDefinition definition)
			throws SQLException {
		Assert.notNull(con, "No connection specified");

		// Set read-only flag.
		if (definition != null && definition.isReadOnly()) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Setting JDBC connection [" + con + "] read-only");
				}
				con.setReadOnly(true);
			}
			catch (Exception ex) {
				// SQLException or UnsupportedOperationException
				// -> ignore, it's just a hint anyway.
				logger.debug("Could not set JDBC connection read-only", ex);
			}
		}

		// Apply specific isolation level, if any.
		Integer previousIsolationLevel = null;
		if (definition != null && definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
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
	 * @param con the Connection to reset
	 * @param previousIsolationLevel the isolation level to restore, if any
	 * @see #prepareConnectionForTransaction
	 */
	public static void resetConnectionAfterTransaction(Connection con, Integer previousIsolationLevel) {
		Assert.notNull(con, "No connection specified");
		try {
			// Reset transaction isolation to previous value, if changed for the transaction.
			if (previousIsolationLevel != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting isolation level of connection [" + con + "] to " + previousIsolationLevel);
				}
				con.setTransactionIsolation(previousIsolationLevel.intValue());
			}

			// Reset read-only flag.
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
	 * @param dataSource DataSource that the Connection came from
	 * @see java.sql.Statement#setQueryTimeout
	 */
	public static void applyTransactionTimeout(Statement stmt, DataSource dataSource) throws SQLException {
		Assert.notNull(stmt, "No statement specified");
		ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		if (holder != null && holder.hasTimeout()) {
			stmt.setQueryTimeout(holder.getTimeToLiveInSeconds());
		}
	}

	/**
	 * Close the given Connection if necessary, i.e. if it is not bound to the thread
	 * and it is not created by a SmartDataSource returning shouldClose=false.
	 * @param con Connection to close if necessary
	 * (if this is null, the call will be ignored)
	 * @param dataSource DataSource that the Connection came from
	 * @see SmartDataSource#shouldClose
	 */
	public static void closeConnectionIfNecessary(Connection con, DataSource dataSource) {
		try {
			doCloseConnectionIfNecessary(con, dataSource);
		}
		catch (SQLException ex) {
			logger.error("Could not close JDBC connection", ex);
		}
	}

	/**
	 * Actually close a JDBC Connection for the given DataSource.
	 * Same as closeConnectionIfNecessary, but throwing the original SQLException.
	 * <p>Directly accessed by TransactionAwareDataSourceProxy.
	 * @param con Connection to close if necessary
	 * (if this is null, the call will be ignored)
	 * @param dataSource DataSource that the Connection came from
	 * @throws SQLException if thrown by JDBC methods
	 * @see #closeConnectionIfNecessary
	 * @see TransactionAwareDataSourceProxy
	 */
	protected static void doCloseConnectionIfNecessary(Connection con, DataSource dataSource) throws SQLException {
		if (con == null) {
			return;
		}

		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		if (conHolder != null && connectionEquals(conHolder.getConnection(), con)) {
			// It's the transactional Connection: Don't close it.
			conHolder.released();
			return;
		}
		
		// Leave the Connection open only if the DataSource is our
		// special data source, and it wants the Connection left open.
		if (!(dataSource instanceof SmartDataSource) || ((SmartDataSource) dataSource).shouldClose(con)) {
			logger.debug("Closing JDBC connection");
			con.close();
		}
	}

	/**
	 * Return whether the given two Connections are equal, asking the target
	 * Connection in case of a proxy. Used to detect equality even if the
	 * user passed in a raw target Connection while the held one is a proxy.
	 * @param heldCon the held Connection (potentially a proxy)
	 * @param passedInCon the Connection passed-in by the user
	 * (potentially a target Connection without proxy)
	 * @see #getTargetConnection
	 */
	private static boolean connectionEquals(Connection heldCon, Connection passedInCon) {
		// Explicitly check for identity too: for Connection handles that do not implement
		// "equals" properly, such as the ones Commons DBCP exposes).
		return (heldCon == passedInCon || heldCon.equals(passedInCon) ||
				getTargetConnection(heldCon).equals(passedInCon));
	}

	/**
	 * Return the innermost target Connection of the given Connection. If the given
	 * Connection is a proxy, it will be unwrapped until a non-proxy Connection is
	 * found. Else, the passed-in Connection will be returned as-is.
	 * @param con the Connection proxy to unwrap
	 * @return the innermost target Connection, or the passed-in one if no proxy
	 * @see ConnectionProxy#getTargetConnection
	 */
	public static Connection getTargetConnection(Connection con) {
		Connection conToUse = con;
		while (conToUse instanceof ConnectionProxy) {
			conToUse = ((ConnectionProxy) conToUse).getTargetConnection();
		}
		return conToUse;
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

		public int getOrder() {
			return CONNECTION_SYNCHRONIZATION_ORDER;
		}

		public void suspend() {
			TransactionSynchronizationManager.unbindResource(this.dataSource);
		}

		public void resume() {
			TransactionSynchronizationManager.bindResource(this.dataSource, this.connectionHolder);
		}

		public void beforeCompletion() {
			// Release Connection early if the holder is not open anymore
			// (i.e. not used by another resource like a Hibernate Session
			// that has its own cleanup via transaction synchronization),
			// to avoid issues with strict JTA implementations that expect
			// the close call before transaction completion.
			if (!this.connectionHolder.isOpen()) {
				TransactionSynchronizationManager.unbindResource(this.dataSource);
				closeConnectionIfNecessary(this.connectionHolder.getConnection(), this.dataSource);
			}
		}

		public void afterCompletion(int status) {
			// If we haven't closed the Connection in beforeCompletion,
			// close it now. The holder might have been used for other
			// cleanup in the meantime, for example by a Hibernate Session.
			if (TransactionSynchronizationManager.hasResource(this.dataSource)) {
				TransactionSynchronizationManager.unbindResource(this.dataSource);
				closeConnectionIfNecessary(this.connectionHolder.getConnection(), this.dataSource);
			}
		}
	}

}
