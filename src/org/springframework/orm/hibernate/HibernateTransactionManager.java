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

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.JDBCException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.connection.ConnectionProvider;
import net.sf.hibernate.engine.SessionFactoryImplementor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * PlatformTransactionManager implementation for single Hibernate session factories.
 * Binds a Hibernate Session from the specified factory to the thread, potentially
 * allowing for one thread Session per factory. SessionFactoryUtils and
 * HibernateTemplate are aware of thread-bound Sessions and participate in such
 * transactions automatically. Using either is required for Hibernate access code
 * that needs to support this transaction handling mechanism.
 *
 * <p>Supports custom isolation levels, and timeouts that get applied as appropriate
 * Hibernate query timeouts. To support the latter, application code must either use
 * HibernateTemplate.find or call SessionFactoryUtils' applyTransactionTimeout
 * method for each created Hibernate Query object.
 *
 * <p>This implementation is appropriate for applications that solely use Hibernate
 * for transactional data access, but it also supports direct data source access
 * within a transaction (i.e. plain JDBC code working with the same DataSource).
 * This allows for mixing services that access Hibernate (including transactional
 * caching) and services that use plain JDBC (without being aware of Hibernate)!
 * Application code needs to stick to the same simple Connection lookup pattern as
 * with DataSourceTransactionManager (i.e. DataSourceUtils.getConnection).
 *
 * <p>Note that to be able to register a DataSource's Connection for plain JDBC
 * code, this instance needs to be aware of the DataSource (see setDataSource).
 * The given DataSource should obviously match the one used by the given
 * SessionFactory. To achieve this, configure both to the same JNDI DataSource,
 * or preferably create the SessionFactory with LocalSessionFactoryBean and
 * a local DataSource (which will be auto-detected by this transaction manager).
 * In the latter case, the Hibernate settings do not have to define a connection
 * provider at all, avoiding duplicated configuration.
 *
 * <p>JTA respectively JtaTransactionManager is necessary for accessing multiple
 * transactional resources. The DataSource that Hibernate uses needs to be JTA-enabled
 * then (see container setup), alternatively the Hibernate JCA connector can be used
 * for direct container integration. Normally, JTA setup for Hibernate is somewhat
 * container-specific due to the JTA TransactionManager lookup, required for proper
 * transactional handling of the SessionFactory-level read-write cache. Using the
 * JCA Connector can solve this but involves packaging issue and container-specific
 * connector deployment.
 *
 * <p>Fortunately, there is an easier way with Spring: SessionFactoryUtils (and thus
 * HibernateTemplate) registers synchronizations with TransactionSynchronizationManager
 * (as used by JtaTransactionManager), for proper afterCompletion callbacks. Therefore,
 * as long as Spring's JtaTransactionManager drives the JTA transactions, Hibernate
 * does not require any special configuration for proper JTA participation.
 * Note that there are special cases with EJB CMT and restrictive JTA subsystems:
 * See JtaTransactionManager's javadoc for details.
 *
 * <p>Note: Spring's Hibernate support requires Hibernate 2.1 (as of Spring 1.0).
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see #setSessionFactory
 * @see #setDataSource
 * @see SessionFactoryUtils#getSession
 * @see SessionFactoryUtils#applyTransactionTimeout
 * @see SessionFactoryUtils#closeSessionIfNecessary
 * @see HibernateTemplate#execute
 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection
 * @see org.springframework.jdbc.datasource.DataSourceUtils#applyTransactionTimeout
 * @see org.springframework.jdbc.datasource.DataSourceUtils#closeConnectionIfNecessary
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
 */
public class HibernateTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {

	private SessionFactory sessionFactory;

	private DataSource dataSource;

	private Interceptor entityInterceptor;

	private SQLExceptionTranslator jdbcExceptionTranslator = new SQLStateSQLExceptionTranslator();


	/**
	 * Create a new HibernateTransactionManager instance.
	 * A SessionFactory has to be set to be able to use it.
	 * @see #setSessionFactory
	 */
	public HibernateTransactionManager() {
	}

	/**
	 * Create a new HibernateTransactionManager instance.
	 * @param sessionFactory SessionFactory to manage transactions for
	 */
	public HibernateTransactionManager(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		afterPropertiesSet();
	}

