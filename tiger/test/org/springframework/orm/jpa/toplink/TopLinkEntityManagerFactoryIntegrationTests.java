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
import org.springframework.orm.jpa.AbstractEntityManagerFactoryIntegrationTests;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryIntegrationTests;

/**
 * Toplink specific tests.
 * 
 * @author Costin Leau
 * @author Rod Johnson
 */
public class TopLinkEntityManagerFactoryIntegrationTests extends AbstractEntityManagerFactoryIntegrationTests {
	
	public void testCanCastNativeEntityManagerFactoryToToplinkEntityManagerFactoryImpl() {
		EntityManagerFactoryInfo emfi = (EntityManagerFactoryInfo) entityManagerFactory;
		assertTrue(emfi.getNativeEntityManagerFactory() instanceof EntityManagerFactoryImpl);
	}
	
	public void testCannotCastSharedEntityManagerProxyToToplinkEntityManager() {
		assertFalse(sharedEntityManager instanceof oracle.toplink.essentials.ejb.cmp3.EntityManager);
	}
	
	public void testCanGetSharedToplinkEntityManagerProxy() {
		oracle.toplink.essentials.ejb.cmp3.EntityManager toplinkEntityManager =
				(oracle.toplink.essentials.ejb.cmp3.EntityManager)
				SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory,
						oracle.toplink.essentials.ejb.cmp3.EntityManager.class);
		assertNotNull(toplinkEntityManager.getActiveSession());
	}
	
	@Override
	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		return (getProvider() != Provider.TOPLINK);
	}

}
