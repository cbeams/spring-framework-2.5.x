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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.FlushMode;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.LockMode;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.type.Type;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * Helper class that simplifies Hibernate data access code, and converts
 * checked HibernateExceptions into unchecked DataAccessExceptions,
 * compatible to the org.springframework.dao exception hierarchy.
 * Uses the same SQLExceptionTranslator mechanism as JdbcTemplate.
 *
 * <p>Typically used to implement data access or business logic services that
 * use Hibernate within their implementation but are Hibernate-agnostic in their
 * interface. The latter or code calling the latter only have to deal with
 * domain objects, query objects, and <code>org.springframework.dao</code> exceptions.
 *
 * <p>The central method is "execute", supporting Hibernate code implementing
 * the HibernateCallback interface. It provides Hibernate Session handling
 * such that neither the HibernateCallback implementation nor the calling
 * code needs to explicitly care about retrieving/closing Hibernate Sessions,
 * or handling Session lifecycle exceptions. For typical single step actions,
 * there are various convenience methods (find, load, saveOrUpdate, delete).
 *
 * <p>Can be used within a service implementation via direct instantiation
 * with a SessionFactory reference, or get prepared in an application context
 * and given to services as bean reference. Note: The SessionFactory should
 * always be configured as bean in the application context, in the first case
 * given to the service directly, in the second case to the prepared template.
 *
 * <p>This class can be considered a programmatic alternative to
 * HibernateInterceptor. The major advantage is its straightforwardness, the
 * major disadvantage that no checked application exceptions can get thrown
 * from within data access code. Such checks and the actual throwing of such
 * exceptions can often be deferred to after callback execution, though.
 *
 * <p>Note that even if HibernateTransactionManager is used for transaction
 * demarcation in higher-level services, all those services above the data
 * access layer don't need need to be Hibernate-aware. Setting such a special
 * PlatformTransactionManager is a configuration issue: For example,
 * switching to JTA is just a matter of Spring configuration (use
 * JtaTransactionManager instead) that does not affect application code.
 *
 * <p>LocalSessionFactoryBean is the preferred way of obtaining a reference
 * to a specific Hibernate SessionFactory, at least in a non-EJB environment.
 * Alternatively, use a JndiObjectFactoryBean to fetch a SessionFactory
 * from JNDI (possibly set up via a JCA Connector).
 *
 * <p>Note that operations that return an Iterator (i.e. <code>iterate</code>)
 * are supposed to be used within Spring-driven or JTA-driven transactions
 * (with HibernateTransactionManager, JtaTransactionManager, or EJB CMT).
 * Else, the Iterator won't be able to read results from its ResultSet anymore,
 * as the underlying Hibernate Session will already have been closed.
 *
 * <p>Lazy loading will also just work with an open Hibernate Session,
 * either within a transaction or within OpenSessionInViewFilter/Interceptor.
 * Furthermore, some operations just make sense within transactions,
 * for example: <code>contains</code>, <code>evict</code>, <code>lock</code>,
 * <code>flush</code>, <code>clear</code>.
 *
 * <p>Note: Spring's Hibernate support requires Hibernate 2.1 (as of Spring 1.0).
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see #setSessionFactory
 * @see #setJdbcExceptionTranslator
 * @see HibernateCallback
 * @see net.sf.hibernate.Session
 * @see HibernateInterceptor
 * @see LocalSessionFactoryBean
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see org.springframework.jdbc.support.SQLExceptionTranslator
 * @see HibernateTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.orm.hibernate.support.OpenSessionInViewFilter
 * @see org.springframework.orm.hibernate.support.OpenSessionInViewInterceptor
 */
public class HibernateTemplate extends HibernateAccessor implements HibernateOperations {

	private boolean allowCreate = true;

	private boolean alwaysUseNewSession = false;

	private boolean exposeNativeSession = false;

	private boolean checkWriteOperations = true;

	private boolean cacheQueries = false;

	private String queryCacheRegion;


	/**
	 * Create a new HibernateTemplate instance.
	 */
	public HibernateTemplate() {
	}

	/**
	 * Create a new HibernateTemplate instance.
	 * @param sessionFactory SessionFactory to create Sessions
	 */
	public HibernateTemplate(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
		afterPropertiesSet();
	}

