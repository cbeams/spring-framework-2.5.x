package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
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
		if (txObject.getConnectionHolder() == null) {
			logger.debug("Opening new connection for JDBC transaction");
			Connection con = DataSourceUtils.getConnection(this.dataSource);
			txObject.setConnectionHolder(new ConnectionHolder(con));
		}

		Connection con = txObject.getConnectionHolder().getConnection();
		logger.debug("Switching JDBC connection [" + con + "] to manual commit");
		try {
			// apply isolation level
			if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
				logger.debug("Changing isolation level to " + definition.getIsolationLevel());
				txObject.setPreviousIsolationLevel(new Integer(con.getTransactionIsolation()));
				con.setTransactionIsolation(definition.getIsolationLevel());
			}

			// apply read-only
			if (definition.isReadOnly()) {
				try {
					con.setReadOnly(true);
				}
				catch (Exception ex) {
					// SQLException or UnsupportedOperationException
					logger.warn("Could not set JDBC connection read-only", ex);
				}
			}

			// switch to manual commit
			con.setAutoCommit(false);

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

	protected boolean isRollbackOnly(Object transaction) throws TransactionException {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
		return txObject.getConnectionHolder().isRollbackOnly();
	}

	protected void doCommit(TransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		logger.debug("Committing JDBC transaction [" + txObject.getConnectionHolder().getConnection() + "]");
		try {
			txObject.getConnectionHolder().getConnection().commit();
		}
		catch (SQLException ex) {
			throw new TransactionSystemException("Could not commit", ex);
		}
	}

	protected void doRollback(TransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		logger.debug("Rolling back JDBC transaction [" + txObject.getConnectionHolder().getConnection() + "]");
		try {
			txObject.getConnectionHolder().getConnection().rollback();
		}
		catch (SQLException ex) {
			throw new TransactionSystemException("Could not rollback", ex);
		}
	}

	protected void doSetRollbackOnly(TransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		logger.debug("Setting JDBC transaction [" + txObject.getConnectionHolder().getConnection() + "] rollback-only");
		txObject.getConnectionHolder().setRollbackOnly();
	}

	protected void cleanupAfterCompletion(Object transaction) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;

		// remove the connection holder from the thread
		TransactionSynchronizationManager.unbindResource(this.dataSource);
		
		// reset connection
		Connection con = txObject.getConnectionHolder().getConnection();

		try {
			// reset to auto-commit
			con.setAutoCommit(true);

			// reset transaction isolation to previous value, if changed for the transaction
			if (txObject.getPreviousIsolationLevel() != null) {
				logger.debug("Resetting isolation level to " + txObject.getPreviousIsolationLevel());
				con.setTransactionIsolation(txObject.getPreviousIsolationLevel().intValue());
			}

			// reset read-only
			if (con.isReadOnly()) {
				con.setReadOnly(false);
			}
		}
		catch (Exception ex) {
			// SQLException or UnsupportedOperationException
			logger.warn("Could not reset JDBC connection", ex);
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