	/**
	 * Set the SessionFactory that this instance should manage transactions for.
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Return the SessionFactory that this instance should manage transactions for.
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * Set the JDBC DataSource that this instance should manage transactions for.
	 * The DataSource should match the one used by the Hibernate SessionFactory:
	 * for example, you could specify the same JNDI DataSource for both.
	 * <p>If the SessionFactory was configured with LocalDataSourceConnectionProvider,
	 * i.e. by Spring's LocalSessionFactoryBean with a specified "dataSource",
	 * the DataSource will be auto-detected: You can still explictly specify the
	 * DataSource, but you don't need to in this case.
	 * <p>A transactional JDBC Connection for this DataSource will be provided to
	 * application code accessing this DataSource directly via DataSourceUtils
	 * or JdbcTemplate. The Connection will be taken from the Hibernate Session.
	 * @see LocalDataSourceConnectionProvider
	 * @see LocalSessionFactoryBean#setDataSource
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Return the JDBC DataSource that this instance manages transactions for.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Set a Hibernate entity interceptor that allows to inspect and change
	 * property values before writing to and reading from the database.
	 * Will get applied to any new Session created by this transaction manager.
	 * <p>Such an interceptor can either be set at the SessionFactory level,
	 * i.e. on LocalSessionFactoryBean, or at the Session level, i.e. on
	 * HibernateTemplate, HibernateInterceptor, and HibernateTransactionManager.
	 * It's preferable to set it on LocalSessionFactoryBean or HibernateTransactionManager
	 * to avoid repeated configuration and guarantee consistent behavior in transactions.
	 * @see LocalSessionFactoryBean#setEntityInterceptor
	 * @see HibernateTemplate#setEntityInterceptor
	 * @see HibernateInterceptor#setEntityInterceptor
	 */
	public void setEntityInterceptor(Interceptor entityInterceptor) {
		this.entityInterceptor = entityInterceptor;
	}

	/**
	 * Return the current Hibernate entity interceptor, or null if none.
	 */
	public Interceptor getEntityInterceptor() {
		return entityInterceptor;
	}

	/**
	 * Set the JDBC exception translator for this transaction manager.
	 * Applied to SQLExceptions (wrapped by Hibernate's JDBCException)
	 * thrown by flushing on commit.
	 * <p>The default exception translator evaluates the exception's SQLState.
	 * @param jdbcExceptionTranslator exception translator
	 * @see org.springframework.jdbc.support.SQLStateSQLExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
	 */
	public void setJdbcExceptionTranslator(SQLExceptionTranslator jdbcExceptionTranslator) {
		this.jdbcExceptionTranslator = jdbcExceptionTranslator;
	}

	/**
	 * Return the JDBC exception translator for this transaction manager.
	 */
	public SQLExceptionTranslator getJdbcExceptionTranslator() {
		return this.jdbcExceptionTranslator;
	}

	public void afterPropertiesSet() {
		if (this.sessionFactory == null) {
			throw new IllegalArgumentException("sessionFactory is required");
		}
		// check for LocalDataSourceConnectionProvider
		if (this.sessionFactory instanceof SessionFactoryImplementor) {
			ConnectionProvider cp = ((SessionFactoryImplementor) this.sessionFactory).getConnectionProvider();
			if (cp instanceof LocalDataSourceConnectionProvider) {
				DataSource cpds = ((LocalDataSourceConnectionProvider) cp).getDataSource();
				if (this.dataSource == null) {
					// use the SessionFactory's DataSource for exposing transactions to JDBC code
					logger.info("Using DataSource [" + cpds +	"] from Hibernate SessionFactory for HibernateTransactionManager");
					this.dataSource = cpds;
				}
				else if (this.dataSource == cpds) {
					// let the configuration through: it's consistent
				}
				else {
					throw new IllegalArgumentException("Specified dataSource [" + this.dataSource +
																						 "] does not match [" + cpds + "] used by the SessionFactory");
				}
			}
		}
	}


	protected Object doGetTransaction() {
		if (TransactionSynchronizationManager.hasResource(this.sessionFactory)) {
			SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(this.sessionFactory);
			logger.debug("Found thread-bound session [" + sessionHolder.getSession() + "] for Hibernate transaction");
			return new HibernateTransactionObject(sessionHolder);
		}
		else {
			return new HibernateTransactionObject();
		}
	}

