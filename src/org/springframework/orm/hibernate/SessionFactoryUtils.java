package org.springframework.orm.hibernate;

import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.JDBCException;
import net.sf.hibernate.ObjectDeletedException;
import net.sf.hibernate.ObjectNotFoundException;
import net.sf.hibernate.PersistentObjectException;
import net.sf.hibernate.Query;
import net.sf.hibernate.QueryException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.StaleObjectStateException;
import net.sf.hibernate.TransientObjectException;
import net.sf.hibernate.WrongClassException;
import net.sf.hibernate.engine.SessionFactoryImplementor;
import net.sf.hibernate.engine.SessionImplementor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Helper class featuring methods for Hibernate session handling,
 * allowing for reuse of Hibernate Session instances within transactions.
 *
 * <p>Used internally by HibernateTemplate, HibernateInterceptor, and
 * HibernateTransactionManager. Can also be used directly in application code,
 * e.g. in combination with HibernateInterceptor.
 *
 * <p>Note: Spring's Hibernate support requires Hibernate 2.x (2.1 recommended).
 * This class' SessionSynchronization mechanism requires Hibernate 2.1 because
 * of its JTA TransactionManagerLookup check.
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see HibernateTemplate
 * @see HibernateInterceptor
 * @see HibernateTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 */
public abstract class SessionFactoryUtils {

	private static final Log logger = LogFactory.getLog(SessionFactoryUtils.class);

	/**
	 * Get a Hibernate Session for the given factory. Is aware of a respective Session
	 * bound to the current thread, for example when using HibernateTransactionManager.
	 * Will create a new Session else, if allowCreate is true.
	 * @param sessionFactory Hibernate SessionFactory to create the session with
	 * @param allowCreate if a new Session should be created if no thread-bound found
	 * @return the Hibernate Session
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @throws IllegalStateException if no thread-bound Session found and allowCreate false
	 */
	public static Session getSession(SessionFactory sessionFactory, boolean allowCreate)
	    throws DataAccessResourceFailureException, IllegalStateException {
		if (!TransactionSynchronizationManager.hasResource(sessionFactory) && !allowCreate) {
			throw new IllegalStateException("Not allowed to create new Hibernate session");
		}
		return getSession(sessionFactory, null, null);
	}

	/**
	 * Get a Hibernate Session for the given factory. Is aware of a respective Session
	 * bound to the current thread, for example when using HibernateTransactionManager.
	 * Will always create a new Session else.
	 * <p>Supports synchronization with JTA transactions via TransactionSynchronizationManager,
	 * to allow for proper transactional handling of the JVM-level cache.
	 * <p>Supports setting a Session-level Hibernate entity interceptor that allows
	 * to inspect and change property values before writing to and reading from the
	 * database. Such an interceptor can also be set at the SessionFactory level,
	 * i.e. on LocalSessionFactoryBean.
	 * @param sessionFactory Hibernate SessionFactory to create the session with
	 * @param entityInterceptor Hibernate entity interceptor, or null if none
	 * @return the Hibernate Session
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @see LocalSessionFactoryBean#setEntityInterceptor
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager
	 */
	public static Session getSession(SessionFactory sessionFactory, Interceptor entityInterceptor,
																	 SQLExceptionTranslator jdbcExceptionTranslator)
			throws DataAccessResourceFailureException {
		SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		if (holder != null) {
			return holder.getSession();
		}
		try {
			logger.debug("Opening Hibernate session");
			Session session = (entityInterceptor != null ?
			    sessionFactory.openSession(entityInterceptor) : sessionFactory.openSession());
			if (TransactionSynchronizationManager.isSynchronizationActive()) {
				logger.debug("Registering transaction synchronization for Hibernate session");
				// use same Session for further Hibernate actions within the transaction
				// thread object will get removed by synchronization at transaction completion
				TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
				TransactionSynchronizationManager.registerSynchronization(
						new SessionSynchronization(session, sessionFactory, jdbcExceptionTranslator));
			}
			return session;
		}
		catch (JDBCException ex) {
			// SQLException underneath
			throw new DataAccessResourceFailureException("Cannot open Hibernate session", ex.getSQLException());
		}
		catch (HibernateException ex) {
			throw new DataAccessResourceFailureException("Cannot open Hibernate session", ex);
		}
	}

	/**
	 * Apply the current transaction timeout, if any,
	 * to the given Hibernate Query object.
	 * @param query the Hibernate Query object
	 * @param sessionFactory Hibernate SessionFactory that the Query was created for
	 */
	public static void applyTransactionTimeout(Query query, SessionFactory sessionFactory) {
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		if (sessionHolder != null && sessionHolder.getDeadline() != null) {
			query.setTimeout(sessionHolder.getTimeToLiveInSeconds());
		}
	}

