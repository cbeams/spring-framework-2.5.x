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
package org.springframework.orm.jpa.toplink;

import oracle.toplink.essentials.internal.ejb.cmp3.base.EntityManagerFactoryImpl;

import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.orm.jpa.PortableEntityManagerFactoryPlus;
import org.springframework.orm.jpa.spi.AbstractContainerEntityManagerFactoryBeanIntegrationTests;
import org.springframework.orm.jpa.spi.AbstractContainerEntityManagerFactoryBeanIntegrationTests.Provider;
import org.springframework.orm.jpa.support.SharedEntityManagerFactory;
import org.springframework.test.jpa.AbstractJpaTests;

/**
 * Toplink specific tests.
 * 
 * @author Costin Leau
 * @author Rod Johnson
 * 
 */
public class ToplinkContainerEntityManagerFactoryBeanTests extends AbstractJpaTests {
	
	public static final String[] CONFIG_LOCATIONS = new String[] { 
		"/org/springframework/orm/jpa/toplink/toplink-manager.xml",
		"/org/springframework/orm/jpa/spi/memdb.xml",
		"/org/springframework/orm/jpa/spi/inject.xml"
	};

	@Override
	protected String[] getConfigLocations() {
		return CONFIG_LOCATIONS;
	}
	
	public void testCanCastNativeEntityManagerFactoryToToplinkEntityManagerFactoryImpl() {
		EntityManagerFactoryInfo emfi = ((PortableEntityManagerFactoryPlus) entityManagerFactory).getEntityManagerFactoryInfo();		
		assertTrue(emfi.getNativeEntityManagerFactory() instanceof EntityManagerFactoryImpl);
	}
	
	public void testCannotCastSharedEntityManagerProxyToToplinkEntityManager() {
		assertFalse(sharedEntityManagerProxy instanceof oracle.toplink.essentials.ejb.cmp3.EntityManager);
	}
	
	public void testCanGetSharedToplinkEntityManagerProxy() {
		oracle.toplink.essentials.ejb.cmp3.EntityManager toplinkEntityManager = (oracle.toplink.essentials.ejb.cmp3.EntityManager) SharedEntityManagerFactory.createEntityManagerProxy(
				getClass().getClassLoader(), 
				entityManagerFactory,
				oracle.toplink.essentials.ejb.cmp3.EntityManager.class);
		assertNotNull(toplinkEntityManager.getActiveSession());
	}
	
	@Override
	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		return AbstractContainerEntityManagerFactoryBeanIntegrationTests.getProvider() != Provider.TOPLINK;
	}

}
