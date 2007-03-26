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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.CollectionUtils;

/**
 * Factory for a shared JPA EntityManager for a given EntityManagerFactory.
 *
 * <p>The shared EntityManager will behave just like an EntityManager fetched
 * from an application server's JNDI environment, as defined by the JPA
 * specification. It will delegate all calls to the current transactional
 * EntityManager, if any; else, it will fall back to a newly created
 * EntityManager per operation.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.orm.jpa.LocalEntityManagerFactoryBean
 * @see org.springframework.orm.jpa.JpaTransactionManager
 */
public abstract class SharedEntityManagerCreator {

	/**
	 * Create a shared transactional EntityManager proxy,
	 * given this EntityManagerFactory
	 * @param emf the EntityManagerFactory to delegate to.
	 * If this implements the EntityManagerFactoryInfo interface, appropriate handling
	 * of the native EntityManagerFactory and available EntityManagerPlusOperations
	 * will automatically apply.
	 * @return a shareable transaction EntityManager proxy
	 */
	public static EntityManager createSharedEntityManager(EntityManagerFactory emf) {
		return createSharedEntityManager(emf, null);
	}

	/**
	 * Create a shared transactional EntityManager proxy,
	 * given this EntityManagerFactory
	 * @param emf the EntityManagerFactory to delegate to.
	 * If this implements the EntityManagerFactoryInfo interface, appropriate handling
	 * of the native EntityManagerFactory and available EntityManagerPlusOperations
	 * will automatically apply.
	 * @param properties the properties to be passed into the <code>createEntityManager</code>
	 * call (may be <code>null</code>)
	 * @return a shareable transaction EntityManager proxy
	 */
	public static EntityManager createSharedEntityManager(EntityManagerFactory emf, Map properties) {
		Class[] entityManagerInterfaces = null;
		if (emf instanceof EntityManagerFactoryInfo) {
			EntityManagerFactoryInfo emfInfo = (EntityManagerFactoryInfo) emf;
			Class entityManagerInterface = emfInfo.getEntityManagerInterface();
			JpaDialect jpaDialect = emfInfo.getJpaDialect();
			if (jpaDialect != null && jpaDialect.supportsEntityManagerPlusOperations()) {
				entityManagerInterfaces = new Class[] {entityManagerInterface, EntityManagerPlus.class};
			}
			else {
				entityManagerInterfaces = new Class[] {entityManagerInterface};
			}
		}
		else {
			entityManagerInterfaces = new Class[] {EntityManager.class};
		}
		return createSharedEntityManager(emf, properties, entityManagerInterfaces);
	}

	/**
	 * Create a shared transactional EntityManager proxy,
	 * given this EntityManagerFactory
	 * @param emf EntityManagerFactory to obtain EntityManagers from as needed
	 * @param properties the properties to be passed into the <code>createEntityManager</code>
	 * call (may be <code>null</code>)
	 * @param entityManagerInterfaces interfaces to be implemented by the
	 * EntityManager. Allows the addition or specification of proprietary interfaces.
	 * @return a shareable transaction EntityManager proxy
	 */
	public static EntityManager createSharedEntityManager(
			EntityManagerFactory emf, Map properties, Class... entityManagerInterfaces) {

		return (EntityManager) Proxy.newProxyInstance(
				SharedEntityManagerCreator.class.getClassLoader(),
				entityManagerInterfaces,
				new SharedEntityManagerInvocationHandler(emf, properties));
	}


	/**
	 * Invocation handler that delegates all calls to the current
	 * transactional EntityManager, if any; else, it will fall back
	 * to a newly created EntityManager per operation.
	 */
	private static class SharedEntityManagerInvocationHandler implements InvocationHandler {

		private final Log logger = LogFactory.getLog(getClass());

		private final EntityManagerFactory targetFactory;

		private final Map properties;

		public SharedEntityManagerInvocationHandler(EntityManagerFactory target, Map properties) {
			this.targetFactory = target;
			this.properties = properties;
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
			else if (method.getName().equals("toString")) {
				// Deliver toString without touching a target EntityManager.
				return "Shared EntityManager proxy for target factory [" + this.targetFactory + "]";
			}
			else if (method.getName().equals("isOpen")) {
				// Handle isOpen method: always return true.
				return true;
			}
			else if (method.getName().equals("close")) {
				// Handle close method: suppress, not valid.
				return null;
			}
			else if (method.getName().equals("getTransaction")) {
				throw new IllegalStateException(
						"Not allowed to create transaction on shared EntityManager - " +
						"use Spring transactions or EJB CMT instead");
			}
			else if (method.getName().equals("joinTransaction")) {
				throw new IllegalStateException(
						"Not allowed to join transaction on shared EntityManager - " +
						"use Spring transactions or EJB CMT instead");
			}

			// Determine current EntityManager: either the transactional one
			// managed by the factory or a temporary one for the given invocation.
			EntityManager target =
					EntityManagerFactoryUtils.doGetTransactionalEntityManager(this.targetFactory, this.properties);
			boolean isNewEm = false;
			if (target == null) {
				logger.debug("Creating new EntityManager for shared EntityManager invocation");
				target = (!CollectionUtils.isEmpty(this.properties) ?
						this.targetFactory.createEntityManager(this.properties) :
						this.targetFactory.createEntityManager());
				isNewEm = true;
			}

			// Invoke method on current EntityManager.
			try {
				return method.invoke(target, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
			finally {
				if (isNewEm) {
					target.close();
				}
			}
		}
	}

}
