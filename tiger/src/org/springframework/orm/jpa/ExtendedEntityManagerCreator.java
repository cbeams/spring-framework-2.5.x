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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TransactionRequiredException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ClassUtils;

/**
 * Factory for dynamic proxy InvocationHandlers for transactional application-managed
 * entity managers. Acquires a transaction before any method if there is a current
 * transaction and this entity manager is not already part of it. Enlists
 * application-managed EntityManagers in transactions using a special
 * TransactionSynchronization.
 *
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class ExtendedEntityManagerCreator {

	/**
	 * Create an EntityManager that can join transactions with the
	 * <code>joinTransaction()</code> method, but is not automatically
	 * managed by the container.
	 * @param rawEntityManager raw EntityManager
	 * @return an application-managed EntityManager that can join transactions
	 * but does not participate in them automatically
	 */
	public static EntityManager createApplicationManagedEntityManager(EntityManager rawEntityManager) {
		return (EntityManager) Proxy.newProxyInstance(
				ExtendedEntityManagerCreator.class.getClassLoader(),
				ClassUtils.getAllInterfaces(rawEntityManager),
				new ExtendedEntityManagerInvocationHandler(rawEntityManager, false));
	}

	/**
	 * Create an EntityManager that automaticlaly joins transactions on each
	 * operation in a transaction
	 * @param emf EntityManagerFactory to use to create EntityManagers
	 * @return a container-managed EntityManager that will automatically participate
	 * in any managed transaction
	 */
	public static EntityManager createContainerManagedEntityManager(EntityManagerFactory emf) {
		// Unwrap using the EntityManagerFactoryInfo if necessary.
		EntityManagerFactory emfToUse = emf;
		if (emf instanceof EntityManagerFactoryInfo) {
			emfToUse = ((EntityManagerFactoryInfo) emf).getNativeEntityManagerFactory();
		}
		EntityManager rawEntityManager = emfToUse.createEntityManager();
		return (EntityManager) Proxy.newProxyInstance(
				ExtendedEntityManagerCreator.class.getClassLoader(),
				ClassUtils.getAllInterfaces(rawEntityManager),
				new ExtendedEntityManagerInvocationHandler(rawEntityManager, true));
	}

	
	private static class ExtendedEntityManagerInvocationHandler implements InvocationHandler {
		
		private static final Log logger = LogFactory.getLog(ExtendedEntityManagerInvocationHandler.class);

		private final EntityManager targetEntityManager;

		private final boolean containerManaged;

		private boolean jta;

		private ExtendedEntityManagerInvocationHandler(EntityManager target, boolean containerManaged) {
			this.targetEntityManager = target;
			this.containerManaged = containerManaged;
			this.jta = isJtaEntityManager();
		}

		private boolean isJtaEntityManager() {
			try {
				this.targetEntityManager.getTransaction();
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
				// Use hashCode of SessionFactory proxy.
				return proxy.hashCode();
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
			if (this.containerManaged) {
				doJoinTransaction(false);
			}

			// Invoke method on current EntityManager.
			try {
				return method.invoke(this.targetEntityManager, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}

		/**
		 * Join an existing transaction, if not already joined.
		 */
		private void doJoinTransaction(boolean enforce) {
			if (this.jta) {
				// Let's try whether we're in a JTA transaction.
				try {
					this.targetEntityManager.joinTransaction();
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
					if (!TransactionSynchronizationManager.hasResource(this.targetEntityManager)) {
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
			EntityTransaction et = this.targetEntityManager.getTransaction();
			et.begin();
			if (logger.isDebugEnabled()) {
				logger.debug("Starting resource local transaction on application-managed " +
						"EntityManager [" + this.targetEntityManager + "]");
			}
			EntityManagerHolder emh = new EntityManagerHolder(this.targetEntityManager);
			ContainerManagedExtendedEntityManagerSynchronization applicationManagedEntityManagerSynchronization =
					new ContainerManagedExtendedEntityManagerSynchronization(emh);
			TransactionSynchronizationManager.bindResource(this.targetEntityManager,
					applicationManagedEntityManagerSynchronization);
			TransactionSynchronizationManager.registerSynchronization(applicationManagedEntityManagerSynchronization);
		}
	}


	/**
	 * TransactionSynchronization enlisting a container-managed extended
	 * EntityManager with a current transaction.
	 */
	private static class ContainerManagedExtendedEntityManagerSynchronization extends
			TransactionSynchronizationAdapter {

		private final EntityManagerHolder entityManagerHolder;

		private boolean holderActive = true;

		public ContainerManagedExtendedEntityManagerSynchronization(EntityManagerHolder emHolder) {
			this.entityManagerHolder = emHolder;
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

		public void afterCompletion(int status) {
			this.entityManagerHolder.setSynchronizedWithTransaction(false);
			if (status != TransactionSynchronization.STATUS_COMMITTED) {
				this.entityManagerHolder.getEntityManager().getTransaction().rollback();
			}
			else {
				this.entityManagerHolder.getEntityManager().getTransaction().commit();
			}
			// Don't close the EntityManager...that's up to the user
		}
	}

}
