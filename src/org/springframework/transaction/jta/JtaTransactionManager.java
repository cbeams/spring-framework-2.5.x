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

import java.io.IOException;
import java.io.ObjectInputStream;

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
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSuspensionNotSupportedException;
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
 * complaining even on close calls: In that case, it is indeed necessary to configure a
 * Hibernate TransactionManagerLookup, potentially via Spring's LocalSessionFactoryBean.
 *
 * <p>If JtaTransactionManager participates in an existing JTA transaction, e.g. from
 * EJB CMT, synchronization will be triggered on finishing the nested transaction,
 * before passing transaction control back to the J2EE container. In this case, a
 * container-specific Hibernate TransactionManagerLookup is the only way to achieve
 * exact afterCompletion callbacks for transactional cache handling with Hibernate.
 * In such a scenario, use Hibernate >=2.1 which features automatic JTA detection.
 *
 * <p><b>For typical JTA transactions (REQUIRED, SUPPORTS, MANDATORY, NEVER), a plain
 * JtaTransactionManager definition is all you need, completely portable across all
 * J2EE servers.</b> This corresponds to the functionality of the JTA UserTransaction,
 * for which J2EE specifies a standard JNDI name ("java:comp/UserTransaction").
 * There is no need to configure a server-specific TransactionManager lookup for this
 * kind of JTA usage.
 *
 * <p><b>Note: Advanced JTA usage below. Dealing with these mechanisms is not
 * necessary for typical usage scenarios.</b>
 *
 * <p>Transaction suspension (REQUIRES_NEW, NOT_SUPPORTED) is just available with
 * a JTA TransactionManager being registered, via the "transactionManagerName" or
 * "transactionManager" property. The location of this internal JTA object is
 * <i>not</i> specified by J2EE; it is individual for each J2EE server, often kept
 * in JNDI like the UserTransaction. Some well-known JNDI locations are:
 * <ul>
 * <li>"java:comp/UserTransaction" for Resin, Orion (Oracle OC4J), JOnAS (JOTM),
 * BEA WebLogic (unofficial)
 * <li>"javax.transaction.TransactionManager" for BEA WebLogic (official)
 * <li>"java:/TransactionManager" for JBoss, JRun4
 * </ul>
 *
 * <p>"java:comp/UserTransaction" as JNDI name for the TransactionManager means that
 * the same JTA object implements both the UserTransaction and the TransactionManager
 * interface. As this is easy to test when looking up the UserTransaction, this will
 * be autodetected on initialization of JtaTransactionManager. In this case, there's
 * no need to specify the "transactionManagerName" (for Resin, Orion, JOnAS, WebLogic).
 *
 * <p>A JNDI lookup can also be factored out into a corresponding JndiObjectFactoryBean,
 * passed into JtaTransactionManager's "transactionManager" property. Such a bean
 * definition can then be reused by other objects, for example Spring's
 * LocalSessionFactoryBean for Hibernate (see below).
 *
 * <p>For IBM WebSphere and standalone JOTM, static accessor methods are required to
 * obtain the JTA TransactionManager: Therefore, WebSphere and JOTM have their own
 * FactoryBean implementations, to be wired with the "transactionManager" property.
 * In case of JotmFactoryBean, the same JTA object implements UserTransaction too:
 * Therefore, passing the object to the "userTransaction" property is sufficient.
 *
 * <p><b>Note: Support for the JTA TransactionManager interface is not required by J2EE.
 * Almost all J2EE servers expose it, but do so as extension to J2EE. There might be some
 * issues with compatibility, despite the TransactionManager interface being part of JTA.</b>
 * The only currently known problem is resuming a transaction on WebLogic, which by default
 * fails if the suspended transaction was marked rollback-only; for other usages, it works
 * properly. Use Spring's WebLogicJtaTransactionManager to enforce a resume in any case.
 *
 * <p>The JTA TransactionManager can also be used to register custom synchronizations
 * with the JTA transaction itself instead of Spring's transaction manager. This is
 * particularly useful for closing resources with rigid JTA implementations like
 * Weblogic's or WebSphere's that do not allow any access to resources after transaction
 * completion, not even for cleanup. For example, Hibernate access is affected by this
 * issue, as outlined above in the discussion of transaction synchronization.
 *
 * <p>Spring's LocalSessionFactoryBean for Hibernate supports plugging a given
 * JTA TransactionManager into Hibernate's TransactionManagerLookup mechanism,
 * for Hibernate-driven cache synchronization and proper cleanup without warnings.
 * The same JTA TransactionManager configuration as above can be used in this case
 * (with a JndiObjectFactoryBean for a JNDI lookup, or one of the FactoryBeans),
 * avoiding double configuration. Alternatively, specify corresponding Hibernate
 * properties (see Hibernate docs for details).
 *
 * <p>JtaTransactionManager supports timeouts but not custom isolation levels.
 * Custom subclasses can override applyIsolationLevel for specific JTA
 * implementations. Note that some resource-specific transaction managers like
 * DataSourceTransactionManager and HibernateTransactionManager do support timeouts,
 * custom isolation levels, and transaction suspension without JTA's restrictions.
 *
 * <p>This class is serializable. Active synchronizations do not survive serialization.
 *
 * @author Juergen Hoeller
 * @since 24.03.2003
 * @see #setUserTransactionName
 * @see #setUserTransaction
 * @see #setTransactionManagerName
 * @see #setTransactionManager
 * @see #applyIsolationLevel
 * @see JotmFactoryBean
 * @see WebSphereTransactionManagerFactoryBean
 * @see WebLogicJtaTransactionManager
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see org.springframework.orm.hibernate.LocalSessionFactoryBean#setJtaTransactionManager
 * @see org.springframework.orm.hibernate.HibernateTransactionManager
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
 */
