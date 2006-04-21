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

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;


/**
 * FactoryBean that creates a local JPA EntityManagerFactory instance.
 * Behaves like a EntityManagerFactory instance when used as bean
 * reference, e.g. for JpaTemplate's "entityManagerFactory" property.
 * Note that switching to a JndiObjectFactoryBean or a bean-style
 * EntityManagerFactory instance is just a matter of configuration!
 *
 * <p>The typical usage will be to register this as singleton factory
 * (for a certain underlying data source) in an application context,
 * and give bean references to application services that need it.
 *
 * <p>Configuration settings are usually read in from a <code>persistence.xml</code>
 * config file, residing in the class path - according to the JPA spec's
 * bootstrap mechanism. See the Java Persistence API specification for details.
 *
 * <p>This EntityManager handling strategy is most appropriate for
 * applications that solely use JPA for data access. If you want to set up
 * your persistence provider for global transactions, you will need to
 * deploy it into a full Java EE 5 application server and access the
 * deployed EntityManagerFactory via JNDI (-> JndiObjectFactoryBean).
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see JpaTemplate#setEntityManagerFactory
 * @see JpaTransactionManager#setEntityManagerFactory
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public class LocalEntityManagerFactoryBean extends AbstractEntityManagerFactoryBean {

	private String entityManagerName;

	private Properties jpaProperties;


	/**
	 * Set the name of the EntityManager configuration for the factory.
	 * <p>Default is none, indicating the default EntityManager configuration.
	 * The persistence provider will throw an exception if ambiguous
	 * EntityManager configurations are found.
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String)
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String, java.util.Map)
	 */
	public void setEntityManagerName(String entityManagerName) {
		this.entityManagerName = entityManagerName;
	}

	/**
	 * Set JPA properties, to be passed into
	 * <code>Persistence.createEntityManagerFactory</code> (if any).
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String, java.util.Map)
	 */
	public void setJpaProperties(Properties jpaProperties) {
		this.jpaProperties = jpaProperties;
	}


	/**
	 * Initialize the EntityManagerFactory for the given configuration.
	 * @throws javax.persistence.PersistenceException in case of JPA initialization errors
	 */
	protected EntityManagerFactory createEntityManagerFactory() throws PersistenceException {
		if (this.persistenceProviderClass != null) {
			// Create EntityManagerFactory directly through PersistenceProvider.
			PersistenceProvider pp =
					instantiatePersistenceProvider();
			EntityManagerFactory emf = pp.createEntityManagerFactory(this.entityManagerName, this.jpaProperties);
			if (emf == null) {
				throw new IllegalStateException(
						"PersistenceProvider [" + this.persistenceProviderClass.getName() +
						"] did not return an EntityManagerFactory for name '" + this.entityManagerName + "'");
			}
			return emf;
		}
		else {
			// Let JPA perform its PersistenceProvider autodetection.
			return Persistence.createEntityManagerFactory(this.entityManagerName, this.jpaProperties);
		}
	}

}