	/**
	 * Create a new HibernateTemplate instance.
	 * @param sessionFactory SessionFactory to create Sessions
	 * @param allowCreate if a new Session should be created
	 * if no thread-bound found
	 */
	public HibernateTemplate(SessionFactory sessionFactory, boolean allowCreate) {
		setSessionFactory(sessionFactory);
		setAllowCreate(allowCreate);
		afterPropertiesSet();
	}

	/**
	 * Set if a new Session should be created if no thread-bound found.
	 * <p>HibernateTemplate is aware of a respective Session bound to the
	 * current thread, for example when using HibernateTransactionManager.
	 * If allowCreate is true, a new Session will be created if none found.
	 * If false, an IllegalStateException will get thrown in this case.
	 * @see SessionFactoryUtils#getSession(SessionFactory, boolean)
	 */
	public void setAllowCreate(boolean allowCreate) {
		this.allowCreate = allowCreate;
	}

	/**
	 * Return if a new Session should be created if no thread-bound found.
	 */
	public boolean isAllowCreate() {
		return allowCreate;
	}

	/**
	 * Set whether to always use a new Hibernate Session for this template.
	 * Default is false; if activated, all operations on this template will
	 * work on a new Hibernate Session even in case of a pre-bound Session
	 * (for example, within a transaction or OpenSessionInViewFilter).
	 * <p>Within a transaction, a new Hibernate Session used by this template
	 * will participate in the transaction through using the same JDBC
	 * Connection. In such a scenario, multiple Sessions will participate
	 * in the same database transaction.
	 * <p>Turn this on for operations that are supposed to always execute
	 * independently, without side effects caused by a shared Hibernate
	 * Session.
	 */
	public void setAlwaysUseNewSession(boolean alwaysUseNewSession) {
		this.alwaysUseNewSession = alwaysUseNewSession;
	}

	/**
	 * Return whether to always use a new Hibernate Session for this template.
	 */
	public boolean isAlwaysUseNewSession() {
		return alwaysUseNewSession;
	}

	/**
	 * Set whether to expose the native Hibernate Session to HibernateCallback
	 * code. Default is false; instead, a Session proxy will be returned,
	 * suppressing <code>close</code> calls and automatically applying
	 * query cache settings and transaction timeouts.
	 * @see HibernateCallback
	 * @see net.sf.hibernate.Session
	 * @see #setCacheQueries
	 * @see #setQueryCacheRegion
	 * @see #prepareQuery
	 * @see #prepareCriteria
	 */
	public void setExposeNativeSession(boolean exposeNativeSession) {
		this.exposeNativeSession = exposeNativeSession;
	}

	/**
	 * Return whether to expose the native Hibernate Session to HibernateCallback
	 * code, or rather a Session proxy.
	 */
	public boolean isExposeNativeSession() {
		return exposeNativeSession;
	}

	/**
	 * Set whether to check that the Hibernate Session is not in read-only mode
	 * in case of write operations (save/update/delete).
	 * <p>Default is true, for fail-fast behavior when attempting write operations
	 * within a read-only transaction. Turn this off to allow save/update/delete
	 * on a Session with flush mode NEVER.
	 * @see #setFlushMode
	 * @see #checkWriteOperationAllowed
	 * @see org.springframework.transaction.TransactionDefinition#isReadOnly
	 */
	public void setCheckWriteOperations(boolean checkWriteOperations) {
		this.checkWriteOperations = checkWriteOperations;
	}

	/**
	 * Return whether to check that the Hibernate Session is not in read-only
	 * mode in case of write operations (save/update/delete).
	 */
	public boolean isCheckWriteOperations() {
		return checkWriteOperations;
	}

	/**
	 * Set whether to cache all queries executed by this template.
	 * If this is true, all Query and Criteria objects created by
	 * this template will be marked as cacheable (including all
	 * queries through find methods).
	 * <p>To specify the query region to be used for queries cached
	 * by this template, set the "queryCacheRegion" property.
	 * @see #setQueryCacheRegion
	 * @see net.sf.hibernate.Query#setCacheable
	 * @see net.sf.hibernate.Criteria#setCacheable
	 */
	public void setCacheQueries(boolean cacheQueries) {
		this.cacheQueries = cacheQueries;
	}

	/**
	 * Return whether to cache all queries executed by this template.
	 */
	public boolean isCacheQueries() {
		return cacheQueries;
	}

