/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.context.access;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocatorTests;
import org.springframework.beans.factory.access.TestBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Colin Sampaleanu
 * @version $Revision: 1.1 $
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
		BeanFactory fac = facLoc.useBeanFactory("a.qualified.name.of.some.sort")
				.getFactory();
		fac = facLoc.useBeanFactory("another.qualified.name").getFactory();
		// verify that the same instance is returned
		TestBean tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("beans1.bean1"));
		tb.setName("was beans1.bean1");
		fac = facLoc.useBeanFactory("another.qualified.name").getFactory();
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("was beans1.bean1"));
		fac = facLoc.useBeanFactory("a.qualified.name.which.is.an.alias")
				.getFactory();
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("was beans1.bean1"));
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
