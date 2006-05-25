/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.orm.jpa;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.util.ClassUtils;

/**
 * Helper class that simplifies JPA data access code, and converts
 * PersistenceExceptions into Spring DataAccessExceptions, following the
 * <code>org.springframework.dao</code> exception hierarchy.
 *
 * <p>The central method is "execute", supporting JPA code implementing
 * the JpaCallback interface. It provides JPA EntityManager handling
 * such that neither the JpaCallback implementation nor the calling code
 * needs to explicitly care about retrieving/closing EntityManagers,
 * or handling JPA lifecycle exceptions.
 *
 * <p>Typically used to implement data access or business logic services that
 * use JPA within their implementation but are JPA-agnostic in their interface.
 * The latter or code calling the latter only have to deal with business
 * objects, query objects, and <code>org.springframework.dao</code> exceptions.
 *
 * <p>Can be used within a service implementation via direct instantiation
 * with a EntityManagerFactory reference, or get prepared in an
 * application context and given to services as bean reference.
 * Note: The EntityManagerFactory should always be configured as bean in
 * the application context, in the first case given to the service directly,
 * in the second case to the prepared template.
 *
 * <p>This class can be considered as direct alternative to working with the raw
 * JPA EntityManager API (through <code>EntityManagerFactory.getEntityManager()</code>
 * or a shared EntityManager reference). The major advantage is its automatic
 * conversion to DataAccessExceptions, the major disadvantage that no checked
 * application exceptions can get thrown from within data access code.
 * Corresponding checks and the actual throwing of such exceptions can often
 * be deferred to after callback execution, though.
 *
 * <p>Note that even if JpaTransactionManager is used for transaction
 * demarcation in higher-level services, all those services above the data
 * access layer don't need need to be JPA-aware. Setting such a special
 * PlatformTransactionManager is a configuration issue, without introducing
 * code dependencies: For example, switching to JTA is just a matter of
 * Spring configuration (use JtaTransactionManager instead) and JPA provider
 * configuration, neither affecting application code.
 *
 * <p>LocalEntityManagerFactoryBean is the preferred way of obtaining a
 * reference to an EntityManagerFactory outside of a full Java EE 5 environment.
 * Within a Java EE 5 environment, you will typically work with either an
 * EntityManagerFactory obtained from JNDI or a shared EntityManager proxy
 * obtained from JNDI (via Spring's JndiObjectFactoryBean).
 *
 * <p>Note that lazy loading will just work with an open JPA EntityManager,
 * either within a Spring-driven transaction (with JpaTransactionManager or
 * JtaTransactionManager) or within OpenEntityManagerInViewFilter/Interceptor.
 * Furthermore, some operations just make sense within transactions,
 * for example: <code>evict</code>, <code>evictAll</code>, <code>flush</code>.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setEntityManagerFactory
 * @see JpaCallback
 * @see javax.persistence.EntityManager
 * @see JpaInterceptor
 * @see LocalEntityManagerFactoryBean
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see JpaTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter
 * @see org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor
 */
public class JpaTemplate extends JpaAccessor implements JpaOperations {

	private boolean exposeNativeEntityManager = false;


	/**
	 * Create a new JpaTemplate instance.
	 */
	public JpaTemplate() {
	}

	/**
	 * Create a new JpaTemplate instance.
	 * @param emf EntityManagerFactory to create EntityManagers
	 */
	public JpaTemplate(EntityManagerFactory emf) {
		setEntityManagerFactory(emf);
		afterPropertiesSet();
	}

	/**
	 * Create a new JpaTemplate instance.
	 * @param em EntityManager to use
	 */
	public JpaTemplate(EntityManager em) {
		setEntityManager(em);
		afterPropertiesSet();
	}


	/**
	 * Set whether to expose the native JPA EntityManager to JpaCallback
	 * code. Default is "false": a EntityManager proxy will be returned,
	 * suppressing <code>close</code> calls and automatically applying transaction
	 * timeouts (if any).
	 * <p>As there is often a need to cast to a provider-specific EntityManager
	 * class in DAOs that use the JPA 1.0 API, for JPA 2.0 previews and other
	 * provider-specific functionality, the exposed proxy implements all interfaces
	 * implemented by the original EntityManager. If this is not sufficient,
	 * turn this flag to "true".
	 * @see JpaCallback
	 * @see javax.persistence.EntityManager
	 */
	public void setExposeNativeEntityManager(boolean exposeNativeEntityManager) {
		this.exposeNativeEntityManager = exposeNativeEntityManager;
	}

	/**
	 * Return whether to expose the native JPA EntityManager to JpaCallback
	 * code, or rather an EntityManager proxy.
	 */
	public boolean isExposeNativeEntityManager() {
		return exposeNativeEntityManager;
	}


	public Object execute(JpaCallback action) throws DataAccessException {
		return execute(action, isExposeNativeEntityManager());
	}

	public List executeFind(JpaCallback action) throws DataAccessException {
		Object result = execute(action, isExposeNativeEntityManager());
		if (!(result instanceof List)) {
			throw new InvalidDataAccessApiUsageException(
					"Result object returned from JpaCallback isn't a List: [" + result + "]");
		}
		return (List) result;
	}

