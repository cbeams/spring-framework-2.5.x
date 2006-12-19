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

package org.springframework.beans.factory.xml;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class ShortcutTests extends TestCase {

	private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.beanFactory);

	public void testSimpleBeanConfigured() throws Exception {
		loadDefinitions("shortcutTests.xml");
		ITestBean rob = (TestBean) beanFactory.getBean("rob");
		ITestBean sally = (TestBean) beanFactory.getBean("sally");
		assertEquals("Rob Harrop", rob.getName());
		assertEquals(24, rob.getAge());
		assertEquals(rob.getSpouse(), sally);
	}

	public void testInnerBeanConfigured() throws Exception {
		loadDefinitions("shortcutTests.xml");
		TestBean sally = (TestBean) beanFactory.getBean("sally2");
		ITestBean rob = (TestBean) sally.getSpouse();
		assertEquals("Rob Harrop", rob.getName());
		assertEquals(24, rob.getAge());
		assertEquals(rob.getSpouse(), sally);
	}

	public void testWithPropertyDefinedTwice() throws Exception {
		try {
			loadDefinitions("shortcutWithErrorsTests.xml");
			fail("Should not be able to load a file with property specified twice.");
		}
		catch (BeanDefinitionStoreException e) {
			// success
		}
	}

	private void loadDefinitions(String path) {
		reader.loadBeanDefinitions(new ClassPathResource(path, getClass()));
	}

}
