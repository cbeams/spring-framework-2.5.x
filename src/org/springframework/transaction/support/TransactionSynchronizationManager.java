package org.springframework.transaction.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manages resources and transactions synchronizations per thread.
 * Supports one resource per key without overwriting, i.e. a resource needs to
 * be removed before a new one can be set for the same key.
 * Supports a list of transaction synchronizations if synchronization is active.
 *
 * <p>Resource management code should check for thread-bound resources, e.g. JDBC
 * Connections or Hibernate Sessions, via getResource. It is normally not supposed
 * to bind resources to threads, as this is the responsiblity of transaction managers.
 * An exception is binding on first use if transaction synchronization is active.
 *
 * <p>Transaction synchronization must be activated and deactivated by a transaction
 * manager via initSynchronization and clearSynchronization. Automatically supported
 * by AbstractPlatformTransactionManager, and thus by all standard Spring transaction
 * managers, like JtaTransactionManager.
 *
 * <p>Resource management code should only register synchronizations when this
 * manager is active, and perform resource cleanup immediately else.
 * If transaction synchronization isn't active, there is either no current
 * transaction, or the transaction manager doesn't support synchronizations.
 *
 * <p>Synchronization is for example used to always return the same resources like
 * JDBC Connections or Hibernate Sessions within a JTA transaction, for any given
 * DataSource or SessionFactory. In the Hibernate case, the afterCompletion Session
 * close calls allow for proper transactional handling of the JVM-level cache with JTA.
 *
 * @author Juergen Hoeller
 * @since 02.06.2003
 * @see #isSynchronizationActive
 * @see #registerSynchronization
 * @see TransactionSynchronization
 * @see AbstractPlatformTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection
 * @see org.springframework.orm.hibernate.SessionFactoryUtils#getSession
 * @see org.springframework.orm.jdo.PersistenceManagerFactoryUtils#getPersistenceManager
 */
public abstract class TransactionSynchronizationManager {

	private static final Log logger = LogFactory.getLog(TransactionSynchronizationManager.class);

	private static ThreadLocal resources = new ThreadLocal() {
		protected Object initialValue() {
			return new HashMap();
		}
	};

	private static final ThreadLocal synchronizations = new ThreadLocal();


	/**
	 * Return all resources that are bound to the current thread.
	 * <p>Mainly for debugging purposes. Resource managers should always invoke
	 * hasResource for a specific resource key that they are interested in.
	 * @return Map with resource keys and resource objects
	 * @see #hasResource
	 */
	public static Map getResourceMap() {
		return (Map) resources.get();
	}

	/**
	 * Check if there is a resource for the given key bound to the current thread.
	 * @param key key to check
	 * @return if there is a value bound to the current thread
	 */
	public static boolean hasResource(Object key) {
		return getResourceMap().containsKey(key);
	}

	/**
	 * Retrieve a resource for the given key that is bound to the current thread.
	 * @param key key to check
	 * @return a value bound to the current thread, or null if none
	 */
	public static Object getResource(Object key) {
		Object value = getResourceMap().get(key);
		if (value != null && logger.isDebugEnabled()) {
			logger.debug("Retrieved value [" + value + "] for key [" + key + "] bound to thread [" + Thread.currentThread().getName() + "]");
		}
		return value;
	}

	/**
	 * Bind the given resource for the given key to the current thread.
	 * @param key key to bind the value to
	 * @param value value to bind
	 * @throws java.lang.IllegalStateException if there is already a value bound to the thread
	 */
	public static void bindResource(Object key, Object value) {
		if (hasResource(key)) {
			throw new IllegalStateException("Already a value for key [" + key + "] bound to thread");
		}
		getResourceMap().put(key, value);
		if (logger.isDebugEnabled()) {
			logger.debug("Bound value [" + value + "] for key [" + key + "] to thread [" + Thread.currentThread().getName() + "]");
		}
	}

	/**
	 * Unbind a resource for the given key from the current thread.
	 * @param key key to check
	 * @throws java.lang.IllegalStateException if there is no value bound to the thread
	 */
	public static void unbindResource(Object key) {
		if (!hasResource(key)) {
			throw new IllegalStateException("No value for key [" + key + "] bound to thread");
		}
		Object value = getResourceMap().remove(key);
		if (logger.isDebugEnabled()) {
			logger.debug("Removed value [" + value + "] for key [" + key + "] from thread [" + Thread.currentThread().getName() + "]");
		}
	}


	/**
	 * Activate thread synchronizations for the current thread.
	 * Called by transaction manager on transaction begin.
	 */
	public static void initSynchronization() {
		synchronizations.set(new ArrayList());
	}

	/**
	 * Return if thread synchronizations are active for the current thread.
	 * Can be called before register to avoid unnecessary instance creation.
	 * @see #registerSynchronization
	 */
	public static boolean isSynchronizationActive() {
		return (synchronizations.get() != null);
	}

	/**
	 * Register a new JTA synchronization for the current thread.
	 * Called by resource management code.
	 * Calls get ignored if transaction synchronization isn't active.
	 */
	public static void registerSynchronization(TransactionSynchronization synchronization) {
		if (isSynchronizationActive()) {
			((List) synchronizations.get()).add(synchronization);
		}
	}

	/**
	 * Trigger beforeCommit calls for the current thread.
	 * Called by transaction manager before transaction commit.
	 * <p>Calls get ignored if transaction synchronization isn't active.
	 * @see TransactionSynchronization#beforeCommit
	 */
	public static void triggerBeforeCommit() throws RuntimeException {
		if (isSynchronizationActive()) {
			for (Iterator it = ((List) synchronizations.get()).iterator(); it.hasNext();) {
				TransactionSynchronization synchronization = (TransactionSynchronization) it.next();
				synchronization.beforeCommit();
			}
		}
	}

	/**
	 * Trigger beforeCompletion calls for the current thread.
	 * Called by transaction manager before transaction commit/rollback.
	 * <p>Calls get ignored if transaction synchronization isn't active.
	 * @see TransactionSynchronization#beforeCommit
	 */
	public static void triggerBeforeCompletion() throws RuntimeException {
		if (isSynchronizationActive()) {
			for (Iterator it = ((List) synchronizations.get()).iterator(); it.hasNext();) {
				TransactionSynchronization synchronization = (TransactionSynchronization) it.next();
				synchronization.beforeCompletion();
			}
		}
	}

	/**
	 * Trigger afterCompletion calls for the current thread.
	 * Called by transaction manager after transaction commit/rollback.
	 * <p>Calls get ignored if transaction synchronization isn't active.
	 * @param status completion status according to TransactionSynchronization constants
	 * @see TransactionSynchronization#afterCompletion
	 */
	public static void triggerAfterCompletion(int status) throws RuntimeException {
		if (isSynchronizationActive()) {
			for (Iterator it = ((List) synchronizations.get()).iterator(); it.hasNext();) {
				TransactionSynchronization synchronization = (TransactionSynchronization) it.next();
				synchronization.afterCompletion(status);
			}
		}
	}

	/**
	 * Deactivate thread synchronizations for the current thread.
	 * Called by transaction manager on transaction cleanup.
	 */
	public static void clearSynchronization() {
		synchronizations.set(null);
	}

}