	protected boolean isExistingTransaction(Object transaction) {
		return ((HibernateTransactionObject) transaction).hasTransaction();
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;

		// cache to avoid repeated checks
		boolean debugEnabled = logger.isDebugEnabled();

		if (txObject.getSessionHolder() == null) {
			Session session = SessionFactoryUtils.getSession(this.sessionFactory, this.entityInterceptor,
																											 this.jdbcExceptionTranslator, false);
			if (debugEnabled) {
				logger.debug("Opened new session [" + session + "] for Hibernate transaction");
			}
			txObject.setSessionHolder(new SessionHolder(session));
		}

		try {
			txObject.getSessionHolder().setSynchronizedWithTransaction(true);
			Session session = txObject.getSessionHolder().getSession();
			if (debugEnabled) {
				logger.debug("Beginning Hibernate transaction on session [" + session + "]");
			}

			// apply read-only
			if (definition.isReadOnly()) {
				if (txObject.isNewSessionHolder()) {
					// just set to NEVER in case of a new Session for this transaction
					session.setFlushMode(FlushMode.NEVER);
				}
				try {
					Connection con = session.connection();
					if (debugEnabled) {
						logger.debug("Setting JDBC connection [" + con + "] read-only");
					}
					con.setReadOnly(true);
				}
				catch (Exception ex) {
					// SQLException or UnsupportedOperationException
					logger.warn("Could not set JDBC connection read-only", ex);
				}
			}
			else if (!txObject.isNewSessionHolder()) {
				// we need AUTO or COMMIT for a non-read-only transaction
				FlushMode flushMode = session.getFlushMode();
				if (FlushMode.NEVER.equals(flushMode)) {
					txObject.setPreviousFlushMode(flushMode);
					session.setFlushMode(FlushMode.AUTO);
				}
			}

			// apply isolation level
			if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
				Connection con = session.connection();
				if (debugEnabled) {
					logger.debug("Changing isolation level of JDBC connection [" + con + "] to " +
					             definition.getIsolationLevel());
				}
				txObject.setPreviousIsolationLevel(new Integer(con.getTransactionIsolation()));
				session.connection().setTransactionIsolation(definition.getIsolationLevel());
			}

			// add the Hibernate transaction to the session holder
			txObject.getSessionHolder().setTransaction(session.beginTransaction());

			// register transaction timeout
			if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
				txObject.getSessionHolder().setTimeoutInSeconds(definition.getTimeout());
			}

			// bind the session holder to the thread
			if (txObject.isNewSessionHolder()) {
				TransactionSynchronizationManager.bindResource(this.sessionFactory, txObject.getSessionHolder());
			}

