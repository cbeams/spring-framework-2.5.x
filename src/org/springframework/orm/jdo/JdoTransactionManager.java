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
import javax.jdo.JDOFatalException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.InvalidTimeoutException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * PlatformTransactionManager implementation for single JDO persistence manager factories.
 * Binds a JDO PersistenceManager from the specified factory to the thread, potentially
 * allowing for one thread PersistenceManager per factory. PersistenceManagerFactoryUtils
 * and JdoTemplate are aware of thread-bound persistence managers and take part in such
 * transactions automatically. Using either is required for JDO access code supporting
 * this transaction management mechanism.
 *
 * <p>This implementation is appropriate for applications that solely use JDO for
 * transactional data access. JTA resp. JtaTransactionManager is necessary for accessing
 * multiple transactional resources. Note that you need to configure your JDO tool
 * accordingly to make it participate in JTA transactions. In contrast to Hibernate,
 * this cannot be transparently provided by the Spring transaction manager implementation.
 *
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @see #setPersistenceManagerFactory
 * @see #setDataSource
 * @see PersistenceManagerFactoryUtils#getPersistenceManager
 * @see PersistenceManagerFactoryUtils#closePersistenceManagerIfNecessary
 * @see JdoTemplate#execute
 * @see org.springframework.orm.hibernate.HibernateTransactionManager
 */
public class JdoTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {

	private PersistenceManagerFactory persistenceManagerFactory;

	private DataSource dataSource;

	private JdoDialect jdoDialect;


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
		if (this.jdoDialect == null && this.dataSource != null) {
			throw new IllegalArgumentException("A jdoDialect is required to expose JDO transactions as JDBC transactions");
		}
		if (this.jdoDialect != null) {
			// check for DataSource as connection factory
			Object pmfcf = this.persistenceManagerFactory.getConnectionFactory();
			if (pmfcf instanceof DataSource) {
				if (this.dataSource == null) {
					// use the PersistenceManagerFactory's DataSource for exposing transactions to JDBC code
					logger.info("Using DataSource [" + pmfcf + "] from JDO PersistenceManagerFactory for JdoTransactionManager");
					this.dataSource = (DataSource) pmfcf;
				}
				else if (this.dataSource == pmfcf) {
					// let the configuration through: it's consistent
				}
				else {
					throw new IllegalArgumentException("Specified dataSource [" + this.dataSource +
																						 "] does not match [" + pmfcf + "] used by the PersistenceManagerFactory");
				}
			}
		}
	}


	protected Object doGetTransaction() {
		if (TransactionSynchronizationManager.hasResource(this.persistenceManagerFactory)) {
			logger.debug("Found thread-bound persistence manager for JDO transaction");
			PersistenceManagerHolder pmHolder = (PersistenceManagerHolder) TransactionSynchronizationManager.getResource(this.persistenceManagerFactory);
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
		if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException("JdoTransactionManager does not support custom isolation levels");
		}
		if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
			throw new InvalidTimeoutException("JdoTransactionManager does not support timeouts", definition.getTimeout());
		}
		if (definition.isReadOnly()) {
			logger.warn("JdoTransactionManager does not support read-only transactions: ignoring 'readOnly' hint");
		}

		JdoTransactionObject txObject = (JdoTransactionObject) transaction;
		if (txObject.getPersistenceManagerHolder() == null) {
			logger.debug("Opening new persistence manager for JDO transaction");
			PersistenceManager pm = PersistenceManagerFactoryUtils.getPersistenceManager(this.persistenceManagerFactory,
			                                                                             true, false);
			txObject.setPersistenceManagerHolder(new PersistenceManagerHolder(pm));
		}

		logger.debug("Beginning JDO transaction");
		try {
			PersistenceManager pm = txObject.getPersistenceManagerHolder().getPersistenceManager();
			pm.currentTransaction().begin();
			if (txObject.isNewPersistenceManagerHolder()) {
				TransactionSynchronizationManager.bindResource(this.persistenceManagerFactory, txObject.getPersistenceManagerHolder());
			}

			// register the JDO PersistenceManager's JDBC Connection for the DataSource, if set
			if (this.dataSource != null && this.jdoDialect != null) {
				ConnectionHolder conHolder = new ConnectionHolder(this.jdoDialect.getJdbcConnection(pm));
				if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
					conHolder.setTimeoutInSeconds(definition.getTimeout());
				}
				TransactionSynchronizationManager.bindResource(this.dataSource, conHolder);
			}
		}
		catch (JDOException ex) {
			throw new CannotCreateTransactionException("Could not create JDO transaction", ex);
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
		TransactionSynchronizationManager.bindResource(this.persistenceManagerFactory, resourcesHolder.getPersistenceManagerHolder());
		if (this.dataSource != null) {
			TransactionSynchronizationManager.bindResource(this.dataSource, resourcesHolder.getConnectionHolder());
		}
	}

	protected boolean isRollbackOnly(Object transaction) {
		return ((JdoTransactionObject) transaction).getPersistenceManagerHolder().isRollbackOnly();
	}

	protected void doCommit(DefaultTransactionStatus status) {
		JdoTransactionObject txObject = (JdoTransactionObject) status.getTransaction();
		logger.debug("Committing JDO transaction");
		try {
			txObject.getPersistenceManagerHolder().getPersistenceManager().currentTransaction().commit();
		}
		catch (JDOFatalException ex) {
			// assumably from commit call to underlying JDBC connection
			throw new TransactionSystemException("Could not commit JDO transaction", ex);
		}
		catch (JDOException ex) {
			// assumably failed to flush changes to database
			throw convertJdoAccessException(ex);
		}
	}

	protected void doRollback(DefaultTransactionStatus status) {
		JdoTransactionObject txObject = (JdoTransactionObject) status.getTransaction();
		logger.debug("Rolling back JDO transaction");
		try {
			txObject.getPersistenceManagerHolder().getPersistenceManager().currentTransaction().rollback();
		}
		catch (JDOException ex) {
			throw new TransactionSystemException("Could not rollback JDO transaction", ex);
		}
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		JdoTransactionObject txObject = (JdoTransactionObject) status.getTransaction();
		logger.debug("Setting JDO transaction rollback-only");
		txObject.getPersistenceManagerHolder().setRollbackOnly();
	}

	protected void doCleanupAfterCompletion(Object transaction) {
		JdoTransactionObject txObject = (JdoTransactionObject) transaction;

		// remove the JDBC connection holder from the thread, if set
		if (this.dataSource != null) {
			TransactionSynchronizationManager.unbindResource(this.dataSource);
		}

		// remove the persistence manager holder from the thread
		if (txObject.isNewPersistenceManagerHolder()) {
			TransactionSynchronizationManager.unbindResource(this.persistenceManagerFactory);
			try {
				PersistenceManagerFactoryUtils.closePersistenceManagerIfNecessary(
				    txObject.getPersistenceManagerHolder().getPersistenceManager(), this.persistenceManagerFactory);
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
		if (this.jdoDialect != null) {
			return this.jdoDialect.translateException(ex);
		}
		else {
			return PersistenceManagerFactoryUtils.convertJdoAccessException(ex);
		}
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
