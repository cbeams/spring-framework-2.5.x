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

import java.sql.SQLException;

import javax.jdo.JDOException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * Default implementation of the JdoDialect interface.
 * Used by JdoAccessor and JdoTransactionManager as default.
 *
 * <p>Simply begins a standard JDO transaction in <code>beginTransaction</code>.
 * Returns null on <code>getJdbcConnection</code>.
 * Throws a JDOUnsupportedOptionException on <code>flush</code>.
 * Delegates to PersistenceManagerFactoryUtils for exception translation.
 *
 * <p>This class will be adapted to JDO 2.0 as soon as the latter is available.
 * JDBC Connection retrieval and flushing will then default to the respective
 * JDO 2.0 methods. Vendor-specific subclasses will still be necessary for
 * special transaction semantics and more sophisticated exception translation.
 *
 * @author Juergen Hoeller
 * @since 12.06.2004
 * @see JdoAccessor#setJdoDialect
 * @see JdoTransactionManager#setJdoDialect
 */
public class DefaultJdoDialect implements JdoDialect {

	/**
	 * This implementation invokes the standard JDO <code>Transaction.begin</code>
	 * method. Throws an InvalidIsolationLevelException if a non-default isolation
	 * level is set.
	 * @see javax.jdo.Transaction#begin
	 * @see org.springframework.transaction.InvalidIsolationLevelException
	 */
	public void beginTransaction(Transaction transaction, TransactionDefinition definition)
			throws JDOException, SQLException, TransactionException {
		if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException("Standard JDO does not support custom isolation levels - " +
																							 "use a special JdoAdapter for your JDO implementation");
		}
		transaction.begin();
	}

	/**
	 * This implementation returns null, to indicate that JDBC Connection
	 * retrieval is not supported.
	 */
	public ConnectionHandle getJdbcConnection(PersistenceManager pm, boolean readOnly)
			throws JDOException, SQLException {
		return null;
	}

	/**
	 * This implementation does nothing, assuming that the Connection
	 * will implicitly be closed with the PersistenceManager.
	 * <p>If the JDO implementation returns a Connection handle that
	 * it expects the application to close, the dialect needs to invoke
	 * <code>Connection.close</code> here.
	 * @see java.sql.Connection#close
	 */
	public void releaseJdbcConnection(ConnectionHandle conHandle, PersistenceManager pm)
			throws JDOException, SQLException {
	}

	/**
	 * This implementation throws a JDOUnsupportedOptionException.
	 * @see javax.jdo.JDOUnsupportedOptionException
	 */
	public void flush(PersistenceManager pm) throws JDOException {
		throw new JDOUnsupportedOptionException("Cannot eagerly flush persistence manager");
	}

	/**
	 * This implementation delegates to PersistenceManagerFactoryUtils.
	 * @see PersistenceManagerFactoryUtils#convertJdoAccessException
	 */
	public DataAccessException translateException(JDOException ex) {
		return PersistenceManagerFactoryUtils.convertJdoAccessException(ex);
	}

}
