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
 */
public class BeanNamePointcutTests extends AbstractDependencyInjectionSpringContextTests {

	protected ITestBean testBean1;
	protected ITestBean testBean2;
	protected ITestBean testBeanContainingNestedBean;
	protected Counter counterAspect;


	public BeanNamePointcutTests() {
		setPopulateProtectedVariables(true);
	}

	protected String getConfigPath() {
		return "bean-name-pointcut-tests.xml";
	}

	protected void onSetUp() throws Exception {
		this.counterAspect.reset();
		super.onSetUp();
	}


	// We don't need to test all combination of pointcuts due to BeanNamePointcutMatchingTests

	public void testMatchingBeanName() {
		assertTrue("Matching bean must be advised (proxied)", this.testBean1 instanceof Advised);
		// Call two methods to test for SPR-3953-like condition
		this.testBean1.setAge(20);
		this.testBean1.setName("");
		assertEquals("Advice not executed: must have been", 2, this.counterAspect.getCount());
	}

	public void testNonMatchingBeanName() {
		assertFalse("Non-matching bean must *not* be advised (proxied)", this.testBean2 instanceof Advised);
		this.testBean2.setAge(20);
		assertEquals("Advice must *not* have been executed", 0, this.counterAspect.getCount());
	}

	public void testNonMatchingNestedBeanName() {
		assertFalse("Non-matching bean must *not* be advised (proxied)", this.testBeanContainingNestedBean.getDoctor() instanceof Advised);
	}

}
