/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.context.access;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocatorTests;
import org.springframework.beans.factory.access.TestBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Colin Sampaleanu
 * @version $Revision: 1.3 $
 */
public class ContextSingletonBeanFactoryLocatorTests extends TestCase {

	public void testBaseBeanFactoryDefs() {
		// just test the base BeanFactory/AppContext defs we are going to work
		// with in other tests
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] {"/org/springframework/beans/factory/access/beans1.xml",
				              "/org/springframework/beans/factory/access/beans2.xml"});
	}

	public void testBasicFunctionality() {
		// test a custom subclass of KeyedDefaultBeanFactoryLocator which overrides
		// getAllDefinitionResources method, since we can't really supply two files
		// with the same name in the classpath for this test
		ContextSingletonBeanFactoryLocator facLoc = new ContextSingletonBeanFactoryLocator() {
			protected Collection getAllDefinitionResources(String resourceName) throws IOException {
				ArrayList list = new ArrayList();
				list.add(SingletonBeanFactoryLocatorTests.class.getResource("ref1.xml"));
				list.add(SingletonBeanFactoryLocatorTests.class.getResource("ref2.xml"));
				return list;
			}
		};
		
		BeanFactoryReference bfr = facLoc.useBeanFactory("a.qualified.name.of.some.sort");
		BeanFactory fac = bfr.getFactory();
		bfr = facLoc.useBeanFactory("another.qualified.name");
		fac = bfr.getFactory();
		// verify that the same instance is returned
		TestBean tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("beans1.bean1"));
		tb.setName("was beans1.bean1");
		bfr = facLoc.useBeanFactory("another.qualified.name");
		fac = bfr.getFactory();
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("was beans1.bean1"));

		bfr = facLoc.useBeanFactory("a.qualified.name.which.is.an.alias");
		fac = bfr.getFactory();
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("was beans1.bean1"));
		
		// now verify that we can call release 4 times, and the 5th should log warning
		bfr.release();
		bfr.release();
		bfr.release();
		bfr.release();
		bfr.release();
	}

	/**
	 * Now since we overrode getAllDefinitionResources in previous test, verify
	 * that the class is actually asking for the resource names it should
	 */
	public void testBeanRefFileLookup() {
		ContextSingletonBeanFactoryLocator facLoc = new ContextSingletonBeanFactoryLocator() {
			protected Collection getAllDefinitionResources(String resourceName) throws IOException {
				// make sure it asks for "bean-refs.xml", but then ignore that
				assertTrue(resourceName.equals(ContextSingletonBeanFactoryLocator.BEANS_REFS_XML_NAME));
				ArrayList list = new ArrayList();
				list.add(SingletonBeanFactoryLocatorTests.class.getResource("ref1.xml"));
				list.add(SingletonBeanFactoryLocatorTests.class.getResource("ref2.xml"));
				return list;
			}
		};
		BeanFactory fac = facLoc.useBeanFactory("a.qualified.name.of.some.sort").getFactory();
		facLoc = new ContextSingletonBeanFactoryLocator("my-bean-refs.xml") {
			protected Collection getAllDefinitionResources(String resourceName)
					throws IOException {
				// make sure it asks for "my-bean-refs.xml", but then ignore
				// that
				assertTrue(resourceName.equals("my-bean-refs.xml"));
				ArrayList list = new ArrayList();
				list.add(SingletonBeanFactoryLocatorTests.class.getResource("ref1.xml"));
				list.add(SingletonBeanFactoryLocatorTests.class.getResource("ref2.xml"));
				return list;
			}
		};
		fac = facLoc.useBeanFactory("a.qualified.name.of.some.sort").getFactory();
	}
}