	/**
	 * Set the name of the cache region for queries executed by this template.
	 * If this is specified, it will be applied to all Query and Criteria objects
	 * created by this template (including all queries through find methods).
	 * <p>The cache region will not take effect unless queries created by this
	 * template are configured to be cached via the "cacheQueries" property.
	 * @see #setCacheQueries
	 * @see net.sf.hibernate.Query#setCacheRegion
	 * @see net.sf.hibernate.Criteria#setCacheRegion
	 */
	public void setQueryCacheRegion(String queryCacheRegion) {
		this.queryCacheRegion = queryCacheRegion;
	}

	/**
	 * Return the name of the cache region for queries executed by this template.
	 */
	public String getQueryCacheRegion() {
		return queryCacheRegion;
	}


	public Object execute(HibernateCallback action) throws DataAccessException {
		return execute(action, isExposeNativeSession());
	}

	public List executeFind(HibernateCallback action) throws DataAccessException {
		return (List) execute(action, isExposeNativeSession());
	}

	/**
	 * Execute the action specified by the given action object within a Session.
	 * @param action callback object that specifies the Hibernate action
	 * @param exposeNativeSession whether to expose the native Hibernate Session
	 * to callback code
	 * @return a result object returned by the action, or null
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 */
	public Object execute(HibernateCallback action, boolean exposeNativeSession) throws DataAccessException {
		Session session = getSession();
		boolean existingTransaction = SessionFactoryUtils.isSessionTransactional(session, getSessionFactory());
		if (!existingTransaction && getFlushMode() == FLUSH_NEVER) {
			session.setFlushMode(FlushMode.NEVER);
		}
		try {
			Session sessionToExpose = (exposeNativeSession ? session : createSessionProxy(session));
			Object result = action.doInHibernate(sessionToExpose);
			flushIfNecessary(session, existingTransaction);
			return result;
		}
		catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
		catch (SQLException ex) {
			throw convertJdbcAccessException(ex);
		}
		catch (RuntimeException ex) {
			// callback code threw application exception
			throw ex;
		}
		finally {
			SessionFactoryUtils.closeSessionIfNecessary(session, getSessionFactory());
		}
	}

	/**
	 * Return a Session for use by this template.
	 * <p>Returns a new Session in case of "alwaysUseNewSession" (using the same
	 * JDBC Connection as a transactional Session, if applicable), a pre-bound
	 * Session in case of "allowCreate" turned off, and a pre-bound or new Session
	 * else (new only if no transactional or otherwise pre-bound Session exists).
	 * @see SessionFactoryUtils#getSession
	 * @see SessionFactoryUtils#getNewSession
	 * @see #setAlwaysUseNewSession
	 * @see #setAllowCreate
	 */
	protected Session getSession() {
		if (isAlwaysUseNewSession()) {
			return SessionFactoryUtils.getNewSession(getSessionFactory(), getEntityInterceptor());
		}
		else if (!isAllowCreate()) {
			return SessionFactoryUtils.getSession(getSessionFactory(), false);
		}
		else {
			return SessionFactoryUtils.getSession(
					getSessionFactory(), getEntityInterceptor(), getJdbcExceptionTranslator());
		}
	}

