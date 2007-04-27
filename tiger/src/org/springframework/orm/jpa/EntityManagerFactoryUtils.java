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

package org.springframework.orm.jpa;

import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.persistence.TransactionRequiredException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Helper class featuring methods for JPA EntityManager handling,
 * allowing for reuse of EntityManager instances within transactions.
 * Also provides support for exception translation.
 *
 * <p>Mainly intended for internal use within the framework.
 *
 * @author Juergen Hoeller
 * @since 2.0
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
	 * Find an EntityManagerFactory with the given name in the given
	 * Spring application context (represented as ListableBeanFactory).
	 * <p>The specified unit name will be matched against the configured
	 * peristence unit, provided that a discovered EntityManagerFactory
	 * implements the {@link EntityManagerFactoryInfo} interface. If not,
	 * the persistence unit name will be matched against the Spring bean name,
	 * assuming that the EntityManagerFactory bean names follow that convention.
	 * @param beanFactory the ListableBeanFactory to search
	 * @param unitName the name of the persistence unit (never empty)
	 * @return the EntityManagerFactory
	 * @throws NoSuchBeanDefinitionException if there is no such EntityManagerFactory in the context
	 * @see EntityManagerFactoryInfo#getPersistenceUnitName()
	 */
	public static EntityManagerFactory findEntityManagerFactory(
			ListableBeanFactory beanFactory, String unitName) throws NoSuchBeanDefinitionException {

		Assert.notNull(beanFactory, "ListableBeanFactory must not be null");
		Assert.hasLength(unitName, "Unit name must not be empty");

		// See whether we can find an EntityManagerFactory with matching persistence unit name.
		String[] candidateNames =
				BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, EntityManagerFactory.class);
		for (String candidateName : candidateNames) {
			EntityManagerFactory emf = (EntityManagerFactory) beanFactory.getBean(candidateName);
			if (emf instanceof EntityManagerFactoryInfo) {
				if (unitName.equals(((EntityManagerFactoryInfo) emf).getPersistenceUnitName())) {
					return emf;
				}
			}
		}
		// No matching persistence unit found - simply take the EntityManagerFactory
		// with the persistence unit name as bean name (by convention).
		return (EntityManagerFactory) beanFactory.getBean(unitName, EntityManagerFactory.class);
	}

	/**
	 * Obtain a JPA EntityManager from the given factory. Is aware of a
	 * corresponding EntityManager bound to the current thread,
	 * for example when using JpaTransactionManager.
	 * <p>Note: Will return <code>null</code> if no thread-bound EntityManager found!
	 * @param emf EntityManagerFactory to create the EntityManager with
	 * @return the EntityManager, or <code>null</code> if none found
	 * @throws DataAccessResourceFailureException if the EntityManager couldn't be obtained
	 * @see JpaTransactionManager
	 */
	public static EntityManager getTransactionalEntityManager(EntityManagerFactory emf)
			throws DataAccessResourceFailureException {

		return getTransactionalEntityManager(emf, null);
	}

	/**
	 * Obtain a JPA EntityManager from the given factory. Is aware of a
	 * corresponding EntityManager bound to the current thread,
	 * for example when using JpaTransactionManager.
	 * <p>Note: Will return <code>null</code> if no thread-bound EntityManager found!
	 * @param emf EntityManagerFactory to create the EntityManager with
	 * @param properties the properties to be passed into the <code>createEntityManager</code>
	 * call (may be <code>null</code>)
	 * @return the EntityManager, or <code>null</code> if none found
	 * @throws DataAccessResourceFailureException if the EntityManager couldn't be obtained
	 * @see JpaTransactionManager
	 */
	public static EntityManager getTransactionalEntityManager(EntityManagerFactory emf, Map properties)
			throws DataAccessResourceFailureException {
		try {
			return doGetTransactionalEntityManager(emf, properties);
		}
		catch (PersistenceException ex) {
			throw new DataAccessResourceFailureException("Could not obtain JPA EntityManager", ex);
		}
	}

	/**
	 * Obtain a JPA EntityManager from the given factory. Is aware of a
	 * corresponding EntityManager bound to the current thread,
	 * for example when using JpaTransactionManager.
	 * <p>Same as <code>getEntityManager</code>, but throwing the original PersistenceException.
	 * @param emf EntityManagerFactory to create the EntityManager with
	 * @param properties the properties to be passed into the <code>createEntityManager</code>
	 * call (may be <code>null</code>)
	 * @return the EntityManager, or <code>null</code> if none found
	 * @throws javax.persistence.PersistenceException if the EntityManager couldn't be created
	 * @see #getTransactionalEntityManager(javax.persistence.EntityManagerFactory)
	 * @see JpaTransactionManager
	 */
	public static EntityManager doGetTransactionalEntityManager(
			EntityManagerFactory emf, Map properties) throws PersistenceException {

		Assert.notNull(emf, "No EntityManagerFactory specified");

		EntityManagerHolder emHolder =
				(EntityManagerHolder) TransactionSynchronizationManager.getResource(emf);
		if (emHolder != null) {
			if (!emHolder.isSynchronizedWithTransaction() &&
					TransactionSynchronizationManager.isSynchronizationActive()) {
				// Try to explicitly synchronize the EntityManager itself
				// with an ongoing JTA transaction, if any.
				try {
					emHolder.getEntityManager().joinTransaction();
				}
				catch (TransactionRequiredException ex) {
					logger.debug("Could not join JTA transaction because none was active", ex);
				}
				emHolder.setSynchronizedWithTransaction(true);
				TransactionSynchronizationManager.registerSynchronization(
						new EntityManagerSynchronization(emHolder, emf, false));
			}
			return emHolder.getEntityManager();
		}

		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			// Indicate that we can't obtain a transactional EntityManager.
			return null;
		}

		// Create a new EntityManager for use within the current transaction.
		logger.debug("Opening JPA EntityManager");
		EntityManager em =
				(!CollectionUtils.isEmpty(properties) ? emf.createEntityManager(properties) : emf.createEntityManager());

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			logger.debug("Registering transaction synchronization for JPA EntityManager");
			// Use same EntityManager for further JPA actions within the transaction.
			// Thread object will get removed by synchronization at transaction completion.
			emHolder = new EntityManagerHolder(em);
			emHolder.setSynchronizedWithTransaction(true);
			TransactionSynchronizationManager.registerSynchronization(
					new EntityManagerSynchronization(emHolder, emf, true));
			TransactionSynchronizationManager.bindResource(emf, emHolder);
		}

		return em;
	}

	/**
	 * Convert the given runtime exception to an appropriate exception from the
	 * <code>org.springframework.dao</code> hierarchy.
	 * Return null if no translation is appropriate: any other exception may
	 * have resulted from user code, and should not be translated.
	 * <p>The most important cases like object not found or optimistic locking
	 * failure are covered here. For more fine-granular conversion, JpaAccessor and
	 * JpaTransactionManager support sophisticated translation of exceptions via a
	 * JpaDialect.
	 * @param ex runtime exception that occured
	 * @return the corresponding DataAccessException instance,
	 * or <code>null</code> if the exception should not be translated
	 */
	public static DataAccessException convertJpaAccessExceptionIfPossible(RuntimeException ex) {
		// Following the JPA specification, a persistence provider can also
		// throw these two exceptions, besides PersistenceException.
		if (ex instanceof IllegalStateException) {
			return new InvalidDataAccessApiUsageException(ex.getMessage(), ex);
		}
		if (ex instanceof IllegalArgumentException) {
			return new InvalidDataAccessApiUsageException(ex.getMessage(), ex);
		}

		// Check for well-known PersistenceException subclasses.
		if (ex instanceof EntityNotFoundException) {
			return new JpaObjectRetrievalFailureException((EntityNotFoundException) ex);
		}
		if (ex instanceof NoResultException) {
			return new EmptyResultDataAccessException(ex.getMessage(), 1);
		}
		if (ex instanceof NonUniqueResultException) {
			return new IncorrectResultSizeDataAccessException(ex.getMessage(), 1);
		}
		if (ex instanceof OptimisticLockException) {
			return new JpaOptimisticLockingFailureException((OptimisticLockException) ex);
		}
		if (ex instanceof EntityExistsException) {
			return new DataIntegrityViolationException(ex.getMessage(), ex);
		}
		if (ex instanceof TransactionRequiredException) {
			return new InvalidDataAccessApiUsageException(ex.getMessage(), ex);
		}

		// If we have another kind of PersistenceException, throw it.
		if (ex instanceof PersistenceException) {
			return new JpaSystemException((PersistenceException) ex);
		}
		
		// If we get here, we have an exception that resulted from user code,
		// rather than the persistence provider, so we return null to indicate
		// that translation should not occur.
		return null;				
	}


	/**
	 * Callback for resource cleanup at the end of a non-JPA transaction
	 * (e.g. when participating in a JtaTransactionManager transaction).
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	private static class EntityManagerSynchronization extends TransactionSynchronizationAdapter {

		private final EntityManagerHolder entityManagerHolder;

		private final EntityManagerFactory entityManagerFactory;

		private final boolean newEntityManager;

		private boolean holderActive = true;

		public EntityManagerSynchronization(
				EntityManagerHolder emHolder, EntityManagerFactory emf, boolean newEntityManager) {
			this.entityManagerHolder = emHolder;
			this.entityManagerFactory = emf;
			this.newEntityManager = newEntityManager;
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
			if (this.newEntityManager) {
				TransactionSynchronizationManager.unbindResource(this.entityManagerFactory);
				this.holderActive = false;
				this.entityManagerHolder.getEntityManager().close();
			}
		}

		public void afterCompletion(int status) {
			if (!this.newEntityManager && status != STATUS_COMMITTED) {
				// Clear all pending inserts/updates/deletes in the EntityManager.
				// Necessary for pre-bound EntityManagers, to avoid inconsistent state.
				this.entityManagerHolder.getEntityManager().clear();
			}
			this.entityManagerHolder.setSynchronizedWithTransaction(false);
		}
	}

}
