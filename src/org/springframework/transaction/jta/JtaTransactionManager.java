/*
 * Copyright 2002-2006 the original author or authors.
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
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
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
import org.springframework.transaction.support.TransactionSynchronization;

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
 * "transactionManager" property. The location of this well-defined JTA object is
 * <i>not</i> specified by J2EE; it is specific to each J2EE server, often kept
 * in JNDI like the JTA UserTransaction. Some well-known JNDI locations are:
 * <ul>
 * <li>"java:comp/UserTransaction" for Resin 2.x, Oracle OC4J (Orion),
 * JOnAS (JOTM), BEA WebLogic
 * <li>"java:comp/TransactionManager" for Resin 3.x
 * <li>"java:pm/TransactionManager" for Borland Enterprise Server and
 * Sun Application Server (Sun ONE 7 and later)
 * <li>"java:/TransactionManager" for JBoss Application Server
 * </ul>
 *
 * <p>All of these cases are autodetected by JtaTransactionManager (since Spring 1.2),
 * provided that the "autodetectTransactionManager" flag is set to "true" (which it is
 * by default). Consequently, JtaTransactionManager will support transaction suspension
 * out-of-the-box on many J2EE servers.
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
 * <p>It is also possible to specify a JTA TransactionManager only, either through
 * the corresponding constructor or through the "transactionManager" property.
 * In the latter case, the "userTransactionName" property needs to be set to <code>null</code>,
 * to suppress the default "java:comp/UserTransaction" JNDI lookup and thus enforce
 * use of the given JTA TransactionManager even for begin, commit and rollback.
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
 * particularly useful for closing resources with strict JTA implementations such as
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
 * <p><b>This standard JtaTransactionManager supports timeouts but not per-transaction
 * isolation levels.</b> Custom subclasses can override <code>doJtaBegin</code> for
 * specific JTA implementations to provide this functionality; Spring includes a
 * corresponding WebLogicJtaTransactionManager class, for example. Such adapters
 * for specific J2EE transaction coordinators can also expose transaction names
 * for monitoring; with standard JTA, transaction names will be ignored.
 *
 * <p><b>Consider using WebLogicJtaTransactionManager on BEA WebLogic, which supports
 * the full power of Spring's transaction definitions on WebLogic's transaction
 * coordinator</b>, <i>beyond standard JTA</i>: transaction names, per-transaction
 * isolation levels, and proper resuming of transactions in all cases.
 * WebLogicJtaTransactionManager automatically adapts to WebLogic 7.0 or 8.1+.
 *
 * <p>This class is serializable. Active synchronizations do not survive serialization,
 * though.
 *
 * @author Juergen Hoeller
 * @since 24.03.2003
 * @see #setUserTransactionName
 * @see #setUserTransaction
 * @see #setTransactionManagerName
 * @see #setTransactionManager
 * @see #doJtaBegin
 * @see JotmFactoryBean
 * @see WebSphereTransactionManagerFactoryBean
 * @see WebLogicJtaTransactionManager
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see org.springframework.orm.hibernate.LocalSessionFactoryBean#setJtaTransactionManager
 */
