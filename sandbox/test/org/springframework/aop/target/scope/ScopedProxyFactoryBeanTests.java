package org.springframework.aop.target.scope;

import java.util.HashMap;

import junit.framework.TestCase;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.target.scope.ScopedTargetSourceTests.HashMapScopeMap;
import org.springframework.aop.target.scope.ScopedTargetSourceTests.MapScopeIdentiferResolver;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Tests execute against both local construction of the ProxyFactoryBean,
 * and loading an XML file.
 * Instance variables must be populated in each case.
 * @author Rod Johnson
 * @since 1.3
 */
public class ScopedProxyFactoryBeanTests extends TestCase {
	
	private DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
	
	private ScopedProxyFactoryBean spfb;
	
	private Object proxy;
	
	public void testPrototypeNotFound() throws Exception {
		// Don't add to bean factory
		spfb = new ScopedProxyFactoryBean();
		spfb.setSessionKey("sessionKey");
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
		spfb.setSessionKey("sessionKey");
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
		spfb.setSessionKey(sessionKey);
		String targetBeanName = "targetBeanName";
		spfb.setTargetBeanName(targetBeanName);
		HashMapScopeMap hashMapScopeMap = new HashMapScopeMap(true);
		spfb.setScopeMap(hashMapScopeMap);
		
		String name = "steven";
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, false);
		bd.getPropertyValues().addPropertyValue(new PropertyValue("name", name));
		bf.registerBeanDefinition(targetBeanName, bd);
		
		MapScopeIdentiferResolver sir = new MapScopeIdentiferResolver();
		spfb.setScopeIdentifierResolver(sir);
		
		assertEquals(targetBeanName, spfb.getTargetBeanName());
		spfb.setProxyTargetClass(true);
		spfb.setBeanFactory(bf);
		spfb.afterPropertiesSet();
		proxy = spfb.getObject();
	}
	
	/**
	 * Run scoping tests, initializing instance variables from
	 * local method.
	 * @throws Exception
	 */
	public void testPrototypesObeyScopeLocal() throws Exception {
		initLocal();
		doTestPrototypesObeyScope();
	}
	
	/**
	 * Run scoping tests, initializing instance variables from XML
	 * definitions.
	 * @throws Exception
	 */
	public void testPrototypesObeyScopeXml() throws Exception {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("org/springframework/aop/target/scope/scope.xml");
		String scopedBeanName = "scopedBean";
		spfb = (ScopedProxyFactoryBean) ac.getBean("&" + scopedBeanName);
		proxy = ac.getBean(scopedBeanName);
		doTestPrototypesObeyScope();
	}
	
	/**
	 * Relies on instance fields having been set
	 * @throws Exception
	 */
	private void doTestPrototypesObeyScope() throws Exception {
		
		Advised advised = (Advised) proxy;
		//System.out.println(advised.toProxyConfigString());
		
		TestBean proxy = (TestBean) advised;
		assertEquals("Must be a singleton FactoryBean", proxy, spfb.getObject());
		
		assertNull("Target must be lazily initialized", spfb.getScopeMap().get(spfb.getScopeIdentifierResolver().getScopeIdentifier(), spfb.getSessionKey()));
		
		Object scopeIdentifierA = spfb.getScopeIdentifierResolver().getScopeIdentifier();
		assertSame(scopeIdentifierA, spfb.getScopeIdentifierResolver().getScopeIdentifier());
		
		assertEquals(0, proxy.getAge());
		// This will work only after we've invoked the target source
		TestBean scopeA = (TestBean) spfb.getScopeMap().get(spfb.getScopeIdentifierResolver().getScopeIdentifier(), spfb.getSessionKey());
		assertEquals(0, scopeA.getAge());
		assertSame(scopeA, advised.getTargetSource().getTarget());
		
		assertEquals(0, proxy.getAge());
		assertEquals(0, proxy.getAge());
		proxy.haveBirthday();
		assertEquals(1, proxy.getAge());
		assertEquals(1, proxy.getAge());
		assertEquals(1, scopeA.getAge());
		
		((MapScopeIdentiferResolver) spfb.getScopeIdentifierResolver()).setMap(new HashMap());
		assertNotSame(scopeIdentifierA, spfb.getScopeIdentifierResolver().getScopeIdentifier());
		
		assertNull("Target must be lazily initialized", spfb.getScopeMap().get(spfb.getScopeIdentifierResolver().getScopeIdentifier(), spfb.getSessionKey()));
		
		assertEquals(0, proxy.getAge());
		// This will work only after we've invoked the target source
		TestBean scopeB = (TestBean) spfb.getScopeMap().get(spfb.getScopeIdentifierResolver().getScopeIdentifier(), spfb.getSessionKey());
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
		
		((MapScopeIdentiferResolver) spfb.getScopeIdentifierResolver()).setMap(new HashMap());
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
		spfb.setSessionKey(sessionKey);
		String targetBeanName = "targetBeanName";
		spfb.setTargetBeanName(targetBeanName);
		HashMapScopeMap hashMapScopeMap = new HashMapScopeMap(persistent);
		spfb.setScopeMap(hashMapScopeMap);
		MapScopeIdentiferResolver sir = new MapScopeIdentiferResolver();
		spfb.setScopeIdentifierResolver(sir);
		
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
		assertEquals(sessionKey, so.getSessionKey());
		assertEquals(hashMapScopeMap, so.getScopeMap());
		assertSame(sir.getScopeIdentifier(), so.getHandle().getScopeIdentifier());
		assertSame(targetBeanName, so.getHandle().getTargetBeanName());
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
			assertEquals(so.getHandle().getScopeIdentifier(), soReconnected.getHandle().getScopeIdentifier());
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
		Handle h = new ScopedProxyFactoryBean.DefaultHandle(ScopeIdentifierResolver.CONTEXT_BASED_SCOPE_IDENTIFIER_RESOLVER, 
				new ScopedTargetSourceTests.HashMapScopeMap(true), "tt");
		try {
			spfb.reconnect(h);
			fail("Should reject null handle");
		}
		catch (IncompatibleHandleException ex) {
			// Ok
		}
	}

}
