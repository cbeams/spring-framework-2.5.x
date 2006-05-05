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

import junit.framework.TestCase;

import org.easymock.MockControl;

/**
 * Superclass for unit tests for EntityManagerFactory FactoryBeans.
 * Note: subclasses must set any expectations on the mock
 * EntityManagerFactory and call close on it
 * @author Rod Johnson
 *
 */
public abstract class AbstractEntityManagerFactoryBeanTests extends TestCase {
	
	protected static MockControl emfMc;
	
	protected static EntityManagerFactory mockEmf;

	
	@Override
	protected void setUp() throws Exception {
		emfMc = MockControl.createControl(EntityManagerFactory.class);
		mockEmf = (EntityManagerFactory) emfMc.getMock();
//		mockEmf.close();
//		emfMc.setVoidCallable(1);
	}
		
	
	protected void checkInvariants(AbstractEntityManagerFactoryBean demf) {
		assertTrue(EntityManagerFactory.class.isAssignableFrom(demf.getObjectType()));
		assertSame(mockEmf, demf.getObject());
		assertSame(mockEmf, demf.getObject());
		assertSame(mockEmf, demf.getEntityManagerFactory());
	}


	protected static class DummyEntityManagerFactoryBean extends AbstractEntityManagerFactoryBean {
		private final EntityManagerFactory emf;
		
		public DummyEntityManagerFactoryBean(EntityManagerFactory emf) {
			this.emf = emf;
		}
		
		@Override
		protected EntityManagerFactory createEntityManagerFactory() throws PersistenceException {
			return emf;
		}
	}
}
