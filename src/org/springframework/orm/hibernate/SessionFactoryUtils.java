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

package org.springframework.orm.hibernate;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import net.sf.hibernate.Criteria;
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
import net.sf.hibernate.UnresolvableObjectException;
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
 * <p>Supports synchronization with both Spring-managed JTA transactions
 * (i.e. JtaTransactionManager) and non-Spring JTA transactions (i.e. plain JTA
 * or EJB CMT). See the getSession version with all parameters for details.
 *
 * <p>Used internally by HibernateTemplate, HibernateInterceptor, and
 * HibernateTransactionManager. Can also be used directly in application code,
 * e.g. in combination with HibernateInterceptor.
 *
 * <p>Note: Spring's Hibernate support requires Hibernate 2.1 (as of Spring 1.0).
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see #getSession(SessionFactory, Interceptor, SQLExceptionTranslator, boolean)
 * @see HibernateTemplate
 * @see HibernateInterceptor
 * @see HibernateTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 */
public abstract class SessionFactoryUtils {

	private static final Log logger = LogFactory.getLog(SessionFactoryUtils.class);

	/**
	 * Get a Hibernate Session for the given SessionFactory. Is aware of and will
	 * return any existing corresponding Session bound to the current thread, for
	 * example when using HibernateTransactionManager. Will create a new Session
	 * otherwise, if allowCreate is true.
	 * <p>This is the getSession method used by typical data access code, in
	 * combination with closeSessionIfNecessary called when done with the Session.
	 * Note that HibernateTemplate allows to write data access code without caring
	 * about such resource handling.
	 * <p>Supports synchronization with both Spring-managed JTA transactions
	 * (i.e. JtaTransactionManager) and non-Spring JTA transactions (i.e. plain JTA
	 * or EJB CMT). See the getSession version with all parameters for details.
	 * @param sessionFactory Hibernate SessionFactory to create the session with
	 * @param allowCreate if a new Session should be created if no thread-bound found
	 * @return the Hibernate Session
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @throws IllegalStateException if no thread-bound Session found and allowCreate false
	 * @see #getSession(SessionFactory, Interceptor, SQLExceptionTranslator, boolean)
	 * @see #closeSessionIfNecessary
	 * @see HibernateTemplate
	 */
	public static Session getSession(SessionFactory sessionFactory, boolean allowCreate)
	    throws DataAccessResourceFailureException, IllegalStateException {
		if (!TransactionSynchronizationManager.hasResource(sessionFactory) && !allowCreate) {
			throw new IllegalStateException("No Hibernate Session bound to thread, and configuration " +
																			"does not allow creation of new one here");
		}
		return getSession(sessionFactory, null, null, true);
	}

	/**
	 * Get a Hibernate Session for the given SessionFactory. Is aware of and will
	 * return any existing corresponding Session bound to the current thread, for
	 * example when using HibernateTransactionManager. Will always create a new
	 * Session otherwise.
	 * <p>Supports synchronization with both Spring-managed JTA transactions
	 * (i.e. JtaTransactionManager) and non-Spring JTA transactions (i.e. plain JTA
	 * or EJB CMT). See the getSession version with all parameters for details.
	 * @param sessionFactory Hibernate SessionFactory to create the session with
	 * @param entityInterceptor Hibernate entity interceptor, or null if none
	 * @param jdbcExceptionTranslator SQLExcepionTranslator to use for flushing the
	 * Session on transaction synchronization (can be null; only used when actually
	 * registering a transaction synchronization)
	 * @return the Hibernate Session
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @see #getSession(SessionFactory, Interceptor, SQLExceptionTranslator, boolean)
	 */
	public static Session getSession(SessionFactory sessionFactory, Interceptor entityInterceptor,
	                                 SQLExceptionTranslator jdbcExceptionTranslator) {
		return getSession(sessionFactory, entityInterceptor, jdbcExceptionTranslator, true);
	}

