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

package org.springframework.context.annotation;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

/**
 * @author Mark Fisher
 */
public class ComponentScanParserTests extends TestCase {

	public void testAspectJTypeFilter() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"org/springframework/context/annotation/aspectjTypeFilterTests.xml");
		assertTrue(context.containsBean("fooServiceImpl"));
		assertTrue(context.containsBean("stubFooDao"));
		assertFalse(context.containsBean("scopedProxyTestBean"));
	}

	public void testNonMatchingResourcePattern() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"org/springframework/context/annotation/nonMatchingResourcePatternTests.xml");
		assertFalse(context.containsBean("fooServiceImpl"));
	}

	public void testMatchingResourcePattern() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"org/springframework/context/annotation/matchingResourcePatternTests.xml");
		assertTrue(context.containsBean("fooServiceImpl"));
	}

}
