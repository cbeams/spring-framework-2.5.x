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

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocatorTests;
import org.springframework.beans.factory.access.TestBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ClassUtils;

/**
 * @author Colin Sampaleanu
 * 
 * TODO: need to consolidate this with SingletonBeanFactoryLocatorTests!
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
		
		// just use definition file from the SingletonBeanFactoryLocator test, since it's
		// completely valid
		ContextSingletonBeanFactoryLocator facLoc = new ContextSingletonBeanFactoryLocator(
				"classpath*:" + ClassUtils.addResourcePathToPackagePath(
				SingletonBeanFactoryLocatorTests.class, "ref1.xml"));
		
		BeanFactoryReference bfr = facLoc.useBeanFactory("a.qualified.name.of.some.sort");
		BeanFactory fac = bfr.getFactory();
		BeanFactoryReference bfr2 = facLoc.useBeanFactory("another.qualified.name");
		fac = bfr2.getFactory();
		// verify that the same instance is returned
		TestBean tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("beans1.bean1"));
		tb.setName("was beans1.bean1");
		BeanFactoryReference bfr3 = facLoc.useBeanFactory("another.qualified.name");
		fac = bfr3.getFactory();
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("was beans1.bean1"));

		BeanFactoryReference bfr4 = facLoc.useBeanFactory("a.qualified.name.which.is.an.alias");
		fac = bfr4.getFactory();
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("was beans1.bean1"));
		
		// now verify that we can call release in any order
		// unfortunately this doesn't validate complete release after the last one
		bfr2.release();
		bfr3.release();
		bfr.release();
		bfr4.release();
	}
	
	// this test can run multiple times, but due to static keyed lookup of the locators,
	// 2nd and subsequent calls will actuall get back same locator instance. This is not
	// an issue really, since the contained beanfactories will still be loaded and released
	public void testGetInstance() {
		
        // try with and without 'classpath*:' prefix, and with 'classpath:' prefix
		BeanFactoryLocator facLoc = ContextSingletonBeanFactoryLocator.getInstance(
				ClassUtils.addResourcePathToPackagePath(
				SingletonBeanFactoryLocatorTests.class, "ref1.xml"));
		
		BeanFactoryReference bfr = facLoc.useBeanFactory("a.qualified.name.of.some.sort");
		BeanFactory fac = bfr.getFactory();
		BeanFactoryReference bfr2 = facLoc.useBeanFactory("another.qualified.name");
		fac = bfr2.getFactory();
		// verify that the same instance is returned
		TestBean tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("beans1.bean1"));
		tb.setName("was beans1.bean1");
		BeanFactoryReference bfr3 = facLoc.useBeanFactory("another.qualified.name");
		fac = bfr3.getFactory();
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("was beans1.bean1"));

		BeanFactoryReference bfr4 = facLoc.useBeanFactory("a.qualified.name.which.is.an.alias");
		fac = bfr4.getFactory();
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("was beans1.bean1"));
		
		bfr.release();
		bfr3.release();
		bfr2.release();
		bfr4.release();
		
		facLoc = ContextSingletonBeanFactoryLocator.getInstance(
				"classpath*:" + ClassUtils.addResourcePathToPackagePath(
				SingletonBeanFactoryLocatorTests.class, "ref1.xml"));

		bfr = facLoc.useBeanFactory("a.qualified.name.of.some.sort");
		fac = bfr.getFactory();
		bfr2 = facLoc.useBeanFactory("another.qualified.name");
		fac = bfr2.getFactory();
		// verify that the same instance is returned
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("beans1.bean1"));
		tb.setName("was beans1.bean1");
		bfr3 = facLoc.useBeanFactory("another.qualified.name");
		fac = bfr3.getFactory();
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("was beans1.bean1"));

		bfr4 = facLoc.useBeanFactory("a.qualified.name.which.is.an.alias");
		fac = bfr4.getFactory();
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("was beans1.bean1"));
		
		bfr.release();
		bfr2.release();
		bfr4.release();
		bfr3.release();

		// this will actually get another locator instance, as the key is the resource name
		facLoc = ContextSingletonBeanFactoryLocator.getInstance(
				"classpath:" + ClassUtils.addResourcePathToPackagePath(
				SingletonBeanFactoryLocatorTests.class, "ref1.xml"));
		
		bfr = facLoc.useBeanFactory("a.qualified.name.of.some.sort");
		fac = bfr.getFactory();
		bfr2 = facLoc.useBeanFactory("another.qualified.name");
		fac = bfr2.getFactory();
		// verify that the same instance is returned
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("beans1.bean1"));
		tb.setName("was beans1.bean1");
		bfr3 = facLoc.useBeanFactory("another.qualified.name");
		fac = bfr3.getFactory();
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("was beans1.bean1"));

		bfr4 = facLoc.useBeanFactory("a.qualified.name.which.is.an.alias");
		fac = bfr4.getFactory();
		tb = (TestBean) fac.getBean("beans1.bean1");
		assertTrue(tb.getName().equals("was beans1.bean1"));
		
		bfr4.release();
		bfr3.release();
		bfr2.release();
		bfr.release();
	}
}
