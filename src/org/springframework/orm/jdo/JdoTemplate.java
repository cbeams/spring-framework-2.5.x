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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Helper class that simplifies JDO data access code, and converts
 * JDOExceptions into JdoUsage/JdoSystemException, compatible to the
 * org.springframework.dao exception hierarchy.
 *
 * <p>The central method is "execute", supporting JDO code implementing
 * the JdoCallback interface. It provides JDO PersistenceManager handling
 * such that neither the JdoCallback implementation nor the calling code
 * needs to explicitly care about retrieving/closing PersistenceManagers,
 * or handling JDO lifecycle exceptions.
 *
 * <p>Typically used to implement data access or business logic services that
 * use JDO within their implementation but are JDO-agnostic in their interface.
 * The latter or code calling the latter only have to deal with business
 * objects, query objects, and <code>org.springframework.dao</code> exceptions.
 *
 * <p>Can be used within a service implementation via direct instantiation
 * with a PersistenceManagerFactory reference, or get prepared in an
 * application context and given to services as bean reference.
 * Note: The PersistenceManagerFactory should always be configured as bean in
 * the application context, in the first case given to the service directly,
 * in the second case to the prepared template.
 *
 * <p>This class can be considered a programmatic alternative to
 * JdoInterceptor. The major advantage is its straightforwardness, the
 * major disadvantage that no checked application exceptions can get thrown
 * from within data access code. Respective checks and the actual throwing of
 * such exceptions can often be deferred to after callback execution, though.
 *
 * <p>Note that even if JdoTransactionManager is used for transaction
 * demarcation in higher-level services, all those services above the data
 * access layer don't need need to be JDO-aware. Setting such a special
 * PlatformTransactionManager is a configuration issue, without introducing
 * code dependencies.
 *
 * <p>LocalPersistenceManagerFactoryBean is the preferred way of obtaining a
 * reference to a specific PersistenceManagerFactory, at least in a non-EJB
 * environment. Registering a PersistenceManagerFactory with JNDI is only
 * advisable when using a JCA Connector, i.e. when the application server
 * cares for initialization. Else, portability is rather limited: Manual
 * JNDI binding isn't supported by some application servers (e.g. Tomcat).
 *
 * <p>Note that lazy loading will just work with an open JDO PersistenceManager,
 * either within a Spring-driven transaction (with JdoTransactionManager or
 * JtaTransactionManager) or within OpenPersistenceManagerInViewFilter/Interceptor.
 * Furthermore, some operations just make sense within transactions,
 * for example: <code>evict</code>, <code>evictAll</code>, <code>flush</code>.
 *
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @see #setPersistenceManagerFactory
 * @see JdoCallback
 * @see javax.jdo.PersistenceManager
 * @see JdoInterceptor
 * @see LocalPersistenceManagerFactoryBean
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see JdoTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.orm.jdo.support.OpenPersistenceManagerInViewFilter
 * @see org.springframework.orm.jdo.support.OpenPersistenceManagerInViewInterceptor
 */
public class JdoTemplate extends JdoAccessor implements JdoOperations {

	private boolean allowCreate = true;

	private boolean exposeNativePersistenceManager = true;


	/**
	 * Create a new JdoTemplate instance.
	 */
	public JdoTemplate() {
	}

	/**
	 * Create a new JdoTemplate instance.
	 * @param pmf PersistenceManagerFactory to create PersistenceManagers
	 */
	public JdoTemplate(PersistenceManagerFactory pmf) {
		setPersistenceManagerFactory(pmf);
		afterPropertiesSet();
	}

	/**
	 * Create a new JdoTemplate instance.
	 * @param pmf PersistenceManagerFactory to create PersistenceManagers
	 * @param allowCreate if a new PersistenceManager should be created
	 * if no thread-bound found
	 */
	public JdoTemplate(PersistenceManagerFactory pmf, boolean allowCreate) {
		setPersistenceManagerFactory(pmf);
		setAllowCreate(allowCreate);
		afterPropertiesSet();
	}

	/**
	 * Set if a new PersistenceManager should be created if no thread-bound found.
	 * <p>JdoTemplate is aware of a respective PersistenceManager bound to the
	 * current thread, for example when using JdoTransactionManager.
	 * If allowCreate is true, a new PersistenceManager will be created if none
	 * found. If false, an IllegalStateException will get thrown in this case.
	 * @see PersistenceManagerFactoryUtils#getPersistenceManager
	 */
	public void setAllowCreate(boolean allowCreate) {
		this.allowCreate = allowCreate;
	}

