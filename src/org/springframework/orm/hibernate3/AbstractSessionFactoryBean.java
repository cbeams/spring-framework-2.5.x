/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.orm.hibernate3;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.SessionFactory;
import org.hibernate.engine.SessionFactoryImplementor;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

/**
 * Abstract {@link org.springframework.beans.factory.FactoryBean} that creates
 * a Hibernate {@link org.hibernate.SessionFactory} within a Spring application
 * context. Supports building a transaction-aware SessionFactory proxy that
 * exposes a Spring-managed transactional Session as "current Session".
 *
 * <p>This class also implements the
 * {@link org.springframework.dao.support.PersistenceExceptionTranslator}
 * interface, as autodetected by Spring's
 * {@link org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor},
 * for AOP-based translation of native exceptions to Spring DataAccessExceptions.
 * Hence, the presence of e.g. LocalSessionFactoryBean automatically enables
 * a PersistenceExceptionTranslationPostProcessor to translate Hibernate exceptions.
 *
 * <p>This class mainly serves as common base class for {@link LocalSessionFactoryBean}.
 * For details on typical SessionFactory setup, see the LocalSessionFactoryBean javadoc.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setExposeTransactionAwareSessionFactory
 * @see org.hibernate.SessionFactory#getCurrentSession()
 * @see org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
 */
