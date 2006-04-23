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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract base class for FactoryBeans that expose a 
 * JPA EntityManager reference for a
 * given EntityManagerFactory. Typically used for an EntityManagerFactory
 * created by LocalEntityManagerFactoryBean, as direct alternative to a
 * JndiObjectFactoryBean definition for a Java EE 5 server's EntityManager.
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
 * @author Rod Johnson
 * @since 2.0
 * @see #setEntityManagerFactory
 * @see #setEntityManagerInterface
 * @see org.springframework.orm.jpa.LocalEntityManagerFactoryBean
 * @see org.springframework.orm.jpa.JpaTransactionManager
 */
public abstract class AbstractEntityManagerProxyFactoryBean implements FactoryBean, InitializingBean {

	private EntityManagerFactory target;

	private EntityManager shared;


	/**
	 * Set the EntityManagerFactory that this adapter is supposed to
	 * expose a shared JPA EntityManager for. This should be the raw
	 * EntityManagerFactory, as accessed by JpaTransactionManager.
	 * @see org.springframework.orm.jpa.JpaTransactionManager
	 */
	public void setEntityManagerFactory(EntityManagerFactory target) {
		this.target = target;
	}
	
	public EntityManagerFactory getTarget() {
		return target;
	}


	public final void afterPropertiesSet() {
		if (this.target == null) {
			throw new IllegalArgumentException("entityManagerFactory is required");
		}
		this.shared = createEntityManagerProxy();
	}



	/**
	 * Subclasses must implement this
	 * @return a shared entity manager proxy
	 */
	protected abstract EntityManager createEntityManagerProxy();

	public Object getObject() {
		return this.shared;
	}

	public Class getObjectType() {
		return EntityManager.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
