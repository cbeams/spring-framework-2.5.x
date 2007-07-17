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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TransactionRequiredException;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * Factory for dynamic EntityManager proxies that follow the JPA spec's
 * semantics for "extended" EntityManagers.
 *
 * <p>Supports explicit joining of a transaction through the
 * <code>joinTransaction()</code> method ("application-managed extended
 * EntityManager") as well as automatic joining on each operation
 * ("container-managed extended EntityManager").
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class ExtendedEntityManagerCreator {

	/**
	 * Create an EntityManager that can join transactions with the
	 * <code>joinTransaction()</code> method, but is not automatically
	 * managed by the container.
	 * @param rawEntityManager raw EntityManager
	 * @param plusOperations an implementation of the EntityManagerPlusOperations
	 * interface, if those operations should be exposed (may be <code>null</code>)
	 * @return an application-managed EntityManager that can join transactions
	 * but does not participate in them automatically
	 */
	public static EntityManager createApplicationManagedEntityManager(
			EntityManager rawEntityManager, EntityManagerPlusOperations plusOperations) {

		return createProxy(rawEntityManager, plusOperations, null, null, false);
	}

	/**
	 * Create an EntityManager that can join transactions with the
	 * <code>joinTransaction()</code> method, but is not automatically
	 * managed by the container.
	 * @param rawEntityManager raw EntityManager
	 * @param plusOperations an implementation of the EntityManagerPlusOperations
	 * interface, if those operations should be exposed (may be <code>null</code>)
	 * @param exceptionTranslator the exception translator to use for translating
	 * JPA commit/rollback exceptions during transaction synchronization
	 * (may be <code>null</code>)
	 * @return an application-managed EntityManager that can join transactions
	 * but does not participate in them automatically
	 */
	public static EntityManager createApplicationManagedEntityManager(
			EntityManager rawEntityManager, EntityManagerPlusOperations plusOperations,
			PersistenceExceptionTranslator exceptionTranslator) {

		return createProxy(rawEntityManager, plusOperations, exceptionTranslator, null, false);
	}

	/**
	 * Create an EntityManager that can join transactions with the
	 * <code>joinTransaction()</code> method, but is not automatically
	 * managed by the container.
	 * @param rawEntityManager raw EntityManager
	 * @param emfInfo the EntityManagerFactoryInfo to obtain the
	 * EntityManagerPlusOperations and PersistenceUnitInfo from
	 * @return an application-managed EntityManager that can join transactions
	 * but does not participate in them automatically
	 */
	public static EntityManager createApplicationManagedEntityManager(
			EntityManager rawEntityManager, EntityManagerFactoryInfo emfInfo) {

		return createProxy(rawEntityManager, emfInfo, false);
	}


	/**
	 * Create an EntityManager that automatically joins transactions on each
	 * operation in a transaction.
	 * @param rawEntityManager raw EntityManager
	 * @param plusOperations an implementation of the EntityManagerPlusOperations
	 * interface, if those operations should be exposed (may be <code>null</code>)
	 * @return a container-managed EntityManager that will automatically participate
	 * in any managed transaction
	 */
	public static EntityManager createContainerManagedEntityManager(
			EntityManager rawEntityManager, EntityManagerPlusOperations plusOperations) {

		return createProxy(rawEntityManager, plusOperations, null, null, true);
	}

	/**
	 * Create an EntityManager that automatically joins transactions on each
	 * operation in a transaction.
	 * @param rawEntityManager raw EntityManager
	 * @param plusOperations an implementation of the EntityManagerPlusOperations
	 * interface, if those operations should be exposed (may be <code>null</code>)
	 * @param exceptionTranslator the exception translator to use for translating
	 * JPA commit/rollback exceptions during transaction synchronization
	 * (may be <code>null</code>)
	 * @return a container-managed EntityManager that will automatically participate
	 * in any managed transaction
	 */
	public static EntityManager createContainerManagedEntityManager(
			EntityManager rawEntityManager, EntityManagerPlusOperations plusOperations,
			PersistenceExceptionTranslator exceptionTranslator) {

		return createProxy(rawEntityManager, plusOperations, exceptionTranslator, null, true);
	}

	/**
	 * Create an EntityManager that automatically joins transactions on each
	 * operation in a transaction.
	 * @param rawEntityManager raw EntityManager
	 * @param emfInfo the EntityManagerFactoryInfo to obtain the
	 * EntityManagerPlusOperations and PersistenceUnitInfo from
	 * @return a container-managed EntityManager that will automatically participate
	 * in any managed transaction
	 */
	public static EntityManager createContainerManagedEntityManager(
			EntityManager rawEntityManager, EntityManagerFactoryInfo emfInfo) {

		return createProxy(rawEntityManager, emfInfo, true);
	}


	/**
	 * Create an EntityManager that automatically joins transactions on each
	 * operation in a transaction.
	 * @param emf the EntityManagerFactory to create the EntityManager with.
	 * If this implements the EntityManagerFactoryInfo interface, appropriate handling
	 * of the native EntityManagerFactory and available EntityManagerPlusOperations
	 * will automatically apply.
	 * @return a container-managed EntityManager that will automatically participate
	 * in any managed transaction
	 * @see javax.persistence.EntityManagerFactory#createEntityManager()
	 */
	public static EntityManager createContainerManagedEntityManager(EntityManagerFactory emf) {
		return createContainerManagedEntityManager(emf, null);
	}

	/**
	 * Create an EntityManager that automatically joins transactions on each
	 * operation in a transaction.
	 * @param emf the EntityManagerFactory to create the EntityManager with.
	 * If this implements the EntityManagerFactoryInfo interface, appropriate handling
	 * of the native EntityManagerFactory and available EntityManagerPlusOperations
	 * will automatically apply.
	 * @param properties the properties to be passed into the <code>createEntityManager</code>
	 * call (may be <code>null</code>)
	 * @return a container-managed EntityManager that will automatically participate
	 * in any managed transaction
	 * @see javax.persistence.EntityManagerFactory#createEntityManager(java.util.Map)
	 */
	public static EntityManager createContainerManagedEntityManager(EntityManagerFactory emf, Map properties) {
		Assert.notNull(emf, "EntityManagerFactory must not be null");
		if (emf instanceof EntityManagerFactoryInfo) {
			EntityManagerFactoryInfo emfInfo = (EntityManagerFactoryInfo) emf;
			EntityManagerFactory nativeEmf = emfInfo.getNativeEntityManagerFactory();
			EntityManager rawEntityManager = (!CollectionUtils.isEmpty(properties) ?
					nativeEmf.createEntityManager(properties) : nativeEmf.createEntityManager());
			return createProxy(rawEntityManager, emfInfo, true);
		}
		else {
			EntityManager rawEntityManager = (!CollectionUtils.isEmpty(properties) ?
					emf.createEntityManager(properties) : emf.createEntityManager());
			return createProxy(rawEntityManager, null, null, null, true);
		}
	}


	/**
	 * Actually create the EntityManager proxy.
	 * @param rawEntityManager raw EntityManager
	 * @param emfInfo the EntityManagerFactoryInfo to obtain the
	 * EntityManagerPlusOperations and PersistenceUnitInfo from
	 * @param containerManaged whether to follow container-managed EntityManager
	 * or application-managed EntityManager semantics
	 * @return the EntityManager proxy
	 */
	private static EntityManager createProxy(
			EntityManager rawEntityManager, EntityManagerFactoryInfo emfInfo, boolean containerManaged) {

		Assert.notNull(emfInfo, "EntityManagerFactoryInfo must not be null");
		JpaDialect jpaDialect = emfInfo.getJpaDialect();
		EntityManagerPlusOperations plusOperations = null;
		if (jpaDialect != null && jpaDialect.supportsEntityManagerPlusOperations()) {
			plusOperations = jpaDialect.getEntityManagerPlusOperations(rawEntityManager);
		}
		PersistenceUnitInfo pui = emfInfo.getPersistenceUnitInfo();
		Boolean jta = (pui != null ? pui.getTransactionType() == PersistenceUnitTransactionType.JTA : null);
		return createProxy(rawEntityManager, plusOperations, jpaDialect, jta, containerManaged);
	}

	/**
	 * Actually create the EntityManager proxy.
	 * @param rawEntityManager raw EntityManager
	 * @param plusOperations an implementation of the EntityManagerPlusOperations
	 * interface, if those operations should be exposed (may be <code>null</code>)
	 * @param exceptionTranslator the PersistenceException translator to use
	 * @param jta whether to create a JTA-aware EntityManager
	 * (or <code>null</code> if not known in advance)
	 * @param containerManaged whether to follow container-managed EntityManager
	 * or application-managed EntityManager semantics
	 * @return the EntityManager proxy
	 */
	private static EntityManager createProxy(
			EntityManager rawEntityManager, EntityManagerPlusOperations plusOperations,
			PersistenceExceptionTranslator exceptionTranslator, Boolean jta, boolean containerManaged) {

		Assert.notNull(rawEntityManager, "EntityManager must not be null");
		Class[] ifcs = ClassUtils.getAllInterfaces(rawEntityManager);
		if (plusOperations != null) {
			ifcs = (Class[]) ObjectUtils.addObjectToArray(ifcs, EntityManagerPlusOperations.class);
		}
		return (EntityManager) Proxy.newProxyInstance(
				ExtendedEntityManagerCreator.class.getClassLoader(), ifcs,
				new ExtendedEntityManagerInvocationHandler(
						rawEntityManager, plusOperations, exceptionTranslator, jta, containerManaged));
	}


	/**
	 * InvocationHandler for extended EntityManagers as defined in the JPA spec.
	 */
	private static class ExtendedEntityManagerInvocationHandler implements InvocationHandler, Serializable {

		private static final Log logger = LogFactory.getLog(ExtendedEntityManagerInvocationHandler.class);

		private final EntityManager target;

		private final EntityManagerPlusOperations plusOperations;

		private final PersistenceExceptionTranslator exceptionTranslator;

		private final boolean containerManaged;

		private boolean jta;

		private ExtendedEntityManagerInvocationHandler(
				EntityManager target, EntityManagerPlusOperations plusOperations,
				PersistenceExceptionTranslator exceptionTranslator, Boolean jta, boolean containerManaged) {

			this.target = target;
			this.plusOperations = plusOperations;
			this.exceptionTranslator = exceptionTranslator;
			this.jta = (jta != null ? jta.booleanValue() : isJtaEntityManager());
			this.containerManaged = containerManaged;
		}

		private boolean isJtaEntityManager() {
			try {
				this.target.getTransaction();
				return false;
			}
			catch (IllegalStateException ex) {
				logger.debug("Cannot access EntityTransaction handle - assuming we're in a JTA environment");
				return true;
			}
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on EntityManager interface coming in...

			if (method.getName().equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0]);
			}
			else if (method.getName().equals("hashCode")) {
				// Use hashCode of EntityManager proxy.
				return hashCode();
			}
			else if (method.getName().equals("joinTransaction")) {
				doJoinTransaction(true);
				return null;
			}
			else if (method.getName().equals("getTransaction")) {
				if (this.containerManaged) {
					throw new IllegalStateException("Cannot execute getTransaction() on " +
						"a container-managed EntityManager");
				}
			}
			else if (method.getName().equals("close")) {
				if (this.containerManaged) {
					throw new IllegalStateException("Invalid usage: Cannot close a container-managed EntityManager");
				}
			}
			else if (method.getName().equals("isOpen")) {
				if (this.containerManaged) {
					return true;
				}
			}

			// Do automatic joining if required.
			if (this.containerManaged && method.getDeclaringClass().isInterface()) {
				doJoinTransaction(false);
			}

			// Invoke method on current EntityManager.
			try {
				if (method.getDeclaringClass().equals(EntityManagerPlusOperations.class)) {
					return method.invoke(this.plusOperations, args);
				}
				else {
					return method.invoke(this.target, args);
				}
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}

		/**
		 * Join an existing transaction, if not already joined.
		 * @param enforce whether to enforce the transaction
		 * (i.e. whether failure to join is considered fatal)
		 */
		private void doJoinTransaction(boolean enforce) {
			if (this.jta) {
				// Let's try whether we're in a JTA transaction.
				try {
					this.target.joinTransaction();
					logger.debug("Joined JTA transaction");
				}
				catch (TransactionRequiredException ex) {
					if (!enforce) {
						logger.debug("No JTA transaction to join: " + ex);
					}
					else {
						throw ex;
					}
				}
			}
			else {
				if (TransactionSynchronizationManager.isSynchronizationActive()) {
					if (!TransactionSynchronizationManager.hasResource(this.target) &&
							!this.target.getTransaction().isActive()) {
						enlistInCurrentTransaction();
					}
					logger.debug("Joined local transaction");
				}
				else {
					if (!enforce) {
						logger.debug("No local transaction to join");
					}
					else {
						throw new TransactionRequiredException("No local transaction to join");
					}
				}
			}
		}

		/**
		 * Enlist this application-managed EntityManager in the current transaction.
		 */
		private void enlistInCurrentTransaction() {
			// Resource local transaction, need to acquire the EntityTransaction,
			// start a transaction now and enlist a synchronization for
			// commit or rollback later.
			EntityTransaction et = this.target.getTransaction();
			et.begin();
			if (logger.isDebugEnabled()) {
				logger.debug("Starting resource local transaction on application-managed " +
						"EntityManager [" + this.target + "]");
			}
			EntityManagerHolder emh = new EntityManagerHolder(this.target);
			ExtendedEntityManagerSynchronization extendedEntityManagerSynchronization =
					new ExtendedEntityManagerSynchronization(emh, this.exceptionTranslator);
			TransactionSynchronizationManager.bindResource(this.target,
					extendedEntityManagerSynchronization);
			TransactionSynchronizationManager.registerSynchronization(extendedEntityManagerSynchronization);
		}
	}


	/**
	 * TransactionSynchronization enlisting an extended EntityManager
	 * with a current Spring transaction.
	 */
	private static class ExtendedEntityManagerSynchronization extends TransactionSynchronizationAdapter {

		private final EntityManagerHolder entityManagerHolder;

		private final PersistenceExceptionTranslator exceptionTranslator;

		private boolean holderActive = true;

		public ExtendedEntityManagerSynchronization(
				EntityManagerHolder emHolder, PersistenceExceptionTranslator exceptionTranslator) {

			this.entityManagerHolder = emHolder;
			this.exceptionTranslator = exceptionTranslator;
		}

		public int getOrder() {
			return EntityManagerFactoryUtils.ENTITY_MANAGER_SYNCHRONIZATION_ORDER + 1;
		}

		public void suspend() {
			if (this.holderActive) {
				TransactionSynchronizationManager.unbindResource(this.entityManagerHolder.getEntityManager());
			}
		}

		public void resume() {
			if (this.holderActive) {
				TransactionSynchronizationManager.bindResource(
						this.entityManagerHolder.getEntityManager(), this.entityManagerHolder);
			}
		}

		public void beforeCompletion() {
			TransactionSynchronizationManager.unbindResource(this.entityManagerHolder.getEntityManager());
			this.holderActive = false;
		}

		public void afterCommit() {
			// Trigger commit here to let exceptions propagate to the caller.
			try {
				this.entityManagerHolder.getEntityManager().getTransaction().commit();
			}
			catch (RuntimeException ex) {
				throw convertCompletionException(ex);
			}
		}

		public void afterCompletion(int status) {
			this.entityManagerHolder.setSynchronizedWithTransaction(false);
			if (status != STATUS_COMMITTED) {
				// Haven't had an afterCommit call: trigger a rollback.
				try {
					this.entityManagerHolder.getEntityManager().getTransaction().rollback();
				}
				catch (RuntimeException ex) {
					throw convertCompletionException(ex);
				}
			}
			// Don't close the EntityManager... That's up to the user.
		}

		private RuntimeException convertCompletionException(RuntimeException ex) {
			DataAccessException daex = (this.exceptionTranslator != null) ?
					this.exceptionTranslator.translateExceptionIfPossible(ex) :
					EntityManagerFactoryUtils.convertJpaAccessExceptionIfPossible(ex);
			return (daex != null ? daex : ex);
		}
	}

}
