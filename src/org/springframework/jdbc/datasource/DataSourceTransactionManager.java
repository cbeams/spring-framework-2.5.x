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
import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * PlatformTransactionManager implementation for single JDBC data sources.
 * Binds a JDBC connection from the specified data source to the thread,
 * potentially allowing for one thread connection per data source.
 *
 * <p>Application code is required to retrieve the JDBC connection via
 * DataSourceUtils.getConnection(DataSource) instead of J2EE's standard
 * DataSource.getConnection. This is recommended anyway, as it throws
 * unchecked org.springframework.dao exceptions instead of checked SQLException.
 * All framework classes like JdbcTemplate use this strategy implicitly.
 * If not used with this transaction manager, the lookup strategy
 * behaves exactly like the common one - it can thus be used in any case.
 *
 * <p>Supports custom isolation levels, and timeouts that get applied as
 * appropriate JDBC statement query timeouts. To support the latter,
 * application code must either use JdbcTemplate or call DataSourceUtils'
 * applyTransactionTimeout method for each created statement.
 *
 * <p>This implementation can be used instead of JtaTransactionManager
 * in the single resource case, as it does not require the container to
 * support JTA. Switching between both is just a matter of configuration,
 * if you stick to the required connection lookup pattern. Note that JTA
 * does not support custom isolation levels!
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see DataSourceUtils#getConnection
 * @see DataSourceUtils#applyTransactionTimeout
 * @see DataSourceUtils#closeConnectionIfNecessary
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @version $Id: DataSourceTransactionManager.java,v 1.14 2004-03-18 02:46:05 trisberg Exp $
 */
public class DataSourceTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {

	private DataSource dataSource;

	/**
	 * Create a new DataSourceTransactionManager instance.
	 * A DataSource has to be set to be able to use it.
	 * @see #setDataSource
	 */
	public DataSourceTransactionManager() {
	}

	/**
	 * Create a new DataSourceTransactionManager instance.
	 * @param dataSource DataSource to manage transactions for
	 */
	public DataSourceTransactionManager(DataSource dataSource) {
		this.dataSource = dataSource;
		afterPropertiesSet();
	}

	/**
	 * Set the J2EE DataSource that this instance should manage transactions for.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Return the J2EE DataSource that this instance manages transactions for.
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
		if (TransactionSynchronizationManager.hasResource(this.dataSource)) {
			ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(this.dataSource);
			return new DataSourceTransactionObject(holder);
		}
		else {
			return new DataSourceTransactionObject();
		}
	}

	protected boolean isExistingTransaction(Object transaction) {
		// standard DataSource -> check existence of thread connection
		return TransactionSynchronizationManager.hasResource(this.dataSource);
	}

	/**
	 * This implementation sets the isolation level but ignores the timeout.
	 */
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;

		// cache to avoid repeated checks
		boolean debugEnabled = logger.isDebugEnabled();

		if (txObject.getConnectionHolder() == null) {
			if (debugEnabled) {
				logger.debug("Opening new connection for JDBC transaction");
			}
			Connection con = DataSourceUtils.getConnection(this.dataSource, false);
			txObject.setConnectionHolder(new ConnectionHolder(con));
		}

		Connection con = txObject.getConnectionHolder().getConnection();
		try {
			// apply read-only
			if (definition.isReadOnly()) {
				if (debugEnabled) {
					logger.debug("Setting JDBC connection [" + con + "] read-only");
				}
				try {
					con.setReadOnly(true);
				}
				catch (Exception ex) {
					// SQLException or UnsupportedOperationException
					logger.warn("Could not set JDBC connection read-only", ex);
				}
			}

			// apply isolation level
			if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
				if (debugEnabled) {
					logger.debug("Changing isolation level of JDBC connection [" + con + "] to " +
											 definition.getIsolationLevel());
				}
				txObject.setPreviousIsolationLevel(new Integer(con.getTransactionIsolation()));
				con.setTransactionIsolation(definition.getIsolationLevel());
			}

			// Switch to manual commit if necessary. This is very expensive in some JDBC
			// drivers, so we don't want to do it unnecessarily (for example if we're configured
			// Commons DBCP to set it already)
			if (con.getAutoCommit()) {
				txObject.setMustRestoreAutoCommit(true);
				if (debugEnabled) {
					logger.debug("Switching JDBC connection [" + con + "] to manual commit");
				}
				con.setAutoCommit(false);
			}

			// register transaction timeout
			if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
				txObject.getConnectionHolder().setTimeoutInSeconds(definition.getTimeout());
			}

			// bind the connection holder to the thread
			TransactionSynchronizationManager.bindResource(this.dataSource, txObject.getConnectionHolder());
		}
		catch (SQLException ex) {
			throw new CannotCreateTransactionException("Could not configure connection", ex);
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

	protected boolean isRollbackOnly(Object transaction) throws TransactionException {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
		return txObject.getConnectionHolder().isRollbackOnly();
	}

	protected void doCommit(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Committing JDBC transaction [" + txObject.getConnectionHolder().getConnection() + "]");
		}
		try {
			txObject.getConnectionHolder().getConnection().commit();
		}
		catch (SQLException ex) {
			throw new TransactionSystemException("Could not commit", ex);
		}
	}

	protected void doRollback(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Rolling back JDBC transaction [" + txObject.getConnectionHolder().getConnection() + "]");
		}
		try {
			txObject.getConnectionHolder().getConnection().rollback();
		}
		catch (SQLException ex) {
			throw new TransactionSystemException("Could not rollback", ex);
		}
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Setting JDBC transaction [" + txObject.getConnectionHolder().getConnection() + "] rollback-only");
		}
		txObject.getConnectionHolder().setRollbackOnly();
	}

	protected void doCleanupAfterCompletion(Object transaction) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;

		// remove the connection holder from the thread
		TransactionSynchronizationManager.unbindResource(this.dataSource);
		
		// reset connection
		Connection con = txObject.getConnectionHolder().getConnection();

		try {
			// reset to auto-commit
			if (txObject.getMustRestoreAutoCommit()) {
				con.setAutoCommit(true);
			}

			// reset transaction isolation to previous value, if changed for the transaction
			if (txObject.getPreviousIsolationLevel() != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting isolation level of connection [" + con + "] to " +
											 txObject.getPreviousIsolationLevel());
				}
				con.setTransactionIsolation(txObject.getPreviousIsolationLevel().intValue());
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
			// SQLException or UnsupportedOperationException
			// typically not something to worry about, can be ignored
			logger.info("Could not reset JDBC connection", ex);
		}

		try {
			DataSourceUtils.closeConnectionIfNecessary(con, this.dataSource);
		}
		catch (CleanupFailureDataAccessException ex) {
			// just log it, to keep a transaction-related exception
			logger.error("Could not close connection after transaction", ex);
		}
	}

}
