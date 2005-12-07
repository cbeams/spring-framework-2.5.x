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
import java.util.Collection;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.EntityManager;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * SPI strategy that encapsulates certain functionality that standard JPA 1.0 does
 * not offer despite being relevant in the context of O/R mapping, like access to
 * the underlying JDBC Connection and explicit flushing of changes to the database.
 * Also defines various further hooks that even go beyond standard JPA 2.0.
 *
 * <p>To be implemented for specific JPA implementations such as JPOX, Kodo, Lido,
 * Versant Open Access. Almost every O/R-based JPA implementation offers proprietary
 * means to access the underlying JDBC Connection and to explicitly flush changes;
 * hence, this would be the minimum functionality level that should be supported.
 *
 * <p>JPA 2.0 defines standard ways for most of the functionality covered here.
 * Hence, Spring's DefaultJpaDialect uses the corresponding JPA 2.0 methods
 * by default, to be overridden in a vendor-specific fashion if necessary.
 * Vendor-specific subclasses of DefaultJpaDialect are still required for special
 * transaction semantics and more sophisticated exception translation (if needed).
 *
 * <p>In general, it is recommended to derive from DefaultJpaDialect instead of
 * implementing this interface directly. This allows for inheriting common
 * behavior (present and future) from DefaultJpaDialect, only overriding
 * specific hooks to plug in concrete vendor-specific behavior.
 *
 * @author Juergen Hoeller
 * @since 1.3
 * @see JpaTransactionManager#setJpaDialect
 * @see JpaAccessor#setJpaDialect
 * @see DefaultJpaDialect
 */
public interface JpaDialect {

	//-------------------------------------------------------------------------
	// Hooks for transaction management (used by JpaTransactionManager)
	//-------------------------------------------------------------------------

	/**
	 * Begin the given JPA transaction, applying the semantics specified by the
	 * given Spring transaction definition (in particular, an isolation level
	 * and a timeout). Invoked by JpaTransactionManager on transaction begin.
	 * <p>An implementation can configure the JPA Transaction object and then
	 * invoke <code>begin</code>, or invoke a special begin method that takes,
	 * for example, an isolation level.
	 * <p>An implementation can also apply read-only flag and isolation level to the
	 * underlying JDBC Connection before beginning the transaction. In that case,
	 * a transaction data object can be returned that holds the previous isolation
	 * level (and possibly other data), to be reset in <code>cleanupTransaction</code>.
	 * <p>Implementations can also use the Spring transaction name, as exposed by the
	 * passed-in TransactionDefinition, to optimize for specific data access use cases
	 * (effectively using the current transaction name as use case identifier).
	 * @param transaction the JPA transaction to begin
	 * @param definition the Spring transaction definition that defines semantics
	 * @return an arbitrary object that holds transaction data, if any
	 * (to be passed into cleanupTransaction)
	 * @throws javax.persistence.PersistenceException if thrown by JPA methods
	 * @throws java.sql.SQLException if thrown by JDBC methods
	 * @throws org.springframework.transaction.TransactionException in case of invalid arguments
	 * @see #cleanupTransaction
	 * @see javax.persistence.EntityTransaction#begin
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#prepareConnectionForTransaction
	 */
	Object beginTransaction(EntityManager entityManager, TransactionDefinition definition)
			throws PersistenceException, SQLException, TransactionException;

	/**
	 * Clean up the transaction via the given transaction data.
	 * Invoked by JpaTransactionManager on transaction cleanup.
	 * <p>An implementation can, for example, reset read-only flag and
	 * isolation level of the underlying JDBC Connection. Furthermore,
	 * an exposed data access use case can be reset here.
	 * @param transactionData arbitrary object that holds transaction data, if any
	 * (as returned by beginTransaction)
	 * @see #beginTransaction
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#resetConnectionAfterTransaction
	 */
	void cleanupTransaction(Object transactionData);

