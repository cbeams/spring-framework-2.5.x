package org.springframework.orm.jdo;

import javax.jdo.JDOException;
import javax.jdo.JDOFatalException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.InvalidIsolationException;
import org.springframework.transaction.InvalidTimeoutException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

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
   * The DataSource should match the one used by the JDO PersistenceManagerFactory.
	 * Note that you need to use a JDO dialect for a specific JDO implementation
	 * to allow for exposing JDO transactions as JDBC transactions.
	 * <p>A transactional JDBC Connection for this DataSource will be provided to
	 * application code accessing this DataSource directly via DataSourceUtils
	 * or JdbcTemplate. The Connection will be taken from the JDO PersistenceManager.
	 * @see #setJdoDialect
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
		if (this.dataSource != null && this.jdoDialect == null) {
			throw new IllegalArgumentException("A jdoDialect is required to expose JDO transactions as JDBC transactions");
		}
	}

	protected Object doGetTransaction() throws CannotCreateTransactionException, TransactionException {
		if (PersistenceManagerFactoryUtils.getThreadObjectManager().hasThreadObject(this.persistenceManagerFactory)) {
			logger.debug("Found thread-bound PersistenceManager for JDO transaction");
			PersistenceManagerHolder pmHolder = (PersistenceManagerHolder) PersistenceManagerFactoryUtils.getThreadObjectManager().getThreadObject(this.persistenceManagerFactory);
			return new JdoTransactionObject(pmHolder, false);
		}
		else {
			logger.debug("Using new PersistenceManager for JDO transaction");
			PersistenceManager pm = PersistenceManagerFactoryUtils.getPersistenceManager(this.persistenceManagerFactory, true);
			return new JdoTransactionObject(new PersistenceManagerHolder(pm), true);
		}
	}

	protected boolean isExistingTransaction(Object transaction) throws TransactionException {
		JdoTransactionObject txObject = (JdoTransactionObject) transaction;
		return txObject.getPersistenceManagerHolder().getPersistenceManager().currentTransaction().isActive();
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
		if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationException("JdoTransactionManager does not support custom isolation levels");
		}
		if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
			throw new InvalidTimeoutException("JdoTransactionManager does not support timeouts", definition.getTimeout());
		}
		if (definition.isReadOnly()) {
			logger.warn("JdoTransactionManager does not support read-only transactions: ignoring 'readOnly' hint");
		}
		JdoTransactionObject txObject = (JdoTransactionObject) transaction;
		logger.debug("Beginning JDO transaction");

		try {
			PersistenceManager pm = txObject.getPersistenceManagerHolder().getPersistenceManager();
			pm.currentTransaction().begin();
			if (txObject.isNewPersistenceManagerHolder()) {
				PersistenceManagerFactoryUtils.getThreadObjectManager().bindThreadObject(
						this.persistenceManagerFactory, txObject.getPersistenceManagerHolder());
			}

			// register the JDO PersistenceManager's JDBC Connection for the DataSource, if set
			if (this.dataSource != null && this.jdoDialect != null) {
				ConnectionHolder conHolder = new ConnectionHolder(this.jdoDialect.getJdbcConnection(pm));
				if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
					conHolder.setTimeoutInSeconds(definition.getTimeout());
				}
				DataSourceUtils.getThreadObjectManager().bindThreadObject(this.dataSource, conHolder);
			}
		}
		catch (JDOException ex) {
			throw new CannotCreateTransactionException("Could not create JDO transaction", ex);
		}
	}

	protected boolean isRollbackOnly(Object transaction) throws TransactionException {
		JdoTransactionObject txObject = (JdoTransactionObject) transaction;
		return txObject.getPersistenceManagerHolder().isRollbackOnly();
	}

	protected void doCommit(TransactionStatus status) throws TransactionException {
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
			throw PersistenceManagerFactoryUtils.convertJdoAccessException(ex);
		}
	}

	protected void doRollback(TransactionStatus status) throws TransactionException {
		JdoTransactionObject txObject = (JdoTransactionObject) status.getTransaction();
		logger.debug("Rolling back JDO transaction");
		try {
			txObject.getPersistenceManagerHolder().getPersistenceManager().currentTransaction().rollback();
		}
		catch (JDOException ex) {
			throw new TransactionSystemException("Could not rollback JDO transaction", ex);
		}
	}

	protected void doSetRollbackOnly(TransactionStatus status) throws TransactionException {
		JdoTransactionObject txObject = (JdoTransactionObject) status.getTransaction();
		logger.debug("Setting JDO transaction rollback-only");
		txObject.getPersistenceManagerHolder().setRollbackOnly();
	}

	protected void cleanupAfterCompletion(Object transaction) {
		JdoTransactionObject txObject = (JdoTransactionObject) transaction;

		// remove the JDBC connection holder from the thread, if set
		if (this.dataSource != null) {
			DataSourceUtils.getThreadObjectManager().removeThreadObject(this.dataSource);
		}

		// remove the persistence manager holder from the thread
		if (txObject.isNewPersistenceManagerHolder()) {
			PersistenceManagerFactoryUtils.getThreadObjectManager().removeThreadObject(this.persistenceManagerFactory);
			try {
				PersistenceManagerFactoryUtils.closePersistenceManagerIfNecessary(
				    txObject.getPersistenceManagerHolder().getPersistenceManager(), this.persistenceManagerFactory);
			}
			catch (CleanupFailureDataAccessException ex) {
				// just log it, to keep a transaction-related exception
				logger.error("Could not close JDO PersistenceManager after transaction", ex);
			}
		}
		else {
			logger.debug("Not closing pre-bound JDO PersistenceManager after transaction");
		}
	}

}
