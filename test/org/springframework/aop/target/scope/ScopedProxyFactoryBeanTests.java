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

import org.springframework.aop.framework.Advised;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Tests execute against both local construction of the ProxyFactoryBean,
 * and loading an XML file. Instance variables must be populated in each case.
 *
 * @author Rod Johnson
 */
public class ScopedProxyFactoryBeanTests extends TestCase {
	
	private DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
	
	private ScopedProxyFactoryBean spfb;
	
	private Object proxy;
	
	public void testPrototypeNotFound() throws Exception {
		// Don't add to bean factory
		spfb = new ScopedProxyFactoryBean();
		spfb.setScopeKey("sessionKey");
		String targetBeanName = "targetBeanName";
		spfb.setTargetBeanName(targetBeanName);
		spfb.setScopeMap(new HashMapScopeMap(true));
		
		assertEquals(targetBeanName, spfb.getTargetBeanName());
		// Try to initialize. Should fail.
		try {
			spfb.setBeanFactory(bf);
			spfb.afterPropertiesSet();
			fail("No such bean definition");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Ok
		}
	}
	
	public void testSingletonNotAccepted() throws Exception {
		ScopedProxyFactoryBean spfb = new ScopedProxyFactoryBean();
		spfb.setScopeKey("sessionKey");
		String targetBeanName = "targetBeanName";
		spfb.setTargetBeanName(targetBeanName);
		spfb.setScopeMap(new HashMapScopeMap(true));
		
		assertEquals(targetBeanName, spfb.getTargetBeanName());
		bf.registerSingleton(targetBeanName, new TestBean());
		
		// Try to initialize. Should fail.
		try {
			spfb.setBeanFactory(bf);
			spfb.afterPropertiesSet();
			fail("No such bean definition");
		}
		catch (BeanDefinitionStoreException ex) {
			// Ok
		}
	}

	private void initLocal() throws Exception {
		spfb = new ScopedProxyFactoryBean();
		String sessionKey = "sessionKey";
		spfb.setScopeKey(sessionKey);
		String targetBeanName = "targetBeanName";
		spfb.setTargetBeanName(targetBeanName);
		HashMapScopeMap hashMapScopeMap = new HashMapScopeMap(true);
		spfb.setScopeMap(hashMapScopeMap);
		
		String name = "steven";
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, false);
		bd.getPropertyValues().addPropertyValue(new PropertyValue("name", name));
		bf.registerBeanDefinition(targetBeanName, bd);
		
