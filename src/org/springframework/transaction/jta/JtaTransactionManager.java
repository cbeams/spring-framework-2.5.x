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
import javax.transaction.TransactionManager;
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
 * Can also work with a locally configured JTA implementation.
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
 * transaction completion time. Spring's support classes for JDBC, Hibernate and JDO
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
 * <p>Transaction suspension (REQUIRES_NEW, NOT_SUPPORTED) is just available with
 * a JTA TransactionManager being registered, via the "transactionManagerName" or
 * "transactionManager" property. The location of this internal JTA object is
 * <i>not</i> specified by J2EE; it is individual for each J2EE server, often kept
 * in JNDI like the UserTransaction. Some well-known JNDI locations are:
 * <ul>
 * <li>"java:comp/UserTransaction" for Resin, Orion, JOnAS (JOTM)
 * <li>"java:/TransactionManager" for JBoss, JRun4
 * <li>"javax.transaction.TransactionManager" for BEA WebLogic
 * </ul>
 *
 * <p>"java:comp/UserTransaction" as JNDI name for the TransactionManager means that
 * the same JTA object implements both the UserTransaction and the TransactionManager
 * interface. As this is easy to test when looking up the UserTransaction, this will
 * be auto-detected on initialization of JtaTransactionManager. Thus, there's no need
 * to specify the "transactionManagerName" in this case (for Resin, Orion, JOnAS).
 *
 * <p>For IBM WebSphere and standalone JOTM, static accessor methods are required to
 * obtain the JTA TransactionManager: Therefore, WebSphere and JOTM have their own
 * FactoryBean implementations, to be wired with the "transactionManager" property.
 * In case of JotmFactoryBean, the same JTA object implements UserTransaction too:
 * Therefore, passing the object to the "userTransaction" property is sufficient.
 *
 * <p>The internal JTA TransactionManager can also be used to register custom
 * synchronizations with the JTA transaction itself instead of Spring's
 * transaction manager. LocalSessionFactoryBean supports plugging a given
 * TransactionManager into Hibernate's TransactionManagerLookup mechanism,
 * for Hibernate-driven cache synchronization without double configuration.
 *
 * <p>JtaTransactionManager supports timeouts but not custom isolation levels.
 * Custom subclasses can override applyIsolationLevel for specific JTA
 * implementations. Note that some resource-specific transaction managers like
 * DataSourceTransactionManager and HibernateTransactionManager do support timeouts,
 * custom isolation levels, and transaction suspension without JTA's restrictions.
 *
 * @author Juergen Hoeller
 * @since 24.03.2003
 * @see #setUserTransactionName
 * @see #setTransactionManagerName
 * @see #applyIsolationLevel
 * @see JotmFactoryBean
 * @see WebSphereTransactionManagerFactoryBean
 * @see org.springframework.orm.hibernate.LocalSessionFactoryBean#setJtaTransactionManager
 * @see org.springframework.orm.hibernate.HibernateTransactionManager
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
 */
public class JtaTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {

	public static final String DEFAULT_USER_TRANSACTION_NAME = "java:comp/UserTransaction";

	private JndiTemplate jndiTemplate = new JndiTemplate();

	private UserTransaction userTransaction;

	private String userTransactionName = DEFAULT_USER_TRANSACTION_NAME;

	private TransactionManager transactionManager;

	private String transactionManagerName;


	/**
	 * Set the JndiTemplate to use for JNDI lookups.
	 * A default one is used if not set.
	 */
	public void setJndiTemplate(JndiTemplate jndiTemplate) {
		if (jndiTemplate == null) {
			throw new IllegalArgumentException("jndiTemplate must not be null");
		}
		this.jndiTemplate = jndiTemplate;
	}

	/**
	 * Set the JTA UserTransaction to use as direct reference.
	 * Typically just used for local JTA setups; in a J2EE environment,
	 * the UserTransaction will always be fetched from JNDI.
	 */
	public void setUserTransaction(UserTransaction userTransaction) {
		this.userTransaction = userTransaction;
	}

	/**
	 * Set the JNDI name of the JTA UserTransaction.
	 * The default one is used if not set.
	 * @see #DEFAULT_USER_TRANSACTION_NAME
	 */
	public void setUserTransactionName(String userTransactionName) {
		this.userTransactionName = userTransactionName;
	}

	/**
	 * Set the JTA TransactionManager to use as direct reference.
	 * <p>A TransactionManager is necessary for suspending and resuming transactions,
	 * as this not supported by the UserTransaction interface.
	 */
	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Set the JNDI name of the JTA TransactionManager.
	 * <p>A TransactionManager is necessary for suspending and resuming transactions,
	 * as this not supported by the UserTransaction interface.
	 */
	public void setTransactionManagerName(String transactionManagerName) {
		this.transactionManagerName = transactionManagerName;
	}


