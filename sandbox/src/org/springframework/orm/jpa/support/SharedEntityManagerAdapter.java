/*
 * Copyright 2002-2005 the original author or authors.
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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

/**
 * FactoryBean that exposes a shared JPA EntityManager reference for a
 * given EntityManagerFactory. Typically used for an EntityManagerFactory
 * created by LocalEntityManagerFactoryBean, as direct alternative to
 * a JndiObjectFactoryBean definition for a J2EE server's EntityManager.
 *
 * <p>The shared EntityManager will behave just like an EntityManager
 * fetched from an application server's JNDI environment, as defined
 * by the JPA specification. It will delegate all calls to the current
 * transactional EntityManager, if any; else, it will fall back to
 * a newly created EntityManager per operation.
 *
 * <p>Can be passed to DAOs that expect a shared EntityManager reference
 * rather than an EntityManagerFactory reference. Note that Spring's
 * JpaTransactionManager always needs an EntityManagerFactory reference,
 * to be able to create new transactional EntityManager instances.
 *
 * @author Juergen Hoeller
 * @since 1.3
 * @see org.springframework.orm.jpa.LocalEntityManagerFactoryBean
 * @see org.springframework.orm.jpa.JpaTransactionManager
 */
public class SharedEntityManagerAdapter implements FactoryBean {

	private EntityManager entityManager;


	/**
	 * Set the EntityManagerFactory that this adapter is supposed to
	 * expose a shared JPA EntityManager for. This should be the raw
	 * EntityManagerFactory, as accessed by JpaTransactionManager.
	 * @see org.springframework.orm.jpa.JpaTransactionManager
	 */
	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.entityManager = (EntityManager) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class[] {EntityManager.class},
				new SharedEntityManagerInvocationHandler(emf));
	}


	public Object getObject() {
		return this.entityManager;
	}

	public Class getObjectType() {
		return EntityManager.class;
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Invocation handler that delegates all calls to the current
	 * transactional EntityManager, if any; else, it will fall back
	 * to a newly created EntityManager per operation.
	 */
	private static class SharedEntityManagerInvocationHandler implements InvocationHandler {

		private final EntityManagerFactory targetFactory;

		public SharedEntityManagerInvocationHandler(EntityManagerFactory target) {
			this.targetFactory = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on EntityManager interface coming in...

			if (method.getName().equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			}
			else if (method.getName().equals("hashCode")) {
				// Use hashCode of SessionFactory proxy.
				return new Integer(hashCode());
			}
			else if (method.getName().equals("isOpen")) {
				// Handle isOpen method: always return true.
				return Boolean.TRUE;
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

			// Determine current EntityManager: either the transactional one
			// managed by the factory or a temporary one for the given invocation.
			EntityManager target = null;
			boolean isNewEm = false;
			try {
				target = EntityManagerFactoryUtils.getEntityManager(this.targetFactory);
			}
			catch (IllegalStateException ex) {
				target = this.targetFactory.createEntityManager();
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
