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
import org.springframework.beans.factory.access.SingletonBeanFactoryLocatorTest;
import org.springframework.beans.factory.access.TestBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ClassLoaderUtils;

/**
 * Test for DefaultBeanFactoryLocator
 * 
 * @version $Revision: 1.1 $
 * @author colin sampaleanu
 */
public class DefaultBeanFactoryLocatorTest extends TestCase {

	public void testBaseBeanFactoryDefs() {
		// just test the base BeanFactory/AppContext defs we are going to work with
		// in other tests
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[]{
						ClassLoaderUtils.addResourcePathToPackagePath(
								SingletonBeanFactoryLocatorTest.class, "beans1.xml"),
						ClassLoaderUtils.addResourcePathToPackagePath(
								SingletonBeanFactoryLocatorTest.class, "beans2.xml")});
	}

	public void testBasicFunctionality() {

		// test a custom subclass of KeyedDefaultBeanFactoryLocator which overrides
		// getAllDefinitionResources method, since we can't really supply two files with
		// the same name in the classpath for this test
		DefaultBeanFactoryLocator facLoc = new DefaultBeanFactoryLocator() {
			protected Collection getAllDefinitionResources(String resourceName)
					throws IOException {

				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				ArrayList list = new ArrayList();
				list.add(cl.getResource(ClassLoaderUtils
						.addResourcePathToPackagePath(
								SingletonBeanFactoryLocatorTest.class, "ref1.xml")));
				list.add(cl.getResource(ClassLoaderUtils
						.addResourcePathToPackagePath(
								SingletonBeanFactoryLocatorTest.class, "ref2.xml")));
				return list;
			}
		};

		BeanFactory fac = facLoc.useFactory("a.qualified.name.of.some.sort")
				.getFactory();
		fac = facLoc.useFactory("another.qualified.name").getFactory();
		// verify that the same instance is returned
		TestBean tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("beans1.bean1"));
		tb.setName("was beans1.bean1");
		fac = facLoc.useFactory("another.qualified.name").getFactory();
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("was beans1.bean1"));

		fac = facLoc.useFactory("a.qualified.name.which.is.an.alias").getFactory();
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("was beans1.bean1"));
	}

	/**
	 * Now since we overrode getAllDefinitionResources in previous test, verify that
	 * the class is actually asking for the resource names it should
	 */
	public void testBeanRefFileLookup() {

		DefaultBeanFactoryLocator facLoc = new DefaultBeanFactoryLocator() {
			protected Collection getAllDefinitionResources(String resourceName)
					throws IOException {

				// make sure it asks for "bean-refs.xml", but then ignore that
				assertTrue(resourceName.equals("bean-refs.xml"));

				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				ArrayList list = new ArrayList();
				list.add(cl.getResource(ClassLoaderUtils
						.addResourcePathToPackagePath(
								SingletonBeanFactoryLocatorTest.class, "ref1.xml")));
				list.add(cl.getResource(ClassLoaderUtils
						.addResourcePathToPackagePath(
								SingletonBeanFactoryLocatorTest.class, "ref2.xml")));
				return list;
			}
		};

		BeanFactory fac = facLoc.useFactory("a.qualified.name.of.some.sort")
				.getFactory();

		facLoc = new DefaultBeanFactoryLocator("my-bean-refs.xml") {
			protected Collection getAllDefinitionResources(String resourceName)
					throws IOException {

				// make sure it asks for "my-bean-refs.xml", but then ignore that
				assertTrue(resourceName.equals("my-bean-refs.xml"));

				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				ArrayList list = new ArrayList();
				list.add(cl.getResource(ClassLoaderUtils
						.addResourcePathToPackagePath(
								SingletonBeanFactoryLocatorTest.class, "ref1.xml")));
				list.add(cl.getResource(ClassLoaderUtils
						.addResourcePathToPackagePath(
								SingletonBeanFactoryLocatorTest.class, "ref2.xml")));
				return list;
			}
		};

		fac = facLoc.useFactory("a.qualified.name.of.some.sort").getFactory();
	}
}
