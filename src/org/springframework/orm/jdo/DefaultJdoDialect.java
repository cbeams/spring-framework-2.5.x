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
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * Default implementation of the JdoDialect interface.
 * Used by JdoAccessor and JdoTransactionManager as default.
 *
 * <p>Simply begins a standard JDO transaction in <code>beginTransaction</code>.
 * Returns null on <code>getJdbcConnection</code>.
 * Ignores a given query timeout in <code>applyQueryTimeout</code>.
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
public class DefaultJdoDialect implements JdoDialect, InitializingBean {

	protected Log logger = LogFactory.getLog(getClass());

	private PersistenceManagerFactory persistenceManagerFactory;

	private SQLExceptionTranslator jdbcExceptionTranslator;


	/**
	 * Create a new DefaultJdoDialect.
	 */
	public DefaultJdoDialect() {
	}

	/**
	 * Create a new DefaultJdoDialect.
	 * @param pmf the JDO PersistenceManagerFactory, which is used
	 * to initialize the default JDBC exception translator
	 */
	public DefaultJdoDialect(PersistenceManagerFactory pmf) {
		setPersistenceManagerFactory(pmf);
		afterPropertiesSet();
	}

	/**
	 * Set the JDO PersistenceManagerFactory, which is used to initialize
	 * the default JDBC exception translator if none specified.
	 * @see #setJdbcExceptionTranslator
	 */
	public void setPersistenceManagerFactory(PersistenceManagerFactory pmf) {
		this.persistenceManagerFactory = pmf;
	}

	/**
	 * Return the JDO PersistenceManagerFactory that should be used to create
	 * PersistenceManagers.
	 */
	public PersistenceManagerFactory getPersistenceManagerFactory() {
		return persistenceManagerFactory;
	}

	/**
	 * Set the JDBC exception translator for this dialect.
	 * Applied to SQLExceptions that are the cause of JDOExceptions.
	 * <p>The default exception translator is either a SQLErrorCodeSQLExceptionTranslator
	 * if a DataSource is available, or a SQLStateSQLExceptionTranslator else.
	 * @param jdbcExceptionTranslator exception translator
	 * @see java.sql.SQLException
	 * @see javax.jdo.JDOException#getCause
	 * @see PersistenceManagerFactoryUtils#newJdbcExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLStateSQLExceptionTranslator
	 */
	public void setJdbcExceptionTranslator(SQLExceptionTranslator jdbcExceptionTranslator) {
		this.jdbcExceptionTranslator = jdbcExceptionTranslator;
	}

	/**
	 * Return the JDBC exception translator for this instance.
	 * Creates a default one for the specified PersistenceManagerFactory if none set.
	 */
	public SQLExceptionTranslator getJdbcExceptionTranslator() {
		if (this.jdbcExceptionTranslator == null) {
			if (this.persistenceManagerFactory != null) {
				this.jdbcExceptionTranslator =
						PersistenceManagerFactoryUtils.newJdbcExceptionTranslator(this.persistenceManagerFactory);
			}
			else {
				this.jdbcExceptionTranslator = new SQLStateSQLExceptionTranslator();
			}
		}
		return this.jdbcExceptionTranslator;
	}

	/**
	 * Eagerly initialize the exception translator, creating a default one
	 * for the specified PersistenceManagerFactory if none set.
	 */
	public void afterPropertiesSet() {
		getJdbcExceptionTranslator();
	}


	/**
	 * This implementation invokes the standard JDO <code>Transaction.begin</code>
	 * method. Throws an InvalidIsolationLevelException if a non-default isolation
	 * level is set.
	 * @see javax.jdo.Transaction#begin
	 * @see org.springframework.transaction.InvalidIsolationLevelException
	 */
	public Object beginTransaction(Transaction transaction, TransactionDefinition definition)
			throws JDOException, SQLException, TransactionException {
		if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException(
					"Standard JDO does not support custom isolation levels - " +
					"use a special JdoDialect for your JDO implementation");
		}
		transaction.begin();
		return null;
	}

	/**
	 * This implementation does nothing, as the default beginTransaction implementation
	 * does not require any cleanup.
	 * @see #beginTransaction
	 */
	public void cleanupTransaction(Object transactionData) {
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
	 * This implementation logs a warning that it cannot apply a query timeout.
	 */
	public void applyQueryTimeout(Query query, int remainingTimeInSeconds) throws JDOException {
		logger.info("DefaultJdoDialect does not support query timeouts: ignoring remaining transaction time");
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
		if (ex.getCause() instanceof SQLException) {
			return getJdbcExceptionTranslator().translate("JDO operation", null, (SQLException) ex.getCause());
		}
		else {
			return PersistenceManagerFactoryUtils.convertJdoAccessException(ex);
		}
	}

}