			// register the Hibernate Session's JDBC Connection for the DataSource, if set
			if (this.dataSource != null) {
				ConnectionHolder conHolder = new ConnectionHolder(session.connection());
				if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
					conHolder.setTimeoutInSeconds(definition.getTimeout());
				}
				TransactionSynchronizationManager.bindResource(this.dataSource, conHolder);
			}
		}
		catch (SQLException ex) {
			throw new CannotCreateTransactionException("Could not set transaction isolation", ex);
		}
		catch (HibernateException ex) {
			throw new CannotCreateTransactionException("Could not create Hibernate transaction", ex);
		}
	}

	protected Object doSuspend(Object transaction) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;
		txObject.setSessionHolder(null);
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(this.sessionFactory);
		ConnectionHolder connectionHolder = null;
		if (this.dataSource != null) {
			connectionHolder = (ConnectionHolder) TransactionSynchronizationManager.unbindResource(this.dataSource);
		}
		return new SuspendedResourcesHolder(sessionHolder, connectionHolder);
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		SuspendedResourcesHolder resourcesHolder = (SuspendedResourcesHolder) suspendedResources;
		if (TransactionSynchronizationManager.hasResource(this.sessionFactory)) {
			// from non-transactional code running in active transaction synchronization
			// -> can be safely removed, will be closed on transaction completion
			TransactionSynchronizationManager.unbindResource(this.sessionFactory);
		}
		TransactionSynchronizationManager.bindResource(this.sessionFactory, resourcesHolder.getSessionHolder());
		if (this.dataSource != null) {
			TransactionSynchronizationManager.bindResource(this.dataSource, resourcesHolder.getConnectionHolder());
		}
	}

	protected boolean isRollbackOnly(Object transaction) {
		return ((HibernateTransactionObject) transaction).getSessionHolder().isRollbackOnly();
	}

	protected void doCommit(DefaultTransactionStatus status) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Committing Hibernate transaction on session [" +
									 txObject.getSessionHolder().getSession() + "]");
		}
		try {
			txObject.getSessionHolder().getTransaction().commit();
		}
		catch (net.sf.hibernate.TransactionException ex) {
			// assumably from commit call to underlying JDBC connection
			throw new TransactionSystemException("Could not commit Hibernate transaction", ex);
		}
		catch (JDBCException ex) {
			// assumably failed to flush changes to database
			throw convertJdbcAccessException(ex.getSQLException());
		}
		catch (HibernateException ex) {
			// assumably failed to flush changes to database
			throw convertHibernateAccessException(ex);
		}
	}

	protected void doRollback(DefaultTransactionStatus status) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Rolling back Hibernate transaction on session [" +
									 txObject.getSessionHolder().getSession() + "]");
		}
		try {
			txObject.getSessionHolder().getTransaction().rollback();
		}
		catch (net.sf.hibernate.TransactionException ex) {
			throw new TransactionSystemException("Could not rollback Hibernate transaction", ex);
		}
		catch (JDBCException ex) {
			// shouldn't really happen, as a rollback doesn't cause a flush
			throw convertJdbcAccessException(ex.getSQLException());
		}
		catch (HibernateException ex) {
			// shouldn't really happen, as a rollback doesn't cause a flush
			throw convertHibernateAccessException(ex);
		}
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Setting Hibernate transaction on Session [" +
									 txObject.getSessionHolder().getSession() + "] rollback-only");
		}
		txObject.getSessionHolder().setRollbackOnly();
	}

	protected void doCleanupAfterCompletion(Object transaction) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;

		// remove the JDBC connection holder from the thread, if set
		if (this.dataSource != null) {
			TransactionSynchronizationManager.unbindResource(this.dataSource);
		}

		// remove the session holder from the thread
		if (txObject.isNewSessionHolder()) {
			TransactionSynchronizationManager.unbindResource(this.sessionFactory);
		}

		try {
			Connection con = txObject.getSessionHolder().getSession().connection();

			// reset transaction isolation to previous value, if changed for the transaction
			if (txObject.getPreviousIsolationLevel() != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting isolation level of connection [" + con + "] to " +
											 txObject.getPreviousIsolationLevel());
				}
				con.setTransactionIsolation(txObject.getPreviousIsolationLevel().intValue());
			}

			// reset read-only
			if (con.isReadOnly()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting read-only flag of connection [" + con + "]");
				}
				con.setReadOnly(false);
			}
		}
		catch (Exception ex) {
			// HibernateException, SQLException, or UnsupportedOperationException
			// typically not something to worry about, can be ignored
			logger.info("Could not reset JDBC connection of Hibernate session", ex);
		}

		Session session = txObject.getSessionHolder().getSession();
		if (txObject.isNewSessionHolder()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Closing Hibernate session [" + session + "] after transaction");
			}
			try {
				SessionFactoryUtils.closeSessionIfNecessary(session, this.sessionFactory);
			}
			catch (CleanupFailureDataAccessException ex) {
				// just log it, to keep a transaction-related exception
				logger.error("Count not close Hibernate session after transaction", ex);
			}
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Not closing pre-bound Hibernate session [" + session + "] after transaction");
			}
			txObject.getSessionHolder().setTransaction(null);
			if (txObject.getPreviousFlushMode() != null) {
				session.setFlushMode(txObject.getPreviousFlushMode());
			}
		}
	}

	/**
	 * Convert the given HibernateException to an appropriate exception from
	 * the org.springframework.dao hierarchy. Can be overridden in subclasses.
	 * @param ex HibernateException that occured
	 * @return the corresponding DataAccessException instance
	 */
	protected DataAccessException convertHibernateAccessException(HibernateException ex) {
		return SessionFactoryUtils.convertHibernateAccessException(ex);
	}

	/**
	 * Convert the given SQLException to an appropriate exception from the
	 * org.springframework.dao hierarchy. Uses a JDBC exception translater if set,
	 * and a generic HibernateJdbcException else. Can be overridden in subclasses.
	 * @param ex SQLException that occured
	 * @return the corresponding DataAccessException instance
	 * @see #setJdbcExceptionTranslator
	 */
	protected DataAccessException convertJdbcAccessException(SQLException ex) {
		if (this.jdbcExceptionTranslator != null) {
			return this.jdbcExceptionTranslator.translate("HibernateTemplate", null, ex);
		}
		else {
			return new HibernateJdbcException(ex);
		}
	}


	/**
	 * Holder for suspended resources.
	 * Used internally by doSuspend and doResume.
	 * @see #doSuspend
	 * @see #doResume
	 */
	private static class SuspendedResourcesHolder {

		private final SessionHolder sessionHolder;

		private final ConnectionHolder connectionHolder;

		private SuspendedResourcesHolder(SessionHolder sessionHolder, ConnectionHolder connectionHolder) {
			this.sessionHolder = sessionHolder;
			this.connectionHolder = connectionHolder;
		}

		private SessionHolder getSessionHolder() {
			return sessionHolder;
		}

		private ConnectionHolder getConnectionHolder() {
			return connectionHolder;
		}
	}

}
