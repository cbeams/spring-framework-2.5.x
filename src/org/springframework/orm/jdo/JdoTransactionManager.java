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

package org.springframework.orm.jdo;

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * PlatformTransactionManager implementation for single JDO persistence manager factories.
 * Binds a JDO PersistenceManager from the specified factory to the thread, potentially
 * allowing for one thread PersistenceManager per factory. PersistenceManagerFactoryUtils
 * and JdoTemplate are aware of thread-bound persistence managers and participate in such
 * transactions automatically. Using either is required for JDO access code supporting
 * this transaction management mechanism.
 *
 * <p>This implementation is appropriate for applications that solely use JDO for
 * transactional data access. JTA respectively JtaTransactionManager is necessary for
 * accessing multiple transactional resources. Note that you need to configure your JDO
 * tool accordingly to make it participate in JTA transactions. In contrast to Hibernate,
 * this cannot be transparently provided by the Spring transaction manager implementation.
 *
 * <p>With a JdoDialect specified, this implementation also supports direct data source
 * access within a transaction (i.e. plain JDBC code working with the same DataSource).
 * This allows for mixing services that access JDO (including transactional caching)
 * and services that use plain JDBC (without being aware of JDO)!
 * Application code needs to stick to the same simple Connection lookup pattern as
 * with DataSourceTransactionManager (i.e. DataSourceUtils.getConnection).
 *
 * <p>Note that to be able to register a DataSource's Connection for plain JDBC
 * code, this instance needs to be aware of the DataSource (see setDataSource).
 * The given DataSource should obviously match the one used by the given
 * PersistenceManagerFactory. To achieve this, configure both to the same JNDI
 * DataSource, or preferably use a LocalPersistenceManagerFactoryBean and a
 * local DataSource (which will be auto-detected by this transaction manager).
 *
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @see #setPersistenceManagerFactory
 * @see #setDataSource
 * @see LocalPersistenceManagerFactoryBean
 * @see PersistenceManagerFactoryUtils#getPersistenceManager
 * @see PersistenceManagerFactoryUtils#closePersistenceManagerIfNecessary
 * @see JdoTemplate#execute
 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection
 * @see org.springframework.jdbc.datasource.DataSourceUtils#closeConnectionIfNecessary
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.orm.hibernate.HibernateTransactionManager
 */
public class JdoTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {

	private PersistenceManagerFactory persistenceManagerFactory;

	private DataSource dataSource;

	private JdoDialect jdoDialect = new DefaultJdoDialect();


	/**
	 * Create a new JdoTransactionManager instance.
	 * A PersistenceManagerFactory has to be set to be able to use it.
	 * @see #setPersistenceManagerFactory
	 */
	public JdoTransactionManager() {
	}

	/**
	 * Create a new JdoTransactionManager instance.
	 * @param pmf PersistenceManagerFactory to manage transactions for
	 */
	public JdoTransactionManager(PersistenceManagerFactory pmf) {
		this.persistenceManagerFactory = pmf;
		afterPropertiesSet();
	}

	/**
	 * Set the PersistenceManagerFactory that this instance should manage transactions for.
	 */
	public void setPersistenceManagerFactory(PersistenceManagerFactory pmf) {
		this.persistenceManagerFactory = pmf;
	}

	/**
	 * Return the PersistenceManagerFactory that this instance should manage transactions for.
	 */
	public PersistenceManagerFactory getPersistenceManagerFactory() {
		return persistenceManagerFactory;
	}

	/**
	 * Set the JDBC DataSource that this instance should manage transactions for.
   * The DataSource should match the one used by the JDO PersistenceManagerFactory:
	 * for example, you could specify the same JNDI DataSource for both.
	 * <p>If the PersistenceManagerFactory uses a DataSource as connection factory,
	 * the DataSource will be auto-detected: You can still explictly specify the
	 * DataSource, but you don't need to in this case.
	 * <p>A transactional JDBC Connection for this DataSource will be provided to
	 * application code accessing this DataSource directly via DataSourceUtils
	 * or JdbcTemplate. The Connection will be taken from the JDO PersistenceManager.
	 * <p>Note that you need to use a JDO dialect for a specific JDO implementation
	 * to allow for exposing JDO transactions as JDBC transactions.
	 * @see #setJdoDialect
	 * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory
	 * @see LocalPersistenceManagerFactoryBean#setDataSource
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

	/**
	 * Set the JDO dialect to use for this transaction manager.
	 * <p>The dialect object can be used to retrieve the underlying JDBC connection
	 * and thus allows for exposing JDO transactions as JDBC transactions.
	 * @see JdoDialect#getJdbcConnection
	 */
	public void setJdoDialect(JdoDialect jdoDialect) {
		this.jdoDialect = jdoDialect;
	}

	/**
	 * Return the JDO dialect to use for this transaction manager.
	 */
	public JdoDialect getJdoDialect() {
		return jdoDialect;
	}

