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

import java.sql.Connection;

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;

import org.springframework.dao.DataAccessException;

/**
 * Strategy that encapsulates certain functionality that standard JDO 1.0 does not offer
 * despite being relevant in the context of O/R mapping, like access to the underlying
 * JDBC connection and explicit flushing of changes to the database.
 *
 * <p>To be implemented for specific JDO implementations like Kodo, Lido, or JDO Genie.
 * Almost every O/R-based JDO implementation will offer proprietary means to access the
 * underlying JDBC Connection and to explicitly flush changes. JDO 2.0 respectively
 * JDO/R 2.0 are likely to define standard ways for these: If applicable, a JdoDialect
 * implementation for JDO 2.0 will be provided to leverage them with Spring's JDO support.
 *
 * @author Juergen Hoeller
 * @since 02.11.2003
 */
public interface JdoDialect {

	/**
	 * Retrieve the JDBC connection that the given JDO persistence manager uses underneath,
	 * if accessing a relational database. This method will just get invoked if actually
	 * needing access to the underlying JDBC connection.
	 * <p>This strategy is necessary as JDO 1.0 does not provide a standard way to retrieve
	 * the underlying JDBC Connection (due to the fact that a JDO implementation might not
	 * work with a relational database at all).
	 * @param pm the current JDO persistence manager
	 * @return the underlying JDBC connection
	 * @throws JDOException in case of retrieval errors
	 * @see JdoTransactionManager#setDataSource
	 */
	Connection getJdbcConnection(PersistenceManager pm) throws JDOException;

	/**
	 * Flush the given persistence manager, i.e. flush all changes (that have been applied
	 * to persistent objects) to the underlying database. This method will just get invoked
	 * if eager flushing is actually necessary, for example if JDBC access code needs to
	 * see changes within the same transaction.
	 * @param pm the current JDO persistence manager
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
	 * ObjectRetrievalFailureException, ObjectOptimisticLockingFailureException,
	 * and DataIntegrityViolationException. Unfortunately, standard JDO does not
	 * allow for portable detection of those.
	 * @param ex the JDOException thrown
	 * @return the corresponding DataAccessException (must not be null)
	 * @see JdoAccessor#convertJdoAccessException
	 * @see JdoTransactionManager#convertJdoAccessException
	 * @see PersistenceManagerFactoryUtils#convertJdoAccessException
	 * @see org.springframework.orm.ObjectRetrievalFailureException
	 * @see org.springframework.orm.ObjectOptimisticLockingFailureException
	 * @see org.springframework.dao.DataIntegrityViolationException
	 */
	DataAccessException translateException(JDOException ex);

}
