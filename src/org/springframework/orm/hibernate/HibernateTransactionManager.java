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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * PlatformTransactionManager implementation for a single Hibernate SessionFactory.
 * Binds a Hibernate Session from the specified factory to the thread, potentially
 * allowing for one thread Session per factory. SessionFactoryUtils and
 * HibernateTemplate are aware of thread-bound Sessions and participate in such
 * transactions automatically. Using either is required for Hibernate access code
 * that needs to support this transaction handling mechanism.
 *
 * <p>Supports custom isolation levels, and timeouts that get applied as appropriate
 * Hibernate query timeouts. To support the latter, application code must either use
 * one of the <code>HibernateTemplate.find</code> methods or call
 * <code>SessionFactoryUtils.applyTransactionTimeout</code> for each created
 * Hibernate Query object.
 *
 * <p>This implementation is appropriate for applications that solely use Hibernate
 * for transactional data access, but it also supports direct data source access
 * within a transaction (i.e. plain JDBC code working with the same DataSource).
 * This allows for mixing services that access Hibernate (including transactional
 * caching) and services that use plain JDBC (without being aware of Hibernate)!
 * Application code needs to stick to the same simple Connection lookup pattern as
 * with DataSourceTransactionManager (i.e. <code>DataSourceUtils.getConnection</code>).
 *
 * <p>Note that to be able to register a DataSource's Connection for plain JDBC
 * code, this instance needs to be aware of the DataSource (see setDataSource).
 * The given DataSource should obviously match the one used by the given
 * SessionFactory. To achieve this, configure both to the same JNDI DataSource,
 * or preferably create the SessionFactory with LocalSessionFactoryBean and
 * a local DataSource (which will be auto-detected by this transaction manager).
 *
 * <p>JTA respectively JtaTransactionManager is necessary for accessing multiple
 * transactional resources. The DataSource that Hibernate uses needs to be JTA-enabled
 * then (see container setup), alternatively the Hibernate JCA connector can be used
 * for direct container integration. Normally, JTA setup for Hibernate is somewhat
 * container-specific due to the JTA TransactionManager lookup, required for proper
 * transactional handling of the SessionFactory-level read-write cache. Using the
 * JCA Connector can solve this but involves packaging issues and container-specific
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
 * <p>On JDBC 3.0, this transaction manager supports nested transactions via JDBC
 * 3.0 Savepoints. The "nestedTransactionAllowed" flag defaults to false, though,
 * as nested transactions will just apply to the JDBC Connection, not to the
 * Hibernate Session and its cached objects. You can manually set the flag to true
 * if you want to use nested transactions for JDBC access code that participates
 * in Hibernate transactions (provided that your JDBC driver supports Savepoints).
 *
 * <p>Note: Spring's Hibernate support requires Hibernate 2.1 (as of Spring 1.0).
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see #setSessionFactory
 * @see #setDataSource
 * @see LocalSessionFactoryBean
 * @see SessionFactoryUtils#getSession
 * @see SessionFactoryUtils#applyTransactionTimeout
 * @see SessionFactoryUtils#closeSessionIfNecessary
 * @see HibernateTemplate#execute
 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection
 * @see org.springframework.jdbc.datasource.DataSourceUtils#applyTransactionTimeout
 * @see org.springframework.jdbc.datasource.DataSourceUtils#closeConnectionIfNecessary
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
 */