	/**
	 * Execute the action specified by the given action object within a
	 * EntityManager.
	 * @param action callback object that specifies the JPA action
	 * @param exposeNativeEntityManager whether to expose the native
	 * JPA persistence manager to callback code
	 * @return a result object returned by the action, or <code>null</code>
	 * @throws org.springframework.dao.DataAccessException in case of JPA errors
	 */
	public Object execute(JpaCallback action, boolean exposeNativeEntityManager) throws DataAccessException {
		EntityManager em = getEntityManager();
		boolean isNewEm = false;
		if (em == null) {
			em = EntityManagerFactoryUtils.getTransactionalEntityManager(getEntityManagerFactory());
			if (em == null) {
				logger.debug("Creating new EntityManager for JPA template execution");
				em = getEntityManagerFactory().createEntityManager();
				isNewEm = true;
			}
		}

		try {
			EntityManager emToExpose = (exposeNativeEntityManager ? em : createEntityManagerProxy(em));
			Object result = action.doInJpa(emToExpose);
			flushIfNecessary(em, !isNewEm);
			return result;
		}
		catch (PersistenceException ex) {
			// TODO translation util method
			throw translateExceptionIfPossible(ex);
		}
		catch (RuntimeException ex) {
			// callback code threw application exception
			throw ex;
		}
		finally {
			if (isNewEm) {
				logger.debug("Closing new EntityManager after JPA template execution");
				em.close();
			}
		}
	}

	/**
	 * Create a close-suppressing proxy for the given JPA EntityManager.
	 * The proxy also prepares returned JPA Query objects.
	 * @param em the JPA EntityManager to create a proxy for
	 * @return the EntityManager proxy, implementing all interfaces
	 * implemented by the passed-in EntityManager object (that is,
	 * also implementing all provider-specific extension interfaces)
	 * @see javax.persistence.EntityManager#close
	 */
	protected EntityManager createEntityManagerProxy(EntityManager em) {
		Class[] ifcs = ClassUtils.getAllInterfaces(em);
		return (EntityManager) Proxy.newProxyInstance(
				getClass().getClassLoader(), ifcs, new CloseSuppressingInvocationHandler(em));
	}


	//-------------------------------------------------------------------------
	// Convenience methods for load, save, delete
	//-------------------------------------------------------------------------

	public <T> T find(final Class<T> entityClass, final Object id) throws DataAccessException {
		return (T) execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				return em.find(entityClass, id);
			}
		}, true);
	}

	public <T> T getReference(final Class<T> entityClass, final Object id) throws DataAccessException {
		return (T) execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				return em.getReference(entityClass, id);
			}
		}, true);
	}

	public boolean contains(final Object entity) throws DataAccessException {
		Boolean result = (Boolean) execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				return new Boolean(em.contains(entity));
			}
		}, true);
		return result.booleanValue();
	}

	public void refresh(final Object entity) throws DataAccessException {
		execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				em.refresh(entity);
				return null;
			}
		}, true);
	}

	public void persist(final Object entity) throws DataAccessException {
		execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				em.persist(entity);
				return null;
			}
		}, true);
	}

	public <T> T merge(final T entity) throws DataAccessException {
		return (T) execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				return em.merge(entity);
			}
		}, true);
	}

	public void remove(final Object entity) throws DataAccessException {
		execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				em.remove(entity);
				return null;
			}
		}, true);
	}

	public void flush() throws DataAccessException {
		execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				em.flush();
				return null;
			}
		}, true);
	}


	//-------------------------------------------------------------------------
	// Convenience finder methods
	//-------------------------------------------------------------------------

	public List find(String queryString) throws DataAccessException {
		return find(queryString, (Object[]) null);
	}

	public List find(final String queryString, final Object... values) throws DataAccessException {
		return executeFind(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query queryObject = em.createQuery(queryString);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						queryObject.setParameter(i + 1, values[i]);
					}
				}
				return queryObject.getResultList();
			}
		});
	}

	public List find(final String queryString, final Map<String,Object> params) throws DataAccessException {
		return executeFind(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query queryObject = em.createQuery(queryString);
				if (params != null) {
					for (Iterator it = params.entrySet().iterator(); it.hasNext();) {
						Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
						queryObject.setParameter(entry.getKey(), entry.getValue());
					}
				}
				return queryObject.getResultList();
			}
		});
	}

	public List findByNamedQuery(String queryName) throws DataAccessException {
		return findByNamedQuery(queryName, (Object[]) null);
	}

	public List findByNamedQuery(final String queryName, final Object... values) throws DataAccessException {
		return executeFind(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query queryObject = em.createNamedQuery(queryName);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						queryObject.setParameter(i + 1, values[i]);
					}
				}
				return queryObject.getResultList();
			}
		});
	}

	public List findByNamedQuery(final String queryName, final Map<String, Object> params)
			throws DataAccessException {

		return executeFind(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query queryObject = em.createNamedQuery(queryName);
				if (params != null) {
					for (Iterator it = params.entrySet().iterator(); it.hasNext();) {
						Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
						queryObject.setParameter(entry.getKey(), entry.getValue());
					}
				}
				return queryObject.getResultList();
			}
		});
	}


	/**
	 * Invocation handler that suppresses close calls on JPA EntityManagers.
	 * Also prepares returned Query and Criteria objects.
	 * @see javax.persistence.EntityManager#close
	 */
	private class CloseSuppressingInvocationHandler implements InvocationHandler {

		private final EntityManager target;

		public CloseSuppressingInvocationHandler(EntityManager target) {
			this.target = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on EntityManager interface (or provider-specific extension) coming in...

			if (method.getName().equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			}
			else if (method.getName().equals("hashCode")) {
				// Use hashCode of EntityManager proxy.
				return new Integer(hashCode());
			}
			else if (method.getName().equals("close")) {
				// Handle close method: suppress, not valid.
				return null;
			}

			// Invoke method on target EntityManager.
			try {
				return method.invoke(this.target, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
