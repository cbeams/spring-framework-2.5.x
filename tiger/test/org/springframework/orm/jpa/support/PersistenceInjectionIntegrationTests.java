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

import org.springframework.orm.jpa.support.PersistenceInjectionTests.DefaultPublicPersistenceContextSetter;
import org.springframework.orm.jpa.support.PersistenceInjectionTests.DefaultPublicPersistenceUnitSetterNamedPerson;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryIntegrationTests;

/**
 * @author Rod Johnson
 */
public class PersistenceInjectionIntegrationTests extends AbstractEntityManagerFactoryIntegrationTests {
	
	private DefaultPublicPersistenceContextSetter defaultSetterInjected;
	
	private DefaultPublicPersistenceUnitSetterNamedPerson namedSetterInjected;
	
	public void setDefaultPublicPersistenceContextSetter(DefaultPublicPersistenceContextSetter setterInjected) {
		this.defaultSetterInjected = setterInjected;
	}
	
	public void setNamedSetterInjected(DefaultPublicPersistenceUnitSetterNamedPerson namedSetterInjected) {
		this.namedSetterInjected = namedSetterInjected;
	}
	
	public void testDefaultSetterInjection() {
		EntityManager injectedEm = defaultSetterInjected.getEntityManager();
		assertNotNull("Default PersistenceContext Setter was injected", injectedEm);
	}
	
	public void testInjectedEntityManagerImplmentsPortableEntityManagerPlus() {
		EntityManager injectedEm = defaultSetterInjected.getEntityManager();
		assertNotNull("Default PersistenceContext Setter was injected", injectedEm);
	}
	
	public void testSetterInjectionOfNamedPersistenceContext() {
		assertNotNull("Named PersistenceContext Setter was injected", namedSetterInjected.getEntityManagerFactory());
	}

}