public class HibernateTransactionManager extends AbstractPlatformTransactionManager
		implements BeanFactoryAware, InitializingBean {

	private SessionFactory sessionFactory;

	private DataSource dataSource;

	private boolean autodetectDataSource = true;

	private Object entityInterceptor;

	private SQLExceptionTranslator jdbcExceptionTranslator = new SQLStateSQLExceptionTranslator();

	/**
	 * Just needed for entityInterceptorBeanName.
	 * @see #setEntityInterceptorBeanName
	 */
	private BeanFactory beanFactory;


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
	 * @see #setAutodetectDataSource
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
	 * Set whether to autodetect a JDBC DataSource used by the Hibernate SessionFactory,
	 * if set via LocalSessionFactoryBean's <code>setDataSource</code>. Default is true.
	 * <p>Can be turned off to deliberately ignore an available DataSource,
	 * to not expose Hibernate transactions as JDBC transactions for that DataSource.
	 * @see #setDataSource
	 * @see LocalSessionFactoryBean#setDataSource
	 */
	public void setAutodetectDataSource(boolean autodetectDataSource) {
		this.autodetectDataSource = autodetectDataSource;
	}

	/**
	 * Set the bean name of a Hibernate entity interceptor that allows to inspect
	 * and change property values before writing to and reading from the database.
	 * Will get applied to any new Session created by this transaction manager.
	 * <p>Requires the bean factory to be known, to be able to resolve the bean
	 * name to an interceptor instance on session creation. Typically used for
	 * prototype interceptors, i.e. a new interceptor instance per session.
	 * <p>Can also be used for shared interceptor instances, but it is recommended
	 * to set the interceptor reference directly in such a scenario.
	 * @param entityInterceptorBeanName the name of the entity interceptor in
	 * the bean factory
	 * @see #setBeanFactory
	 * @see #setEntityInterceptor
	 */
	public void setEntityInterceptorBeanName(String entityInterceptorBeanName) {
		this.entityInterceptor = entityInterceptorBeanName;
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
	 * Resolves an entity interceptor bean name via the bean factory,
	 * if necessary.
	 * @throws IllegalStateException if bean name specified but no bean factory set
	 * @throws BeansException if bean name resolution via the bean factory failed
	 * @see #setEntityInterceptor
	 * @see #setEntityInterceptorBeanName
	 * @see #setBeanFactory
	 */
	public Interceptor getEntityInterceptor() throws IllegalStateException, BeansException {
		if (this.entityInterceptor instanceof Interceptor) {
			return (Interceptor) entityInterceptor;
		}
		else if (this.entityInterceptor instanceof String) {
			if (this.beanFactory == null) {
				throw new IllegalStateException("Cannot get entity interceptor via bean name if no bean factory set");
			}
			String beanName = (String) this.entityInterceptor;
			return (Interceptor) this.beanFactory.getBean(beanName, Interceptor.class);
		}
		else {
			return null;
		}
	}

	/**
	 * Set the JDBC exception translator for this transaction manager.
	 * Applied to SQLExceptions (wrapped by Hibernate's JDBCException)
	 * thrown by flushing on commit.
	 * <p>The default exception translator is either a SQLErrorCodeSQLExceptionTranslator
	 * if a DataSource is available, or a SQLStateSQLExceptionTranslator else.
	 * @param jdbcExceptionTranslator exception translator
	 * @see java.sql.SQLException
	 * @see net.sf.hibernate.JDBCException
	 * @see SessionFactoryUtils#newJdbcExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLStateSQLExceptionTranslator
	 */
	public void setJdbcExceptionTranslator(SQLExceptionTranslator jdbcExceptionTranslator) {
		this.jdbcExceptionTranslator = jdbcExceptionTranslator;
	}

	/**
	 * Return the JDBC exception translator for this transaction manager.
	 * Creates a default one for the specified SessionFactory if none set.
	 */
	public SQLExceptionTranslator getJdbcExceptionTranslator() {
		if (this.jdbcExceptionTranslator == null) {
			if (getDataSource() != null) {
				setJdbcExceptionTranslator(new SQLErrorCodeSQLExceptionTranslator(getDataSource()));
			}
			else {
				setJdbcExceptionTranslator(SessionFactoryUtils.newJdbcExceptionTranslator(getSessionFactory()));
			}
		}
		return this.jdbcExceptionTranslator;
	}

	/**
	 * The bean factory just needs to be known for resolving entity interceptor
	 * bean names. It does not need to be set for any other mode of operation.
	 * @see #setEntityInterceptorBeanName
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void afterPropertiesSet() {
		if (getSessionFactory() == null) {
			throw new IllegalArgumentException("sessionFactory is required");
		}
		if (this.entityInterceptor instanceof String && this.beanFactory == null) {
			throw new IllegalArgumentException("beanFactory is required for entityInterceptorBeanName");
		}

		// initialize the JDBC exception translator
		getJdbcExceptionTranslator();

		// check for SessionFactory's DataSource
		if (this.autodetectDataSource && getDataSource() == null) {
			DataSource sfds = SessionFactoryUtils.getDataSource(getSessionFactory());
			if (sfds != null) {
				// use the SessionFactory's DataSource for exposing transactions to JDBC code
				if (logger.isInfoEnabled()) {
					logger.info("Using DataSource [" + sfds +
							"] of Hibernate SessionFactory for HibernateTransactionManager");
				}
				setDataSource(sfds);
			}
		}
	}


	protected Object doGetTransaction() {
		HibernateTransactionObject txObject = new HibernateTransactionObject();
		txObject.setSavepointAllowed(isNestedTransactionAllowed());

		if (TransactionSynchronizationManager.hasResource(getSessionFactory())) {
			SessionHolder sessionHolder =
					(SessionHolder) TransactionSynchronizationManager.getResource(getSessionFactory());
			if (logger.isDebugEnabled()) {
				logger.debug("Found thread-bound session [" + sessionHolder.getSession() +
						"] for Hibernate transaction");
			}
			txObject.setSessionHolder(sessionHolder, false);
			if (getDataSource() != null) {
				ConnectionHolder conHolder = (ConnectionHolder)
						TransactionSynchronizationManager.getResource(getDataSource());
				txObject.setConnectionHolder(conHolder);
			}
		}

		return txObject;
	}

	protected boolean isExistingTransaction(Object transaction) {
		return ((HibernateTransactionObject) transaction).hasTransaction();
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;

		if (txObject.getSessionHolder() == null) {
			Session session = SessionFactoryUtils.getSession(
					getSessionFactory(), getEntityInterceptor(), getJdbcExceptionTranslator(), false);
			if (logger.isDebugEnabled()) {
				logger.debug("Opened new session [" + session + "] for Hibernate transaction");
			}
			txObject.setSessionHolder(new SessionHolder(session), true);
		}

		txObject.getSessionHolder().setSynchronizedWithTransaction(true);
		Session session = txObject.getSessionHolder().getSession();

		try {
			Connection con = session.connection();
			Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
			txObject.setPreviousIsolationLevel(previousIsolationLevel);

			if (definition.isReadOnly() && txObject.isNewSessionHolder()) {
				// just set to NEVER in case of a new Session for this transaction
				session.setFlushMode(FlushMode.NEVER);
			}

			if (!definition.isReadOnly() && !txObject.isNewSessionHolder()) {
				// we need AUTO or COMMIT for a non-read-only transaction
				FlushMode flushMode = session.getFlushMode();
				if (FlushMode.NEVER.equals(flushMode)) {
					session.setFlushMode(FlushMode.AUTO);
					txObject.getSessionHolder().setPreviousFlushMode(flushMode);
				}
			}

			// add the Hibernate transaction to the session holder
			txObject.getSessionHolder().setTransaction(session.beginTransaction());

			// register transaction timeout
			if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
				txObject.getSessionHolder().setTimeoutInSeconds(definition.getTimeout());
			}

			// register the Hibernate Session's JDBC Connection for the DataSource, if set
			if (getDataSource() != null) {
				ConnectionHolder conHolder = new ConnectionHolder(con);
				if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
					conHolder.setTimeoutInSeconds(definition.getTimeout());
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Exposing Hibernate transaction as JDBC transaction [" +
							conHolder.getConnection() + "]");
				}
				TransactionSynchronizationManager.bindResource(getDataSource(), conHolder);
				txObject.setConnectionHolder(conHolder);
			}

			// bind the session holder to the thread
			if (txObject.isNewSessionHolder()) {
				TransactionSynchronizationManager.bindResource(getSessionFactory(), txObject.getSessionHolder());
			}
		}

		catch (Exception ex) {
			SessionFactoryUtils.closeSessionIfNecessary(session, getSessionFactory());
			throw new CannotCreateTransactionException("Could not create Hibernate transaction", ex);
		}
	}

	protected Object doSuspend(Object transaction) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;
		txObject.setSessionHolder(null, false);
		SessionHolder sessionHolder =
				(SessionHolder) TransactionSynchronizationManager.unbindResource(getSessionFactory());
		ConnectionHolder connectionHolder = null;
		if (getDataSource() != null) {
			connectionHolder = (ConnectionHolder) TransactionSynchronizationManager.unbindResource(getDataSource());
		}
		return new SuspendedResourcesHolder(sessionHolder, connectionHolder);
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		SuspendedResourcesHolder resourcesHolder = (SuspendedResourcesHolder) suspendedResources;
		if (TransactionSynchronizationManager.hasResource(getSessionFactory())) {
			// from non-transactional code running in active transaction synchronization
			// -> can be safely removed, will be closed on transaction completion
			TransactionSynchronizationManager.unbindResource(getSessionFactory());
		}
		TransactionSynchronizationManager.bindResource(getSessionFactory(), resourcesHolder.getSessionHolder());
		if (getDataSource() != null) {
			TransactionSynchronizationManager.bindResource(getDataSource(), resourcesHolder.getConnectionHolder());
		}
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
			throw new TransactionSystemException("Could not roll back Hibernate transaction", ex);
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
			logger.debug("Setting Hibernate transaction on session [" +
					txObject.getSessionHolder().getSession() + "] rollback-only");
		}
		txObject.getSessionHolder().setRollbackOnly();
	}

	protected void doCleanupAfterCompletion(Object transaction) {
		HibernateTransactionObject txObject = (HibernateTransactionObject) transaction;

		// remove the session holder from the thread
		if (txObject.isNewSessionHolder()) {
			TransactionSynchronizationManager.unbindResource(getSessionFactory());
		}
		txObject.getSessionHolder().clear();

		// remove the JDBC connection holder from the thread, if set
		if (getDataSource() != null) {
			TransactionSynchronizationManager.unbindResource(getDataSource());
		}

		try {
			Connection con = txObject.getSessionHolder().getSession().connection();
			DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
		}
		catch (HibernateException ex) {
			logger.info("Could not access JDBC connection of Hibernate session", ex);
		}

		Session session = txObject.getSessionHolder().getSession();
		if (txObject.isNewSessionHolder()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Closing Hibernate session [" + session + "] after transaction");
			}
			SessionFactoryUtils.closeSessionIfNecessary(session, getSessionFactory());
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Not closing pre-bound Hibernate session [" + session + "] after transaction");
			}
			txObject.getSessionHolder().setTransaction(null);
			if (txObject.getSessionHolder().getPreviousFlushMode() != null) {
				session.setFlushMode(txObject.getSessionHolder().getPreviousFlushMode());
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
		if (getJdbcExceptionTranslator() != null) {
			return getJdbcExceptionTranslator().translate("HibernateTemplate", null, ex);
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

		private SuspendedResourcesHolder(SessionHolder sessionHolder, ConnectionHolder conHolder) {
			this.sessionHolder = sessionHolder;
			this.connectionHolder = conHolder;
		}

		private SessionHolder getSessionHolder() {
			return sessionHolder;
		}

		private ConnectionHolder getConnectionHolder() {
			return connectionHolder;
		}
	}

}
