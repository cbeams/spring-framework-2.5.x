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

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;

/**
 * SPI interface that allows to plug in vendor-specific behavior
 * into Spring's EntityManagerFactory creators. Serves as single
 * configuration point for all vendor-specific properties.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 2.0
 */
public interface JpaVendorAdapter {

	/**
	 * Return the vendor-specific persistence provider.
	 */
	PersistenceProvider getPersistenceProvider();

	/**
	 * Return a Map of vendor-specific JPA properties,
	 * based on settings in this JpaVendorAdapter instance.
	 */
	Map getJpaPropertyMap();

	/**
	 * Return the vendor-specific EntityManager interface that this
	 * factory's EntityManagers will implement.
	 */
	Class getEntityManagerInterface();

	/**
	 * Return the vendor-specific JpaDialect implementation for this
	 * EntityManagerFactory, or <code>null</code> if not known.
	 */
	JpaDialect getJpaDialect();

	/**
	 * Optional callback for post-processing the native EntityManagerFactory
	 * before active use.
	 */
	void postProcessEntityManagerFactory(EntityManagerFactory emf);

}
