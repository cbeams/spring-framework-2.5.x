/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.aop.support;

import junit.framework.TestCase;

import org.springframework.aop.framework.autoproxy.CountingTestBean;
import org.springframework.beans.IOther;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * @author Rod Johnson
 */
public class TypePatternClassFilterTests extends TestCase {

	public void testInvalidPattern() {
		try {
			new TypePatternClassFilter("-");
			fail("Pattern should be recognized as invalid");
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	
	public void testValidPatternMatching() {
		TypePatternClassFilter tpcf = new TypePatternClassFilter("org.springframework.beans.*");
		assertTrue("Should match: in package", tpcf.matches(TestBean.class));
		assertTrue("Should match: in package", tpcf.matches(ITestBean.class));
		assertTrue("Should match: in package", tpcf.matches(IOther.class));
		assertFalse("Should be excluded: in wrong package", tpcf.matches(CountingTestBean.class));
		assertFalse("Should be excluded: in wrong package", tpcf.matches(BeanFactory.class));
		assertFalse("Should be excluded: in wrong package", tpcf.matches(DefaultListableBeanFactory.class));
	}
	
	public void testSubclassMatching() {
		TypePatternClassFilter tpcf = new TypePatternClassFilter("org.springframework.beans.ITestBean+");
		assertTrue("Should match: in package", tpcf.matches(TestBean.class));
		assertTrue("Should match: in package", tpcf.matches(ITestBean.class));
		assertTrue("Should match: in package", tpcf.matches(CountingTestBean.class));
		assertFalse("Should be excluded: not subclass", tpcf.matches(IOther.class));
		assertFalse("Should be excluded: not subclass", tpcf.matches(DefaultListableBeanFactory.class));
	}
}
