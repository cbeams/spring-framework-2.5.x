/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.transaction;

import java.sql.Connection;

/**
 * Interface for classes that define transaction properties.
 * Base interface for TransactionAttribute.
 *
 * <p>Note that isolation level, timeout and read-only settings will not get
 * applied unless a new transaction gets started. As only PROPAGATION_REQUIRED
 * and PROPAGATION_REQUIRES_NEW can actually cause that, it usually doesn't make
 * sense to specify those settings in all other cases. Furthermore, be aware
 * that not all transaction managers will support those advanced features and
 * thus might throw corresponding exceptions when given non-default values.
 *
 * @author Juergen Hoeller
 * @since 08.05.2003
 * @see PlatformTransactionManager#getTransaction(TransactionDefinition)
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
	 * Support a current transaction, execute non-transactionally if none exists.
	 * Analogous to EJB transaction attribute of the same name.
	 * <p>Note: For transaction managers with transaction synchronization,
	 * PROPAGATION_SUPPORTS is slightly different from no transaction at all,
	 * as it defines a transaction scopp that synchronization will apply for.
	 * As a consequence, the same resources (JDBC Connection, Hibernate Session, etc)
	 * will be shared for the entire specified scope. Note that this depends on
	 * the actual synchronization configuration of the transaction manager.
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setTransactionSynchronization
	 */
	int PROPAGATION_SUPPORTS = 1;

	/**
	 * Support a current transaction, throw an exception if none exists.
	 * Analogous to EJB transaction attribute of the same name.
	 */
	int PROPAGATION_MANDATORY = 2;

	/**
	 * Create a new transaction, suspend the current transaction if one exists.
	 * Analogous to EJB transaction attribute of the same name.
	 * <p>Note: Actual transaction suspension will not work on out-of-the-box
	 * on all transaction managers. This in particular applies to JtaTransactionManager,
	 * which requires the <code>javax.transaction.TransactionManager</code> to be
	 * made available it to it (which is server-specific in standard J2EE).
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
	 */
	int PROPAGATION_REQUIRES_NEW = 3;

	/**
	 * Execute non-transactionally, suspend the current transaction if one exists.
	 * Analogous to EJB transaction attribute of the same name.
	 * <p>Note: Actual transaction suspension will not work on out-of-the-box
	 * on all transaction managers. This in particular applies to JtaTransactionManager,
	 * which requires the <code>javax.transaction.TransactionManager</code> to be
	 * made available it to it (which is server-specific in standard J2EE).
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
	 */
	int PROPAGATION_NOT_SUPPORTED = 4;

	/**
	 * Execute non-transactionally, throw an exception if a transaction exists.
	 * Analogous to EJB transaction attribute of the same name.
	 */
	int PROPAGATION_NEVER = 5;

	/**
	 * Execute within a nested transaction if a current transaction exists,
	 * behave like PROPAGATION_REQUIRED else. There is no analogous feature in EJB.
	 * <p>Note: Actual creation of a nested transaction will only work on specific
	 * transaction managers. Out of the box, this only applies to the JDBC
	 * DataSourceTransactionManager when working on a JDBC 3.0 driver.
	 * Some JTA providers might support nested transactions as well.
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
	 */
	int PROPAGATION_NESTED = 6;


	/**
	 * Use the default isolation level of the underlying datastore.
	 * All other levels correspond to the JDBC isolation levels.
	 * @see java.sql.Connection
	 */
	int ISOLATION_DEFAULT          = -1;

	/**
	 * A constant indicating that dirty reads, non-repeatable reads and phantom reads
	 * can occur. This level allows a row changed by one transaction to be read by
	 * another transaction before any changes in that row have been committed
	 * (a "dirty read"). If any of the changes are rolled back, the second
	 * transaction will have retrieved an invalid row.
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 */
	int ISOLATION_READ_UNCOMMITTED = Connection.TRANSACTION_READ_UNCOMMITTED;

	/**
	 * A constant indicating that dirty reads are prevented; non-repeatable reads
	 * and phantom reads can occur. This level only prohibits a transaction
	 * from reading a row with uncommitted changes in it.
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 */
	int ISOLATION_READ_COMMITTED   = Connection.TRANSACTION_READ_COMMITTED;

	/**
	 * A constant indicating that dirty reads and non-repeatable reads are
	 * prevented; phantom reads can occur. This level prohibits a transaction
	 * from reading a row with uncommitted changes in it, and it also prohibits
	 * the situation where one transaction reads a row, a second transaction
	 * alters the row, and the first transaction rereads the row, getting
	 * different values the second time (a "non-repeatable read").
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 */
	int ISOLATION_REPEATABLE_READ  = Connection.TRANSACTION_REPEATABLE_READ;

	/**
	 * A constant indicating that dirty reads, non-repeatable reads and phantom
	 * reads are prevented. This level includes the prohibitions in
	 * <code>ISOLATION_REPEATABLE_READ</code> and further prohibits the situation
	 * where one transaction reads all rows that satisfy a <code>WHERE</code>
	 * condition, a second transaction inserts a row that satisfies that
	 * <code>WHERE</code> condition, and the first transaction rereads for the
	 * same condition, retrieving the additional "phantom" row in the second read.
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 */
	int ISOLATION_SERIALIZABLE     = Connection.TRANSACTION_SERIALIZABLE;


	/**
	 * Use the default timeout of the underlying transaction system,
	 * or none if timeouts are not supported. 
	 */
	int TIMEOUT_DEFAULT = -1;


	/**
	 * Return the propagation behavior.
	 * Must return one of the PROPAGATION constants.
	 * @see #PROPAGATION_REQUIRED
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isActualTransactionActive()
	 */
	int getPropagationBehavior();

	/**
	 * Return the isolation level.
	 * Must return one of the ISOLATION constants.
	 * <p>Only makes sense in combination with PROPAGATION_REQUIRED or
	 * PROPAGATION_REQUIRES_NEW.
	 * <p>Note that a transaction manager that does not support custom
	 * isolation levels will throw an exception when given any other level
	 * than ISOLATION_DEFAULT.
	 * @see #ISOLATION_DEFAULT
	 */
	int getIsolationLevel();

	/**
	 * Return the transaction timeout.
	 * Must return a number of seconds, or TIMEOUT_DEFAULT.
	 * <p>Only makes sense in combination with PROPAGATION_REQUIRED or
	 * PROPAGATION_REQUIRES_NEW.
	 * <p>Note that a transaction manager that does not support timeouts will
	 * throw an exception when given any other timeout than TIMEOUT_DEFAULT.
	 * @see #TIMEOUT_DEFAULT
	 */
	int getTimeout();

	/**
	 * Return whether to optimize as read-only transaction.
	 * This just serves as a hint for the actual transaction subsystem,
	 * it will <i>not necessarily</i> cause failure of write access attempts.
	 * <p>Intended to be used in combination with PROPAGATION_REQUIRED or
	 * PROPAGATION_REQUIRES_NEW. Beyond optimizing such actual transactions
	 * accordingly, a transaction manager might also pass the read-only flag
	 * to transaction synchronizations, even outside an actual transaction.
	 * <p>A transaction manager that cannot interpret the read-only hint
	 * will <i>not</i> throw an exception when given readOnly=true.
	 * @see org.springframework.transaction.support.TransactionSynchronization#beforeCommit(boolean)
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isCurrentTransactionReadOnly()
	 */
	boolean isReadOnly();

	/**
	 * Return the name of this transaction. Can be <code>null</code>.
	 * This will be used as transaction name to be shown in a
	 * transaction monitor, if applicable (for example, WebLogic's).
	 * <p>In case of Spring's declarative transactions, the exposed name will
	 * be the fully-qualified class name + "." + method name (by default).
	 * @see org.springframework.transaction.interceptor.TransactionAspectSupport
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#getCurrentTransactionName()
	 */
	String getName();

}
