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

package org.springframework.beans.factory.xml;

import org.springframework.aop.interceptor.DebugInterceptor;
import org.springframework.beans.ITestBean;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Tests lookup methods wrapped by a CGLIB proxy (see SPR-391).
 * @author Rod Johnson
 */
public class LookupMethodWrappedByCglibProxyTests extends AbstractDependencyInjectionSpringContextTests {

	protected String[] getConfigLocations() {
		return new String[] {"/org/springframework/beans/factory/xml/overloadOverrides.xml"};
	}

	protected void onSetUp() {
		resetInterceptor();
	}

	public void testAutoProxiedLookup() {
		OverloadLookup olup = (OverloadLookup) applicationContext.getBean("autoProxiedOverload");
		ITestBean jenny = olup.newTestBean();
		assertEquals("Jenny", jenny.getName());
		assertEquals("foo", olup.testMethod());
		assertInterceptorCount(2);
	}

	public void testRegularlyProxiedLookup() {
		OverloadLookup olup = (OverloadLookup) applicationContext.getBean("regularlyProxiedOverload");
		ITestBean jenny = olup.newTestBean();
		assertEquals("Jenny", jenny.getName());
		assertEquals("foo", olup.testMethod());
		assertInterceptorCount(2);
	}

	private void assertInterceptorCount(int count) {
		DebugInterceptor interceptor = getInterceptor();
		assertEquals("Interceptor count is incorrect", count, interceptor.getCount());
	}

	private void resetInterceptor() {
		DebugInterceptor interceptor = getInterceptor();
		interceptor.resetCount();
	}

	private DebugInterceptor getInterceptor() {
		return (DebugInterceptor) applicationContext.getBean("interceptor");
	}


	public static abstract class OverloadLookup {

		public abstract ITestBean newTestBean();

		public String testMethod() {
			return "foo";
		}
	}

}
