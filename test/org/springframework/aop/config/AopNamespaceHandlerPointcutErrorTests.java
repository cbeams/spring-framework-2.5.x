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

package org.springframework.aop.config;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Mark Fisher
 */
public class AopNamespaceHandlerPointcutErrorTests extends TestCase {

	private static final String duplicatePointcutConfigLocation = "org/springframework/aop/config/aopNamespaceHandlerPointcutDuplicationTests.xml";
	private static final String missingPointcutConfigLocation = "org/springframework/aop/config/aopNamespaceHandlerPointcutMissingTests.xml";


	public void testDuplicatePointcutConfig() {
		try {
			new ClassPathXmlApplicationContext(duplicatePointcutConfigLocation);
			fail("parsing should have caused a BeansException");
		}
		catch (BeansException ex) {
			// expected
		}
	}
	
	public void testMissingPointcutConfig() {
		try {
			new ClassPathXmlApplicationContext(missingPointcutConfigLocation);
			fail("parsing should have caused a BeansException");
		}
		catch (BeansException ex) {
			// expected
		}
	}

}
