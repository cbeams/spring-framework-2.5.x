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

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * PlatformTransactionManager implementation for a single JDBC DataSource.
 * Binds a JDBC connection from the specified DataSource to the thread,
 * potentially allowing for one thread connection per data source.
 *
 * <p>Application code is required to retrieve the JDBC Connection via
 * <code>DataSourceUtils.getConnection(DataSource)</code> instead of J2EE's standard
 * <code>DataSource.getConnection()</code>. This is recommended anyway, as it throws
 * unchecked org.springframework.dao exceptions instead of checked SQLException.
 * All framework classes like JdbcTemplate use this strategy implicitly.
 * If not used with this transaction manager, the lookup strategy behaves exactly
 * like the common one - it can thus be used in any case.
 *
 * <p>Alternatively, you can also allow application code to work with the standard
 * J2EE lookup pattern <code>DataSource.getConnection()</code>, for example for
 * legacy code that is not aware of Spring at all. In that case, define a
 * TransactionAwareDataSourceProxy for your target DataSource, and pass that proxy
 * DataSource to your DAOs, which will automatically participate in Spring-managed
 * transactions through it. Note that DataSourceTransactionManager still needs to
 * be wired with the target DataSource, driving transactions for it.
 *
 * <p>Supports custom isolation levels, and timeouts that get applied as
 * appropriate JDBC statement query timeouts. To support the latter,
 * application code must either use JdbcTemplate or call
 * <code>DataSourceUtils.applyTransactionTimeout</code> for each created statement.
 *
 * <p>On JDBC 3.0, this transaction manager supports nested transactions via JDBC
 * 3.0 Savepoints. The "nestedTransactionAllowed" flag defaults to true, as nested
 * transactions work without restrictions on JDBC drivers that support Savepoints
 * (like Oracle).
 *
 * <p>This implementation can be used instead of JtaTransactionManager in the single
 * resource case, as it does not require the container to support JTA: typically,
 * in combination with a locally defined JDBC DataSource like a Jakarta Commons DBCP
 * connection pool. Switching between this local strategy and a JTA environment is
 * just a matter of configuration, if you stick to the required connection lookup
 * pattern. Note that JTA does not support custom isolation levels!
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see #setNestedTransactionAllowed
 * @see java.sql.Savepoint
 * @see DataSourceUtils#getConnection
 * @see DataSourceUtils#applyTransactionTimeout
 * @see DataSourceUtils#closeConnectionIfNecessary
 * @see TransactionAwareDataSourceProxy
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public class DataSourceTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {

	private DataSource dataSource;


	/**
	 * Create a new DataSourceTransactionManager instance.
	 * A DataSource has to be set to be able to use it.
	 * @see #setDataSource
	 */
	public DataSourceTransactionManager() {
		setNestedTransactionAllowed(true);
	}

	/**
	 * Create a new DataSourceTransactionManager instance.
	 * @param dataSource JDBC DataSource to manage transactions for
	 */
	public DataSourceTransactionManager(DataSource dataSource) {
		this();
		this.dataSource = dataSource;
		afterPropertiesSet();
	}

	/**
	 * Set the JDBC DataSource that this instance should manage transactions for.
	 * <p>This will typically be a locally defined DataSource, for example a
	 * Jakarta Commons DBCP connection pool. Alternatively, you can also drive
	 * transactions for a non-XA J2EE DataSource fetched from JNDI. For an XA
	 * DataSource, use JtaTransactionManager.
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Return the JDBC DataSource that this instance manages transactions for.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	public void afterPropertiesSet() {
		if (this.dataSource == null) {
			throw new IllegalArgumentException("dataSource is required");
		}
	}


	protected Object doGetTransaction() {
		DataSourceTransactionObject txObject = new DataSourceTransactionObject();
		txObject.setSavepointAllowed(isNestedTransactionAllowed());
		ConnectionHolder conHolder =
		    (ConnectionHolder) TransactionSynchronizationManager.getResource(this.dataSource);
		txObject.setConnectionHolder(conHolder);
		return txObject;
	}

	protected boolean isExistingTransaction(Object transaction) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
		// consider a pre-bound Connection as transaction
		return (txObject.getConnectionHolder() != null);
	}

	/**
	 * This implementation sets the isolation level but ignores the timeout.
	 */
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;

		if (logger.isDebugEnabled()) {
			logger.debug("Opening new connection for JDBC transaction");
		}
		Connection con = DataSourceUtils.getConnection(this.dataSource, false);

		txObject.setConnectionHolder(new ConnectionHolder(con));
		txObject.getConnectionHolder().setSynchronizedWithTransaction(true);

		try {
			Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
			txObject.setPreviousIsolationLevel(previousIsolationLevel);

			// Switch to manual commit if necessary. This is very expensive in some JDBC drivers,
			// so we don't want to do it unnecessarily (for example if we're configured
			// Commons DBCP to set it already).
			if (con.getAutoCommit()) {
				txObject.setMustRestoreAutoCommit(true);
				if (logger.isDebugEnabled()) {
					logger.debug("Switching JDBC connection [" + con + "] to manual commit");
				}
				con.setAutoCommit(false);
			}

			if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
				txObject.getConnectionHolder().setTimeoutInSeconds(definition.getTimeout());
			}
			TransactionSynchronizationManager.bindResource(getDataSource(), txObject.getConnectionHolder());
		}

		catch (SQLException ex) {
			DataSourceUtils.closeConnectionIfNecessary(con, this.dataSource);
			throw new CannotCreateTransactionException("Could not configure JDBC connection for transaction", ex);
		}
	}

	protected Object doSuspend(Object transaction) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
		txObject.setConnectionHolder(null);
		return TransactionSynchronizationManager.unbindResource(this.dataSource);
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		ConnectionHolder conHolder = (ConnectionHolder) suspendedResources;
		TransactionSynchronizationManager.bindResource(this.dataSource, conHolder);
	}

	protected void doCommit(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		Connection con = txObject.getConnectionHolder().getConnection();
		if (status.isDebug()) {
			logger.debug("Committing JDBC transaction on connection [" + con + "]");
		}
		try {
			con.commit();
		}
		catch (SQLException ex) {
			throw new TransactionSystemException("Could not commit JDBC transaction", ex);
		}
	}

	protected void doRollback(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		Connection con = txObject.getConnectionHolder().getConnection();
		if (status.isDebug()) {
			logger.debug("Rolling back JDBC transaction on connection [" + con + "]");
		}
		try {
			con.rollback();
		}
		catch (SQLException ex) {
			throw new TransactionSystemException("Could not roll back JDBC transaction", ex);
		}
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Setting JDBC transaction [" + txObject.getConnectionHolder().getConnection() +
									 "] rollback-only");
		}
		txObject.getConnectionHolder().setRollbackOnly();
	}

	protected void doCleanupAfterCompletion(Object transaction) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;

		// remove the connection holder from the thread
		TransactionSynchronizationManager.unbindResource(this.dataSource);
		txObject.getConnectionHolder().clear();

		// reset connection
		Connection con = txObject.getConnectionHolder().getConnection();
		try {
			if (txObject.getMustRestoreAutoCommit()) {
				con.setAutoCommit(true);
			}
			DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
		}
		catch (SQLException ex) {
			logger.info("Could not reset JDBC connection after transaction", ex);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Closing JDBC connection [" + con + "] after transaction");
		}
		DataSourceUtils.closeConnectionIfNecessary(con, this.dataSource);
	}

}
