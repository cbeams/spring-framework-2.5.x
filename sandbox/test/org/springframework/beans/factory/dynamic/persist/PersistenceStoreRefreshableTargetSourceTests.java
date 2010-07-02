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
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * TODO shorter class names:
 *
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

	public void setDependsOnTestBean(final DependsOnTestBean dotb) {

		this.dotb = dotb;
	}

	protected String[] getConfigLocations() {

		return new String[] { "classpath:org/springframework/beans/factory/dynamic/persist/test.xml" };
	}

	// TODO think about true unit tests, not requiring a context
	public void testPopulatesByAutowiring() {

		// NOT default state
		assertNotSame("Can't be same instance: must be advised", dependsOnTestBean1, this.dotb);
		// TODO direct field access not supported in AOP framework
		assertEquals("Equals to original", dependsOnTestBean1.getState(), this.dotb.getState());
		assertTrue("Should be a CGLIB proxy", AopUtils.isCglibProxy(this.dotb));
		assertSame("Populated by autowiring", this.applicationContext.getBean("tb"), this.dotb.getTestBean());

		// TODO Uncomment the following test code once it passes...
		// Now try casts...
		// Refreshable dyno = (Refreshable) dotb;
		// assertEquals(1, dyno.getRefreshCount());
	}

	public void testChangePrimaryKeyToValidValue() {

		assertEquals("Equals to original", dependsOnTestBean1.getState(), this.dotb.getState());
		assertSame("Populated by autowiring", this.applicationContext.getBean("tb"), this.dotb.getTestBean());

		// TODO Uncomment the following test code once it passes...
		// final Refreshable dyno = (Refreshable) this.dotb;
		// assertEquals(1, dyno.getRefreshCount());

		// TODO What about prototype references from the factory? They'll
		// change...
		// Document or allow only singletons?

		// TODO Uncomment the following test code once it passes...
		// final DatabaseBean db = (DatabaseBean) this.dotb;
		// db.setPrimaryKey(2);
		// assertEquals("Now behaves like new", dependsOnTestBean2.getState(),
		// this.dotb.getState());
		// assertSame("Populated by autowiring",
		// this.applicationContext.getBean("tb"), this.dotb.getTestBean());

		// XXX Uncomment the following test code once it passes...
		// assertEquals(2, dyno.getRefreshCount());

		// We modified context
		setDirty();
	}

	public void testChangePrimaryKeyToInvalidValue() {

		// TODO Uncomment the following test code once it passes...
		// final DatabaseBean db = (DatabaseBean) this.dotb;
		// try {
		// db.setPrimaryKey(45);
		// fail("Shouldn't allow setting to bogus id");
		// }
		// catch (final DataAccessException ex) {
		// // Ok
		// }

		// Should still work
		assertEquals("Equals to original", dependsOnTestBean1.getState(), this.dotb.getState());
		assertSame("Populated by autowiring", this.applicationContext.getBean("tb"), this.dotb.getTestBean());
	}

	public static class DependsOnTestBean {

		public TestBean tb;

		private int state;

		public void setTestBean(final TestBean tb) {

			this.tb = tb;
		}

		public int getState() {

			return this.state;
		}

		public TestBean getTestBean() {

			return this.tb;
		}

	}

	public static class PopulatedMapPersistenceStoreRefreshableTargetSource extends
			MapPersistenceStoreRefreshableTargetSource {

		public PopulatedMapPersistenceStoreRefreshableTargetSource() {

			put(1, dependsOnTestBean1);
			put(2, dependsOnTestBean2);
		}
	}
}