	/**
	 * Retrieve the JDBC Connection that the given JPA EntityManager uses underneath,
	 * if accessing a relational database. This method will just get invoked if actually
	 * needing access to the underlying JDBC Connection, usually within an active JPA
	 * transaction (for example, by JpaTransactionManager). The returned handle will
	 * be passed into the <code>releaseJdbcConnection</code> method when not needed anymore.
	 * <p>This strategy is necessary as JPA 1.0 does not provide a standard way to retrieve
	 * the underlying JDBC Connection (due to the fact that a JPA implementation might not
	 * work with a relational database at all).
	 * <p>Implementations are encouraged to return an unwrapped Connection object, i.e.
	 * the Connection as they got it from the connection pool. This makes it easier for
	 * application code to get at the underlying native JDBC Connection, like an
	 * OracleConnection, which is sometimes necessary for LOB handling etc. We assume
	 * that calling code knows how to properly handle the returned Connection object.
	 * <p>In a simple case where the returned Connection will be auto-closed with the
	 * EntityManager or can be released via the Connection object itself, an
	 * implementation can return a SimpleConnectionHandle that just contains the
	 * Connection. If some other object is needed in <code>releaseJdbcConnection</code>,
	 * an implementation should use a special handle that references that other object.
	 * @param em the current JPA EntityManager
	 * @return a handle for the JDBC Connection, to be passed into
	 * <code>releaseJdbcConnection</code>, or <code>null</code>
	 * if no JDBC Connection can be retrieved
	 * @throws javax.persistence.PersistenceException if thrown by JPA methods
	 * @throws java.sql.SQLException if thrown by JDBC methods
	 * @see #releaseJdbcConnection
	 * @see org.springframework.jdbc.datasource.ConnectionHandle#getConnection
	 * @see org.springframework.jdbc.datasource.SimpleConnectionHandle
	 * @see JpaTransactionManager#setDataSource
	 * @see org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor
	 */
	ConnectionHandle getJdbcConnection(EntityManager em, boolean readOnly)
			throws PersistenceException, SQLException;

	/**
	 * Release the given JDBC Connection, which has originally been retrieved
	 * via <code>getJdbcConnection</code>. This should be invoked in any case,
	 * to allow for proper release of the retrieved Connection handle.
	 * <p>An implementation might simply do nothing, if the Connection returned
	 * by <code>getJdbcConnection</code> will be implicitly closed when the JPA
	 * transaction completes or when the EntityManager is closed.
	 * @param conHandle the JDBC Connection handle to release
	 * @param em the current JPA EntityManager
	 * @throws javax.persistence.PersistenceException if thrown by JPA methods
	 * @throws java.sql.SQLException if thrown by JDBC methods
	 * @see #getJdbcConnection
	 */
	void releaseJdbcConnection(ConnectionHandle conHandle, EntityManager em)
			throws PersistenceException, SQLException;


	//-----------------------------------------------------------------------------------
	// Hook for exception translation (used by JpaTransactionManager and JpaTemplate)
	//-----------------------------------------------------------------------------------

	/**
	 * Translate the given PersistenceException to a corresponding exception from Spring's
	 * generic DataAccessException hierarchy. An implementation should apply
	 * EntityManagerFactoryUtils' standard exception translation if can't do
	 * anything more specific.
	 * <p>Of particular importance is the correct translation to
	 * DataIntegrityViolationException, for example on constraint violation.
	 * Unfortunately, standard JPA does not allow for portable detection of this.
	 * <p>Can use a SQLExceptionTranslator for translating underlying SQLExceptions
	 * in a database-specific fashion.
	 * @param ex the PersistenceException thrown
	 * @return the corresponding DataAccessException (must not be <code>null</code>)
	 * @see JpaAccessor#convertJpaAccessException
	 * @see JpaTransactionManager#convertJpaAccessException
	 * @see EntityManagerFactoryUtils#convertJpaAccessException
	 * @see org.springframework.dao.DataIntegrityViolationException
	 * @see org.springframework.jdbc.support.SQLExceptionTranslator
	 */
	DataAccessException translateException(PersistenceException ex);

}
