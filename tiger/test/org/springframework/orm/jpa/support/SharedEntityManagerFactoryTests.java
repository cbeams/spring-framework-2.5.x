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
import javax.persistence.EntityManagerFactory;

import junit.framework.TestCase;
import org.easymock.MockControl;

/**
 * @author Rod Johnson
 */
public class SharedEntityManagerFactoryTests extends TestCase {
	
	public void testValidUsage() {
		Object o = new Object();
		
		MockControl emMc = MockControl.createControl(EntityManager.class);
		EntityManager mockEm = (EntityManager) emMc.getMock();
		
		mockEm.contains(o);
		emMc.setReturnValue(false, 1);
		
		mockEm.close();
		emMc.setVoidCallable(1);
		emMc.replay();
		
		
		MockControl emfMc = MockControl.createControl(EntityManagerFactory.class);
		EntityManagerFactory mockEmf = (EntityManagerFactory) emfMc.getMock();
		mockEmf.createEntityManager();
		emfMc.setReturnValue(mockEm, 1);
		emfMc.replay();
		
		SharedEntityManagerBean proxyFactoryBean = new SharedEntityManagerBean();
		proxyFactoryBean.setEntityManagerFactory(mockEmf);
		proxyFactoryBean.afterPropertiesSet();
		
		assertTrue(EntityManager.class.isAssignableFrom(proxyFactoryBean.getObjectType()));
		assertTrue(proxyFactoryBean.isSingleton());
		
		EntityManager proxy = (EntityManager) proxyFactoryBean.getObject();
		assertSame(proxy, proxyFactoryBean.getObject());
		
		assertFalse(proxy.contains(o));
		
		emfMc.verify();
		emMc.verify();
	}

}
