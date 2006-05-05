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

package org.springframework.beans.factory.annotation;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class RequiredBeanFactoryPostProcessorTests extends TestCase {

	public void testNothing() {
	}

	public void XtestWithRequiredPropertyOmitted() throws Exception {
		try {
			new ClassPathXmlApplicationContext("org/springframework/beans/factory/annotation/requiredWithOneRequiredPropertyOmitted.xml");
			fail("Should have thrown IllegalArgumentException.");
		}
		catch (IllegalArgumentException ex) {
			String message = ex.getMessage();
			System.out.println(message);
			assertTrue(message.indexOf("Property") > -1);
			assertTrue(message.indexOf("age") > -1);
			assertTrue(message.indexOf("testBean") > -1);
		}
	}

	public void XtestWithThreeRequiredPropertiesOmitted() throws Exception {
		try {
			new ClassPathXmlApplicationContext("org/springframework/beans/factory/annotation/requiredWithThreeRequiredPropertiesOmitted.xml");
			fail("Should have thrown IllegalArgumentException.");
		}
		catch (IllegalArgumentException ex) {
			String message = ex.getMessage();
			System.out.println(message);
			assertTrue(message.indexOf("Properties") > -1);
			assertTrue(message.indexOf("age") > -1);
			assertTrue(message.indexOf("favouriteColour") > -1);
			assertTrue(message.indexOf("jobTitle") > -1);
			assertTrue(message.indexOf("testBean") > -1);
		}
	}

	public void XtestWithOnlyRequiredPropertiesSpecified() throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("org/springframework/beans/factory/annotation/requiredWithAllRequiredPropertiesProvided.xml");
		RequiredTestBean bean = (RequiredTestBean) context.getBean("testBean");
		assertEquals(24, bean.getAge());
		assertEquals("Blue", bean.getFavouriteColour());
	}

	public void XtestWithCustomAnnotation() throws Exception {
		try {
			new ClassPathXmlApplicationContext("org/springframework/beans/factory/annotation/requiredWithCustomAnnotation.xml");
			fail("Should have thrown IllegalArgumentException.");
		}
		catch (IllegalArgumentException ex) {
			String message = ex.getMessage();
			System.out.println(message);
			assertTrue(message.indexOf("Property") > -1);
			assertTrue(message.indexOf("name") > -1);
			assertTrue(message.indexOf("testBean") > -1);
		}
	}

}