	public void afterPropertiesSet() {
		if (this.persistenceManagerFactory == null) {
			throw new IllegalArgumentException("persistenceManagerFactory is required");
		}

		// check for DataSource as connection factory
		if (this.dataSource == null) {
			Object pmfcf = this.persistenceManagerFactory.getConnectionFactory();
			if (pmfcf instanceof DataSource) {
				// use the PersistenceManagerFactory's DataSource for exposing transactions to JDBC code
				logger.info("Using DataSource [" + pmfcf +
										"] from JDO PersistenceManagerFactory for JdoTransactionManager");
				this.dataSource = (DataSource) pmfcf;
			}
		}
	}


	protected Object doGetTransaction() {
		if (TransactionSynchronizationManager.hasResource(this.persistenceManagerFactory)) {
			logger.debug("Found thread-bound persistence manager for JDO transaction");
			PersistenceManagerHolder pmHolder = (PersistenceManagerHolder)
					TransactionSynchronizationManager.getResource(this.persistenceManagerFactory);
			return new JdoTransactionObject(pmHolder);
		}
		else {
			return new JdoTransactionObject();
		}
	}

	protected boolean isExistingTransaction(Object transaction) {
		return ((JdoTransactionObject) transaction).hasTransaction();
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) {
		if (definition.isReadOnly()) {
			logger.info("JdoTransactionManager does not support read-only transactions: ignoring 'readOnly' hint");
		}

		JdoTransactionObject txObject = (JdoTransactionObject) transaction;
		if (txObject.getPersistenceManagerHolder() == null) {
			PersistenceManager pm = PersistenceManagerFactoryUtils.getPersistenceManager(this.persistenceManagerFactory,
			                                                                             true, false);
			if (logger.isDebugEnabled()) {
				logger.debug("Opened new persistence manager [" + pm + "] for JDO transaction");
			}
			txObject.setPersistenceManagerHolder(new PersistenceManagerHolder(pm));
		}

		txObject.getPersistenceManagerHolder().setSynchronizedWithTransaction(true);
		PersistenceManager pm = txObject.getPersistenceManagerHolder().getPersistenceManager();
		try {

			// delegate to JdoDialect for actual transaction begin
			this.jdoDialect.beginTransaction(pm.currentTransaction(), definition);

			// register transaction timeout
			if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
				txObject.getPersistenceManagerHolder().setTimeoutInSeconds(definition.getTimeout());
			}

			// register the JDO PersistenceManager's JDBC Connection for the DataSource, if set
			if (this.dataSource != null) {
				ConnectionHandle conHandle = this.jdoDialect.getJdbcConnection(pm, definition.isReadOnly());
				if (conHandle != null) {
					ConnectionHolder conHolder = new ConnectionHolder(conHandle);
					if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
						conHolder.setTimeoutInSeconds(definition.getTimeout());
					}
					if (logger.isDebugEnabled()) {
						logger.debug("Exposing JDO transaction [" + pm + "] as JDBC transaction [" +
												 conHolder.getConnection() + "]");
					}
					TransactionSynchronizationManager.bindResource(this.dataSource, conHolder);
					txObject.setJdbcTransactionExposed(true);
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("Not exposing JDO transaction [" + pm + "] as JDBC transaction because JdoDialect ["
												 + this.jdoDialect + "] does not support JDBC connection retrieval");
					}
				}
			}

			// bind the persistence manager holder to the thread
			if (txObject.isNewPersistenceManagerHolder()) {
				TransactionSynchronizationManager.bindResource(this.persistenceManagerFactory,
																											 txObject.getPersistenceManagerHolder());
			}
		}

		catch (TransactionException ex) {
			cleanupAfterTransactionBeginFailed(pm);
			throw ex;
		}
		catch (Exception ex) {
			cleanupAfterTransactionBeginFailed(pm);
			throw new CannotCreateTransactionException("Could not create JDO transaction", ex);
		}
	}

	private void cleanupAfterTransactionBeginFailed(PersistenceManager pm) {
		try {
			PersistenceManagerFactoryUtils.closePersistenceManagerIfNecessary(pm, this.persistenceManagerFactory);
		}
		catch (CleanupFailureDataAccessException ex) {
			// just log it, to keep the transaction-related exception
			logger.error("Could not close JDO persistence manager after transaction begin failed", ex);
		}
	}

	protected Object doSuspend(Object transaction) {
		JdoTransactionObject txObject = (JdoTransactionObject) transaction;
		txObject.setPersistenceManagerHolder(null);
		PersistenceManagerHolder persistenceManagerHolder =
		    (PersistenceManagerHolder) TransactionSynchronizationManager.unbindResource(this.persistenceManagerFactory);
		ConnectionHolder connectionHolder = null;
		if (this.dataSource != null) {
			connectionHolder = (ConnectionHolder) TransactionSynchronizationManager.unbindResource(this.dataSource);
		}
		return new SuspendedResourcesHolder(persistenceManagerHolder, connectionHolder);
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		SuspendedResourcesHolder resourcesHolder = (SuspendedResourcesHolder) suspendedResources;
		TransactionSynchronizationManager.bindResource(this.persistenceManagerFactory,
																									 resourcesHolder.getPersistenceManagerHolder());
		if (this.dataSource != null) {
			TransactionSynchronizationManager.bindResource(this.dataSource, resourcesHolder.getConnectionHolder());
		}
	}

	protected boolean isRollbackOnly(Object transaction) {
		return ((JdoTransactionObject) transaction).getPersistenceManagerHolder().isRollbackOnly();
	}

	protected void doCommit(DefaultTransactionStatus status) {
		JdoTransactionObject txObject = (JdoTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Committing JDO transaction on persistence manager [" +
									 txObject.getPersistenceManagerHolder().getPersistenceManager() + "]");
		}
		try {
			txObject.getPersistenceManagerHolder().getPersistenceManager().currentTransaction().commit();
		}
		catch (JDOException ex) {
			// assumably failed to flush changes to database
			throw convertJdoAccessException(ex);
		}
	}

	protected void doRollback(DefaultTransactionStatus status) {
		JdoTransactionObject txObject = (JdoTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Rolling back JDO transaction on persistence manager [" +
									 txObject.getPersistenceManagerHolder().getPersistenceManager() + "]");
		}
		try {
			txObject.getPersistenceManagerHolder().getPersistenceManager().currentTransaction().rollback();
		}
		catch (JDOException ex) {
			throw new TransactionSystemException("Could not roll back JDO transaction", ex);
		}
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		JdoTransactionObject txObject = (JdoTransactionObject) status.getTransaction();
		logger.debug("Setting JDO transaction rollback-only");
		txObject.getPersistenceManagerHolder().setRollbackOnly();
	}

	protected void doCleanupAfterCompletion(Object transaction) {
		JdoTransactionObject txObject = (JdoTransactionObject) transaction;

		// remove the persistence manager holder from the thread
		if (txObject.isNewPersistenceManagerHolder()) {
			TransactionSynchronizationManager.unbindResource(this.persistenceManagerFactory);
		}

		// remove the JDBC connection holder from the thread, if set
		if (txObject.isJdbcTransactionExposed()) {
			ConnectionHolder conHolder =
					(ConnectionHolder) TransactionSynchronizationManager.unbindResource(this.dataSource);
			try {
				this.jdoDialect.releaseJdbcConnection(conHolder.getConnectionHandle(),
				                                      txObject.getPersistenceManagerHolder().getPersistenceManager());
			}
			catch (Exception ex) {
				// just log it, to keep a transaction-related exception
				logger.error("Could not close JDBC connection after transaction", ex);
			}
		}

		txObject.getPersistenceManagerHolder().setSynchronizedWithTransaction(false);
		txObject.getPersistenceManagerHolder().clearTimeout();

		// remove the persistence manager holder from the thread
		if (txObject.isNewPersistenceManagerHolder()) {
			PersistenceManager pm = txObject.getPersistenceManagerHolder().getPersistenceManager();
			if (logger.isDebugEnabled()) {
				logger.debug("Closing JDO persistence manager [" + pm + "] after transaction");
			}
			try {
				PersistenceManagerFactoryUtils.closePersistenceManagerIfNecessary(pm, this.persistenceManagerFactory);
			}
			catch (CleanupFailureDataAccessException ex) {
				// just log it, to keep a transaction-related exception
				logger.error("Could not close JDO persistence manager after transaction", ex);
			}
		}
		else {
			logger.debug("Not closing pre-bound JDO persistence manager after transaction");
		}
	}

	/**
	 * Convert the given JDOException to an appropriate exception from the
	 * org.springframework.dao hierarchy. Delegates to the JdoDialect if set, falls
	 * back to PersistenceManagerFactoryUtils' standard exception translation else.
	 * May be overridden in subclasses.
	 * @param ex JDOException that occured
	 * @return the corresponding DataAccessException instance
	 * @see JdoDialect#translateException
	 * @see PersistenceManagerFactoryUtils#convertJdoAccessException
	 */
	protected DataAccessException convertJdoAccessException(JDOException ex) {
		return this.jdoDialect.translateException(ex);
	}


	/**
	 * Holder for suspended resources.
	 * Used internally by doSuspend and doResume.
	 * @see #doSuspend
	 * @see #doResume
	 */
	private static class SuspendedResourcesHolder {

		private final PersistenceManagerHolder persistenceManagerHolder;

		private final ConnectionHolder connectionHolder;

		private SuspendedResourcesHolder(PersistenceManagerHolder persistenceManagerHolder,
		                                 ConnectionHolder connectionHolder) {
			this.persistenceManagerHolder = persistenceManagerHolder;
			this.connectionHolder = connectionHolder;
		}

		private PersistenceManagerHolder getPersistenceManagerHolder() {
			return persistenceManagerHolder;
		}

		private ConnectionHolder getConnectionHolder() {
			return connectionHolder;
		}
	}

}