public class JtaTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {

	public static final String DEFAULT_USER_TRANSACTION_NAME = "java:comp/UserTransaction";


	private transient JndiTemplate jndiTemplate = new JndiTemplate();

	private String userTransactionName = DEFAULT_USER_TRANSACTION_NAME;

	private transient UserTransaction userTransaction;

	private String transactionManagerName;

	private transient TransactionManager transactionManager;

	private boolean autodetectTransactionManager = true;


	/**
	 * Create a new JtaTransactionManager instance, to be configured as bean.
	 * Invoke afterPropertiesSet to activate the configuration.
	 * @see #setUserTransactionName
	 * @see #setUserTransaction
	 * @see #setTransactionManagerName
	 * @see #setTransactionManager
	 * @see #afterPropertiesSet
	 */
	public JtaTransactionManager() {
		setNestedTransactionAllowed(true);
	}

	/**
	 * Create a new JtaTransactionManager instance.
	 * @param userTransaction the JTA UserTransaction to use as direct reference
	 */
	public JtaTransactionManager(UserTransaction userTransaction) {
		this();
		this.userTransaction = userTransaction;
		afterPropertiesSet();
	}

	/**
	 * Create a new JtaTransactionManager instance.
	 * @param userTransaction the JTA UserTransaction to use as direct reference
	 * @param transactionManager the JTA TransactionManager to use as direct reference
	 */
	public JtaTransactionManager(UserTransaction userTransaction, TransactionManager transactionManager) {
		this();
		this.userTransaction = userTransaction;
		this.transactionManager = transactionManager;
		afterPropertiesSet();
	}


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
	 * Return the JndiTemplate used for JNDI lookups.
	 */
	public JndiTemplate getJndiTemplate() {
		return jndiTemplate;
	}

	/**
	 * Set the JNDI name of the JTA UserTransaction.
	 * The J2EE default "java:comp/UserTransaction" is used if not set.
	 * @see #DEFAULT_USER_TRANSACTION_NAME
	 * @see #setUserTransaction
	 */
	public void setUserTransactionName(String userTransactionName) {
		this.userTransactionName = userTransactionName;
	}

	/**
	 * Set the JTA UserTransaction to use as direct reference.
	 * Typically just used for local JTA setups; in a J2EE environment,
	 * the UserTransaction will always be fetched from JNDI.
	 * @see #setUserTransactionName
	 */
	public void setUserTransaction(UserTransaction userTransaction) {
		this.userTransaction = userTransaction;
	}

	/**
	 * Return the JTA UserTransaction that this transaction manager uses.
	 */
	public UserTransaction getUserTransaction() {
		return userTransaction;
	}

	/**
	 * Set the JNDI name of the JTA TransactionManager.
	 * <p>A TransactionManager is necessary for suspending and resuming transactions,
	 * as this not supported by the UserTransaction interface.
	 * <p>Note that the TransactionManager will be autodetected if the JTA
	 * UserTransaction object implements the JTA TransactionManager interface too.
	 * @see #setTransactionManager
	 */
	public void setTransactionManagerName(String transactionManagerName) {
		this.transactionManagerName = transactionManagerName;
	}

