package org.springframework.transaction.jta;

import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.UserTransaction;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.HeuristicCompletionException;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * PlatformTransactionManager implementation for JTA, i.e. J2EE container transactions.
 *
 * <p>This transaction manager is appropriate for handling distributed transactions,
 * i.e. transactions that span multiple resources, and for managing transactions
 * on a J2EE Connector (e.g. a persistence toolkit registered as JCA Connector).
 * For a single JDBC DataSource, DataSourceTransactionManager is perfectly sufficient,
 * and for accessing a single resource with Hibernate (including transactional cache),
 * HibernateTransactionManager is appropriate.
 *
 * <p>Transaction synchronization is active by default, to allow data access support
 * classes to register resources that are opened within the transaction for closing at
 * transaction completion time. Spring's support classes for JDBC, Hibernate, and JDO
 * all perform such registration, allowing for reuse of the same Hibernate Session etc
 * within the transaction. Standard JTA does not even guarantee that for Connections
 * from a transactional JDBC DataSource: Spring's synchronization solves those issues.
 *
 * <p>Synchronization is also leveraged for transactional cache handling with Hibernate.
 * Therefore, as long as JtaTransactionManager drives the JTA transactions, there is
 * no need to configure Hibernate's JTATransaction strategy or a container-specific
 * Hibernate TransactionManagerLookup. However, certain JTA implementations are
 * restrictive in terms of what JDBC calls they allow after transaction completion,
 * complaining even on close calls: In that case, it is indeed necessary to configure
 * a Hibernate TransactionManagerLookup; Spring will automatically adapt to this.
 *
 * <p>If JtaTransactionManager participates in an existing JTA transaction, e.g. from
 * EJB CMT, synchronization will be triggered on finishing the nested transaction,
 * before passing transaction control back to the J2EE container. In this case, a
 * container-specific Hibernate TransactionManagerLookup is the only way to achieve
 * exact afterCompletion callbacks for transactional cache handling with Hibernate.
 * In such a scenario, use Hibernate >=2.1 which features automatic JTA detection.
 *
 * <p>This implementation supports timeouts but not custom isolation levels. The
 * latter need to be interpreted in JtaDialect implementations, to be applied
 * for specific JTA implementations. Transaction suspension is just available with
 * a JtaDialect too. Note that some resource-specific transaction managers like
 * DataSourceTransactionManager and HibernateTransactionManager do support
 * timeouts, custom isolation levels, and transaction suspension.
 *
 * @author Juergen Hoeller
 * @since 24.03.2003
 * @see #setTransactionSynchronization
 * @see #setJtaDialect
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
 * @see org.springframework.orm.hibernate.HibernateTransactionManager
 */
public class JtaTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {

	public static final String DEFAULT_USER_TRANSACTION_NAME = "java:comp/UserTransaction";

	private JtaDialect jtaDialect;

	private JndiTemplate jndiTemplate = new JndiTemplate();

	private String userTransactionName = DEFAULT_USER_TRANSACTION_NAME;

	private boolean cacheUserTransaction = true;

	private UserTransaction cachedUserTransaction;


	/**
	 * Set the JTA dialect to use for this transaction manager.
	 * <p>A dialect is necessary for suspending and resuming transactions, and for
	 * applying custom isolation levels - both are not supported by standard JTA.
	 */
	public void setJtaDialect(JtaDialect jtaDialect) {
		this.jtaDialect = jtaDialect;
	}

	/**
	 * Set the JndiTemplate to use for JNDI lookup.
	 * A default one is used if not set.
	 */
	public void setJndiTemplate(JndiTemplate jndiTemplate) {
		if (jndiTemplate == null) {
			throw new IllegalArgumentException("jndiTemplate must not be null");
		}
		this.jndiTemplate = jndiTemplate;
	}

	/**
	 * Set the JNDI name of the UserTransaction.
	 * The default one is used if not set.
	 * @see #DEFAULT_USER_TRANSACTION_NAME
	 */
	public void setUserTransactionName(String userTransactionName) {
		this.userTransactionName = userTransactionName;
	}

	/**
	 * Set whether the JTA UserTransaction object should be cached, i.e.
	 * looked up from JNDI on afterPropertiesSet. Else, each getTransaction
	 * call will perform a fresh JNDI lookup. Default is true.
	 * <p>As JNDI lookups are expensive even in local JNDI environments,
	 * it is highly advisable to leave this activated. Only turn this off if
	 * your application server does not allow for caching the UserTransaction.
	 */
	public void setCacheUserTransaction(boolean cacheUserTransaction) {
		this.cacheUserTransaction = cacheUserTransaction;
	}


	/**
	 * Eagerly fetch the UserTransaction if cacheUserTransaction is true.
	 * @see #setCacheUserTransaction
	 * @see #lookupUserTransaction
	 */
	public void afterPropertiesSet() throws CannotCreateTransactionException {
		if (this.cacheUserTransaction) {
			this.cachedUserTransaction = lookupUserTransaction();
		}
	}

