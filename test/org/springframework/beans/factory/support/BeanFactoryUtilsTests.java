/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.util.ClassLoaderUtils;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.mvc.DemoController;

/**
 * 
 * @author Rod Johnson
 * @since 04-Jul-2003
 * @version $Id: BeanFactoryUtilsTests.java,v 1.3 2003-10-31 17:01:28 jhoeller Exp $
 */
public class BeanFactoryUtilsTests extends TestCase {

	private final static String BASE_PATH = "org/springframework/beans/factory/support/";
	
	private ListableBeanFactory listableFactory;

	protected void setUp() {
		// Interesting hierarchical factory to test counts
		// Slow to read so we cache it
		XmlBeanFactory grandParent = new XmlBeanFactory();
		grandParent.loadBeanDefinitions(ClassLoaderUtils.getResourceAsStream(getClass(), BASE_PATH + "root.xml"));
		XmlBeanFactory parent = new XmlBeanFactory(grandParent);
		parent.loadBeanDefinitions(ClassLoaderUtils.getResourceAsStream(getClass(), "middle.xml"));
		XmlBeanFactory child = new XmlBeanFactory(parent);
		child.loadBeanDefinitions(ClassLoaderUtils.getResourceAsStream(getClass(), "leaf.xml"));
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
	 * @throws Exception
	 */
	public void testHierarchicalCountBeansWithOverride() throws Exception {
		// Leaf count
		assertTrue(this.listableFactory.getBeanDefinitionCount() == 1);
		// Count minus duplicate
		assertTrue("Should count 5 beans, not " + BeanFactoryUtils.countBeansIncludingAncestors(this.listableFactory),
			BeanFactoryUtils.countBeansIncludingAncestors(this.listableFactory) == 5);
	}
	
	public void testHierarchicalNamesWithOverride() throws Exception {
		List names = Arrays.asList(BeanFactoryUtils.beanNamesIncludingAncestors(ITestBean.class, this.listableFactory));
		assertEquals(2, names.size());
		assertTrue(names.contains("test"));
		assertTrue(names.contains("test3"));
	}

	public void testHierarchicalNamesWithNoMatch() throws Exception {
		List names = Arrays.asList(BeanFactoryUtils.beanNamesIncludingAncestors(HandlerAdapter.class, this.listableFactory));
		assertEquals(0, names.size());
	}

	public void testHierarchicalNamesWithMatchOnlyInRoot() throws Exception {
		List names = Arrays.asList(BeanFactoryUtils.beanNamesIncludingAncestors(DemoController.class, this.listableFactory));
		assertEquals(1, names.size());
		assertTrue(names.contains("demoController"));

		// Distinguish from default ListableBeanFactory behaviour
		assertTrue(listableFactory.getBeanDefinitionNames(DemoController.class).length == 0);
	}

	public void testNoBeansOfType() {
		StaticListableBeanFactory lbf = new StaticListableBeanFactory();
		lbf.addBean("foo", new Object());
		Map beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(ITestBean.class, lbf);
		assertTrue(beans.isEmpty());
	}

	public void testFindsBeansOfType() {
		StaticListableBeanFactory lbf = new StaticListableBeanFactory();
		TestBean t1 = new TestBean();
		TestBean t2 = new TestBean();
		lbf.addBean("t1", t1);
		lbf.addBean("t2", t2);
		Map beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(ITestBean.class, lbf);
		assertEquals(2, beans.size());
		assertEquals(t1, beans.get("t1"));
		assertEquals(t2, beans.get("t2"));
	}

	public void testHierarchicalResolutionWithOverride() throws Exception {
		Map beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(ITestBean.class, this.listableFactory);
		Object test3 = this.listableFactory.getBean("test3");
		Object test = this.listableFactory.getBean("test");
		assertEquals(2, beans.size());
		assertEquals(test3, beans.get("test3"));
		assertEquals(test, beans.get("test"));
	}

}
