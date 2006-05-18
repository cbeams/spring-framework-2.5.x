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

import org.springframework.orm.jpa.ExtendedEntityManagerCreator;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.test.annotation.AbstractAnnotationAwareTransactionalTests;

/**
 * Convenient support class for JPA-related tests.
 *
 * <p>Exposes an EntityManagerFactory and a shared EntityManager.
 * Requires EntityManagerFactory to be injected, plus DataSource and
 * JpaTransactionManager from superclass.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class AbstractJpaTests extends AbstractAnnotationAwareTransactionalTests {
	
	protected EntityManagerFactory entityManagerFactory;
	
	/**
	 * Subclasses can use this in test cases.
	 * It will participate in any current transaction.
	 */
	protected EntityManager sharedEntityManager;


	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
		this.sharedEntityManager = SharedEntityManagerCreator.createSharedEntityManager(
				this.entityManagerFactory, EntityManager.class);
	}

	/**
	 * Create an EntityManager that will always automatically enlist itself in current
	 * transactions, in contrast to an EntityManager returned by
	 * <code>EntityManagerFactory.createEntityManager()</code>
	 * (which requires an explicit <code>joinTransaction()</code> call).
	 */
	protected EntityManager createContainerManagedEntityManager() {
		return ExtendedEntityManagerCreator.createContainerManagedEntityManager(this.entityManagerFactory);
	}
	
}
