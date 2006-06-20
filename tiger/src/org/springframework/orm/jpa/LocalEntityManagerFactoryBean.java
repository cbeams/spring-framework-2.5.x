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
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;

import org.springframework.beans.BeanUtils;

/**
 * FactoryBean that creates a local JPA EntityManagerFactory instance according
 * to JPA's standalone bootstrap contract.
 *
 * <p>Behaves like a EntityManagerFactory instance when used as bean reference,
 * e.g. for JpaTemplate's "entityManagerFactory" property. Note that switching
 * to LocalContainerEntityManagerFactoryBean or JndiObjectFactoryBean is just
 * a matter of configuration! The typical usage will be to register this as
 * singleton factory in an application context, and give bean references to
 * application services that need it.
 *
 * <p>Configuration settings are usually read in from a
 * <code>META-INF/persistence.xml</code> config file, residing in the class path,
 * according to the JPA standalone bootstrap contract. Additionally, most JPA
 * providers will require a special VM agent (specified on JVM startup) that
 * allows them to instrument application classes. See the Java Persistence
 * API specification for details.
 *
 * <p>This EntityManager handling strategy is most appropriate for
 * applications that solely use JPA for data access. If you want to set up
 * your persistence provider for global transactions that span multiple resources,
 * you will need to either deploy it into a full Java EE 5 application server and
 * access the deployed EntityManagerFactory via JNDI (-> JndiObjectFactoryBean),
 * or use Spring's LocalContainerEntityManagerFactoryBean with appropriate
 * configuration for local setup according to JPA's container contract.
 *
 * <p>Note: This FactoryBean has limited configuration power in terms of what
 * it can pass to the JPA provider. If you need more flexible configuration,
 * for example passing a Spring-managed JDBC DataSource to the JPA provider,
 * consider using 's LocalContainerEntityManagerFactoryBean instead.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 2.0
 * @see #setJpaProperties
 * @see #setJpaVendorAdapter
 * @see JpaTemplate#setEntityManagerFactory
 * @see JpaTransactionManager#setEntityManagerFactory
 * @see LocalContainerEntityManagerFactoryBean
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see org.springframework.orm.jpa.support.SharedEntityManagerBean
 * @see javax.persistence.Persistence#createEntityManagerFactory
 * @see javax.persistence.spi.PersistenceProvider#createEntityManagerFactory
 */
public class LocalEntityManagerFactoryBean extends AbstractEntityManagerFactoryBean {

	/**
	 * Initialize the EntityManagerFactory for the given configuration.
	 * @throws javax.persistence.PersistenceException in case of JPA initialization errors
	 */
	protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {
		if (getPersistenceProviderClass() != null) {
			// Create EntityManagerFactory directly through PersistenceProvider.
			PersistenceProvider pp = (PersistenceProvider) BeanUtils.instantiateClass(getPersistenceProviderClass());
			EntityManagerFactory emf = pp.createEntityManagerFactory(getPersistenceUnitName(), getJpaPropertyMap());
			if (emf == null) {
				throw new IllegalStateException(
						"PersistenceProvider [" + pp + "] did not return an EntityManagerFactory for name '" +
						getPersistenceUnitName() + "'");
			}
			return emf;
		}
		else {
			// Let JPA perform its PersistenceProvider autodetection.
			return Persistence.createEntityManagerFactory(getPersistenceUnitName(), getJpaPropertyMap());
		}
	}

}