	/**
	 * Set the JTA TransactionManager to use as direct reference.
	 * <p>A TransactionManager is necessary for suspending and resuming transactions,
	 * as this not supported by the UserTransaction interface.
	 * <p>Note that the TransactionManager will be autodetected if the JTA
	 * UserTransaction object implements the JTA TransactionManager interface too.
	 * @see #setTransactionManagerName
	 * @see #setAutodetectTransactionManager
	 */
	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Return the JTA TransactionManager that this transaction manager uses.
	 */
	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	/**
	 * Set whether to autodetect a JTA UserTransaction object that implements the
	 * JTA TransactionManager interface too. Default is true.
	 * <p>Can be turned off to deliberately ignore an available TransactionManager,
	 * for example when there are known issues with suspend/resume and any attempt
	 * to use REQUIRES_NEW or NOT_SUPPORTED should fail.
	 */
	public void setAutodetectTransactionManager(boolean autodetectTransactionManager) {
		this.autodetectTransactionManager = autodetectTransactionManager;
	}


	public void afterPropertiesSet() throws TransactionSystemException {
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
			else if (this.autodetectTransactionManager && this.userTransaction instanceof TransactionManager) {
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
	 * @throws TransactionSystemException if the JNDI lookup failed
	 * @see #setJndiTemplate
	 * @see #setUserTransactionName
	 */
	protected UserTransaction lookupUserTransaction(String userTransactionName)
			throws TransactionSystemException {
		try {
			Object jndiObj = getJndiTemplate().lookup(userTransactionName);
			if (!(jndiObj instanceof UserTransaction)) {
				throw new TransactionSystemException("Object [" + jndiObj + "] available at JNDI location [" +
						userTransactionName + "] does not implement javax.transaction.UserTransaction");
			}
			UserTransaction ut = (UserTransaction) jndiObj;
			if (logger.isInfoEnabled()) {
				logger.info("Using JTA UserTransaction [" + ut + "] from JNDI location [" + userTransactionName + "]");
			}
			return ut;
		}
		catch (NamingException ex) {
			throw new TransactionSystemException(
					"JTA UserTransaction is not available at JNDI location [" + userTransactionName + "]", ex);
		}
	}

	/**
	 * Look up the JTA TransactionManager in JNDI via the configured name.
	 * Called by afterPropertiesSet if no direct TransactionManager reference was set.
	 * Can be overridden in subclasses to provide a different TransactionManager object.
	 * @param transactionManagerName the JNDI name of the TransactionManager
	 * @return the UserTransaction object
	 * @throws TransactionSystemException if the JNDI lookup failed
	 * @see #setJndiTemplate
	 * @see #setTransactionManagerName
	 */
	protected TransactionManager lookupTransactionManager(String transactionManagerName)
			throws TransactionSystemException {
		try {
			Object jndiObj = getJndiTemplate().lookup(transactionManagerName);
			if (!(jndiObj instanceof TransactionManager)) {
				throw new TransactionSystemException(
						"Object [" + jndiObj + "] available at JNDI location [" +
						transactionManagerName + "] does not implement javax.transaction.TransactionManager");
			}
			TransactionManager tm = (TransactionManager) jndiObj;
			if (logger.isInfoEnabled()) {
				logger.info("Using JTA TransactionManager [" + tm + "] from JNDI location [" +
						transactionManagerName + "]");
			}
			return tm;
		}
		catch (NamingException ex) {
			throw new TransactionSystemException(
					"JTA TransactionManager is not available at JNDI location [" + transactionManagerName + "]", ex);
		}
	}


	/**
	 * This implementation returns a JtaTransactionObject instance for the
	 * JTA UserTransaction.
	 * <p>Note that JtaTransactionManager doesn't need a transaction object,
	 * as it will access the JTA UserTransaction respectively TransactionManager
	 * singletons that it holds directly. Therefore, any transaction object
	 * that's useful for status and identification purposes will do.
	 */
	protected Object doGetTransaction() {
		return new JtaTransactionObject(getUserTransaction());
	}

	protected boolean isExistingTransaction(Object transaction) {
		try {
			return (getUserTransaction().getStatus() != Status.STATUS_NO_TRANSACTION);
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on getStatus", ex);
		}
	}

	/**
	 * This implementation returns false to cause a further invocation
	 * of doBegin despite an already existing transaction.
	 * <p>JTA implementations might support nested transactions via further
	 * <code>UserTransaction.begin</code> invocations, but never support savepoints.
	 * @see #doBegin
	 * @see javax.transaction.UserTransaction#begin
	 */
	protected boolean useSavepointForNestedTransaction() {
		return false;
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) {
		logger.debug("Beginning JTA transaction");
		try {
			applyIsolationLevel(definition.getIsolationLevel());
			if (definition.getTimeout() > TransactionDefinition.TIMEOUT_DEFAULT) {
				getUserTransaction().setTransactionTimeout(definition.getTimeout());
			}
			getUserTransaction().begin();
		}
		catch (NotSupportedException ex) {
			// assume nested transaction not supported
			throw new NestedTransactionNotSupportedException(
			    "JTA implementation does not support nested transactions", ex);
		}
		catch (UnsupportedOperationException ex) {
			// assume nested transaction not supported
			throw new NestedTransactionNotSupportedException(
			    "JTA implementation does not support nested transactions", ex);
		}
		catch (SystemException ex) {
			throw new CannotCreateTransactionException("JTA failure on begin", ex);
		}
	}

	/**
	 * Apply the given transaction isolation level. Default implementation
	 * will throw an exception for any level other than ISOLATION_DEFAULT.
	 * To be overridden in subclasses for specific JTA implementations.
	 * @param isolationLevel isolation level taken from transaction definition
	 * @throws InvalidIsolationLevelException if the given isolation level
	 * cannot be applied
	 * @throws SystemException if thrown by the JTA implementation
	 * @see #getUserTransaction
	 * @see #getTransactionManager
	 */
	protected void applyIsolationLevel(int isolationLevel)
	    throws InvalidIsolationLevelException, SystemException {
		if (isolationLevel != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException(
			    "JtaTransactionManager does not support custom isolation levels");
		}
	}


	protected Object doSuspend(Object transaction) {
		if (getTransactionManager() == null) {
			throw new TransactionSuspensionNotSupportedException(
					"JtaTransactionManager needs a JTA TransactionManager for suspending a transaction - " +
					"specify the 'transactionManager' or 'transactionManagerName' property");
		}
		try {
			return doJtaSuspend();
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on suspend", ex);
		}
	}

	/**
	 * Perform a JTA suspend on the JTA TransactionManager.
	 * <p>Can be overridden in subclasses, for specific JTA implementations.
	 * @return the suspended JTA Transaction object
	 * @throws SystemException if thrown by JTA methods
	 * @see #getTransactionManager
	 */
	protected Transaction doJtaSuspend() throws SystemException {
		return getTransactionManager().suspend();
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		if (getTransactionManager() == null) {
			throw new TransactionSuspensionNotSupportedException(
					"JtaTransactionManager needs a JTA TransactionManager for suspending a transaction - " +
					"specify the 'transactionManager' or 'transactionManagerName' property");
		}
		try {
			doJtaResume((Transaction) suspendedResources);
		}
		catch (InvalidTransactionException ex) {
			throw new IllegalTransactionStateException("Tried to resume invalid JTA transaction", ex);
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on resume", ex);
		}
	}

	/**
	 * Perform a JTA resume on the JTA TransactionManager.
	 * <p>Can be overridden in subclasses, for specific JTA implementations.
	 * @param suspendedTransaction the suspended JTA Transaction object
	 * @throws InvalidTransactionException if thrown by JTA methods
	 * @throws SystemException if thrown by JTA methods
	 * @see #getTransactionManager
	 */
	protected void doJtaResume(Transaction suspendedTransaction)
	    throws InvalidTransactionException, SystemException {
		getTransactionManager().resume(suspendedTransaction);
	}


	protected void doCommit(DefaultTransactionStatus status) {
		logger.debug("Committing JTA transaction");
		try {
			getUserTransaction().commit();
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
		logger.debug("Rolling back JTA transaction");
		try {
			getUserTransaction().rollback();
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on rollback", ex);
		}
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		if (status.isDebug()) {
			logger.debug("Setting JTA transaction rollback-only");
		}
		try {
			getUserTransaction().setRollbackOnly();
		}
		catch (IllegalStateException ex) {
			throw new NoTransactionException("No active JTA transaction");
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on setRollbackOnly", ex);
		}
	}


	//---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException {
		// Rely on default serialization, just initialize state after deserialization.
		try {
			ois.defaultReadObject();
		}
		catch (ClassNotFoundException ex) {
			throw new IOException(
					"Failed to deserialize JtaTransactionManager - check that JTA and Spring transaction " +
					"libraries are available on the client side: " + ex.getMessage());
		}

		// do client-side JNDI lookup
		this.jndiTemplate = new JndiTemplate();

		// run lookup code for UserTransaction
		this.userTransaction = lookupUserTransaction(this.userTransactionName);
	}

}