public abstract class AbstractSessionFactoryBean
		implements FactoryBean, InitializingBean, DisposableBean, PersistenceExceptionTranslator {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private boolean exposeTransactionAwareSessionFactory = true;

	private SQLExceptionTranslator jdbcExceptionTranslator;

	private SessionFactory sessionFactory;


	/**
	 * Set whether to expose a transaction-aware proxy for the SessionFactory,
	 * returning the Session that's associated with the current Spring-managed
	 * transaction on <code>getCurrentSession()</code>, if any.
	 * <p>Default is "true", letting data access code work with the plain
	 * Hibernate SessionFactory and its <code>getCurrentSession()</code> method,
	 * while still being able to participate in current Spring-managed transactions:
	 * with any transaction management strategy, either local or JTA / EJB CMT,
	 * and any transaction synchronization mechanism, either Spring or JTA.
	 * Furthermore, <code>getCurrentSession()</code> will also seamlessly work with
	 * a request-scoped Session managed by OpenSessionInViewFilter/Interceptor.
	 * <p>Turn this flag off to expose the plain Hibernate SessionFactory with
	 * Hibernate's default <code>getCurrentSession()</code> behavior, where
	 * Hibernate 3.0.x only supports plain JTA synchronization. On Hibernate 3.1+,
	 * such a plain SessionFactory will by default have a SpringSessionContext
	 * registered to nevertheless provide Spring-managed Sessions; this can be
	 * overridden through the corresponding Hibernate property
	 * "hibernate.current_session_context_class".
	 * @see org.hibernate.SessionFactory#getCurrentSession()
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 * @see HibernateTransactionManager
	 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewFilter
	 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor
	 * @see SpringSessionContext
	 */
	public void setExposeTransactionAwareSessionFactory(boolean exposeTransactionAwareSessionFactory) {
		this.exposeTransactionAwareSessionFactory = exposeTransactionAwareSessionFactory;
	}

	/**
	 * Return whether to expose a transaction-aware proxy for the SessionFactory.
	 */
	protected boolean isExposeTransactionAwareSessionFactory() {
		return this.exposeTransactionAwareSessionFactory;
	}

	/**
	 * Set the JDBC exception translator for the SessionFactory,
	 * exposed via the PersistenceExceptionTranslator interface.
	 * <p>Applied to any SQLException root cause of a Hibernate JDBCException,
	 * overriding Hibernate's default SQLException translation (which is
	 * based on Hibernate's Dialect for a specific target database).
	 * @param jdbcExceptionTranslator the exception translator
	 * @see java.sql.SQLException
	 * @see org.hibernate.JDBCException
	 * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLStateSQLExceptionTranslator
	 * @see org.springframework.dao.support.PersistenceExceptionTranslator
	 */
	public void setJdbcExceptionTranslator(SQLExceptionTranslator jdbcExceptionTranslator) {
		this.jdbcExceptionTranslator = jdbcExceptionTranslator;
	}


	/**
	 * Build and expose the SessionFactory.
	 * @see #buildSessionFactory()
	 * @see #wrapSessionFactoryIfNecessary
	 */
	public void afterPropertiesSet() throws Exception {
		SessionFactory rawSf = buildSessionFactory();
		this.sessionFactory = wrapSessionFactoryIfNecessary(rawSf);
		afterSessionFactoryCreation();
	}

	/**
	 * Wrap the given SessionFactory with a transaction-aware proxy, if demanded.
	 * @param rawSf the raw SessionFactory as built by <code>buildSessionFactory()</code>
	 * @return the SessionFactory reference to expose
	 * @see #buildSessionFactory()
	 * @see #getTransactionAwareSessionFactoryProxy
	 */
	protected SessionFactory wrapSessionFactoryIfNecessary(SessionFactory rawSf) {
		if (isExposeTransactionAwareSessionFactory()) {
			 return getTransactionAwareSessionFactoryProxy(rawSf);
		}
		else {
			return rawSf;
		}
	}

	/**
	 * Wrap the given SessionFactory with a proxy that delegates every method call
	 * to it but delegates <code>getCurrentSession</code> calls to SessionFactoryUtils,
	 * for participating in Spring-managed transactions.
	 * @param target the original SessionFactory to wrap
	 * @return the wrapped SessionFactory
	 * @see org.hibernate.SessionFactory#getCurrentSession()
	 * @see SessionFactoryUtils#doGetSession(org.hibernate.SessionFactory, boolean)
	 */
	protected SessionFactory getTransactionAwareSessionFactoryProxy(SessionFactory target) {
		Class sfInterface = SessionFactory.class;
		if (target instanceof SessionFactoryImplementor) {
			sfInterface = SessionFactoryImplementor.class;
		}
		return (SessionFactory) Proxy.newProxyInstance(sfInterface.getClassLoader(),
				new Class[] {sfInterface}, new TransactionAwareInvocationHandler(target));
	}

	/**
	 * Return the exposed SessionFactory.
	 * @throws IllegalStateException if the SessionFactory has not been initialized yet
	 */
	protected final SessionFactory getSessionFactory() {
		if (this.sessionFactory == null) {
			throw new IllegalStateException("SessionFactory not initialized yet");
		}
		return this.sessionFactory;
	}

	/**
	 * Close the SessionFactory on bean factory shutdown.
	 */
	public void destroy() throws HibernateException {
		logger.info("Closing Hibernate SessionFactory");
		try {
			beforeSessionFactoryDestruction();
		}
		finally {
			this.sessionFactory.close();
		}
	}


	/**
	 * Return the singleton SessionFactory.
	 */
	public Object getObject() {
		return this.sessionFactory;
	}

	public Class getObjectType() {
		return (this.sessionFactory != null) ? this.sessionFactory.getClass() : SessionFactory.class;
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Implementation of the PersistenceExceptionTranslator interface,
	 * as autodetected by Spring's PersistenceExceptionTranslationPostProcessor.
	 * <p>Converts the exception if it is a HibernateException;
	 * else returns <code>null</code> to indicate an unknown exception.
	 * @see org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
	 * @see #convertHibernateAccessException
	 */
	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		if (ex instanceof HibernateException) {
			return convertHibernateAccessException((HibernateException) ex);
		}
		return null;
	}

	/**
	 * Convert the given HibernateException to an appropriate exception from the
	 * <code>org.springframework.dao</code> hierarchy.
	 * <p>Will automatically apply a specified SQLExceptionTranslator to a
	 * Hibernate JDBCException, else rely on Hibernate's default translation.
	 * @param ex HibernateException that occured
	 * @return a corresponding DataAccessException
	 * @see SessionFactoryUtils#convertHibernateAccessException
	 * @see #setJdbcExceptionTranslator
	 */
	protected DataAccessException convertHibernateAccessException(HibernateException ex) {
		if (this.jdbcExceptionTranslator != null && ex instanceof JDBCException) {
			JDBCException jdbcEx = (JDBCException) ex;
			return this.jdbcExceptionTranslator.translate(
					"Hibernate operation: " + jdbcEx.getMessage(), jdbcEx.getSQL(), jdbcEx.getSQLException());
		}
		return SessionFactoryUtils.convertHibernateAccessException(ex);
	}


	/**
	 * Build the underlying Hibernate SessionFactory.
	 * @return the raw SessionFactory (potentially to be wrapped with a
	 * transaction-aware proxy before it is exposed to the application)
	 * @throws Exception in case of initialization failure
	 */
	protected abstract SessionFactory buildSessionFactory() throws Exception;

	/**
	 * Hook that allows post-processing after the SessionFactory has been
	 * successfully created. The SessionFactory is already available through
	 * <code>getSessionFactory()</code> at this point.
	 * <p>This implementation is empty.
	 * @throws Exception in case of initialization failure
	 * @see #getSessionFactory()
	 */
	protected void afterSessionFactoryCreation() throws Exception {
	}

	/**
	 * Hook that allows shutdown processing before the SessionFactory
	 * will be closed. The SessionFactory is still available through
	 * <code>getSessionFactory()</code> at this point.
	 * <p>This implementation is empty.
	 * @see #getSessionFactory()
	 */
	protected void beforeSessionFactoryDestruction() {
	}


	/**
	 * Invocation handler that delegates <code>getCurrentSession()</code> calls
	 * to SessionFactoryUtils, for being aware of thread-bound transactions.
	 */
	private static class TransactionAwareInvocationHandler implements InvocationHandler {

		private final SessionFactory target;

		public TransactionAwareInvocationHandler(SessionFactory target) {
			this.target = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on SessionFactory/SessionFactoryImplementor interface coming in...

			if (method.getName().equals("getCurrentSession")) {
				// Handle getCurrentSession method: return transactional Session, if any.
				try {
					return SessionFactoryUtils.doGetSession((SessionFactory) proxy, false);
				}
				catch (IllegalStateException ex) {
					throw new HibernateException(ex.getMessage());
				}
			}
			else if (method.getName().equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			}
			else if (method.getName().equals("hashCode")) {
				// Use hashCode of SessionFactory proxy.
				return new Integer(hashCode());
			}

			// Invoke method on target SessionFactory.
			try {
				return method.invoke(this.target, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
