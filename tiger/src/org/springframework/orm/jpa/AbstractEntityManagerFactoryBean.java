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

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract support for FactoryBeans that create a local JPA 
 * EntityManagerFactory instance.
 * Behaves like a EntityManagerFactory instance when used as bean
 * reference, e.g. for JpaTemplate's "entityManagerFactory" property.
 * Note that switching to a JndiObjectFactoryBean or a bean-style
 * EntityManagerFactory instance is just a matter of configuration!
 *
 * @author Rod Johnson
 * @since 2.0
 * @see JpaTemplate#setEntityManagerFactory
 * @see JpaTransactionManager#setEntityManagerFactory
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public abstract class AbstractEntityManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private EntityManagerFactory entityManagerFactory;

	protected Class persistenceProviderClass;

	/**
	 * Subclasses must implement this method to create the EntityManagerFactory that
	 * will be returned by the getObject() method
	 * @return EntityManagerFactory instance returned by this FactoryBean
	 * @throws PersistenceException if the EntityManager cannot be created
	 */
	protected abstract EntityManagerFactory createEntityManagerFactory() throws PersistenceException;

	protected EntityManagerFactory getEntityManagerFactory() {
		return this.entityManagerFactory;
	}

	/**
	 * Return the singleton EntityManagerFactory.
	 */
	public final Object getObject() {
		return this.entityManagerFactory;
	}

	public Class getObjectType() {
		return (this.entityManagerFactory != null) ?
		    this.entityManagerFactory.getClass() : EntityManagerFactory.class;
	}

	public final boolean isSingleton() {
		return true;
	}

	/**
	 * Close the EntityManagerFactory on bean factory shutdown.
	 */
	public void destroy() {
		logger.info("Closing JPA EntityManagerFactory");
		this.entityManagerFactory.close();
	}
	
	public void afterPropertiesSet() throws Exception {
		this.entityManagerFactory = createEntityManagerFactory();
	}

	/**
	/**
	 * Set the PersistenceProvider implementation class to use for creating
	 * the EntityManagerFactory. If not specified (which is the default),
	 * the <code>Persistence</code> class will be used to create the
	 * EntityManagerFactory, relying on JPA's autodetection mechanism.
	 * @see javax.persistence.spi.PersistenceProvider
	 * @see javax.persistence.Persistence
	 */
	public void setPersistenceProviderClass(Class persistenceProviderClass) {
		if (persistenceProviderClass != null &&
				!PersistenceProvider.class.isAssignableFrom(persistenceProviderClass)) {
			throw new IllegalArgumentException(
					"serviceFactoryClass must implement [javax.persistence.spi.PersistenceProvider]");
		}
		this.persistenceProviderClass = persistenceProviderClass;
	}

}
