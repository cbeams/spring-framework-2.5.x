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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.PortableEntityManagerFactoryPlus;
import org.springframework.orm.jpa.PortableEntityManagerPlus;
import org.springframework.orm.jpa.PortableEntityManagerPlusOperations;

/**
 * FactoryBean that exposes a shared JPA EntityManager reference for a
 * given EntityManagerFactory. Typically used for an EntityManagerFactory
 * created by LocalEntityManagerFactoryBean, as direct alternative to a
 * JndiObjectFactoryBean definition for a Java EE 5 server's EntityManager.
 * Also allows convenient creation of shared EntityManager proxies.
 *
 * <p>The shared EntityManager will behave just like an EntityManager fetched
 * from an application server's JNDI environment, as defined by the JPA
 * specification. It will delegate all calls to the current transactional
 * EntityManager, if any; else, it will fall back to a newly created
 * EntityManager per operation.
 *
 * <p>Can be passed to DAOs that expect a shared EntityManager reference
 * rather than an EntityManagerFactory reference. Note that Spring's
 * JpaTransactionManager always needs an EntityManagerFactory reference,
 * to be able to create new transactional EntityManager instances.
 *
 * <p>This adapter is also able to expose a vendor-extended EntityManager
 * interface: Simply specify the extended interface as "entityManagerInterface".
 * By default, only the standard <code>javax.persistence.EntityManager</code>
 * interface will be exposed.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 2.0
 * @see #setEntityManagerFactory
 * @see #setEntityManagerInterface
 * @see org.springframework.orm.jpa.LocalEntityManagerFactoryBean
 * @see org.springframework.orm.jpa.JpaTransactionManager
 */
public class SharedEntityManagerFactory extends AbstractEntityManagerProxyFactoryBean {
	
	/**
	 * Return a shared transactional EntityManager proxy,
	 * given this EntityManagerFactory
	 * @param classLoader class loader to use to create the dynamic proxy
	 * @param entityManagerInterfaces interfaces to be implemented by the
	 * EntityManager. Allows the addition or specification of proprietary interfaces.
	 * @param emf EntityManagerFactory to obtain EntityManagers from as needed
	 * @return a shareable transaction EntityManager proxy
	 */
	public static EntityManager createEntityManagerProxy(ClassLoader classLoader, 
			EntityManagerFactory emf, Class...entityManagerInterfaces) {
		if (entityManagerInterfaces.length == 0) {
			entityManagerInterfaces = new Class[] { PortableEntityManagerPlus.class };
		}
		return (EntityManager) Proxy.newProxyInstance(
				classLoader,
				entityManagerInterfaces,
				new SharedEntityManagerInvocationHandler(emf));
	}
	

	@Override
	protected EntityManager createEntityManagerProxy() {
		return createEntityManagerProxy(
				getClass().getClassLoader(),
				getEntityManagerFactory(),
				getEntityManagerInterface());
	}


	/**
	 * Invocation handler that delegates all calls to the current
	 * transactional EntityManager, if any; else, it will fall back
	 * to a newly created EntityManager per operation.
	 */
	private static class SharedEntityManagerInvocationHandler implements InvocationHandler {

		private final Log logger = LogFactory.getLog(getClass());

		private final EntityManagerFactory targetFactory;

		public SharedEntityManagerInvocationHandler(EntityManagerFactory target) {
			this.targetFactory = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on EntityManager interface coming in...

			if (method.getName().equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0]);
			}
			else if (method.getName().equals("hashCode")) {
				// Use hashCode of SessionFactory proxy.
				return hashCode();
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
			EntityManager target = EntityManagerFactoryUtils.doGetTransactionalEntityManager(this.targetFactory);
			boolean isNewEm = false;
			if (target == null) {
				logger.debug("Creating new EntityManager for shared EntityManager invocation");
				target = this.targetFactory.createEntityManager();
				isNewEm = true;
			}
			
			PortableEntityManagerPlusOperations portableEntityManagerPlusOperations = null;
			if (PortableEntityManagerPlusOperations.class.equals(method.getDeclaringClass())) {
				portableEntityManagerPlusOperations = 
					getPortableEntityManagerPlusOperationTargetIfPossible(targetFactory, target);
			}

			// Invoke method on current EntityManager.
			try {
				return method.invoke(portableEntityManagerPlusOperations != null ?
								portableEntityManagerPlusOperations :
								target, 
						args);
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
		
		private PortableEntityManagerPlusOperations getPortableEntityManagerPlusOperationTargetIfPossible(
				EntityManagerFactory emf, EntityManager em) {
			if (!(emf instanceof PortableEntityManagerFactoryPlus)) {
				throw new UnsupportedOperationException();
			}
			EntityManagerFactoryInfo emfi = ((PortableEntityManagerFactoryPlus) emf).getEntityManagerFactoryInfo();
			if (emfi.getVendorProperties() == null) {
				throw new UnsupportedOperationException("No vendor properties available");
			}
			
			PortableEntityManagerPlusOperations pempo = emfi.getVendorProperties().
				getJpaDialect().
				getPortableEntityManagerPlusOperations(em);
			return pempo;
		}

	}
}