	/**
	 * Return if a new PersistenceManager should be created if no thread-bound found.
	 */
	public boolean isAllowCreate() {
		return allowCreate;
	}

	/**
	 * Set whether to expose the native JDO PersistenceManager to JdoCallback
	 * code. Default is true; if turned off, a PersistenceManager proxy will be
	 * returned, suppressing <code>close</code> calls and automatically applying
	 * transaction timeouts (if any).
	 * <p>The default is "true" for the time being, because there is often a need
	 * to cast to a vendor-specific PersistenceManager class in DAOs that use the
	 * JDO 1.0 API, for JDO 2.0 previews and other vendor-specific functionality.
	 * This is likely to change to "false" in a later Spring version.
	 * @see JdoCallback
	 * @see javax.jdo.PersistenceManager
	 * @see #prepareQuery
	 */
	public void setExposeNativePersistenceManager(boolean exposeNativePersistenceManager) {
		this.exposeNativePersistenceManager = exposeNativePersistenceManager;
	}

	/**
	 * Return whether to expose the native JDO PersistenceManager to JdoCallback
	 * code, or rather a PersistenceManager proxy.
	 */
	public boolean isExposeNativePersistenceManager() {
		return exposeNativePersistenceManager;
	}


	public Object execute(JdoCallback action) throws DataAccessException {
		return execute(action, isExposeNativePersistenceManager());
	}

	public Collection executeFind(JdoCallback action) throws DataAccessException {
		return (Collection) execute(action, isExposeNativePersistenceManager());
	}

	/**
	 * Execute the action specified by the given action object within a
	 * PersistenceManager.
	 * @param action callback object that specifies the JDO action
	 * @param exposeNativePersistenceManager whether to expose the native
	 * JDO persistence manager to callback code
	 * @return a result object returned by the action, or null
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 */
	public Object execute(JdoCallback action, boolean exposeNativePersistenceManager) throws DataAccessException {
		PersistenceManager pm = PersistenceManagerFactoryUtils.getPersistenceManager(
		    getPersistenceManagerFactory(), isAllowCreate());
		boolean existingTransaction =
		    TransactionSynchronizationManager.hasResource(getPersistenceManagerFactory());
		try {
			PersistenceManager pmToExpose = (exposeNativePersistenceManager ? pm : createPersistenceManagerProxy(pm));
			Object result = action.doInJdo(pmToExpose);
			flushIfNecessary(pm, existingTransaction);
			return result;
		}
		catch (JDOException ex) {
			throw convertJdoAccessException(ex);
		}
		catch (RuntimeException ex) {
			// callback code threw application exception
			throw ex;
		}
		finally {
			PersistenceManagerFactoryUtils.closePersistenceManagerIfNecessary(pm, getPersistenceManagerFactory());
		}
	}

