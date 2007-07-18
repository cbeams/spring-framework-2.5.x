/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.aop.aspectj;

import junit.framework.TestCase;

/**
 * Unit tests for the <code>ProxyCreationContext</code> class.
 *
 * @author Rick Evans
 */
public final class ProxyCreationContextTests extends TestCase {

	public void testNotifyProxyCreationStartState() {
		checkStartState();
	}

	public void testNotifyProxyCreationStart() {
		try {
			final String beanName = "someService";
			final boolean isInnerBean = true;

			ProxyCreationContext.notifyProxyCreationStart(beanName, isInnerBean);

			assertTrue(ProxyCreationContext.isProxyCreationInProgress());
			assertNotNull(ProxyCreationContext.getCurrentProxyingBeanName());
			assertEquals(beanName, ProxyCreationContext.getCurrentProxyingBeanName());
			assertEquals(isInnerBean, ProxyCreationContext.isCurrentProxyingBeanAnInnerBean());
		} finally {
			ProxyCreationContext.notifyProxyCreationComplete();
		}
		checkStartState();
	}


	private static void checkStartState() {
		assertFalse(ProxyCreationContext.isProxyCreationInProgress());
		assertNull(ProxyCreationContext.getCurrentProxyingBeanName());
		assertFalse(ProxyCreationContext.isCurrentProxyingBeanAnInnerBean());
	}

}