	/**
	 * Get a Hibernate Session for the given SessionFactory. Is aware of and will
	 * return any existing corresponding Session bound to the current thread, for
	 * example when using HibernateTransactionManager. Will always create a new
	 * Session otherwise.
	 * <p>Supports synchronization with Spring-managed JTA transactions
	 * (i.e. JtaTransactionManager) via TransactionSynchronizationManager, to allow
	 * for transaction-scoped Hibernate Sessions and proper transactional handling
	 * of the JVM-level cache. This will only occur if "allowSynchronization" is true.
	 * <p>Supports synchronization with non-Spring JTA transactions (i.e. plain JTA
	 * or EJB CMT) via TransactionSynchronizationManager, to allow for
	 * transaction-scoped Hibernate Sessions without JtaTransactionManager.
	 * This only applies when a JTA TransactionManagerLookup is specified in the
	 * Hibernate configuration, and when "allowSynchronization" is true.
	 * <p>Supports setting a Session-level Hibernate entity interceptor that allows
	 * to inspect and change property values before writing to and reading from the
	 * database. Such an interceptor can also be set at the SessionFactory level
	 * (i.e. on LocalSessionFactoryBean), on HibernateTransactionManager, or on
	 * HibernateInterceptor/HibernateTemplate.
	 * @param sessionFactory Hibernate SessionFactory to create the session with
	 * @param entityInterceptor Hibernate entity interceptor, or null if none
	 * @param jdbcExceptionTranslator SQLExcepionTranslator to use for flushing the
	 * Session on transaction synchronization (can be null; only used when actually
	 * registering a transaction synchronization)
	 * @param allowSynchronization if a new Hibernate Session is supposed to be
	 * registered with transaction synchronization (if synchronization is active).
	 * This will always be true for typical data access code.
	 * @return the Hibernate Session
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @see LocalSessionFactoryBean#setEntityInterceptor
	 * @see HibernateInterceptor#setEntityInterceptor
	 * @see HibernateTemplate#setEntityInterceptor
	 * @see HibernateTransactionManager
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager
	 */
	public static Session getSession(SessionFactory sessionFactory, Interceptor entityInterceptor,
	                                 SQLExceptionTranslator jdbcExceptionTranslator, boolean allowSynchronization)
			throws DataAccessResourceFailureException {

		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		if (sessionHolder != null) {
			if (allowSynchronization && TransactionSynchronizationManager.isSynchronizationActive() &&
					!sessionHolder.isSynchronizedWithTransaction()) {
				TransactionSynchronizationManager.registerSynchronization(
						new SpringSessionSynchronization(sessionHolder, sessionFactory, jdbcExceptionTranslator, false));
				sessionHolder.setSynchronizedWithTransaction(true);
			}
			return sessionHolder.getSession();
		}

		try {
			logger.debug("Opening Hibernate session");
			Session session = (entityInterceptor != null ?
			    sessionFactory.openSession(entityInterceptor) : sessionFactory.openSession());

			if (allowSynchronization) {
				// Use same Session for further Hibernate actions within the transaction.
				// Thread object will get removed by synchronization at transaction completion.

				if (TransactionSynchronizationManager.isSynchronizationActive()) {
					// We're within a Spring-managed transaction, possibly from JtaTransactionManager.
					logger.debug("Registering Spring transaction synchronization for Hibernate session");
					sessionHolder = new SessionHolder(session);
					sessionHolder.setSynchronizedWithTransaction(true);
					TransactionSynchronizationManager.registerSynchronization(
							new SpringSessionSynchronization(sessionHolder, sessionFactory, jdbcExceptionTranslator, true));
					TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);
				}

				else if (sessionFactory instanceof SessionFactoryImplementor) {
					// JTA synchronization is only possible with a javax.transaction.TransactionManager.
					// We'll check the Hibernate SessionFactory: If a TransactionManagerLookup is specified
					// in Hibernate configuration, it will contain a TransactionManager reference.
					TransactionManager jtaTm = ((SessionFactoryImplementor) sessionFactory).getTransactionManager();
					if (jtaTm != null) {
						try {
							if (jtaTm.getStatus() == Status.STATUS_ACTIVE || jtaTm.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
								logger.debug("Registering JTA transaction synchronization for Hibernate session");
								sessionHolder = new SessionHolder(session);
								sessionHolder.setSynchronizedWithTransaction(true);
								jtaTm.getTransaction().registerSynchronization(
										new JtaSessionSynchronization(
												new SpringSessionSynchronization(sessionHolder, sessionFactory, jdbcExceptionTranslator, true),
												jtaTm));
								TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);
							}
						}
						catch (Exception ex) {
							throw new DataAccessResourceFailureException("Could not register synchronization " +
																													 "with JTA TransactionManager", ex);
						}
					}
				}
			}
			return session;
		}
		catch (JDBCException ex) {
			// SQLException underneath
			throw new DataAccessResourceFailureException("Could not open Hibernate session", ex.getSQLException());
		}
		catch (HibernateException ex) {
			throw new DataAccessResourceFailureException("Could not open Hibernate session", ex);
		}
	}

	/**
	 * Apply the current transaction timeout, if any, to the given
	 * Hibernate Query object.
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
	 * Apply the current transaction timeout, if any, to the given
	 * Hibernate Criteria object.
	 * @param criteria the Hibernate Criteria object
	 * @param sessionFactory Hibernate SessionFactory that the Criteria was created for
	 */
	public static void applyTransactionTimeout(Criteria criteria, SessionFactory sessionFactory) {
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		if (sessionHolder != null && sessionHolder.getDeadline() != null) {
			criteria.setTimeout(sessionHolder.getTimeToLiveInSeconds());
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
		if (ex instanceof UnresolvableObjectException) {
			return new HibernateObjectRetrievalFailureException((UnresolvableObjectException) ex);
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
			throw new CleanupFailureDataAccessException("Could not close Hibernate session", ex.getSQLException());
		}
		catch (HibernateException ex) {
			throw new CleanupFailureDataAccessException("Could not close Hibernate session", ex);
		}
	}


	/**
	 * Callback for resource cleanup at the end of a Spring-managed JTA transaction,
	 * i.e. when participating in a JtaTransactionManager transaction.
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	private static class SpringSessionSynchronization implements TransactionSynchronization {

		private final SessionHolder sessionHolder;

		private final SessionFactory sessionFactory;

		private final SQLExceptionTranslator jdbcExceptionTranslator;

		private final boolean newSession;

		/**
		 * Whether Hibernate has a looked-up JTA TransactionManager that it will
		 * automatically register CacheSynchronizations with on Session connect.
		 */
		private boolean hibernateTransactionCompletion;

		private SpringSessionSynchronization(SessionHolder sessionHolder, SessionFactory sessionFactory,
		                               SQLExceptionTranslator jdbcExceptionTranslator, boolean newSession) {
			this.sessionHolder = sessionHolder;
			this.sessionFactory = sessionFactory;
			this.jdbcExceptionTranslator = jdbcExceptionTranslator;
			// check whether the SessionFactory has a JTA TransactionManager
			this.hibernateTransactionCompletion =
					(sessionFactory instanceof SessionFactoryImplementor &&
					 ((SessionFactoryImplementor) sessionFactory).getTransactionManager() != null);
			this.newSession = newSession;
		}

		public void suspend() {
			TransactionSynchronizationManager.unbindResource(this.sessionFactory);
		}

		public void resume() {
			TransactionSynchronizationManager.bindResource(this.sessionFactory, this.sessionHolder);
		}

		public void beforeCommit(boolean readOnly) throws DataAccessException {
			if (!readOnly && !this.sessionHolder.getSession().getFlushMode().equals(FlushMode.NEVER)) {
				logger.debug("Flushing Hibernate session on transaction synchronization");
				try {
					this.sessionHolder.getSession().flush();
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
			if (this.newSession) {
				TransactionSynchronizationManager.unbindResource(this.sessionFactory);
				if (this.hibernateTransactionCompletion) {
					closeSessionIfNecessary(this.sessionHolder.getSession(), this.sessionFactory);
				}
			}
		}

		public void afterCompletion(int status) {
			if (!this.hibernateTransactionCompletion) {
				Session session = this.sessionHolder.getSession();
				if (session instanceof SessionImplementor) {
					((SessionImplementor) session).afterTransactionCompletion(status == STATUS_COMMITTED);
				}
				if (this.newSession) {
					closeSessionIfNecessary(session, this.sessionFactory);
				}
			}
			this.sessionHolder.setSynchronizedWithTransaction(false);
		}
	}


	/**
	 * Callback for resource cleanup at the end of a non-Spring JTA transaction,
	 * i.e. when plain JTA or EJB CMT is used without Spring's JtaTransactionManager.
	 */
	private static class JtaSessionSynchronization implements Synchronization {

		private final SpringSessionSynchronization springSessionSynchronization;

		private final TransactionManager jtaTransactionManager;

		private JtaSessionSynchronization(SpringSessionSynchronization springSessionSynchronization,
		                                  TransactionManager jtaTransactionManager) {
			this.springSessionSynchronization = springSessionSynchronization;
			this.jtaTransactionManager = jtaTransactionManager;
		}

		/**
		 * JTA beforeCompletion callback: just invoked on commit.
		 * <p>In case of an exception, the JTA transaction gets set to rollback-only.
		 * (Synchronization.beforeCompletion is not supposed to throw an exception.)
		 * @see SpringSessionSynchronization#beforeCommit
		 */
		public void beforeCompletion() {
			try {
				this.springSessionSynchronization.beforeCommit(false);
			}
			catch (Throwable ex) {
				logger.error("beforeCommit callback threw exception", ex);
				try {
					this.jtaTransactionManager.setRollbackOnly();
				}
				catch (SystemException ex2) {
					logger.error("Could not set JTA transaction rollback-only", ex2);
				}
			}
		}

		/**
		 * JTA afterCompletion callback: invoked after commit/rollback.
		 * <p>Needs to invoke SpringSessionSynchronization's beforeCompletion
		 * at this late stage, as there's no corresponding callback with JTA.
		 * @see SpringSessionSynchronization#beforeCompletion
		 * @see SpringSessionSynchronization#afterCompletion
		 */
		public void afterCompletion(int status) {
			// unbind the SessionHolder from the thread
			this.springSessionSynchronization.beforeCompletion();
			// just reset the synchronizedWithTransaction flag
			this.springSessionSynchronization.afterCompletion(-1);
		}
	}

}