public class JtaTransactionManager extends AbstractPlatformTransactionManager
		implements InitializingBean, Serializable {

	/**
	 * Default JNDI location for the JTA UserTransaction. Many J2EE servers
	 * also provide support for the JTA TransactionManager interface there.
	 * @see #setUserTransactionName
	 * @see #setAutodetectTransactionManager
	 */
	public static final String DEFAULT_USER_TRANSACTION_NAME = "java:comp/UserTransaction";

	/**
	 * Fallback JNDI locations for the JTA TransactionManager. Applied if
	 * the JTA UserTransaction does not implement the JTA TransactionManager
	 * interface, provided that the "autodetectTransactionManager" flag is "true".
	 * @see #setTransactionManagerName
	 * @see #setAutodetectTransactionManager
	 */
	public static final String[] FALLBACK_TRANSACTION_MANAGER_NAMES =
			new String[] {"java:comp/TransactionManager", "java:pm/TransactionManager", "java:/TransactionManager"};


	private transient JndiTemplate jndiTemplate = new JndiTemplate();

	private transient UserTransaction userTransaction;

	private String userTransactionName = DEFAULT_USER_TRANSACTION_NAME;

	private boolean cacheUserTransaction = true;

	private transient TransactionManager transactionManager;

	private String transactionManagerName;

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
	 * Create a new JtaTransactionManager instance.
	 * @param transactionManager the JTA TransactionManager to use as direct reference
	 */
	public JtaTransactionManager(TransactionManager transactionManager) {
		this();
		// Do not attempt UserTransaction lookup: use given TransactionManager
		// to get a UserTransaction handle.
		this.userTransactionName = null;
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
	 * Set the JNDI environment to use for JNDI lookups.
	 * Creates a JndiTemplate with the given environment settings.
	 * @see #setJndiTemplate
	 */
	public void setJndiEnvironment(Properties jndiEnvironment) {
		this.jndiTemplate = new JndiTemplate(jndiEnvironment);
	}

	/**
	 * Return the JNDI environment to use for JNDI lookups.
	 */
	public Properties getJndiEnvironment() {
		return this.jndiTemplate.getEnvironment();
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
	 * Set the JNDI name of the JTA UserTransaction.
	 * The J2EE default "java:comp/UserTransaction" is used if not set.
	 * @see #DEFAULT_USER_TRANSACTION_NAME
	 * @see #setUserTransaction
	 */
	public void setUserTransactionName(String userTransactionName) {
		this.userTransactionName = userTransactionName;
	}

	/**
	 * Set whether to cache the JTA UserTransaction object fetched from JNDI.
	 * <p>Default is "true": UserTransaction lookup will only happen at startup,
	 * reusing the same UserTransaction handle for all transactions of all threads.
	 * This is the most efficient choice for all application servers that provide
	 * a shared UserTransaction object (the typical case).
	 * <p>Turn this flag off to enforce a fresh lookup of the UserTransaction
	 * for every transaction. This is only necessary for application servers
	 * that return a new UserTransaction for every transaction, keeping state
	 * tied to the UserTransaction object itself rather than the current thread.
	 */
	public void setCacheUserTransaction(boolean cacheUserTransaction) {
		this.cacheUserTransaction = cacheUserTransaction;
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
	 * Set whether to autodetect a JTA UserTransaction object that implements
	 * the JTA TransactionManager interface too (i.e. the JNDI location for the
	 * TransactionManager is "java:comp/UserTransaction", same as for the UserTransaction).
	 * Also checks the fallback JNDI locations "java:comp/TransactionManager" and
	 * "java:/TransactionManager". Will proceed without TransactionManager if none found.
	 * <p>Default is "true". Can be turned off to deliberately ignore an available
	 * TransactionManager, for example when there are known issues with suspend/resume
	 * and any attempt to use REQUIRES_NEW or NOT_SUPPORTED should fail fast.
	 * @see #FALLBACK_TRANSACTION_MANAGER_NAMES
	 */
	public void setAutodetectTransactionManager(boolean autodetectTransactionManager) {
		this.autodetectTransactionManager = autodetectTransactionManager;
	}


	public void afterPropertiesSet() throws TransactionSystemException {
		// Fetch JTA UserTransaction from JNDI, if necessary.
		if (this.userTransaction == null) {
			if (this.userTransactionName != null) {
				this.userTransaction = lookupUserTransaction(this.userTransactionName);
			}
			else {
				this.userTransaction = retrieveUserTransaction();
			}
		}

		// Fetch JTA TransactionManager from JNDI, if necessary.
		if (this.transactionManager == null) {
			if (this.transactionManagerName != null) {
				this.transactionManager = lookupTransactionManager(this.transactionManagerName);
			}
			else {
				this.transactionManager = retrieveTransactionManager();
			}
		}

		// Autodetect UserTransaction object that implements TransactionManager,
		// and check fallback JNDI locations else.
		if (this.transactionManager == null && this.autodetectTransactionManager) {
			this.transactionManager = findTransactionManager(this.userTransaction);
		}

		// If only JTA TransactionManager specified, create UserTransaction handle for it.
		if (this.userTransaction == null && this.transactionManager != null) {
			if (this.transactionManager instanceof UserTransaction) {
				this.userTransaction = (UserTransaction) this.transactionManager;
			}
			else {
				this.userTransaction = new UserTransactionAdapter(this.transactionManager);
			}
		}

		// We at least need the JTA UserTransaction.
		if (this.userTransaction != null) {
			if (logger.isInfoEnabled()) {
				logger.info("Using JTA UserTransaction: " + this.userTransaction);
			}
		}
		else {
			throw new IllegalArgumentException(
					"Either 'userTransaction' or 'userTransactionName' or 'transactionManager' " +
					"or 'transactionManagerName' must be set");
		}

		// For transaction suspension, the JTA TransactionManager is necessary too.
		if (this.transactionManager != null) {
			if (logger.isInfoEnabled()) {
				logger.info("Using JTA TransactionManager: " + this.transactionManager);
			}
		}
		else {
			logger.warn("No JTA TransactionManager found: " +
					"transaction suspension and synchronization with existing JTA transactions not available");
		}
	}


	/**
	 * Look up the JTA UserTransaction in JNDI via the configured name.
	 * Called by <code>afterPropertiesSet</code> if no direct UserTransaction reference was set.
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
			if (logger.isDebugEnabled()) {
				logger.debug("Retrieving JTA UserTransaction from JNDI location [" + userTransactionName + "]");
			}
			UserTransaction ut = (UserTransaction)
					getJndiTemplate().lookup(userTransactionName, UserTransaction.class);
			return ut;
		}
		catch (NamingException ex) {
			throw new TransactionSystemException(
					"JTA UserTransaction is not available at JNDI location [" + userTransactionName + "]", ex);
		}
	}

	/**
	 * Look up the JTA TransactionManager in JNDI via the configured name.
	 * Called by <code>afterPropertiesSet</code> if no direct TransactionManager reference was set.
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
			if (logger.isDebugEnabled()) {
				logger.debug("Retrieving JTA TransactionManager from JNDI location [" + transactionManagerName + "]");
			}
			TransactionManager tm = (TransactionManager)
					getJndiTemplate().lookup(transactionManagerName, TransactionManager.class);
			return tm;
		}
		catch (NamingException ex) {
			throw new TransactionSystemException(
					"JTA TransactionManager is not available at JNDI location [" + transactionManagerName + "]", ex);
		}
	}

	/**
	 * Allows subclasses to retrieve the JTA UserTransaction in a vendor-specific manner.
	 * Only called if no "userTransaction" or "userTransactionName" specified.
	 * <p>Default implementation simply returns null.
	 * @return the JTA UserTransaction handle to use, or <code>null</code> if none found
	 * @throws TransactionSystemException in case of errors
	 * @see #setUserTransaction
	 * @see #setUserTransactionName
	 */
	protected UserTransaction retrieveUserTransaction() throws TransactionSystemException {
		return null;
	}

	/**
	 * Allows subclasses to retrieve the JTA TransactionManager in a vendor-specific manner.
	 * Only called if no "transactionManager" or "transactionManagerName" specified.
	 * <p>Default implementation simply returns null.
	 * @return the JTA TransactionManager handle to use, or <code>null</code> if none found
	 * @throws TransactionSystemException in case of errors
	 * @see #setTransactionManager
	 * @see #setTransactionManagerName
	 */
	protected TransactionManager retrieveTransactionManager() throws TransactionSystemException {
		return null;
	}

	/**
	 * Find the JTA TransactionManager through autodetection:
	 * checking whether the UserTransaction object implements the
	 * TransactionManager, and checking the fallback JNDI locations.
	 * @param ut the JTA UserTransaction object
	 * @return the JTA TransactionManager reference, or <code>null</code> if not found
	 * @see #FALLBACK_TRANSACTION_MANAGER_NAMES
	 */
	protected TransactionManager findTransactionManager(UserTransaction ut) {
		if (ut instanceof TransactionManager) {
			if (logger.isDebugEnabled()) {
				logger.debug("JTA UserTransaction object [" + ut + "] implements TransactionManager");
			}
			return (TransactionManager) ut;
		}

		// Check fallback JNDI locations.
		for (int i = 0; i < FALLBACK_TRANSACTION_MANAGER_NAMES.length; i++) {
			String jndiName = FALLBACK_TRANSACTION_MANAGER_NAMES[i];
			try {
				TransactionManager tm = lookupTransactionManager(jndiName);
				if (logger.isDebugEnabled()) {
					logger.debug("JTA TransactionManager found at fallback JNDI location [" + jndiName + "]");
				}
				return tm;
			}
			catch (TransactionSystemException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("No JTA TransactionManager found at fallback JNDI location [" + jndiName + "]", ex);
				}
			}
		}

		// OK, so no JTA TransactionManager is available...
		return null;
	}


	/**
	 * This implementation returns a JtaTransactionObject instance for the
	 * JTA UserTransaction.
	 * <p>The UserTransaction object will either be looked up freshly for the
	 * current transaction, or the cached one looked up at startup will be used.
	 * The latter is the default: Most application servers use a shared singleton
	 * UserTransaction that can be cached. Turn off the "cacheUserTransaction"
	 * flag to enforce a fresh lookup for every transaction.
	 * @see #setCacheUserTransaction
	 */
	protected Object doGetTransaction() {
		UserTransaction ut = getUserTransaction();
		if (!this.cacheUserTransaction && this.userTransactionName != null) {
			ut = lookupUserTransaction(this.userTransactionName);
		}
		return doGetJtaTransaction(ut);
	}

	/**
	 * Get a JTA transaction object for the given current UserTransaction.
	 * <p>Subclasses can override this to provide a JtaTransactionObject
	 * subclass, for example holding some additional JTA handle needed.
	 * @param ut the UserTransaction handle to use for the current transaction
	 * @return the JtaTransactionObject holding the UserTransaction
	 */
	protected JtaTransactionObject doGetJtaTransaction(UserTransaction ut) {
		return new JtaTransactionObject(ut);
	}

	protected boolean isExistingTransaction(Object transaction) {
		JtaTransactionObject txObject = (JtaTransactionObject) transaction;
		try {
			return (txObject.getUserTransaction().getStatus() != Status.STATUS_NO_TRANSACTION);
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
		JtaTransactionObject txObject = (JtaTransactionObject) transaction;
		try {
			doJtaBegin(txObject, definition);
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
	 * Perform a JTA begin on the JTA UserTransaction or TransactionManager.
	 * <p>This implementation only supports standard JTA functionality:
	 * that is, no per-transaction isolation levels and no transaction names.
	 * Can be overridden in subclasses, for specific JTA implementations.
	 * <p>Calls <code>applyIsolationLevel</code> and <code>applyTimeout</code>
	 * before invoking the UserTransaction's <code>begin</code> method.
	 * @param txObject the JtaTransactionObject containing the UserTransaction
	 * @param definition TransactionDefinition instance, describing propagation
	 * behavior, isolation level, read-only flag, timeout, and transaction name
	 * @throws NotSupportedException if thrown by JTA methods
	 * @throws SystemException if thrown by JTA methods
	 * @see #getUserTransaction
	 * @see #getTransactionManager
	 * @see #applyIsolationLevel
	 * @see #applyTimeout
	 * @see JtaTransactionObject#getUserTransaction()
	 * @see javax.transaction.UserTransaction#setTransactionTimeout
	 * @see javax.transaction.UserTransaction#begin
	 */
	protected void doJtaBegin(JtaTransactionObject txObject, TransactionDefinition definition)
			throws NotSupportedException, SystemException {

		applyIsolationLevel(txObject, definition.getIsolationLevel());
		applyTimeout(txObject, definition.getTimeout());
		txObject.getUserTransaction().begin();
	}

	/**
	 * Apply the given transaction isolation level. Default implementation
	 * will throw an exception for any level other than ISOLATION_DEFAULT.
	 * <p>To be overridden in subclasses for specific JTA implementations,
	 * as alternative to overriding the full <code>doJtaBegin</code> method.
	 * @param txObject the JtaTransactionObject containing the UserTransaction
	 * @param isolationLevel isolation level taken from transaction definition
	 * @throws InvalidIsolationLevelException if the given isolation level
	 * cannot be applied
	 * @throws SystemException if thrown by the JTA implementation
	 * @see #doJtaBegin
	 * @see JtaTransactionObject#getUserTransaction()
	 * @see #getTransactionManager()
	 */
	protected void applyIsolationLevel(JtaTransactionObject txObject, int isolationLevel)
	    throws InvalidIsolationLevelException, SystemException {

		if (isolationLevel != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException(
			    "JtaTransactionManager does not support custom isolation levels");
		}
	}

	/**
	 * Apply the given transaction timeout. Default implementation will call
	 * <code>setTransactionTimeout</code> for a non-default timeout value.
	 * @param txObject the JtaTransactionObject containing the UserTransaction
	 * @param timeout timeout value taken from transaction definition
	 * @throws SystemException if thrown by the JTA implementation
	 * @see #doJtaBegin
	 * @see JtaTransactionObject#getUserTransaction()
	 * @see javax.transaction.UserTransaction#setTransactionTimeout(int)
	 */
	protected void applyTimeout(JtaTransactionObject txObject, int timeout) throws SystemException {
		if (timeout > TransactionDefinition.TIMEOUT_DEFAULT) {
			txObject.getUserTransaction().setTransactionTimeout(timeout);
		}
	}


	protected Object doSuspend(Object transaction) {
		JtaTransactionObject txObject = (JtaTransactionObject) transaction;
		try {
			return doJtaSuspend(txObject);
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on suspend", ex);
		}
	}

	/**
	 * Perform a JTA suspend on the JTA TransactionManager.
	 * <p>Can be overridden in subclasses, for specific JTA implementations.
	 * @param txObject the JtaTransactionObject containing the UserTransaction
	 * @return the suspended JTA Transaction object
	 * @throws SystemException if thrown by JTA methods
	 * @see #getTransactionManager()
	 * @see javax.transaction.TransactionManager#suspend()
	 */
	protected Object doJtaSuspend(JtaTransactionObject txObject) throws SystemException {
		if (getTransactionManager() == null) {
			throw new TransactionSuspensionNotSupportedException(
					"JtaTransactionManager needs a JTA TransactionManager for suspending a transaction: " +
					"specify the 'transactionManager' or 'transactionManagerName' property");
		}
		return getTransactionManager().suspend();
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		JtaTransactionObject txObject = (JtaTransactionObject) transaction;
		try {
			doJtaResume(txObject, suspendedResources);
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
	 * @param txObject the JtaTransactionObject containing the UserTransaction
	 * @param suspendedTransaction the suspended JTA Transaction object
	 * @throws InvalidTransactionException if thrown by JTA methods
	 * @throws SystemException if thrown by JTA methods
	 * @see #getTransactionManager()
	 * @see javax.transaction.TransactionManager#resume(javax.transaction.Transaction)
	 */
	protected void doJtaResume(JtaTransactionObject txObject, Object suspendedTransaction)
	    throws InvalidTransactionException, SystemException {

		if (getTransactionManager() == null) {
			throw new TransactionSuspensionNotSupportedException(
					"JtaTransactionManager needs a JTA TransactionManager for suspending a transaction: " +
					"specify the 'transactionManager' or 'transactionManagerName' property");
		}
		getTransactionManager().resume((Transaction) suspendedTransaction);
	}


	/**
	 * This implementation returns "true": a JTA commit will properly handle
	 * transactions that have been marked rollback-only at a global level.
	 */
	protected boolean shouldCommitOnGlobalRollbackOnly() {
		return true;
	}

	protected void doCommit(DefaultTransactionStatus status) {
		JtaTransactionObject txObject = (JtaTransactionObject) status.getTransaction();
		try {
			txObject.getUserTransaction().commit();
		}
		catch (RollbackException ex) {
			throw new UnexpectedRollbackException(
					"JTA transaction unexpectedly rolled back (maybe due to a timeout)", ex);
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
		JtaTransactionObject txObject = (JtaTransactionObject) status.getTransaction();
		try {
			if (txObject.getUserTransaction().getStatus() != Status.STATUS_NO_TRANSACTION) {
				txObject.getUserTransaction().rollback();
			}
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on rollback", ex);
		}
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		JtaTransactionObject txObject = (JtaTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Setting JTA transaction rollback-only");
		}
		try {
			txObject.getUserTransaction().setRollbackOnly();
		}
		catch (IllegalStateException ex) {
			throw new NoTransactionException("No active JTA transaction");
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on setRollbackOnly", ex);
		}
	}


	protected void registerAfterCompletionWithExistingTransaction(Object transaction, List synchronizations) {
		JtaTransactionObject txObject = (JtaTransactionObject) transaction;
		logger.debug("Registering after-completion synchronization with existing JTA transaction");
		try {
			doRegisterAfterCompletionWithJtaTransaction(txObject, synchronizations);
		}
		catch (RollbackException ex) {
			logger.debug("Participating in existing JTA transaction that has been marked rollback-only: " +
					"cannot register Spring after-completion callbacks with outer JTA transaction - " +
					"immediately performing Spring after-completion callbacks with outcome status 'rollback'");
			invokeAfterCompletion(synchronizations, TransactionSynchronization.STATUS_ROLLED_BACK);
		}
		catch (IllegalStateException ex) {
			throw new NoTransactionException("No active JTA transaction");
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on registerSynchronization", ex);
		}
	}

	/**
	 * Register a JTA synchronization on the JTA TransactionManager, for calling
	 * <code>afterCompletion</code> on the given Spring TransactionSynchronizations.
	 * <p>Can be overridden in subclasses, for specific JTA implementations.
	 * @param synchronizations List of TransactionSynchronization objects
	 * @throws RollbackException if thrown by JTA methods
	 * @throws SystemException if thrown by JTA methods
	 * @see #getTransactionManager()
	 * @see javax.transaction.Transaction#registerSynchronization
	 * @see #invokeAfterCompletion(java.util.List, int)
	 */
	protected void doRegisterAfterCompletionWithJtaTransaction(
			JtaTransactionObject txObject, List synchronizations)
			throws RollbackException, SystemException {

		if (getTransactionManager() != null) {
			getTransactionManager().getTransaction().registerSynchronization(
					new JtaAfterCompletionSynchronization(synchronizations));
		}
		else {
			// No JTA TransactionManager available.
			logger.warn("Participating in existing JTA transaction, but no JTA TransactionManager available: " +
					"cannot register Spring after-completion callbacks with outer JTA transaction - " +
					"performing Spring after-completion callbacks with outcome status 'unknown'");
			invokeAfterCompletion(synchronizations, TransactionSynchronization.STATUS_UNKNOWN);
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

		// Do client-side JNDI lookup.
		this.jndiTemplate = new JndiTemplate();

		// Perform lookup for JTA UserTransaction and TransactionManager.
		if (this.userTransactionName != null) {
			this.userTransaction = lookupUserTransaction(this.userTransactionName);
		}
		if (this.transactionManagerName != null) {
			this.transactionManager = lookupTransactionManager(this.transactionManagerName);
		}
	}


	/**
	 * Adapter for a JTA Synchronization, invoking the <code>afterCompletion</code> of
	 * Spring TransactionSynchronizations after the outer JTA transaction has completed.
	 * Applied when participating in an existing (non-Spring) JTA transaction.
	 */
	private class JtaAfterCompletionSynchronization implements Synchronization {

		private final List synchronizations;

		public JtaAfterCompletionSynchronization(List synchronizations) {
			this.synchronizations = synchronizations;
		}

		public void beforeCompletion() {
		}

		public void afterCompletion(int status) {
			switch (status) {
				case Status.STATUS_COMMITTED:
					invokeAfterCompletion(this.synchronizations, TransactionSynchronization.STATUS_COMMITTED);
					break;
				case Status.STATUS_ROLLEDBACK:
					invokeAfterCompletion(this.synchronizations, TransactionSynchronization.STATUS_ROLLED_BACK);
					break;
				default:
					invokeAfterCompletion(this.synchronizations, TransactionSynchronization.STATUS_UNKNOWN);
			}
		}
	}

}
