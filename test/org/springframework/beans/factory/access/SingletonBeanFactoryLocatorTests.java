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

package org.springframework.beans.factory.access;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ClassUtils;

/**
 * @author Colin Sampaleanu
 */
public class SingletonBeanFactoryLocatorTests extends TestCase {

	public void testBaseBeanFactoryDefs() {
		// Just test the base BeanFactory/AppContext defs we are going to work with
		// in other tests.
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"/org/springframework/beans/factory/access/beans*.xml");
	}

	public void testBasicFunctionality() {
		SingletonBeanFactoryLocator facLoc = new SingletonBeanFactoryLocator(
				"classpath*:" + ClassUtils.addResourcePathToPackagePath(getClass(), "ref1.xml"));
		
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
	 * This test can run multiple times, but due to static keyed lookup of the locators,
	 * 2nd and subsequent calls will actuall get back same locator instance. This is not
	 * an issue really, since the contained beanfactories will still be loaded and released.
	 */
	public void testGetInstance() {
    // try with and without 'classpath*:' prefix, and with 'classpath:' prefix
		BeanFactoryLocator facLoc = SingletonBeanFactoryLocator.getInstance(
				ClassUtils.addResourcePathToPackagePath(getClass(), "ref1.xml"));
		
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
		
		bfr.release();
		bfr.release();
		bfr.release();
		bfr.release();
		
		facLoc = SingletonBeanFactoryLocator.getInstance(
				"classpath*:/" + ClassUtils.addResourcePathToPackagePath(getClass(), "ref1.xml"));
		
		bfr = facLoc.useBeanFactory("a.qualified.name.of.some.sort");
		fac = bfr.getFactory();
		bfr = facLoc.useBeanFactory("another.qualified.name");
		fac = bfr.getFactory();
		// verify that the same instance is returned
		tb = (TestBean) fac.getBean("beans1.bean1");
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
		
		bfr.release();
		bfr.release();
		bfr.release();
		bfr.release();

		// this will actually get another locator instance, as the key is the resource name
		facLoc = SingletonBeanFactoryLocator.getInstance(
				"classpath:" + ClassUtils.addResourcePathToPackagePath(getClass(), "ref1.xml"));
		
		bfr = facLoc.useBeanFactory("a.qualified.name.of.some.sort");
		fac = bfr.getFactory();
		bfr = facLoc.useBeanFactory("another.qualified.name");
		fac = bfr.getFactory();
		// verify that the same instance is returned
		tb = (TestBean) fac.getBean("beans1.bean1");
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
		
		bfr.release();
		bfr.release();
		bfr.release();
		bfr.release();
	}

}
