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
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * SPI strategy that encapsulates certain functionality that standard JDO 1.0 does
 * not offer despite being relevant in the context of O/R mapping, like access to
 * the underlying JDBC Connection and explicit flushing of changes to the database.
 *
 * <p>To be implemented for specific JDO implementations like Kodo, Lido, or JPOX.
 * Almost every O/R-based JDO implementation offers proprietary means to access the
 * underlying JDBC Connection and to explicitly flush changes. JDO 2.0 respectively
 * JDO/R 2.0 are likely to define standard ways for these: If applicable, a JdoDialect
 * implementation for JDO 2.0 will be provided to leverage them with Spring's JDO support.
 *
 * @author Juergen Hoeller
 * @since 02.11.2003
 * @see JdoTransactionManager#setJdoDialect
 * @see JdoAccessor#setJdoDialect
 */
public interface JdoDialect {

	/**
	 * Begin the given JDO transaction, applying the semantics specified by the
	 * given Spring transaction definition (in particular, an isolation level
	 * and a timeout). Invoked by JdoTransactionManager on transaction begin.
	 * <p>An implementation can configure the JDO Transaction object and then
	 * invoke <code>begin</code>, or invoke a special begin method that takes,
	 * for example, an isolation level.
	 * <p>An implementation can also apply read-only flag and isolation level to the
	 * underlying JDBC Connection before beginning the transaction. In that case,
	 * a transaction data object can be returned that holds the previous isolation
	 * level (and possibly other data), to be reset in cleanupTransaction.
	 * @param transaction the JDO transaction to begin
	 * @param definition the Spring transaction definition that defines semantics
	 * @return an arbitrary object that holds transaction data, if any
	 * (to be passed into cleanupTransaction)
	 * @throws JDOException if thrown by JDO methods
	 * @throws SQLException if thrown by JDBC methods
	 * @throws TransactionException in case of invalid arguments
	 * @see #cleanupTransaction
	 * @see javax.jdo.Transaction#begin
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#prepareConnectionForTransaction
	 */
	Object beginTransaction(Transaction transaction, TransactionDefinition definition)
			throws JDOException, SQLException, TransactionException;

	/**
	 * Clean up the transaction via the given transaction data.
	 * Invoked by JdoTransactionManager on transaction cleanup.
	 * <p>An implementation can, for example, reset read-only flag and
	 * isolation level of the underlying JDBC Connection.
	 * @param transactionData arbitrary object that holds transaction data, if any
	 * (as returned by beginTransaction)
	 * @see #beginTransaction
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#resetConnectionAfterTransaction
	 */
	void cleanupTransaction(Object transactionData);

	/**
	 * Retrieve the JDBC Connection that the given JDO PersistenceManager uses underneath,
	 * if accessing a relational database. This method will just get invoked if actually
	 * needing access to the underlying JDBC Connection, usually within an active JDO
	 * transaction (for example, by JdoTransactionManager). The returned handle will
	 * be passed into the <code>releaseJdbcConnection</code> method when not needed anymore.
	 * <p>This strategy is necessary as JDO 1.0 does not provide a standard way to retrieve
	 * the underlying JDBC Connection (due to the fact that a JDO implementation might not
	 * work with a relational database at all).
	 * <p>Implementations are encouraged to return an unwrapped Connection object, i.e.
	 * the Connection as they got it from the connection pool. This makes it easier for
	 * application code to get at the underlying native JDBC Connection, like an
	 * OracleConnection, which is sometimes necessary for LOB handling etc. We assume
	 * that calling code knows how to properly handle the returned Connection object.
	 * <p>In a simple case where the returned Connection will be auto-closed with the
	 * PersistenceManager or can be released via the Connection object itself, an
	 * implementation can return a SimpleConnectionHandle that just contains the
	 * Connection. If some other object is needed in <code>releaseJdbcConnection</code>,
	 * an implementation should use a special handle that references that other object.
	 * @param pm the current JDO PersistenceManager
	 * @return a handle for the JDBC Connection, to be passed into
	 * <code>releaseJdbcConnection</code>, or null if no JDBC Connection can be retrieved
	 * @throws JDOException if thrown by JDO methods
	 * @throws SQLException if thrown by JDBC methods
	 * @see #releaseJdbcConnection
	 * @see org.springframework.jdbc.datasource.ConnectionHandle#getConnection
	 * @see org.springframework.jdbc.datasource.SimpleConnectionHandle
	 * @see JdoTransactionManager#setDataSource
	 * @see org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor
	 */
	ConnectionHandle getJdbcConnection(PersistenceManager pm, boolean readOnly)
			throws JDOException, SQLException;

	/**
	 * Release the given JDBC Connection, which has originally been retrieved
	 * via <code>getJdbcConnection</code>. This should be invoked in any case,
	 * to allow for proper release of the retrieved Connection handle.
	 * <p>An implementation might simply do nothing, if the Connection returned
	 * by <code>getJdbcConnection</code> will be implicitly closed when the JDO
	 * transaction completes respectively when the PersistenceManager is closed.
	 * @param conHandle the JDBC Connection handle to release
	 * @param pm the current JDO PersistenceManager
	 * @throws JDOException if thrown by JDO methods
	 * @throws SQLException if thrown by JDBC methods
	 * @see #getJdbcConnection
	 */
	void releaseJdbcConnection(ConnectionHandle conHandle, PersistenceManager pm)
			throws JDOException, SQLException;

	/**
	 * Apply the given timeout to the given JDO query object.
	 * <p>Invoked by JdoTemplate with the remaining time of a specified
	 * transaction timeout, if any.
	 * @param query the JDO query object to apply the timeout to
	 * @param timeout the timeout value to apply
	 * @throws JDOException if thrown by JDO methods
	 * @see JdoTemplate#prepareQuery
	 */
	void applyQueryTimeout(Query query, int timeout) throws JDOException;

	/**
	 * Flush the given PersistenceManager, i.e. flush all changes (that have been
	 * applied to persistent objects) to the underlying database. This method will
	 * just get invoked when eager flushing is actually necessary, for example when
	 * JDBC access code needs to see changes within the same transaction.
	 * @param pm the current JDO PersistenceManager
	 * @throws JDOException in case of errors
	 * @see JdoAccessor#setFlushEager
	 */
	void flush(PersistenceManager pm) throws JDOException;

	/**
	 * Translate the given JDOException to a corresponding exception from Spring's
	 * generic DataAccessException hierarchy. An implementation should apply
	 * PersistenceManagerFactoryUtils' standard exception translation if can't do
	 * anything more specific.
	 * <p>Of particular importance is the correct translation to
	 * DataIntegrityViolationException, for example on constraint violation.
	 * Unfortunately, standard JDO does not allow for portable detection of this.
	 * <p>Can use a SQLExceptionTranslator for translating underlying SQLExceptions
	 * in a database-specific fashion.
	 * @param ex the JDOException thrown
	 * @return the corresponding DataAccessException (must not be null)
	 * @see JdoAccessor#convertJdoAccessException
	 * @see JdoTransactionManager#convertJdoAccessException
	 * @see PersistenceManagerFactoryUtils#convertJdoAccessException
	 * @see org.springframework.dao.DataIntegrityViolationException
	 * @see org.springframework.jdbc.support.SQLExceptionTranslator
	 */
	DataAccessException translateException(JDOException ex);

}
