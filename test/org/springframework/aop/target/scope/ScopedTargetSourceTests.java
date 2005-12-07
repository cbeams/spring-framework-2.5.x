/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.aop.target.scope;

import junit.framework.TestCase;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author Rod Johnson
 */
public class ScopedTargetSourceTests extends TestCase {
	
	private DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
	
	public void testPrototypeNotFound() throws Exception {
		// Don't add to bean factory
		ScopedTargetSource sts = new ScopedTargetSource();
		sts.setScopeKey("sessionKey");
		String targetBeanName = "targetBeanName";
		sts.setTargetBeanName(targetBeanName);
		sts.setScopeMap(new HashMapScopeMap(false));
		
		assertEquals(targetBeanName, sts.getTargetBeanName());
		// Try to initialize. Should fail.
		try {
			sts.setBeanFactory(bf);
			fail("No such bean definition");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Ok
		}
	}
	
	public void testSingletonNotAccepted() throws Exception {
		ScopedTargetSource sts = new ScopedTargetSource();
		sts.setScopeKey("sessionKey");
		String targetBeanName = "targetBeanName";
		sts.setTargetBeanName(targetBeanName);
		sts.setScopeMap(new HashMapScopeMap(false));
		bf.registerSingleton(targetBeanName, new TestBean());
		// Try to initialize. Should fail.
		try {
			sts.setBeanFactory(bf);
			fail("No such bean definition");
		}
		catch (BeanDefinitionStoreException ex) {
			// Ok
		}
	}
	
	public void testPrototypesObeyScope() throws Exception {
		ScopedTargetSource sts = new ScopedTargetSource();
		String sessionKey = "sessionKey";
		sts.setScopeKey(sessionKey);
		String targetBeanName = "targetBeanName";
		sts.setTargetBeanName(targetBeanName);
		HashMapScopeMap hashMapScopeMap = new HashMapScopeMap(false);
		hashMapScopeMap.initScope();
		sts.setScopeMap(hashMapScopeMap);
		
		String name = "steven";
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, false);
		bd.getPropertyValues().addPropertyValue(new PropertyValue("name", name));
		bf.registerBeanDefinition(targetBeanName, bd);
		
		assertEquals(targetBeanName, sts.getTargetBeanName());
		sts.setBeanFactory(bf);
		
		TestBean target1 = (TestBean) sts.getTarget();
		TestBean target2 = (TestBean) sts.getTarget();
		
		assertEquals(name, target1.getName());
		assertEquals(name, target2.getName());
		assertSame("Prototypes must be same while scope endures", target1, target2);
		assertEquals(target1, hashMapScopeMap.get(sessionKey));
		assertEquals(1, hashMapScopeMap.getSize());
		
		// Create new scope
		hashMapScopeMap.initScope();

		TestBean target3 = (TestBean) sts.getTarget();
		TestBean target4 = (TestBean) sts.getTarget();
		
		assertEquals(name, target3.getName());
		assertEquals(name, target4.getName());
		assertSame("Prototypes must be same while scope endures", target3, target4);
		assertNotSame("Prototypes must NOT be same outside scope", target3, target1);
		
		hashMapScopeMap.initScope();
		TestBean target5 = (TestBean) sts.getTarget();
		
		assertEquals(name, target5.getName());
		assertNotSame("Prototypes must NOT be same outside scope", target5, target1);
		assertSame(target5, sts.getTarget());
		
		hashMapScopeMap.initScope();
		assertNotSame(target5, sts.getTarget());
	}
	
}
