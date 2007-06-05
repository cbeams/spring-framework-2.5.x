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

import org.springframework.aop.framework.Advised;
import org.springframework.beans.ITestBean;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Test for correct application of the bean() PCD for XML-based AspectJ aspects.
 * 
 * @author Ramnivas Laddad
 *
 */
public class BeanNamePointcutTests extends AbstractDependencyInjectionSpringContextTests {
	protected ITestBean testBean1;
	protected ITestBean testBean2;	
	protected Counter counterAspect;
	
	public BeanNamePointcutTests() {
		setPopulateProtectedVariables(true);
	}
	
	protected String getConfigPath() {
		return "bean-name-pointcut-tests.xml";
	}
	
	protected void onSetUp() throws Exception {
		counterAspect.reset();
		super.onSetUp();
	}

	// We don't need to test all combination of pointcuts due to BeanNamePointcutMatchingTests
	
	public void testMatchingBeanName() {
		assertTrue(testBean1 instanceof Advised);
		testBean1.setAge(20);
		assertEquals(1, counterAspect.getCount());
	}

	public void testNonMatchingBeanName() {
		assertFalse(testBean2 instanceof Advised);
		testBean2.setAge(20);
		assertEquals(0, counterAspect.getCount());
	}
}