	/**
	 * Convert the given HibernateException to an appropriate exception from the
	 * org.springframework.dao hierarchy. Note that it is advisable to handle JDBCException
	 * specifically by using an SQLExceptionTranslator for the underlying SQLException.
	 * @param ex HibernateException that occured
	 * @return the corresponding DataAccessException instance
	 * @see HibernateAccessor#convertHibernateAccessException
	 * @see HibernateAccessor#convertJdbcAccessException
	 * @see HibernateTemplate#execute
	 */
	public static DataAccessException convertHibernateAccessException(HibernateException ex) {
		if (ex instanceof JDBCException) {
			// SQLException during Hibernate access: only passed in here from custom code,
			// as HibernateTemplate etc will use SQLExceptionTranslator-based handling
			return new HibernateJdbcException((JDBCException) ex);
		}
		if (ex instanceof ObjectNotFoundException) {
			return new HibernateObjectRetrievalFailureException((ObjectNotFoundException) ex);
		}
		if (ex instanceof ObjectDeletedException) {
			return new HibernateObjectRetrievalFailureException((ObjectDeletedException) ex);
		}
		if (ex instanceof WrongClassException) {
			return new HibernateObjectRetrievalFailureException((WrongClassException) ex);
		}
		if (ex instanceof StaleObjectStateException) {
			return new HibernateOptimisticLockingFailureException((StaleObjectStateException) ex);
		}
		if (ex instanceof QueryException) {
			return new HibernateQueryException((QueryException) ex);
		}
		if (ex instanceof PersistentObjectException) {
			return new InvalidDataAccessApiUsageException(ex.getMessage());
		}
		if (ex instanceof TransientObjectException) {
			return new InvalidDataAccessApiUsageException(ex.getMessage());
		}
		// fallback
		return new HibernateSystemException(ex);
	}

	/**
	 * Close the given Session, created via the given factory,
	 * if it isn't bound to the thread.
	 * @param session Session to close
	 * @param sessionFactory Hibernate SessionFactory that the Session was created with
	 * @throws DataAccessResourceFailureException if the Session couldn't be closed
	 */
	public static void closeSessionIfNecessary(Session session, SessionFactory sessionFactory)
	    throws CleanupFailureDataAccessException {
		if (session == null || TransactionSynchronizationManager.hasResource(sessionFactory)) {
			return;
		}
		logger.debug("Closing Hibernate session");
		try {
			session.close();
		}
		catch (JDBCException ex) {
			// SQLException underneath
			throw new CleanupFailureDataAccessException("Cannot close Hibernate session", ex.getSQLException());
		}
		catch (HibernateException ex) {
			throw new CleanupFailureDataAccessException("Cannot close Hibernate session", ex);
		}
	}


	/**
	 * Callback for resource cleanup at the end of a non-Hibernate transaction
	 * (e.g. when participating in a JTA transaction).
	 */
	private static class SessionSynchronization implements TransactionSynchronization {

		private Session session;
		
		private SessionFactory sessionFactory;

		private SQLExceptionTranslator jdbcExceptionTranslator;

		/**
		 * Whether Hibernate has a looked-up JTA TransactionManager that it will
		 * automatically register CacheSynchronizations with on Session connect.
		 */
		private boolean hibernateTransactionCompletion;

		private SessionSynchronization(Session session, SessionFactory sessionFactory,
																	SQLExceptionTranslator jdbcExceptionTranslator) {
			this.session = session;
			this.sessionFactory = sessionFactory;
			this.jdbcExceptionTranslator = jdbcExceptionTranslator;
			// check whether the SessionFactory has a looked-up JTA TransactionManager
			this.hibernateTransactionCompletion =
					(sessionFactory instanceof SessionFactoryImplementor &&
					 ((SessionFactoryImplementor) sessionFactory).getTransactionManager() != null);
		}

		public void beforeCommit() throws DataAccessException {
			if (!this.session.getFlushMode().equals(FlushMode.NEVER)) {
				logger.debug("Flushing Hibernate session on transaction synchronization");
				try {
					this.session.flush();
				}
				catch (JDBCException ex) {
					if (this.jdbcExceptionTranslator != null) {
						throw this.jdbcExceptionTranslator.translate("SessionSynchronization", null, ex.getSQLException());
					}
					else {
						throw new HibernateJdbcException(ex);
					}
				}
				catch (HibernateException ex) {
					throw convertHibernateAccessException(ex);
				}
			}
		}

		public void beforeCompletion() throws CleanupFailureDataAccessException {
			TransactionSynchronizationManager.unbindResource(this.sessionFactory);
			if (this.hibernateTransactionCompletion) {
				closeSessionIfNecessary(this.session, this.sessionFactory);
			}
		}

		public void afterCompletion(int status) {
			if (!this.hibernateTransactionCompletion) {
				if (this.session instanceof SessionImplementor) {
					((SessionImplementor) this.session).afterTransactionCompletion(status == STATUS_COMMITTED);
				}
				closeSessionIfNecessary(this.session, this.sessionFactory);
			}
		}
	}

}
