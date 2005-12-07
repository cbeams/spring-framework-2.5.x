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

package org.springframework.orm.jpa;

import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * Default implementation of the JpaDialect interface.
 * Used as default dialect by JpaAccessor and JpaTransactionManager.
 *
 * <p>Simply begins a standard JPA transaction in <code>beginTransaction</code>.
 * Returns a handle for a JPA2 DataStoreConnection on <code>getJdbcConnection</code>.
 * Calls the corresponding JPA2 operations on <code>detachCopy(All)</code>,
 * <code>attachCopy(All)</code>, <code>flush</code> and <code>newNamedQuery</code>.
 * Ignores a given query timeout in <code>applyQueryTimeout</code>.
 * Uses a Spring SQLExceptionTranslator for exception translation, if applicable.
 *
 * <p>Note that, even with JPA2, vendor-specific subclasses are still necessary
 * for special transaction semantics and more sophisticated exception translation.
 * Furthermore, vendor-specific subclasses are encouraged to expose the native JDBC
 * Connection on <code>getJdbcConnection</code>, rather than JPA2's wrapper handle.
 *
 * @author Juergen Hoeller
 * @since 1.3
 * @see JpaAccessor#setJpaDialect
 * @see JpaTransactionManager#setJpaDialect
 */
public class DefaultJpaDialect implements JpaDialect {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());


	/**
	 * Create a new DefaultJpaDialect.
	 */
	public DefaultJpaDialect() {
	}


	//-------------------------------------------------------------------------
	// Hooks for transaction management (used by JpaTransactionManager)
	//-------------------------------------------------------------------------

	/**
	 * This implementation invokes the standard JPA <code>Transaction.begin</code>
	 * method. Throws an InvalidIsolationLevelException if a non-default isolation
	 * level is set.
	 * @see javax.persistence.EntityTransaction#begin
	 * @see org.springframework.transaction.InvalidIsolationLevelException
	 */
	public Object beginTransaction(EntityManager entityManager, TransactionDefinition definition)
			throws PersistenceException, SQLException, TransactionException {

		if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException(
					"Standard JPA does not support custom isolation levels - " +
					"use a special JpaDialect for your JPA implementation");
		}
		entityManager.getTransaction().begin();
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
	 * This implementation returns a DataStoreConnectionHandle for JPA2,
	 * which will also work on JPA1 until actually accessing the JDBC Connection.
	 * <p>For pre-JPA2 implementations, override this method to return the
	 * Connection through the corresponding vendor-specific mechanism, or <code>null</code>
	 * if the Connection is not retrievable.
	 * <p><b>NOTE:</b> A JPA2 DataStoreConnection is always a wrapper,
	 * never the native JDBC Connection. If you need access to the native JDBC
	 * Connection (or the connection pool handle, to be unwrapped via a Spring
	 * NativeJdbcExtractor), override this method to return the native
	 * Connection through the corresponding vendor-specific mechanism.
	 * <p>A JPA2 DataStoreConnection is only "borrowed" from the EntityManager:
	 * it needs to be returned as early as possible. Effectively, JPA2 requires the
	 * fetched Connection to be closed before continuing EntityManager work.
	 * For this reason, the exposed ConnectionHandle eagerly releases its JDBC
	 * Connection at the end of each JDBC data access operation (that is, on
	 * <code>DataSourceUtils.releaseConnection</code>).
	 * @see org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#releaseConnection
	 */
	public ConnectionHandle getJdbcConnection(final EntityManager em, boolean readOnly)
			throws PersistenceException, SQLException {

		return null;
	}

	/**
	 * This implementation does nothing, assuming that the Connection
	 * will implicitly be closed with the EntityManager.
	 * <p>If the JPA implementation returns a Connection handle that
	 * it expects the application to close, the dialect needs to invoke
	 * <code>Connection.close</code> here.
	 * @see java.sql.Connection#close
	 */
	public void releaseJdbcConnection(ConnectionHandle conHandle, EntityManager em)
			throws PersistenceException, SQLException {
	}


	//-----------------------------------------------------------------------------------
	// Hook for exception translation (used by JpaTransactionManager and JpaTemplate)
	//-----------------------------------------------------------------------------------

	/**
	 * This implementation delegates to EntityManagerFactoryUtils.
	 * @see EntityManagerFactoryUtils#convertJpaAccessException
	 */
	public DataAccessException translateException(PersistenceException ex) {
		return EntityManagerFactoryUtils.convertJpaAccessException(ex);
	}

	/**
	 * Template method for extracting a SQL String from the given exception.
	 * <p>Default implementation always returns null. Can be overridden in
	 * subclasses to extract SQL Strings for vendor-specific exception classes.
	 * @param ex the PersistenceException, containing a SQLException
	 * @return the SQL String, or <code>null</code> if none found
	 */
	protected String extractSqlStringFromException(PersistenceException ex) {
		return null;
	}

}
