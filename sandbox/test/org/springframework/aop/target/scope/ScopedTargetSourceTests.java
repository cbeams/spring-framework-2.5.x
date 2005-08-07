package org.springframework.aop.target.scope;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Unit tests.
 * @author Rod Johnson
 */
public class ScopedTargetSourceTests extends TestCase {
	
	private DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
	
	public void testPrototypeNotFound() throws Exception {
		// Don't add to bean factory
		ScopedTargetSource sts = new ScopedTargetSource();
		sts.setSessionKey("sessionKey");
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
		sts.setSessionKey("sessionKey");
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
		sts.setSessionKey(sessionKey);
		String targetBeanName = "targetBeanName";
		sts.setTargetBeanName(targetBeanName);
		HashMapScopeMap hashMapScopeMap = new HashMapScopeMap(false);
		sts.setScopeMap(hashMapScopeMap);
		
		String name = "steven";
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, false);
		bd.getPropertyValues().addPropertyValue(new PropertyValue("name", name));
		bf.registerBeanDefinition(targetBeanName, bd);
		
		MapScopeIdentiferResolver sir = new MapScopeIdentiferResolver();
		sts.setScopeIdentifierResolver(sir);
		
		assertEquals(targetBeanName, sts.getTargetBeanName());
		sts.setBeanFactory(bf);
		
		TestBean target1 = (TestBean) sts.getTarget();
		TestBean target2 = (TestBean) sts.getTarget();
		
		assertEquals(name, target1.getName());
		assertEquals(name, target2.getName());
		assertSame("Prototypes must be same while scope endures", target1, target2);
		assertEquals(target1, hashMapScopeMap.get(sir.getScopeIdentifier(), sessionKey));
		assertEquals(1, sir.m.size());
		
		// Create new scope
		sir.setMap(new HashMap());
		
		TestBean target3 = (TestBean) sts.getTarget();
		TestBean target4 = (TestBean) sts.getTarget();
		
		assertEquals(name, target3.getName());
		assertEquals(name, target4.getName());
		assertSame("Prototypes must be same while scope endures", target3, target4);
		assertNotSame("Prototypes must NOT be same outside scope", target3, target1);
		
		sir.setMap(new HashMap());
		TestBean target5 = (TestBean) sts.getTarget();
		
		assertEquals(name, target5.getName());
		assertNotSame("Prototypes must NOT be same outside scope", target5, target1);
		assertSame(target5, sts.getTarget());
		
		sir.setMap(new HashMap());
		assertNotSame(target5, sts.getTarget());
	}
	
	
	
	/**
	 * Trivial implementation of ScopeMap interface that uses a simple Java HashMap.
	 */
	public static class HashMapScopeMap implements ScopeMap {
		
		private boolean persistent;
		
		public HashMapScopeMap() {
			this(false);
		}

		public HashMapScopeMap(boolean persistent) {
			this.persistent = persistent;
		}
		
		public Object get(Object scopeId, String name) {
			return ((Map) scopeId).get(name);
		}

		public void put(Object scopeId, String name, Object o) {
			((Map) scopeId).put(name, o);
		}
		
		public boolean isPersistent(Object scopeIdentifier) {
			return persistent;
		}

		public void remove(Object scopeId, String name) {
			((Map) scopeId).remove(name);
		}
	}
	
	public static class MapScopeIdentiferResolver implements ScopeIdentifierResolver {
		
		private Map m = new HashMap();
		
		public void setMap(Map m) {
			this.m = m;
		}
		
		public Object getScopeIdentifier() throws IllegalStateException {
			return m;
		}
	}

}
