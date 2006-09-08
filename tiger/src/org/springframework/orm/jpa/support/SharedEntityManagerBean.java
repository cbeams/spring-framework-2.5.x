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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.orm.jpa.EntityManagerPlus;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * FactoryBeans that exposes a shared JPA EntityManager reference for a
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
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setEntityManagerFactory
 * @see #setEntityManagerInterface
 * @see org.springframework.orm.jpa.LocalEntityManagerFactoryBean
 * @see org.springframework.orm.jpa.JpaTransactionManager
 */
public class SharedEntityManagerBean implements FactoryBean, InitializingBean {

	private EntityManagerFactory targetFactory;

	private final Map jpaPropertyMap = new HashMap();

	private Class entityManagerInterface;

	private EntityManager shared;


	/**
	 * Set the EntityManagerFactory that this adapter is supposed to
	 * expose a shared JPA EntityManager for.
	 */
	public void setEntityManagerFactory(EntityManagerFactory targetFactory) {
		this.targetFactory = targetFactory;
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
	 * Specify the EntityManager interface to expose.
	 * <p>Default is the the EntityManager interface as defined by the
	 * EntityManagerFactoryInfo, if available. Else, the standard
	 * <code>javax.persistence.EntityManager</code> interface will be used.
	 * @see org.springframework.orm.jpa.EntityManagerFactoryInfo#getEntityManagerInterface()
	 * @see javax.persistence.EntityManager
	 */
	public void setEntityManagerInterface(Class entityManagerInterface) {
		Assert.notNull(entityManagerInterface, "entityManagerInterface must not be null");
		Assert.isAssignable(EntityManager.class, entityManagerInterface);
		this.entityManagerInterface = entityManagerInterface;
	}


	public final void afterPropertiesSet() {
		if (this.targetFactory == null) {
			throw new IllegalArgumentException("entityManagerFactory is required");
		}
		Class[] ifcs = null;
		if (this.targetFactory instanceof EntityManagerFactoryInfo) {
			EntityManagerFactoryInfo emfInfo = (EntityManagerFactoryInfo) this.targetFactory;
			if (this.entityManagerInterface == null) {
				this.entityManagerInterface = emfInfo.getEntityManagerInterface();
			}
			JpaDialect jpaDialect = emfInfo.getJpaDialect();
			if (jpaDialect != null && jpaDialect.supportsEntityManagerPlusOperations()) {
				ifcs = new Class[] {this.entityManagerInterface, EntityManagerPlus.class};
			}
			else {
				ifcs = new Class[] {this.entityManagerInterface};
			}
		}
		else {
			if (this.entityManagerInterface == null) {
				this.entityManagerInterface = EntityManager.class;
			}
			ifcs = new Class[] {this.entityManagerInterface};
		}
		this.shared = SharedEntityManagerCreator.createSharedEntityManager(this.targetFactory, this.jpaPropertyMap, ifcs);
	}


	public EntityManager getObject() {
		return this.shared;
	}

	public Class getObjectType() {
		return this.entityManagerInterface;
	}

	public boolean isSingleton() {
		return true;
	}

}
