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

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Costin Leau
 * 
 */
public class EntityManagerFactoryUtilsTests extends TestCase {

	/*
	 * Test method for
	 * 'org.springframework.orm.jpa.EntityManagerFactoryUtils.doGetEntityManager(EntityManagerFactory)'
	 */
	public void testDoGetEntityManager() {
		// test null assertion
		try {
			EntityManagerFactoryUtils.doGetEntityManager(null);
			fail("expected exception");
		}
		catch (IllegalArgumentException ex) {
			// it's okay
		}
		MockControl mockControl = MockControl.createControl(EntityManagerFactory.class);
		EntityManagerFactory factory = (EntityManagerFactory) mockControl.getMock();

		mockControl.replay();
		// no tx active
		assertNull(EntityManagerFactoryUtils.doGetEntityManager(factory));
		mockControl.verify();
	}

	public void testDoGetEntityManagerWithTx() throws Exception {
		try {
			MockControl mockControl = MockControl.createControl(EntityManagerFactory.class);
			EntityManagerFactory factory = (EntityManagerFactory) mockControl.getMock();

			MockControl managerControl = MockControl.createControl(EntityManager.class);
			EntityManager manager = (EntityManager) managerControl.getMock();

			TransactionSynchronizationManager.initSynchronization();
			mockControl.expectAndReturn(factory.createEntityManager(), manager);

			mockControl.replay();
			// no tx active
			assertSame(manager, EntityManagerFactoryUtils.doGetEntityManager(factory));
			assertSame(manager, ((EntityManagerHolder)TransactionSynchronizationManager.unbindResource(factory)).getEntityManager());
			
			mockControl.verify();
		}
		finally {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}
	

	/*
	 * Test method for
	 * 'org.springframework.orm.jpa.EntityManagerFactoryUtils.convertJpaAccessException(PersistenceException)'
	 */
	public void testConvertJpaAccessException() {
		EntityNotFoundException entityNotFound = new EntityNotFoundException();
		assertSame(JpaObjectRetrievalFailureException.class, EntityManagerFactoryUtils.convertJpaAccessException(
				entityNotFound).getClass());

		OptimisticLockException optimisticLock = new OptimisticLockException();
		assertSame(JpaOptimisticLockingFailureException.class, EntityManagerFactoryUtils.convertJpaAccessException(
				optimisticLock).getClass());

		EntityExistsException entityExists = new EntityExistsException("foo");
		assertSame(InvalidDataAccessApiUsageException.class, EntityManagerFactoryUtils.convertJpaAccessException(
				entityExists).getClass());

		NoResultException noResult = new NoResultException();
		assertSame(InvalidDataAccessApiUsageException.class, EntityManagerFactoryUtils.convertJpaAccessException(
				noResult).getClass());

		NonUniqueResultException nonUnique = new NonUniqueResultException();
		assertSame(InvalidDataAccessApiUsageException.class, EntityManagerFactoryUtils.convertJpaAccessException(
				nonUnique).getClass());

		PersistenceException unknown = new PersistenceException() {
		};
		assertSame(JpaSystemException.class, EntityManagerFactoryUtils.convertJpaAccessException(unknown).getClass());

	}

}
