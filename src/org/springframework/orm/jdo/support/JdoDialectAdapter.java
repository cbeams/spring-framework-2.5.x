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

package org.springframework.orm.jdo.support;

import java.sql.Connection;
import java.sql.SQLException;

import javax.jdo.JDOException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.jdo.JdoDialect;
import org.springframework.orm.jdo.PersistenceManagerFactoryUtils;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * Abstract adapter class for the JdoDialect interface.
 * Throws JDOUnsupportedOptionException on every operation method.
 * Delegates to PersistenceManagerFactoryUtils for exception translation.
 * @author Juergen Hoeller
 * @since 12.06.2004
 * @see org.springframework.orm.jdo.PersistenceManagerFactoryUtils#convertJdoAccessException
 * @see javax.jdo.JDOUnsupportedOptionException
 */
public abstract class JdoDialectAdapter implements JdoDialect {

	public void beginTransaction(Transaction transaction, TransactionDefinition definition)
			throws JDOException, SQLException, TransactionException {
		if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException("Standard JDO does not support custom isolation levels - " +
																							 "use a special JdoAdapter for your JDO implementation");
		}
		transaction.begin();
	}

	public Connection getJdbcConnection(PersistenceManager pm, boolean readOnly)
			throws JDOException, SQLException {
		throw new JDOUnsupportedOptionException("Cannot retrieve underlying JDBC connection");
	}

	/**
	 * This implementation does nothing, assuming that the Connection
	 * will implicitly be closed with the PersistenceManager.
	 * <p>If the JDO implementation returns a Connection handle that
	 * it expects the application to close, the dialect needs to invoke
	 * <code>Connection.close</code> here.
	 * @see java.sql.Connection#close
	 */
	public void releaseJdbcConnection(Connection con, PersistenceManager pm)
			throws JDOException, SQLException {
	}

	public void flush(PersistenceManager pm) throws JDOException {
		throw new JDOUnsupportedOptionException("Cannot eagerly flush persistence manager");
	}

	public DataAccessException translateException(JDOException ex) {
		return PersistenceManagerFactoryUtils.convertJdoAccessException(ex);
	}

}