	/**
	 * Create a close-suppressing proxy for the given Hibernate Session.
	 * The proxy also prepares returned Query and Criteria objects.
	 * @param session the Hibernate Session to create a proxy for
	 * @return the Session proxy
	 * @see net.sf.hibernate.Session#close
	 * @see #prepareQuery
	 * @see #prepareCriteria
	 */
	protected Session createSessionProxy(Session session) {
		return (Session) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class[] {Session.class},
				new CloseSuppressingInvocationHandler(session));
	}


	//-------------------------------------------------------------------------
	// Convenience methods for loading individual objects
	//-------------------------------------------------------------------------

	public Object get(final Class entityClass, final Serializable id) throws DataAccessException {
		return execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.get(entityClass, id);
			}
		}, true);
	}

	public Object get(final Class entityClass, final Serializable id, final LockMode lockMode)
			throws DataAccessException {
		return execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.get(entityClass, id, lockMode);
			}
		}, true);
	}

	public Object load(final Class entityClass, final Serializable id) throws DataAccessException {
		return execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.load(entityClass, id);
			}
		}, true);
	}

	public Object load(final Class entityClass, final Serializable id, final LockMode lockMode)
			throws DataAccessException {
		return execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.load(entityClass, id, lockMode);
			}
		}, true);
	}

	public List loadAll(final Class entityClass) throws DataAccessException {
		return (List) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Criteria criteria = session.createCriteria(entityClass);
				prepareCriteria(criteria);
				return criteria.list();
			}
		}, true);
	}

	public void load(final Object entity, final Serializable id) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.load(entity, id);
				return null;
			}
		}, true);
	}

	public void refresh(final Object entity) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.refresh(entity);
				return null;
			}
		}, true);
	}

	public void refresh(final Object entity, final LockMode lockMode) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.refresh(entity, lockMode);
				return null;
			}
		}, true);
	}

	public boolean contains(final Object entity) throws DataAccessException {
		Boolean result = (Boolean) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return new Boolean(session.contains(entity));
			}
		}, true);
		return result.booleanValue();
	}

	public void evict(final Object entity) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.evict(entity);
				return null;
			}
		}, true);
	}

	public void initialize(Object proxy) throws DataAccessException {
		try {
			Hibernate.initialize(proxy);
		}
		catch (HibernateException ex) {
			throw SessionFactoryUtils.convertHibernateAccessException(ex);
		}
	}


	//-------------------------------------------------------------------------
	// Convenience methods for storing individual objects
	//-------------------------------------------------------------------------

	public void lock(final Object entity, final LockMode lockMode) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.lock(entity, lockMode);
				return null;
			}
		}, true);
	}

	public Serializable save(final Object entity) throws DataAccessException {
		return (Serializable) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				checkWriteOperationAllowed(session);
				return session.save(entity);
			}
		}, true);
	}

	public void save(final Object entity, final Serializable id) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				checkWriteOperationAllowed(session);
				session.save(entity, id);
				return null;
			}
		}, true);
	}

	public void update(final Object entity) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				checkWriteOperationAllowed(session);
				session.update(entity);
				return null;
			}
		}, true);
	}

	public void update(final Object entity, final LockMode lockMode) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				checkWriteOperationAllowed(session);
				session.update(entity);
				session.lock(entity, lockMode);
				return null;
			}
		}, true);
	}

	public void saveOrUpdate(final Object entity) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				checkWriteOperationAllowed(session);
				session.saveOrUpdate(entity);
				return null;
			}
		}, true);
	}

	public void saveOrUpdateAll(final Collection entities) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				checkWriteOperationAllowed(session);
				for (Iterator it = entities.iterator(); it.hasNext();) {
					session.saveOrUpdate(it.next());
				}
				return null;
			}
		}, true);
	}

	public Object saveOrUpdateCopy(final Object entity) throws DataAccessException {
		return execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				checkWriteOperationAllowed(session);
				return session.saveOrUpdateCopy(entity);
			}
		}, true);
	}

	public void delete(final Object entity) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				checkWriteOperationAllowed(session);
				session.delete(entity);
				return null;
			}
		}, true);
	}

	public void delete(final Object entity, final LockMode lockMode) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				checkWriteOperationAllowed(session);
				session.lock(entity, lockMode);
				session.delete(entity);
				return null;
			}
		}, true);
	}

	public void deleteAll(final Collection entities) throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				checkWriteOperationAllowed(session);
				for (Iterator it = entities.iterator(); it.hasNext();) {
					session.delete(it.next());
				}
				return null;
			}
		}, true);
	}

	public void flush() throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.flush();
				return null;
			}
		}, true);
	}

	public void clear() throws DataAccessException {
		execute(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				session.clear();
				return null;
			}
		}, true);
	}


	//-------------------------------------------------------------------------
	// Convenience finder methods for HQL strings
	//-------------------------------------------------------------------------

	public List find(final String queryString) throws DataAccessException {
		return (List) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = session.createQuery(queryString);
				prepareQuery(queryObject);
				return queryObject.list();
			}
		}, true);
	}

	public List find(String queryString, Object value) throws DataAccessException {
		return find(queryString, value, null);
	}

	public List find(final String queryString, final Object value, final Type type)
			throws DataAccessException {
		return (List) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = session.createQuery(queryString);
				prepareQuery(queryObject);
				if (type != null) {
					queryObject.setParameter(0, value, type);
				}
				else {
					queryObject.setParameter(0, value);
				}
				return queryObject.list();
			}
		}, true);
	}

	public List find(String queryString, Object[] values) throws DataAccessException {
		return find(queryString, values, (Type[]) null);
	}

	public List find(final String queryString, final Object[] values, final Type[] types)
			throws DataAccessException {
		if (types != null && values.length != types.length) {
			throw new IllegalArgumentException("Length of values array must match length of types array");
		}
		return (List) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = session.createQuery(queryString);
				prepareQuery(queryObject);
				for (int i = 0; i < values.length; i++) {
					if (types != null) {
						queryObject.setParameter(i, values[i], types[i]);
					}
					else {
						queryObject.setParameter(i, values[i]);
					}
				}
				return queryObject.list();
			}
		}, true);
	}

	public List findByNamedParam(String queryString, String paramName, Object value)
			throws DataAccessException {
		return findByNamedParam(queryString, paramName, value, null);
	}

	public List findByNamedParam(
	    final String queryString, final String paramName, final Object value, final Type type)
			throws DataAccessException {
		return (List) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = session.createQuery(queryString);
				prepareQuery(queryObject);
				applyNamedParameterToQuery(queryObject, paramName, value, type);
				return queryObject.list();
			}
		}, true);
	}

	public List findByNamedParam(String queryString, String[] paramNames, Object[] values)
			throws DataAccessException {
		return findByNamedParam(queryString, paramNames, values, null);
	}

	public List findByNamedParam(
	    final String queryString, final String[] paramNames, final Object[] values, final Type[] types)
	    throws DataAccessException {
		if (paramNames.length != values.length) {
			throw new IllegalArgumentException("Length of paramNames array must match length of values array");
		}
		if (types != null && paramNames.length != types.length) {
			throw new IllegalArgumentException("Length of paramNames array must match length of types array");
		}
		return (List) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = session.createQuery(queryString);
				prepareQuery(queryObject);
				for (int i = 0; i < values.length; i++) {
					applyNamedParameterToQuery(queryObject, paramNames[i], values[i], (types != null ? types[i] : null));
				}
				return queryObject.list();
			}
		}, true);
	}

	public List findByValueBean(final String queryString, final Object valueBean)
			throws DataAccessException {
		return (List) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = session.createQuery(queryString);
				prepareQuery(queryObject);
				queryObject.setProperties(valueBean);
				return queryObject.list();
			}
		}, true);
	}


	//-------------------------------------------------------------------------
	// Convenience finder methods for named queries
	//-------------------------------------------------------------------------

	public List findByNamedQuery(final String queryName) throws DataAccessException {
		return (List) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				return queryObject.list();
			}
		}, true);
	}

	public List findByNamedQuery(final String queryName, final Object value) throws DataAccessException {
		return findByNamedQuery(queryName, value, null);
	}

	public List findByNamedQuery(final String queryName, final Object value, final Type type)
			throws DataAccessException {
		return (List) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				if (type != null) {
					queryObject.setParameter(0, value, type);
				}
				else {
					queryObject.setParameter(0, value);
				}
				return queryObject.list();
			}
		}, true);
	}

	public List findByNamedQuery(String queryName, Object[] values) throws DataAccessException {
		return findByNamedQuery(queryName, values, (Type[]) null);
	}

	public List findByNamedQuery(final String queryName, final Object[] values, final Type[] types)
			throws DataAccessException {
		if (types != null && values.length != types.length) {
			throw new IllegalArgumentException("Length of values array must match length of types array");
		}
		return (List) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				for (int i = 0; i < values.length; i++) {
					if (types != null) {
						queryObject.setParameter(i, values[i], types[i]);
					}
					else {
						queryObject.setParameter(i, values[i]);
					}
				}
				return queryObject.list();
			}
		}, true);
	}

	/**
	 * @deprecated in favor of findByNamedQueryAndNamedParam,
	 * to avoid parameter overloading ambiguities
	 * @see #findByNamedQueryAndNamedParam
	 */
	public List findByNamedQuery(String queryName, String paramName, Object value)
	    throws DataAccessException {
		return findByNamedQueryAndNamedParam(queryName, paramName, value);
	}

	/**
	 * @deprecated in favor of findByNamedQueryAndNamedParam,
	 * to avoid parameter overloading ambiguities
	 * @see #findByNamedQueryAndNamedParam
	 */
	public List findByNamedQuery(String queryName, String paramName, Object value, Type type)
	    throws DataAccessException {
		return findByNamedQueryAndNamedParam(queryName, paramName, value, type);
	}

	/**
	 * @deprecated in favor of findByNamedQueryAndNamedParam,
	 * to avoid parameter overloading ambiguities
	 * @see #findByNamedQueryAndNamedParam
	 */
	public List findByNamedQuery(String queryName, String[] paramNames, Object[] values)
	    throws DataAccessException {
		return findByNamedQueryAndNamedParam(queryName, paramNames, values);
	}

	/**
	 * @deprecated in favor of findByNamedQueryAndNamedParam,
	 * to avoid parameter overloading ambiguities
	 * @see #findByNamedQueryAndNamedParam
	 */
	public List findByNamedQuery(String queryName, String[] paramNames, Object[] values, Type[] types)
	    throws DataAccessException {
		return findByNamedQueryAndNamedParam(queryName, paramNames, values, types);
	}

	public List findByNamedQueryAndNamedParam(String queryName, String paramName, Object value)
			throws DataAccessException {
		return findByNamedQueryAndNamedParam(queryName, paramName, value, null);
	}

	public List findByNamedQueryAndNamedParam(
	    final String queryName, final String paramName, final Object value, final Type type)
			throws DataAccessException {
		return (List) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				applyNamedParameterToQuery(queryObject, paramName, value, type);
				return queryObject.list();
			}
		}, true);
	}

	public List findByNamedQueryAndNamedParam(String queryName, String[] paramNames, Object[] values)
			throws DataAccessException {
		return findByNamedQueryAndNamedParam(queryName, paramNames, values, null);
	}

	public List findByNamedQueryAndNamedParam(
	    final String queryName, final String[] paramNames, final Object[] values, final Type[] types)
	    throws DataAccessException {
		if (paramNames.length != values.length) {
			throw new IllegalArgumentException("Length of paramNames array must match length of values array");
		}
		if (types != null && paramNames.length != types.length) {
			throw new IllegalArgumentException("Length of paramNames array must match length of types array");
		}
		return (List) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				for (int i = 0; i < values.length; i++) {
					applyNamedParameterToQuery(queryObject, paramNames[i], values[i], (types != null ? types[i] : null));
				}
				return queryObject.list();
			}
		}, true);
	}

	public List findByNamedQueryAndValueBean(final String queryName, final Object valueBean)
			throws DataAccessException {
		return (List) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = getNamedQuery(session, queryName);
				queryObject.setProperties(valueBean);
				return queryObject.list();
			}
		}, true);
	}


	//-------------------------------------------------------------------------
	// Convenience query methods for iterate and delete
	//-------------------------------------------------------------------------

	public Iterator iterate(final String queryString) throws DataAccessException {
		return (Iterator) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = session.createQuery(queryString);
				prepareQuery(queryObject);
				return queryObject.iterate();
			}
		}, true);
	}

	public Iterator iterate(String queryString, Object value) throws DataAccessException {
		return iterate(queryString, value, null);
	}

	public Iterator iterate(final String queryString, final Object value, final Type type)
			throws DataAccessException {
		return (Iterator) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = session.createQuery(queryString);
				prepareQuery(queryObject);
				if (type != null) {
					queryObject.setParameter(0, value, type);
				}
				else {
					queryObject.setParameter(0, value);
				}
				return queryObject.iterate();
			}
		}, true);
	}

	public Iterator iterate(String queryString, Object[] values) throws DataAccessException {
		return iterate(queryString, values, (Type[]) null);
	}

	public Iterator iterate(final String queryString, final Object[] values, final Type[] types)
			throws DataAccessException {
		if (types != null && values.length != types.length) {
			throw new IllegalArgumentException("Length of values array must match length of types array");
		}
		return (Iterator) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Query queryObject = session.createQuery(queryString);
				prepareQuery(queryObject);
				for (int i = 0; i < values.length; i++) {
					if (types != null) {
						queryObject.setParameter(i, values[i], types[i]);
					}
					else {
						queryObject.setParameter(i, values[i]);
					}
				}
				return queryObject.iterate();
			}
		}, true);
	}

	public void closeIterator(Iterator it) throws DataAccessException {
		try {
			Hibernate.close(it);
		}
		catch (HibernateException ex) {
			throw SessionFactoryUtils.convertHibernateAccessException(ex);
		}
	}

	public int delete(final String queryString) throws DataAccessException {
		Integer deleteCount = (Integer) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				checkWriteOperationAllowed(session);
				return new Integer(session.delete(queryString));
			}
		}, true);
		return deleteCount.intValue();
	}

	public int delete(final String queryString, final Object value, final Type type)
			throws DataAccessException {
		Integer deleteCount = (Integer) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				checkWriteOperationAllowed(session);
				return new Integer(session.delete(queryString, value, type));
			}
		}, true);
		return deleteCount.intValue();
	}

	public int delete(final String queryString, final Object[] values, final Type[] types)
			throws DataAccessException {
		Integer deleteCount = (Integer) execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				checkWriteOperationAllowed(session);
				return new Integer(session.delete(queryString, values, types));
			}
		}, true);
		return deleteCount.intValue();
	}


	/**
	 * Check whether write operations are allowed on the given Session.
	 * <p>Default implementation throws an InvalidDataAccessApiUsageException
	 * in case of FlushMode.NEVER. Can be overridden in subclasses.
	 * @param session current Hibernate Session
	 * @throws InvalidDataAccessApiUsageException if write operations are not allowed
	 * @see #setCheckWriteOperations
	 * @see #getFlushMode
	 * @see #FLUSH_EAGER
	 * @see net.sf.hibernate.Session#getFlushMode
	 * @see net.sf.hibernate.FlushMode#NEVER
	 */
	protected void checkWriteOperationAllowed(Session session) throws InvalidDataAccessApiUsageException {
		if (isCheckWriteOperations() && getFlushMode() != FLUSH_EAGER &&
				FlushMode.NEVER.equals(session.getFlushMode())) {
			throw new InvalidDataAccessApiUsageException(
					"Write operations are not allowed in read-only mode (FlushMode.NEVER) - turn your Session " +
					"into FlushMode.AUTO respectively remove 'readOnly' marker from transaction definition");
		}
	}

	/**
	 * Prepare the given Query object, applying cache settings and/or
	 * a transaction timeout.
	 * @param queryObject the Query object to prepare
	 * @see #setCacheQueries
	 * @see #setQueryCacheRegion
	 * @see SessionFactoryUtils#applyTransactionTimeout
	 */
	protected void prepareQuery(Query queryObject) {
		if (isCacheQueries()) {
			queryObject.setCacheable(true);
			if (getQueryCacheRegion() != null) {
				queryObject.setCacheRegion(getQueryCacheRegion());
			}
		}
		SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
	}

	/**
	 * Prepare the given Criteria object, applying cache settings and/or
	 * a transaction timeout.
	 * @param criteria the Criteria object to prepare
	 * @see #setCacheQueries
	 * @see #setQueryCacheRegion
	 * @see SessionFactoryUtils#applyTransactionTimeout
	 */
	protected void prepareCriteria(Criteria criteria) {
		if (isCacheQueries()) {
			criteria.setCacheable(true);
			if (getQueryCacheRegion() != null) {
				criteria.setCacheRegion(getQueryCacheRegion());
			}
		}
		SessionFactoryUtils.applyTransactionTimeout(criteria, getSessionFactory());
	}

	/**
	 * Apply the given name parameter to the given Query object.
	 * @param queryObject the Query object
	 * @param paramName the name of the parameter
	 * @param value the value of the parameter
	 * @param type Hibernate type of the parameter (or null if none specified)
	 * @throws HibernateException if thrown by the Query object
	 */
	protected void applyNamedParameterToQuery(Query queryObject, String paramName, Object value, Type type)
			throws HibernateException {
		if (value instanceof Collection) {
			if (type != null) {
				queryObject.setParameterList(paramName, (Collection) value, type);
			}
			else {
				queryObject.setParameterList(paramName, (Collection) value);
			}
		}
		else if (value instanceof Object[]) {
			if (type != null) {
				queryObject.setParameterList(paramName, (Object[]) value, type);
			}
			else {
				queryObject.setParameterList(paramName, (Object[]) value);
			}
		}
		else {
			if (type != null) {
				queryObject.setParameter(paramName, value, type);
			}
			else {
				queryObject.setParameter(paramName, value);
			}
		}
	}


	/**
	 * Create a Query object for the given Session and the given query string.
	 * <b>To be used within a HibernateCallback</b>:
	 * <pre>
	 * List result = hibernateTemplate.executeFind(new HibernateCallback() {
	 *   public Object doInHibernate(Session session) throws HibernateException {
	 *     Query query = hibernateTemplate.createQuery(session, "...");
	 *     ...
	 *     return query.list();
	 *   }
	 * });</pre>
	 * Applies query cache settings and a transaction timeout, if any. If you don't
	 * use either of those, the call is equivalent to <code>Session.createQuery</code>.
	 * @param session current Hibernate Session
	 * @param queryString the HQL query string
	 * @return the Query object
	 * @throws HibernateException if the Query could not be created
	 * @deprecated Use <code>session.createQuery</code> instead, which will now
	 * automatically apply the template's query cache settings and the transaction
	 * timeout (through the use of a special Session proxy).
	 * @see HibernateCallback#doInHibernate
	 * @see #setCacheQueries
	 * @see SessionFactoryUtils#applyTransactionTimeout
	 * @see net.sf.hibernate.Session#createQuery
	 */
	public Query createQuery(Session session, String queryString) throws HibernateException {
		Query queryObject = session.createQuery(queryString);
		prepareQuery(queryObject);
		return queryObject;
	}

	/**
	 * Create a named Query object for the given Session and the given query name.
	 * <b>To be used within a HibernateCallback</b>:
	 * <pre>
	 * List result = hibernateTemplate.executeFind(new HibernateCallback() {
	 *   public Object doInHibernate(Session session) throws HibernateException {
	 *     Query query = hibernateTemplate.getNamedQuery(session, "...");
	 *     ...
	 *     return query.list();
	 *   }
	 * });</pre>
	 * Applies query cache settings and a transaction timeout, if any. If you don't
	 * use either of those, the call is equivalent to <code>Session.getNamedQuery</code>.
	 * @param session current Hibernate Session
	 * @param queryName the name of the query in the Hibernate mapping file
	 * @return the Query object
	 * @throws HibernateException if the Query could not be created
	 * @deprecated Use <code>session.getNamedQuery</code> instead, which will now
	 * automatically apply the template's query cache settings and the transaction
	 * timeout (through the use of a special Session proxy).
	 * @see HibernateCallback#doInHibernate
	 * @see #setCacheQueries
	 * @see SessionFactoryUtils#applyTransactionTimeout
	 * @see net.sf.hibernate.Session#getNamedQuery
	 */
	public Query getNamedQuery(Session session, String queryName) throws HibernateException {
		Query queryObject = session.getNamedQuery(queryName);
		prepareQuery(queryObject);
		return queryObject;
	}

	/**
	 * Create a Criteria object for the given Session and the given entity class.
	 * <b>To be used within a HibernateCallback</b>:
	 * <pre>
	 * List result = hibernateTemplate.executeFind(new HibernateCallback() {
	 *   public Object doInHibernate(Session session) throws HibernateException {
	 *     Criteria criteria = hibernateTemplate.createCriteria(session, MyClass.class);
	 *     ...
	 *     return query.list();
	 *   }
	 * });</pre>
	 * Applies query cache settings and a transaction timeout, if any. If you don't
	 * use either of those, the call is equivalent to <code>Session.createCriteria</code>.
	 * @param session current Hibernate Session
	 * @param entityClass the entity class to create the Criteria for
	 * @return the Query object
	 * @throws HibernateException if the Criteria could not be created
	 * @deprecated Use <code>session.createCriteria</code> instead, which will now
	 * automatically apply the template's query cache settings and the transaction
	 * timeout (through the use of a special Session proxy).
	 * @see HibernateCallback#doInHibernate
	 * @see #setCacheQueries
	 * @see #setQueryCacheRegion
	 * @see SessionFactoryUtils#applyTransactionTimeout
	 * @see net.sf.hibernate.Session#createCriteria
	 */
	public Criteria createCriteria(Session session, Class entityClass) throws HibernateException {
		Criteria criteria = session.createCriteria(entityClass);
		prepareCriteria(criteria);
		return criteria;
	}


	/**
	 * Invocation handler that suppresses close calls on Hibernate Sessions.
	 * Also prepares returned Query and Criteria objects.
	 * @see net.sf.hibernate.Session#close
	 */
	private class CloseSuppressingInvocationHandler implements InvocationHandler {

		private static final String SESSION_CLOSE_METHOD_NAME = "close";

		private final Session target;

		public CloseSuppressingInvocationHandler(Session target) {
			this.target = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Handle close method: suppress, not valid.
			if (method.getName().equals(SESSION_CLOSE_METHOD_NAME)) {
				return null;
			}

			// Invoke method on target connection.
			try {
				Object retVal = method.invoke(this.target, args);

				// If return value is a Query or Criteria, apply transaction timeout.
				// Applies to createQuery, getNamedQuery, createCriteria.
				if (retVal instanceof Query) {
					prepareQuery(((Query) retVal));
				}
				if (retVal instanceof Criteria) {
					prepareCriteria(((Criteria) retVal));
				}

				return retVal;
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
