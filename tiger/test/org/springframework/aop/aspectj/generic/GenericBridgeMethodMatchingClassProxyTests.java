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
package org.springframework.aop.aspectj.generic;

/**
 * Tests for AspectJ pointcut expression matching when working with bridge methods.
 * 
 * This class focuses on class proxy.
 * 
 * See GenericBridgeMethodMatchingTests for more details.
 * 
 * @author Ramnivas Laddad
 * @since 2.1
 */

public class GenericBridgeMethodMatchingClassProxyTests extends GenericBridgeMethodMatchingTests {

	@Override
	protected String getConfigPath() {
		return "genericBridgeMethodMatchingTests-classProxy-context.xml";
	}

	public void testGenericDerivedInterfaceMethodThroughClass() {
		((DerivedStringParametarizedClass)testBean).genericDerivedInterfaceMethod("");
		assertEquals(1, counterAspect.count);
	}

	public void testGenericBaseInterfaceMethodThroughClass() {
		((DerivedStringParametarizedClass)testBean).genericBaseInterfaceMethod("");
		assertEquals(1, counterAspect.count);
	}
}
