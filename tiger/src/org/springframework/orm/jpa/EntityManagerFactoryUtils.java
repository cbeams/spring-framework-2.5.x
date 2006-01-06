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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Helper class featuring methods for JPA EntityManager handling,
 * allowing for reuse of EntityManager instances within transactions.
 *
 * <p>Used by JpaTemplate, JpaInterceptor, and JpaTransactionManager.
 * Can also be used directly in application code, e.g. in combination
 * with JpaInterceptor.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see JpaTemplate
 * @see JpaInterceptor
 * @see JpaTransactionManager
 */
public abstract class EntityManagerFactoryUtils {

	/**
	 * Order value for TransactionSynchronization objects that clean up JPA
	 * EntityManagers. Return DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 100
	 * to execute EntityManager cleanup before JDBC Connection cleanup, if any.
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#CONNECTION_SYNCHRONIZATION_ORDER
	 */
	public static final int ENTITY_MANAGER_SYNCHRONIZATION_ORDER =
			DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 100;

	private static final Log logger = LogFactory.getLog(EntityManagerFactoryUtils.class);


	/**
	 * Get a JPA EntityManager from the given factory. Is aware of a
	 * corresponding EntityManager bound to the current thread,
	 * for example when using JpaTransactionManager.
	 * <p>Same as <code>doGetEntityManager</code>, but asking the
	 * EntityManagerFactory for its current EntityManager as fallback.
	 * This is the version typically used by applications.
	 * @param emf EntityManagerFactory to create the EntityManager with
	 * @return the EntityManager
	 * @throws javax.persistence.PersistenceException if the EntityManager couldn't be created
	 * @throws IllegalStateException if no thread-bound EntityManager found
	 * @see #doGetEntityManager(javax.persistence.EntityManagerFactory)
	 * @see JpaTransactionManager
	 * @see javax.persistence.EntityManagerFactory#getEntityManager()
	 */
	public static EntityManager getEntityManager(EntityManagerFactory emf)
	    throws PersistenceException, IllegalStateException {

		try {
			return doGetEntityManager((EntityManagerFactory) emf);
		}
		catch (IllegalStateException ex) {
			// Not within a Spring-managed transaction with Spring synchronization:
			// delegate to persistence provider's own synchronization mechanism.
			// Will throw IllegalStateException if not supported by persistence provider.
			return emf.getEntityManager();
		}
	}

	/**
	 * Get a JPA EntityManager from the given factory. Is aware of a
	 * corresponding EntityManager bound to the current thread,
	 * for example when using JpaTransactionManager.
	 * <p>Same as <code>getEntityManager</code>, but <i>not</i> asking the
	 * EntityManagerFactory for its current EntityManager as fallback.
	 * This is the version used by EntityManagerFactory proxies.
	 * @param emf EntityManagerFactory to create the EntityManager with
	 * @return the EntityManager
	 * @throws javax.persistence.PersistenceException if the EntityManager couldn't be created
	 * @throws IllegalStateException if no thread-bound EntityManager found
	 * @see #getEntityManager(javax.persistence.EntityManagerFactory)
	 * @see JpaTransactionManager
	 */
	public static EntityManager doGetEntityManager(EntityManagerFactory emf)
	    throws PersistenceException, IllegalStateException {

		Assert.notNull(emf, "No EntityManagerFactory specified");

		EntityManagerHolder emHolder =
				(EntityManagerHolder) TransactionSynchronizationManager.getResource(emf);
		if (emHolder != null) {
			return emHolder.getEntityManager();
		}

		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			throw new IllegalStateException("No JPA EntityManager bound to thread, " +
					"and configuration does not allow creation of non-transactional one here");
		}

		// Create a new EntityManager with PersistenceContextType.EXTENDED,
		// as we are likely to execute outside of transactions as well
		// (for example, to enable lazy loading through OpenEntityManagerInView).
		logger.debug("Opening JPA EntityManager");
		EntityManager em = emf.createEntityManager();

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			logger.debug("Registering transaction synchronization for JPA EntityManager");
			// Use same EntityManager for further JPA actions within the transaction.
			// Thread object will get removed by synchronization at transaction completion.
			emHolder = new EntityManagerHolder(em);
			emHolder.setSynchronizedWithTransaction(true);
			TransactionSynchronizationManager.registerSynchronization(
					new EntityManagerSynchronization(emHolder, emf));
			TransactionSynchronizationManager.bindResource(emf, emHolder);
		}

		return em;
	}

	/**
	 * Return whether the given JPA EntityManager is transactional, that is,
	 * bound to the current thread by Spring's transaction facilities.
	 * @param em the JPA EntityManager to check
	 * @param emf JPA EntityManagerFactory that the EntityManager
	 * was created with (can be <code>null</code>)
	 * @return whether the EntityManager is transactional
	 */
	public static boolean isEntityManagerTransactional(EntityManager em, EntityManagerFactory emf) {
		if (emf == null) {
			return false;
		}
		EntityManagerHolder emHolder =
				(EntityManagerHolder) TransactionSynchronizationManager.getResource(emf);
		return (emHolder != null && em == emHolder.getEntityManager());
	}

	/**
	 * Convert the given PersistenceException to an appropriate exception from the
	 * org.springframework.dao hierarchy.
	 * <p>The most important cases like object not found or optimistic verification
	 * failure are covered here. For more fine-granular conversion, JpaAccessor and
	 * JpaTransactionManager support sophisticated translation of exceptions via a
	 * JpaDialect.
	 * @param ex PersistenceException that occured
	 * @return the corresponding DataAccessException instance
	 * @see JpaAccessor#convertJpaAccessException
	 * @see JpaTransactionManager#convertJpaAccessException
	 * @see JpaDialect#translateException
	 */
	public static DataAccessException convertJpaAccessException(PersistenceException ex) {
		// TODO: find matching exceptions
		// fallback: assuming internal exception
		return new JpaSystemException(ex);
	}


	/**
	 * Callback for resource cleanup at the end of a non-JPA transaction
	 * (e.g. when participating in a JtaTransactionManager transaction).
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	private static class EntityManagerSynchronization extends TransactionSynchronizationAdapter {

		private final EntityManagerHolder entityManagerHolder;

		private final EntityManagerFactory entityManagerFactory;

		private boolean holderActive = true;

		public EntityManagerSynchronization(EntityManagerHolder emHolder, EntityManagerFactory emf) {
			this.entityManagerHolder = emHolder;
			this.entityManagerFactory = emf;
		}

		public int getOrder() {
			return ENTITY_MANAGER_SYNCHRONIZATION_ORDER;
		}

		public void suspend() {
			if (this.holderActive) {
				TransactionSynchronizationManager.unbindResource(this.entityManagerFactory);
			}
		}

		public void resume() {
			if (this.holderActive) {
				TransactionSynchronizationManager.bindResource(this.entityManagerFactory, this.entityManagerHolder);
			}
		}

		public void beforeCompletion() {
			TransactionSynchronizationManager.unbindResource(this.entityManagerFactory);
			this.holderActive = false;
			this.entityManagerHolder.getEntityManager().close();
		}
	}

}
