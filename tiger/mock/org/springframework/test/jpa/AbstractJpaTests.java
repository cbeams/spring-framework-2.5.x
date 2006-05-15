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

package org.springframework.test.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.orm.jpa.PortableEntityManagerFactoryPlus;
import org.springframework.orm.jpa.support.ExtendedEntityManagerFactory;
import org.springframework.orm.jpa.support.SharedEntityManagerFactory;
import org.springframework.test.AbstractAnnotationAwareTransactionalTests;

/**
 * Convenient support class for JPA-related tests.
 * Exposes an EntityManagerFactory and a sharedEntityManagerProxy. 
 * Requires EntityManagerFactory to be
 * injected, and DataSource and JpaTransactionManager from superclass.
 * 
 * @author Rod Johnson
 * @since 2.0
 * TODO support for in-memory databases? DDL generation?
 */
public abstract class AbstractJpaTests extends AbstractAnnotationAwareTransactionalTests {
	
	protected EntityManagerFactory entityManagerFactory;
	
	/**
	 * Subclasses can use this in test cases. It will run
	 * in the current transaction.
	 */
	protected EntityManager sharedEntityManagerProxy;
	
	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
		this.sharedEntityManagerProxy = SharedEntityManagerFactory.createEntityManagerProxy(
				getClass().getClassLoader(), this.entityManagerFactory);
	}

	/**
	 * @return an EntityManager automatically enlisted in the current transaction,
	 * in contrast to an EntityManager returned by EntityManagerFactory.createEntityManager()
	 */
	protected EntityManager createContainerManagedEntityManager() {
		return ExtendedEntityManagerFactory.createContainerManagedEntityManager(
				getClass().getClassLoader(), 
				this.entityManagerFactory, 
				((PortableEntityManagerFactoryPlus) this.entityManagerFactory).getEntityManagerFactoryInfo(), 
				false);
	}
	
}
