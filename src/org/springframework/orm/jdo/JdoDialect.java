package org.springframework.orm.jdo;

import java.sql.Connection;

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.springframework.dao.DataAccessException;

/**
 * Strategy that encapsulates certain functionality that standard JDO 1.0 does not offer
 * despite being relevant in the context of O/R mapping, like access to the underlying
 * JDBC connection and explicit flushing of changes to the database.
 *
 * <p>To be implemented for specific JDO implementations like Kodo, Lido, or JDO Genie.
 * Almost every O/R-based JDO implementation will offer proprietary means to access the
 * underlying JDBC Connection and to explicitly flush changes. JDO 2.0 resp. JDO/R 2.0
 * might define standard ways to achieve these things: As soon as available, a JdoDialect
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
	 */
	Connection getJdbcConnection(PersistenceManager pm) throws JDOException;

	/**
	 * Flush the given JDO transaction, i.e. flush all changes to persistent objects
	 * to the underlying database. This method will just get invoked if eager flushing
	 * is actually necessary, for example if JDBC access code needs to see changes
	 * within the same transaction.
	 * @param transaction the current JDO transaction
	 * @throws JDOException
	 */
	void flush(Transaction transaction) throws JDOException;

	/**
	 * Translate the given JDOException to a corresponding exception from Spring's
	 * generic DataAccessException hierarchy. An implementation should apply
	 * PersistenceManagerFactoryUtils' standard excepion translation if can't do
	 * anything more specific.
	 * <p>Of particular importance is the correct translation to
	 * OptimisticLockingFailureException and DataIntegrityViolationException.
	 * Unfortunately, standard JDO does not allow for portable detection of those.
	 * @param ex the JDOException thrown
	 * @return the corresponding DataAccessException (must not be null)
	 * @see PersistenceManagerFactoryUtils#convertJdoAccessException
	 * @see org.springframework.orm.ObjectOptimisticLockingFailureException
	 * @see org.springframework.dao.DataIntegrityViolationException
	 */
	DataAccessException translateException(JDOException ex);

}