	/**
	 * Create a close-suppressing proxy for the given JDO PersistenceManager.
	 * The proxy also prepares returned JDO Query objects.
	 * @param pm the JDO PersistenceManager to create a proxy for
	 * @return the PersistenceManager proxy
	 * @see javax.jdo.PersistenceManager#close
	 * @see #prepareQuery
	 */
	protected PersistenceManager createPersistenceManagerProxy(PersistenceManager pm) {
		return (PersistenceManager) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class[] {PersistenceManager.class},
				new CloseSuppressingInvocationHandler(pm));
	}


	//-------------------------------------------------------------------------
	// Convenience methods for load, save, delete
	//-------------------------------------------------------------------------

	public Object getObjectById(final Serializable objectId) throws DataAccessException {
		return execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) throws JDOException {
				return pm.getObjectById(objectId, true);
			}
		});
	}

	public Object getObjectById(final Class entityClass, final Serializable idValue) throws DataAccessException {
		return execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) throws JDOException {
				Object oid = pm.newObjectIdInstance(entityClass, idValue.toString());
				return pm.getObjectById(oid, true);
			}
		});
	}

	public void evict(final Object entity) throws DataAccessException {
		execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) throws JDOException {
				pm.evict(entity);
				return null;
			}
		});
	}

	public void evictAll() throws DataAccessException {
		execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) throws JDOException {
				pm.evictAll();
				return null;
			}
		});
	}

	public void refresh(final Object entity) throws DataAccessException {
		execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) throws JDOException {
				pm.refresh(entity);
				return null;
			}
		});
	}

	public void refreshAll() throws DataAccessException {
		execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) throws JDOException {
				pm.refreshAll();
				return null;
			}
		});
	}

	public void makePersistent(final Object entity) throws DataAccessException {
		execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) throws JDOException {
				pm.makePersistent(entity);
				return null;
			}
		});
	}

	public void deletePersistent(final Object entity) throws DataAccessException {
		execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) throws JDOException {
				pm.deletePersistent(entity);
				return null;
			}
		});
	}

	public void deletePersistentAll(final Collection entities) throws DataAccessException {
		execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) throws JDOException {
				pm.deletePersistentAll(entities);
				return null;
			}
		});
	}

	public void flush() throws DataAccessException {
		execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) throws JDOException {
				getJdoDialect().flush(pm);
				return null;
			}
		});
	}


	//-------------------------------------------------------------------------
	// Convenience finder methods
	//-------------------------------------------------------------------------

	public Collection find(Class entityClass) throws DataAccessException {
		return find(entityClass, null, null);
	}

	public Collection find(Class entityClass, String filter) throws DataAccessException {
		return find(entityClass, filter, null);
	}

	public Collection find(final Class entityClass, final String filter, final String ordering)
			throws DataAccessException {
		return executeFind(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) throws JDOException {
				Query query = (filter != null ? pm.newQuery(entityClass, filter) : pm.newQuery(entityClass));
				prepareQuery(query);
				if (ordering != null) {
					query.setOrdering(ordering);
				}
				return query.execute();
			}
		});
	}

	public Collection find(Class entityClass, String filter, String parameters, Object[] values)
			throws DataAccessException {
		return find(entityClass, filter, parameters, values, null);
	}

	public Collection find(
			final Class entityClass, final String filter, final String parameters, final Object[] values,
			final String ordering) throws DataAccessException {
		return executeFind(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) throws JDOException {
				Query query = pm.newQuery(entityClass, filter);
				prepareQuery(query);
				query.declareParameters(parameters);
				if (ordering != null) {
					query.setOrdering(ordering);
				}
				return query.executeWithArray(values);
			}
		});
	}

	public Collection find(Class entityClass, String filter, String parameters, Map values)
			throws DataAccessException {
		return find(entityClass, filter, parameters, values, null);
	}

	public Collection find(
			final Class entityClass, final String filter, final String parameters, final Map values,
			final String ordering) throws DataAccessException {
		return executeFind(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) throws JDOException {
				Query query = pm.newQuery(entityClass, filter);
				prepareQuery(query);
				query.declareParameters(parameters);
				if (ordering != null) {
					query.setOrdering(ordering);
				}
				return query.executeWithMap(values);
			}
		});
	}


	/**
	 * Prepare the given JDO query object. To be used within a JdoCallback.
	 * Applies a transaction timeout, if any. If you don't use such timeouts,
	 * the call is a no-op.
	 * <p>In general, prefer a proxied PersistenceManager instead, which will
	 * automatically apply the transaction timeout (through the use of a special
	 * PersistenceManager proxy). You need to set the "exposeNativePersistenceManager"
	 * property to "false" to activate this. Note that you won't be able to cast
	 * to a vendor-specific JDO PersistenceManager class anymore then.
	 * @param query the JDO query object
	 * @throws JDOException if the query could not be properly prepared
	 * @see JdoCallback#doInJdo
	 * @see PersistenceManagerFactoryUtils#applyTransactionTimeout
	 * @see #setExposeNativePersistenceManager
	 */
	public void prepareQuery(Query query) throws JDOException {
		PersistenceManagerFactoryUtils.applyTransactionTimeout(
				query, getPersistenceManagerFactory(), getJdoDialect());
	}


	/**
	 * Invocation handler that suppresses close calls on JDO PersistenceManagers.
	 * Also prepares returned Query and Criteria objects.
	 * @see javax.jdo.PersistenceManager#close
	 */
	private class CloseSuppressingInvocationHandler implements InvocationHandler {

		private static final String PERSISTENCE_MANAGER_CLOSE_METHOD_NAME = "close";

		private final PersistenceManager target;

		public CloseSuppressingInvocationHandler(PersistenceManager target) {
			this.target = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Handle close method: suppress, not valid.
			if (method.getName().equals(PERSISTENCE_MANAGER_CLOSE_METHOD_NAME)) {
				return null;
			}

			// Invoke method on target connection.
			try {
				Object retVal = method.invoke(this.target, args);

				// If return value is a JDO Query object, apply transaction timeout.
				if (retVal instanceof Query) {
					prepareQuery(((Query) retVal));
				}

				return retVal;
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
