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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Base class for JpaTemplate and JpaInterceptor, defining common
 * properties like EntityManagerFactory and flushing behavior.
 *
 * <p>Eager flushing is just available for specific JPA implementations.
 * You need to a corresponding JpaDialect to make eager flushing work.
 *
 * <p>Not intended to be used directly. See JpaTemplate and JpaInterceptor.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see JpaTemplate
 * @see JpaInterceptor
 * @see #setFlushEager
 */
public abstract class JpaAccessor implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private EntityManagerFactory entityManagerFactory;

	private final Map jpaPropertyMap = new HashMap();

	private EntityManager entityManager;

	private JpaDialect jpaDialect = new DefaultJpaDialect();

	private boolean flushEager = false;


	/**
	 * Set the JPA EntityManagerFactory that should be used to create
	 * EntityManagers.
	 */
	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.entityManagerFactory = emf;
	}

	/**
	 * Return the JPA EntityManagerFactory that should be used to create
	 * EntityManagers.
	 */
	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	/**
	 * Specify JPA properties, to be passed into
	 * <code>EntityManagerFactory.createEntityManager(Map)</code> (if any).
	 * <p>Can be populated with a String "value" (parsed via PropertiesEditor)
	 * or a "props" element in XML bean definitions.
	 * @see javax.persistence.EntityManagerFactory#createEntityManager(java.util.Map)
	 */
	public void setJpaProperties(Properties jpaProperties) {
		CollectionUtils.mergePropertiesIntoMap(jpaProperties, this.jpaPropertyMap);
	}

	/**
	 * Specify JPA properties as a Map, to be passed into
	 * <code>EntityManagerFactory.createEntityManager(Map)</code> (if any).
	 * <p>Can be populated with a "map" or "props" element in XML bean definitions.
	 * @see javax.persistence.EntityManagerFactory#createEntityManager(java.util.Map)
	 */
	public void setJpaPropertyMap(Map jpaProperties) {
		if (jpaProperties != null) {
			this.jpaPropertyMap.putAll(jpaProperties);
		}
	}

	/**
	 * Allow Map access to the JPA properties to be passed to the persistence
	 * provider, with the option to add or override specific entries.
	 * <p>Useful for specifying entries directly, for example via "jpaPropertyMap[myKey]".
	 */
	public Map getJpaPropertyMap() {
		return jpaPropertyMap;
	}

	/**
	 * Set the JPA EntityManager to use.
	 */
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Return the JPA EntityManager to use.
	 */
	public EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * Set the JPA dialect to use for this accessor.
	 * <p>The dialect object can be used to retrieve the underlying JDBC
	 * connection, for example.
	 */
	public void setJpaDialect(JpaDialect jpaDialect) {
		this.jpaDialect = (jpaDialect != null ? jpaDialect : new DefaultJpaDialect());
	}

	/**
	 * Return the JPA dialect to use for this accessor.
	 * <p>Creates a default one for the specified EntityManagerFactory if none set.
	 */
	public JpaDialect getJpaDialect() {
		return this.jpaDialect;
	}

	/**
	 * Set if this accessor should flush changes to the database eagerly.
	 * <p>Eager flushing leads to immediate synchronization with the database,
	 * even if in a transaction. This causes inconsistencies to show up and throw
	 * a respective exception immediately, and JDBC access code that participates
	 * in the same transaction will see the changes as the database is already
	 * aware of them then. But the drawbacks are:
	 * <ul>
	 * <li>additional communication roundtrips with the database, instead of a
	 * single batch at transaction commit;
	 * <li>the fact that an actual database rollback is needed if the JPA
	 * transaction rolls back (due to already submitted SQL statements).
	 * </ul>
	 */
	public void setFlushEager(boolean flushEager) {
		this.flushEager = flushEager;
	}

	/**
	 * Return if this accessor should flush changes to the database eagerly.
	 */
	public boolean isFlushEager() {
		return flushEager;
	}

	/**
	 * Eagerly initialize the JPA dialect, creating a default one
	 * for the specified EntityManagerFactory if none set.
	 */
	public void afterPropertiesSet() {
		if (getEntityManagerFactory() == null && getEntityManager() == null) {
			throw new IllegalArgumentException("entityManagerFactory or entityManager is required");
		}
		if (getEntityManagerFactory() instanceof EntityManagerFactoryInfo) {
			JpaDialect jpaDialect = ((EntityManagerFactoryInfo) getEntityManagerFactory()).getJpaDialect();
			if (jpaDialect != null) {
				setJpaDialect(jpaDialect);
			}
		}
	}


	/**
	 * Obtain a new EntityManager from this accessor's EntityManagerFactory.
	 * @return a new EntityManager
	 * @throws IllegalStateException if this accessor is not configured with an EntityManagerFactory
	 */
	protected EntityManager createEntityManager() throws IllegalStateException {
		EntityManagerFactory emf = getEntityManagerFactory();
		Assert.state(emf != null, "No EntityManagerFactory specified");
		Map properties = getJpaPropertyMap();
		return (!CollectionUtils.isEmpty(properties) ? emf.createEntityManager(properties) : emf.createEntityManager());
	}

	/**
	 * Obtain the transactional EntityManager for this accessor's EntityManagerFactory, if any.
	 * @return the transactional EntityManager, or <code>null</code> if none
	 * @throws IllegalStateException if this accessor is not configured with an EntityManagerFactory
	 */
	protected EntityManager getTransactionalEntityManager() throws IllegalStateException{
		EntityManagerFactory emf = getEntityManagerFactory();
		Assert.state(emf != null, "No EntityManagerFactory specified");
		return EntityManagerFactoryUtils.getTransactionalEntityManager(emf, getJpaPropertyMap());
	}

	/**
	 * Flush the given JPA entity manager if necessary.
	 * @param em the current JPA PersistenceManage
	 * @param existingTransaction if executing within an existing transaction
	 * @throws javax.persistence.PersistenceException in case of JPA flushing errors
	 */
	protected void flushIfNecessary(EntityManager em, boolean existingTransaction) throws PersistenceException {
		if (isFlushEager()) {
			logger.debug("Eagerly flushing JPA entity manager");
			em.flush();
		}
	}

	/**
	 * Convert the given runtime exception to an appropriate exception from the
	 * <code>org.springframework.dao</code> hierarchy if necessary, or
	 * return the exception itself if it is not persistence related
	 * <p>Default implementation delegates to the JpaDialect.
	 * May be overridden in subclasses.
	 * @param ex runtime exception that occured, which may or may not
	 * be JPA-related
	 * @return the corresponding DataAccessException instance if
	 * wrapping should occur, otherwise the raw exception
	 * @see org.springframework.dao.support.DataAccessUtils#translateIfNecessary
	 */
	public RuntimeException translateIfNecessary(RuntimeException ex) {
		return DataAccessUtils.translateIfNecessary(ex, getJpaDialect());
	}

}