	public void afterPropertiesSet() throws CannotCreateTransactionException {
		if (this.userTransaction == null) {
			if (this.userTransactionName != null) {
				this.userTransaction = lookupUserTransaction(this.userTransactionName);
			}
			else {
				throw new IllegalArgumentException("Either userTransaction or userTransactionName must be set");
			}
		}
		if (this.transactionManager == null) {
			if (this.transactionManagerName != null) {
				this.transactionManager = lookupTransactionManager(this.transactionManagerName);
			}
			else if (this.userTransaction instanceof TransactionManager) {
				if (logger.isInfoEnabled()) {
					logger.info("JTA UserTransaction object [" + this.userTransaction + "] implements TransactionManager");
				}
				this.transactionManager = (TransactionManager) this.userTransaction;
			}
			else {
				logger.info("No JTA TransactionManager specified - transaction suspension not available");
			}
		}
	}

	/**
	 * Look up the JTA UserTransaction in JNDI via the configured name.
	 * Called by afterPropertiesSet if no direct UserTransaction reference was set.
	 * Can be overridden in subclasses to provide a different UserTransaction object.
	 * @param userTransactionName the JNDI name of the UserTransaction
	 * @return the UserTransaction object
	 * @throws CannotCreateTransactionException if the JNDI lookup failed
	 * @see #setJndiTemplate
	 * @see #setUserTransactionName
	 */
	protected UserTransaction lookupUserTransaction(String userTransactionName)
			throws CannotCreateTransactionException {
		try {
			Object jndiObj = this.jndiTemplate.lookup(userTransactionName);
			if (!(jndiObj instanceof UserTransaction)) {
				throw new CannotCreateTransactionException("Object [" + jndiObj + "] available at JNDI location [" +
				                                           userTransactionName + "] does not implement " +
				                                           "javax.transaction.UserTransaction");
			}
			UserTransaction ut = (UserTransaction) jndiObj;
			if (logger.isInfoEnabled()) {
				logger.info("Using JTA UserTransaction [" + ut + "] from JNDI location [" +
				            userTransactionName + "]");
			}
			return ut;
		}
		catch (NamingException ex) {
			throw new CannotCreateTransactionException("JTA UserTransaction is not available at JNDI location [" +
			                                           userTransactionName + "]", ex);
		}
	}

	/**
	 * Look up the JTA TransactionManager in JNDI via the configured name.
	 * Called by afterPropertiesSet if no direct TransactionManager reference was set.
	 * Can be overridden in subclasses to provide a different TransactionManager object.
	 * @param transactionManagerName the JNDI name of the TransactionManager
	 * @return the UserTransaction object
	 * @throws CannotCreateTransactionException if the JNDI lookup failed
	 * @see #setJndiTemplate
	 * @see #setTransactionManagerName
	 */
	protected TransactionManager lookupTransactionManager(String transactionManagerName)
			throws CannotCreateTransactionException {
		try {
			Object jndiObj = this.jndiTemplate.lookup(transactionManagerName);
			if (!(jndiObj instanceof TransactionManager)) {
				throw new CannotCreateTransactionException("Object [" + jndiObj + "] available at JNDI location [" +
				                                           transactionManagerName + "] does not implement " +
				                                           "javax.transaction.TransactionManager");
			}
			TransactionManager tm = (TransactionManager) jndiObj;
			if (logger.isInfoEnabled()) {
				logger.info("Using JTA TransactionManager [" + tm + "] from JNDI location [" +
				            transactionManagerName + "]");
			}
			return tm;
		}
		catch (NamingException ex) {
			throw new CannotCreateTransactionException("JTA TransactionManager is not available at JNDI location [" +
			                                           transactionManagerName + "]", ex);
		}
	}

	protected Object doGetTransaction() {
		return this.userTransaction;
	}

	protected boolean isExistingTransaction(Object transaction) {
		try {
			int status = ((UserTransaction) transaction).getStatus();
			return (status != Status.STATUS_NO_TRANSACTION);
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
		applyIsolationLevel(definition.getIsolationLevel());
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

	/**
	 * Apply the given transaction isolation level. Default implementation
	 * will throw an exception for any level other than ISOLATION_DEFAULT.
	 * To be overridden in subclasses for specific JTA implementations.
	 * @param isolationLevel isolation level taken from transaction definition
	 * @throws InvalidIsolationLevelException if the given isolation level
	 * cannot be applied
	 */
	protected void applyIsolationLevel(int isolationLevel) throws InvalidIsolationLevelException {
		if (isolationLevel != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException("JtaTransactionManager does not support custom isolation levels");
		}
	}

	protected Object doSuspend(Object transaction) {
		if (this.transactionManager == null) {
			throw new IllegalTransactionStateException("JtaTransactionManager needs a JTA TransactionManager for " +
																								 "suspending a transaction - specify the 'transactionManager' " +
																								 "or 'transactionManagerName' property");
		}
		try {
			return this.transactionManager.suspend();
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on suspend", ex);
		}
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		if (this.transactionManager == null) {
			throw new IllegalTransactionStateException("JtaTransactionManager needs a JTA TransactionManager for " +
																								 "suspending a transaction - specify the 'transactionManager' " +
																								 "or 'transactionManagerName' property");
		}
		try {
			this.transactionManager.resume((Transaction) suspendedResources);
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
