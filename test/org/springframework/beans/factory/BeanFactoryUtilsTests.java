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

package org.springframework.beans.factory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.IndexedTestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.HandlerAdapter;

/**
 * @author Rod Johnson
 * @since 04-Jul-2003
 */
public class BeanFactoryUtilsTests extends TestCase {

	private ListableBeanFactory listableFactory;

	protected void setUp() {
		// Interesting hierarchical factory to test counts
		// Slow to read so we cache it
		XmlBeanFactory grandParent = new XmlBeanFactory(new ClassPathResource("root.xml", getClass()));
		XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("middle.xml", getClass()), grandParent);
		XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("leaf.xml", getClass()), parent);
		this.listableFactory = child;
	}

	public void testHierarchicalCountBeansWithNonHierarchicalFactory() {
		StaticListableBeanFactory lbf = new StaticListableBeanFactory();
		lbf.addBean("t1", new TestBean());
		lbf.addBean("t2", new TestBean());
		assertTrue(BeanFactoryUtils.countBeansIncludingAncestors(lbf) == 2);
	}

	/**
	 * Check that override doesn't count as too separate beans
	 * @throws java.lang.Exception
	 */
	public void testHierarchicalCountBeansWithOverride() throws Exception {
		// Leaf count
		assertTrue(this.listableFactory.getBeanDefinitionCount() == 1);
		// Count minus duplicate
		assertTrue("Should count 7 beans, not " + BeanFactoryUtils.countBeansIncludingAncestors(this.listableFactory),
			BeanFactoryUtils.countBeansIncludingAncestors(this.listableFactory) == 7);
	}

	public void testHierarchicalNamesWithOverride() throws Exception {
		List names = Arrays.asList(BeanFactoryUtils.beanNamesIncludingAncestors(this.listableFactory, ITestBean.class));
		assertEquals(2, names.size());
		assertTrue(names.contains("test"));
		assertTrue(names.contains("test3"));
	}

	public void testHierarchicalNamesWithNoMatch() throws Exception {
		List names = Arrays.asList(BeanFactoryUtils.beanNamesIncludingAncestors(this.listableFactory, HandlerAdapter.class));
		assertEquals(0, names.size());
	}

	public void testHierarchicalNamesWithMatchOnlyInRoot() throws Exception {
		List names = Arrays.asList(BeanFactoryUtils.beanNamesIncludingAncestors(this.listableFactory, IndexedTestBean.class));
		assertEquals(1, names.size());
		assertTrue(names.contains("indexedBean"));
		// Distinguish from default ListableBeanFactory behaviour
		assertTrue(listableFactory.getBeanDefinitionNames(IndexedTestBean.class).length == 0);
	}

	public void testNoBeansOfType() {
		StaticListableBeanFactory lbf = new StaticListableBeanFactory();
		lbf.addBean("foo", new Object());
		Map beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(lbf, ITestBean.class, true, false);
		assertTrue(beans.isEmpty());
	}

	public void testFindsBeansOfType() {
		StaticListableBeanFactory lbf = new StaticListableBeanFactory();
		TestBean t1 = new TestBean();
		TestBean t2 = new TestBean();
		DummyFactory t3 = new DummyFactory();
		DummyFactory t4 = new DummyFactory();
		t4.setSingleton(false);
		lbf.addBean("t1", t1);
		lbf.addBean("t2", t2);
		lbf.addBean("t3", t3);
		lbf.addBean("t4", t4);
		Map beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(lbf, ITestBean.class, true, false);
		assertEquals(2, beans.size());
		assertEquals(t1, beans.get("t1"));
		assertEquals(t2, beans.get("t2"));
		beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(lbf, ITestBean.class, false, true);
		assertEquals(3, beans.size());
		assertEquals(t1, beans.get("t1"));
		assertEquals(t2, beans.get("t2"));
		assertEquals(t3.getObject(), beans.get("t3"));
		beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(lbf, ITestBean.class, true, true);
		assertEquals(4, beans.size());
		assertEquals(t1, beans.get("t1"));
		assertEquals(t2, beans.get("t2"));
		assertEquals(t3.getObject(), beans.get("t3"));
		assertTrue(beans.get("t4") instanceof TestBean);
	}

	public void testHierarchicalResolutionWithOverride() throws Exception {
		Object test3 = this.listableFactory.getBean("test3");
		Object test = this.listableFactory.getBean("test");
		Object testFactory1 = this.listableFactory.getBean("testFactory1");
		Map beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableFactory, ITestBean.class, true, false);
		assertEquals(2, beans.size());
		assertEquals(test3, beans.get("test3"));
		assertEquals(test, beans.get("test"));
		beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableFactory, ITestBean.class, false, false);
		assertEquals(1, beans.size());
		assertEquals(test, beans.get("test"));
		beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableFactory, ITestBean.class, false, true);
		assertEquals(2, beans.size());
		assertEquals(test, beans.get("test"));
		assertEquals(testFactory1, beans.get("testFactory1"));
		beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(this.listableFactory, ITestBean.class, true, true);
		assertEquals(4, beans.size());
		assertEquals(test3, beans.get("test3"));
		assertEquals(test, beans.get("test"));
		assertEquals(testFactory1, beans.get("testFactory1"));
		assertTrue(beans.get("testFactory2") instanceof TestBean);
	}

}
