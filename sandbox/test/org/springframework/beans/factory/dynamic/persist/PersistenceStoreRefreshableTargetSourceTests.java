/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.beans.factory.dynamic.persist;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.dynamic.DynamicObject;
import org.springframework.dao.DataAccessException;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * TODO shorter class names:
 * @author Rod Johnson
 */
public class PersistenceStoreRefreshableTargetSourceTests extends AbstractDependencyInjectionSpringContextTests {
	
	private static DependsOnTestBean dependsOnTestBean1 = new DependsOnTestBean();
	private static DependsOnTestBean dependsOnTestBean2 = new DependsOnTestBean();
	
	static {
		// Change from default state that constructor would have
		dependsOnTestBean1.state = 25;
		dependsOnTestBean2.state = 37;
	}
	
	private DependsOnTestBean dotb;
	
	public void setDependsOnTestBean(DependsOnTestBean dotb) {
		this.dotb = dotb;
	}
	
	protected String[] getConfigLocations() {
		return new String[] { "classpath:org/springframework/beans/factory/dynamic/persist/test.xml" };
	}
	
	// TODO think about true unit tests, not requiring a context
	public void testPopulatesByAutowiring() {
		// NOT default state
		assertNotSame("Can't be same instance: must be advised", dependsOnTestBean1, dotb);
		// TODO direct field access not supported in AOP framework
		assertEquals("Equals to original", dependsOnTestBean1.getState(), dotb.getState());
		assertTrue("Should be a CGLIB proxy", AopUtils.isCglibProxy(dotb));
		assertSame("Populated by autowiring", applicationContext.getBean("tb"), dotb.getTestBean());
		
		// Now try casts...
		DynamicObject dyno = (DynamicObject) dotb;
		assertEquals(1, dyno.getLoadCount());
	}
	
	public void testChangePrimaryKeyToValidValue() {
		assertEquals("Equals to original", dependsOnTestBean1.getState(), dotb.getState());
		assertSame("Populated by autowiring", applicationContext.getBean("tb"), dotb.getTestBean());
		
		DynamicObject dyno = (DynamicObject) dotb;
		assertEquals(1, dyno.getLoadCount());
		
		// TODO What about prototype references from the factory? They'll change...
		// Document or allow only singletons?
		DatabaseBean db = (DatabaseBean) dotb;
		db.setPrimaryKey(2);
		assertEquals("Now behaves like new", dependsOnTestBean2.getState(), dotb.getState());
		assertSame("Populated by autowiring", applicationContext.getBean("tb"), dotb.getTestBean());
		
		assertEquals(2, dyno.getLoadCount());
		
		// We modified context
		setDirty();
	}
	
	public void testChangePrimaryKeyToInvalidValue() {
		DatabaseBean db = (DatabaseBean) dotb;
		try {
			db.setPrimaryKey(45);
			fail("Shouldn't allow setting to bogus id");
		}
		catch (DataAccessException ex) {
			// Ok
		}
		
		// Should still work
		assertEquals("Equals to original", dependsOnTestBean1.getState(), dotb.getState());
		assertSame("Populated by autowiring", applicationContext.getBean("tb"), dotb.getTestBean());
	}

	public static class DependsOnTestBean {
		public TestBean tb;
		private int state;
		
		public void setTestBean(TestBean tb) {
			this.tb = tb;
		}
		
		public int getState() {
			return state;
		}
		
		public TestBean getTestBean() {
			return tb;
		}
		
	}
	
	public static class PopulatedMapPersistenceStoreRefreshableTargetSource extends MapPersistenceStoreRefreshableTargetSource {
		public PopulatedMapPersistenceStoreRefreshableTargetSource() {
			put(1, dependsOnTestBean1);
			put(2, dependsOnTestBean2);
		}
	}
}
