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
	 */
	int PROPAGATION_REQUIRES_NEW = 3;

	/**
	 * Execute non-transactionally, suspend the current transaction if one exists.
	 * Analogous to EJB transaction attribute of the same name.
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
	 */
	int PROPAGATION_NESTED = 6;


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
	 * or none if timeouts are not supported. 
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
	 */
	boolean isReadOnly();

	/**
	 * Return the name of this transaction. Can be <code>null</code>.
	 * This will be used as transaction name to be shown in a
	 * transaction monitor, if applicable (for example, WebLogic's).
	 * <p>In case of Spring's declarative transactions, the exposed name will
	 * be the fully-qualified class name + "." + method name (by default).
	 */
	String getName();

}