		assertEquals(targetBeanName, spfb.getTargetBeanName());
		spfb.setProxyTargetClass(true);
		spfb.setBeanFactory(bf);
		spfb.afterPropertiesSet();
		proxy = spfb.getObject();
	}
	
	/**
	 * Run scoping tests, initializing instance variables from local method.
	 */
	public void testPrototypesObeyScopeLocal() throws Exception {
		initLocal();
		doTestPrototypesObeyScope();
	}
	
	/**
	 * Run scoping tests, initializing instance variables from XML definitions.
	 */
	public void testPrototypesObeyScopeXml() throws Exception {
		ClassPathXmlApplicationContext ac =
				new ClassPathXmlApplicationContext("org/springframework/aop/target/scope/scope.xml");
		String scopedBeanName = "scopedBean";
		spfb = (ScopedProxyFactoryBean) ac.getBean("&" + scopedBeanName);
		proxy = ac.getBean(scopedBeanName);
		doTestPrototypesObeyScope();
	}
	
	/**
	 * Relies on instance fields having been set.
	 */
	private void doTestPrototypesObeyScope() throws Exception {
		Advised advised = (Advised) proxy;

		TestBean proxy = (TestBean) advised;
		assertEquals("Must be a singleton FactoryBean", proxy, spfb.getObject());
		
		((HashMapScopeMap) spfb.getScopeMap()).initScope();
		assertNull("Target must be lazily initialized", spfb.getScopeMap().get(spfb.getScopeKey()));
		
		assertEquals(0, proxy.getAge());
		// This will work only after we've invoked the target source
		TestBean scopeA = (TestBean) spfb.getScopeMap().get(spfb.getScopeKey());
		assertEquals(0, scopeA.getAge());
		assertSame(scopeA, advised.getTargetSource().getTarget());
		
		assertEquals(0, proxy.getAge());
		assertEquals(0, proxy.getAge());
		proxy.haveBirthday();
		assertEquals(1, proxy.getAge());
		assertEquals(1, proxy.getAge());
		assertEquals(1, scopeA.getAge());
		
		((HashMapScopeMap) spfb.getScopeMap()).initScope();
		assertNull("Target must be lazily initialized", spfb.getScopeMap().get(spfb.getScopeKey()));
		
		assertEquals(0, proxy.getAge());
		// This will work only after we've invoked the target source
		TestBean scopeB = (TestBean) spfb.getScopeMap().get(spfb.getScopeKey());
		assertEquals(0, scopeB.getAge());
		assertNotSame(scopeA, advised.getTargetSource().getTarget());
		assertSame(scopeB, advised.getTargetSource().getTarget());
		
		assertEquals(0, proxy.getAge());
		assertEquals(0, proxy.getAge());
		proxy.haveBirthday();
		proxy.haveBirthday();
		assertEquals(2, proxy.getAge());
		assertEquals(2, proxy.getAge());
		assertEquals(2, scopeB.getAge());
		
		assertEquals("ScopeA is unchanged", 1, scopeA.getAge());
		
		((HashMapScopeMap) spfb.getScopeMap()).initScope();
		assertEquals(0, proxy.getAge());
	}
	
	public void testIntroductionOfScopedObjectInterfaceNonPersistent() throws Exception {
		doTestIntroductionOfScopedObjectInterface(false);
	}
	
	public void testIntroductionOfScopedObjectInterfacePersistent() throws Exception {
		doTestIntroductionOfScopedObjectInterface(true);
	}
	
	public void doTestIntroductionOfScopedObjectInterface(final boolean persistent) throws Exception {
		ScopedProxyFactoryBean spfb = new ScopedProxyFactoryBean();
		String sessionKey = "xfsessionKey";
		spfb.setScopeKey(sessionKey);
		String targetBeanName = "targetBeanName";
		spfb.setTargetBeanName(targetBeanName);
		HashMapScopeMap hashMapScopeMap = new HashMapScopeMap(persistent);
		spfb.setScopeMap(hashMapScopeMap);

		((HashMapScopeMap) spfb.getScopeMap()).initScope();

		String name = "steven";
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, false);
		bd.getPropertyValues().addPropertyValue(new PropertyValue("name", name));
		bf.registerBeanDefinition(targetBeanName, bd);
		
		assertEquals(targetBeanName, spfb.getTargetBeanName());
		spfb.setProxyTargetClass(true);
		spfb.setBeanFactory(bf);
		spfb.afterPropertiesSet();
		
		TestBean proxy = (TestBean) spfb.getObject();
		int newAge = 24;
		proxy.setAge(newAge);
		
		ScopedObject so = (ScopedObject) proxy;
		assertEquals(targetBeanName, so.getTargetBeanName());
		assertEquals(sessionKey, so.getScopeKey());
		assertEquals(hashMapScopeMap, so.getScopeMap());
		assertEquals(persistent, so.getHandle().isPersistent());
		
		if (!persistent) {
			try {
				spfb.reconnect(so.getHandle());
				fail("Should not be able to create proxy from non-persistent handle");
			}
			catch (HandleNotPersistentException ex) {
				// OK
			}
		}
		else {
			// Check we can recreate from the handle
			TestBean reconnected = (TestBean) spfb.reconnect(so.getHandle());
			ScopedObject soReconnected = (ScopedObject) reconnected;
			assertEquals(so.getHandle(), soReconnected.getHandle());
			assertEquals(newAge, reconnected.getAge());
			Advised advProxy = (Advised) proxy;
			Advised advReconnected = (Advised) reconnected;
			assertSame(advProxy.getTargetSource().getTarget(), advReconnected.getTargetSource().getTarget());
		}
	}
	
	public void testNullHandle() throws Exception {
		initLocal();
		
		try {
			spfb.reconnect(null);
			fail("Should reject null handle");
		}
		catch (HandleNotPersistentException ex) {
			// Ok
		}
	}
	
	public void testIncompatibleHandle() throws Exception {
		initLocal();
		Handle h = new Handle() {
			public boolean isPersistent() {
				return false;
			}
		};
		try {
			spfb.reconnect(h);
			fail("Should reject null handle");
		}
		catch (HandleNotPersistentException ex) {
			// Ok
		}
	}

}