	/**
	 * Look up the JTA UserTransaction from JNDI via the configured name.
	 * Can be overridden in subclasses to provide a different UserTransaction object.
	 * <p>Called either on afterPropertiesSet or on doGetTransaction,
	 * depending on the cacheUserTransaction setting.
	 * @return the UserTransaction object
	 * @throws CannotCreateTransactionException if the JNDI lookup failed
	 * @see #setJndiTemplate
	 * @see #setUserTransactionName
	 * @see #setCacheUserTransaction
	 */
	protected UserTransaction lookupUserTransaction() throws CannotCreateTransactionException {
		try {
			UserTransaction ut = (UserTransaction) this.jndiTemplate.lookup(this.userTransactionName);
			if (logger.isInfoEnabled()) {
				logger.info("Using JTA UserTransaction [" + ut + "] from JNDI location [" +
				            this.userTransactionName + "]");
			}
			return ut;
		}
		catch (NamingException ex) {
			throw new CannotCreateTransactionException("JTA UserTransaction is not available at JNDI location [" +
			                                           this.userTransactionName + "]", ex);
		}
	}

	protected Object doGetTransaction() {
		return (this.cachedUserTransaction != null ? this.cachedUserTransaction : lookupUserTransaction());
	}

	protected boolean isExistingTransaction(Object transaction) {
		try {
			int status = ((UserTransaction) transaction).getStatus();
			return (status != Status.STATUS_NO_TRANSACTION && status != Status.STATUS_MARKED_ROLLBACK);
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on getStatus", ex);
		}
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) {
		if (logger.isDebugEnabled()) {
			logger.debug("Beginning JTA transaction [" + transaction + "] ");
		}
		UserTransaction ut = (UserTransaction) transaction;

		if (this.jtaDialect != null) {
			this.jtaDialect.applyIsolationLevel(ut, definition.getIsolationLevel());
		}
		else if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException("JtaTransactionManager does not support custom isolation levels " +
			                                         "without a JtaDialect");
		}

		try {
			if (definition.getTimeout() > TransactionDefinition.TIMEOUT_DEFAULT) {
				ut.setTransactionTimeout(definition.getTimeout());
			}
			ut.begin();
		}
		catch (NotSupportedException ex) {
			// assume "nested transactions not supported"
			throw new IllegalTransactionStateException(
			    "JTA implementation does not support nested transactions", ex);
		}
		catch (UnsupportedOperationException ex) {
			// assume "nested transactions not supported"
			throw new IllegalTransactionStateException(
			    "JTA implementation does not support nested transactions", ex);
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on begin", ex);
		}
	}

	protected Object doSuspend(Object transaction) {
		if (this.jtaDialect == null) {
			throw new IllegalTransactionStateException("JtaTransactionManager needs a JtaDialect for suspending a transaction");
		}
		try {
			return this.jtaDialect.getInternalTransactionManager().suspend();
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on suspend", ex);
		}
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		if (this.jtaDialect == null) {
			throw new IllegalTransactionStateException("JtaTransactionManager needs a JtaDialect for resuming a transaction");
		}
		try {
			this.jtaDialect.getInternalTransactionManager().resume((Transaction) suspendedResources);
		}
		catch (InvalidTransactionException ex) {
			throw new IllegalTransactionStateException("Tried to resume invalid JTA transaction", ex);
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on resume", ex);
		}
	}

	protected boolean isRollbackOnly(Object transaction) throws TransactionException {
		try {
			return ((UserTransaction) transaction).getStatus() == Status.STATUS_MARKED_ROLLBACK;
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on getStatus", ex);
		}
	}

	protected void doCommit(DefaultTransactionStatus status) {
		if (status.isDebug()) {
			logger.debug("Committing JTA transaction [" + status.getTransaction() + "]");
		}
		try {
			((UserTransaction) status.getTransaction()).commit();
		}
		catch (RollbackException ex) {
			throw new UnexpectedRollbackException("JTA transaction rolled back", ex);
		}
		catch (HeuristicMixedException ex) {
			throw new HeuristicCompletionException(HeuristicCompletionException.STATE_MIXED, ex);
		}
		catch (HeuristicRollbackException ex) {
			throw new HeuristicCompletionException(HeuristicCompletionException.STATE_ROLLED_BACK, ex);
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on commit", ex);
		}
	}

	protected void doRollback(DefaultTransactionStatus status) {
		if (status.isDebug()) {
			logger.debug("Rolling back JTA transaction [" + status.getTransaction() + "]");
		}
		try {
			((UserTransaction) status.getTransaction()).rollback();
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on rollback", ex);
		}
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		if (status.isDebug()) {
			logger.debug("Setting JTA transaction [" + status.getTransaction() + "] rollback-only");
		}
		try {
			((UserTransaction) status.getTransaction()).setRollbackOnly();
		}
		catch (IllegalStateException ex) {
			throw new NoTransactionException("No active JTA transaction");
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on setRollbackOnly", ex);
		}
	}

	protected void doCleanupAfterCompletion(Object transaction) {
		// nothing to do here
	}

}
