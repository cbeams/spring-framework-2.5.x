package org.springframework.transaction;

import java.sql.Connection;

/**
 * Interface for classes that define transaction properties.
 * Base interface for TransactionAttribute.
 *
 * <p>Note that isolation level, timeout, and read-only settings will only
 * get applied when starting a new transaction. As only propagation behavior
 * PROPAGATION_REQUIRED can actually cause that, it doesn't make sense to
 * specify isolation level or timeout else. Furthermore, not all transaction
 * managers will support those features and thus throw respective exceptions
 * when given non-default values.
 *
 * @author Juergen Hoeller
 * @since 08.05.2003
 * @see org.springframework.transaction.support.DefaultTransactionDefinition
 * @see org.springframework.transaction.interceptor.TransactionAttribute
 */
public interface TransactionDefinition {

	String PROPAGATION_CONSTANT_PREFIX = "PROPAGATION";

	String ISOLATION_CONSTANT_PREFIX = "ISOLATION";

	/**
	 * Support a current transaction, create a new one if none exists.
	 * Analogous to EJB transaction attribute of the same name.
	 * <p>This is typically the default setting of a transaction definition.
	 */
	int PROPAGATION_REQUIRED = 0;

	/**
	 * Support a current transaction, execute non-transactional if none exists.
	 * Analogous to EJB transaction attribute of the same name.
	 */
	int PROPAGATION_SUPPORTS = 1;

	/**
	 * Support a current transaction, throw an exception if none exists.
	 * Analogous to EJB transaction attribute of the same name.
	 */
	int PROPAGATION_MANDATORY = 2;

	/**
	 * Use the default isolation level of the underlying datastore.
	 * All other levels correspond to the JDBC isolation levels.
	 * @see java.sql.Connection
	 */
	int ISOLATION_DEFAULT          = -1;

	int ISOLATION_READ_UNCOMMITTED = Connection.TRANSACTION_READ_UNCOMMITTED;

	int ISOLATION_READ_COMMITTED   = Connection.TRANSACTION_READ_COMMITTED;

	int ISOLATION_REPEATABLE_READ  = Connection.TRANSACTION_REPEATABLE_READ;

	int ISOLATION_SERIALIZABLE     = Connection.TRANSACTION_SERIALIZABLE;

	/**
	 * Use the default timeout of the underlying transaction system,
	 * respectively none if timeouts are not supported. 
	 */
	int TIMEOUT_DEFAULT = -1;

	/**
	 * Return the propagation behavior.
	 * Must return one of the PROPAGATION constants.
	 * @see #PROPAGATION_REQUIRED
	 */
	int getPropagationBehavior();

	/**
	 * Return the isolation level.
	 * Must return one of the ISOLATION constants.
	 * <p>Only makes sense in combination with PROPAGATION_REQUIRED.
	 * Note that a transaction manager that does not support custom isolation levels
	 * will throw an exception when given any other level than ISOLATION_DEFAULT.
	 * @see #ISOLATION_DEFAULT
	 */
	int getIsolationLevel();

	/**
	 * Return the transaction timeout.
	 * Must return a number of seconds, or TIMEOUT_DEFAULT.
	 * <p>Only makes sense in combination with PROPAGATION_REQUIRED.
	 * Note that a transaction manager that does not support timeouts will
	 * throw an exception when given any other timeout than TIMEOUT_DEFAULT.
	 * @see #TIMEOUT_DEFAULT
	 */
	int getTimeout();

	/**
	 * Return whether to optimize as read-only transaction.
	 * This just serves as hint for the actual transaction subsystem,
	 * it will <i>not necessarily</i> cause failure of write accesses.
	 * <p>Only makes sense in combination with PROPAGATION_REQUIRED.
	 * A transaction manager that cannot interpret the read-only hint
	 * will <i>not</i> throw an exception when given readOnly=true.
	 */
	boolean isReadOnly();

}
