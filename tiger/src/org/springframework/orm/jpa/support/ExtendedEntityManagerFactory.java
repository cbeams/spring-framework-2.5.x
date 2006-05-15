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

package org.springframework.orm.jpa.support;

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
import org.springframework.orm.jpa.PortableEntityManagerFactoryPlus;
import org.springframework.orm.jpa.PortableEntityManagerPlus;
import org.springframework.orm.jpa.PortableEntityManagerPlusOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Factory for dynamic proxy InvocationHandlers for transactional application-managed entity
 * managers. Acquires a transaction before any method if there is a current
 * transaction and this entity manager is not already part of it. Enlists
 * application-managed EntityManagers in transactions using a special
 * TransactionSynchronization.
 *
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class ExtendedEntityManagerFactory {

	/**
	 * Create an EntityManager that can join transactions with the
	 * joinTransaction() method, but is not automatically managed by the
	 * container
	 * @param rawEntityManager raw EntityManager
	 * @param classLoader ClassLoader to use for dynamic proxy creation
	 * @param targetEntityManagerFactory EntityManagerFactory in use
	 * @return an application-managed EntityManager that can join transactions
	 * but does not participate in them automatically
	 */
	public static EntityManager createApplicationManagedEntityManager(EntityManager rawEntityManager, ClassLoader classLoader,
			EntityManagerFactory targetEntityManagerFactory, EntityManagerFactoryInfo emfi, boolean jta) {
		Class[] implementedInterfacesPlusProxyPortableInterface = ClassUtils.getAllInterfaces(rawEntityManager);
		implementedInterfacesPlusProxyPortableInterface = (Class[]) ObjectUtils.addObjectToArray(
				implementedInterfacesPlusProxyPortableInterface, PortableEntityManagerPlus.class);
		return (EntityManager) Proxy.newProxyInstance(classLoader, 
				implementedInterfacesPlusProxyPortableInterface,
				new ExtendedEntityManagerInvocationHandler(rawEntityManager, targetEntityManagerFactory, emfi, false, jta));
	}

	/**
	 * Create an EntityManager that automaticlaly joins transactions on each
	 * operation in a transaction
	 *
	 * @param classLoader ClassLoader to use for dynamic proxy creation
	 * @param emf EntityManagerFactory to use to create EntityManagers
	 * @return a container-managed EntityManager that will automatically participate
	 * in any managed transaction
	 */
	public static EntityManager createContainerManagedEntityManager(
			ClassLoader classLoader, EntityManagerFactory emf,
			EntityManagerFactoryInfo emfi,
			boolean jta) {
		// Unwrap using the EntityManagerFactoryInfo if necessary
		if (emf instanceof PortableEntityManagerFactoryPlus) {
			emf = ((PortableEntityManagerFactoryPlus) emf).getEntityManagerFactoryInfo().getNativeEntityManagerFactory();
		}
		EntityManager rawEntityManager = emf.createEntityManager();
		
		Class[] implementedInterfacesPlusProxyPortableInterface = ClassUtils.getAllInterfaces(rawEntityManager);
		implementedInterfacesPlusProxyPortableInterface = (Class[]) ObjectUtils.addObjectToArray(
				implementedInterfacesPlusProxyPortableInterface, PortableEntityManagerPlus.class);

		Assert.notNull(rawEntityManager, "EntityManager created by raw delegate cannot be null");
		return (EntityManager) Proxy.newProxyInstance(classLoader, 
				implementedInterfacesPlusProxyPortableInterface,
				new ExtendedEntityManagerInvocationHandler(rawEntityManager, emf, emfi, true, jta));
	}

	
	private static class ExtendedEntityManagerInvocationHandler implements InvocationHandler {
		
		protected final Log logger = LogFactory.getLog(getClass());

		private final EntityManager targetEntityManager;

		private final EntityManagerFactory entityManagerFactory;
		
		private PortableEntityManagerPlusOperations portableEntityManagerPlusOperations;

		private final boolean containerManaged;

		private final boolean jta;

		private ExtendedEntityManagerInvocationHandler(EntityManager target, EntityManagerFactory emf,
				EntityManagerFactoryInfo emfi,
				boolean containerManaged, boolean jta) {
			this.targetEntityManager = target;
			this.entityManagerFactory = emf;
			this.containerManaged = containerManaged;
			this.jta = jta;
			
			if (emfi == null) {				
				logger.warn("Cannot acquire portable entity manager operations: " +
						"No EntityManagerFactoryInfo passed in when calling ExtendedEntityManagerFactory -- " +
						"It is still possible to use JPA via standard EntityManager interface");
				this.portableEntityManagerPlusOperations = null;
			}
			else if (emfi.getVendorProperties() != null) {
				this.portableEntityManagerPlusOperations = emfi.getVendorProperties().
					getJpaDialect().getPortableEntityManagerPlusOperations(target);
			}
			else {
				logger.warn("Cannot acquire portable entity manager operations: " +
						"Null PortableEntityManagerPlusOperations returned by object of " + emfi.getClass() +
						"-- It is still possible to use JPA via standard EntityManager interface");
				this.portableEntityManagerPlusOperations = null;
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
//				if (containerManaged) {
//					// This call is not valid
//					throw new IllegalStateException(
//							"Invalid usage: cannot call joinTransaction in a JTA or Spring-managed transactional environment "
//									+ "on a container-managed EntityManager");
//				}
				
				if (!TransactionSynchronizationManager.isSynchronizationActive()) {
					throw new TransactionRequiredException("Cannot execute joinTransaction(): " +
							"Not in a Spring or JTA-managed transaction");
				}
				
				doJoinTransaction();
				return null;
			}
			else if (method.getName().equals("getTransaction")) {
				if (containerManaged) {
					throw new IllegalStateException("Cannot execute joinTransaction() on " +
						"a container-managed EntityManager");
				}
			}
			else if (method.getName().equals("close")) {
				if (containerManaged) {
					throw new IllegalStateException("Invalid usage: Cannot close a container-managed EntityManager");
				}
			}
			else if (method.getName().equals("isOpen")) {
				if (containerManaged) {
					return true;
				}
			}

			// Do automatic joining if required
			if (containerManaged) {
				doJoinTransaction();
			}

			Object targetToUse;
			if (PortableEntityManagerPlusOperations.class.equals(method.getDeclaringClass())) {
				if (portableEntityManagerPlusOperations == null) {
					throw new UnsupportedOperationException("Was unable to obtain PortableEntityManagerPlusOperations implementation for " +
							"EntityManagerFactory of class " + entityManagerFactory.getClass().getName() + 
							"; check log for warning message");
				}
				targetToUse = portableEntityManagerPlusOperations;
			}
			else {
				targetToUse = targetEntityManager;
			}
			
			// Invoke method on current EntityManager.
			try {
				return method.invoke(targetToUse, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}

		/**
		 * Join an existing transaction, if not already joined
		 */
		private void doJoinTransaction() {
			if (TransactionSynchronizationManager.isSynchronizationActive()
					&& !synchronizedWithTransaction(targetEntityManager)) {
				enlistInCurrentTransaction(targetEntityManager);
			}
		}

		/**
		 * Are we already synchronized with the transaction?
		 * 
		 * @return
		 */
		private boolean synchronizedWithTransaction(EntityManager em) {
			Object resourceBindingKey = computeResourceBindingKey(entityManagerFactory, em);
			if (logger.isDebugEnabled()) {
				logger.debug("Looking for key [" + resourceBindingKey + "]");
			}
			return TransactionSynchronizationManager.hasResource(resourceBindingKey);
		}

		/**
		 * Enlist this application-managed EntityManager in the current
		 * transaction
		 * 
		 * @param em
		 *            EntityManager to enlist
		 */
		private void enlistInCurrentTransaction(EntityManager em) {
			Object resourceBindingKey = computeResourceBindingKey(entityManagerFactory, em);
			if (jta) {
				// JTA transaction. We simply need to call
				// EntityManager.joinTransaction()
				// and remember that we did so. There is no need to call commit
				// or
				// rollback
				// subsequently
				logger.info("Joining JTA transaction");
				em.joinTransaction();
				// Just bind an object to indicate the JTA transaction manager
				// is
				// working behind the scenes
				// This allows tracking binding
				TransactionSynchronizationManager.bindResource(resourceBindingKey, true);
			}
			else {
				// Resource local transaction, need to acquire the
				// EntityTransaction,
				// start a transaction now and enlist a synchronization for
				// commit
				// or rollback later
				EntityTransaction et = em.getTransaction();
				et.begin();
				logger.info("Starting resource local transaction on application managed EntityManager " + em);

				EntityManagerHolder emh = new EntityManagerHolder(em);

				ContainerManagedExtendedEntityManagerSynchronization applicationManagedEntityManagerSynchronization = new ContainerManagedExtendedEntityManagerSynchronization(
						emh, entityManagerFactory, resourceBindingKey);
				TransactionSynchronizationManager.bindResource(resourceBindingKey,
						applicationManagedEntityManagerSynchronization);
				TransactionSynchronizationManager
						.registerSynchronization(applicationManagedEntityManagerSynchronization);
			}

			Assert.isTrue(synchronizedWithTransaction(em), "Now have transaction bound");
		}

		/*
		 * Compute the key that this application-managed entity manager should
		 * be bound to the current TransactionSynchronizationManager with
		 */
		private Object computeResourceBindingKey(EntityManagerFactory emf, EntityManager em) {
			StringBuffer keyBuf = new StringBuffer(emf.getClass().getName());
			keyBuf.append("_").append(System.identityHashCode(emf)).append("_").append(System.identityHashCode(em));
			return keyBuf.toString();
		}

		/**
		 * TransactionSynchronization enlisting a container-managed extended
		 * EntityManager with a current transaction.
		 */
		private static class ContainerManagedExtendedEntityManagerSynchronization extends
				TransactionSynchronizationAdapter {

			private final EntityManagerHolder entityManagerHolder;

			private final Object resourceBindingKey;

			private boolean holderActive = true;

			public ContainerManagedExtendedEntityManagerSynchronization(EntityManagerHolder emHolder,
					EntityManagerFactory emf, Object resourceBindingKey) {
				this.entityManagerHolder = emHolder;
				this.resourceBindingKey = resourceBindingKey;
			}

			public int getOrder() {
				return EntityManagerFactoryUtils.ENTITY_MANAGER_SYNCHRONIZATION_ORDER + 1;
			}

			public void suspend() {
				if (this.holderActive) {
					TransactionSynchronizationManager.unbindResource(resourceBindingKey);
				}
			}

			public void resume() {
				if (this.holderActive) {
					TransactionSynchronizationManager.bindResource(resourceBindingKey, this.entityManagerHolder);
				}
			}

			public void beforeCompletion() {
				TransactionSynchronizationManager.unbindResource(resourceBindingKey);
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
		} 	// class ContainerManagedExtendedEntityManagerSynchronization

	}

}