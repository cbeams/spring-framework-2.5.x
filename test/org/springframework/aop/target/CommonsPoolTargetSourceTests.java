/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.target;

import java.io.InputStream;

import junit.framework.TestCase;

import org.springframework.aop.interceptor.SideEffectBean;
import org.springframework.beans.factory.support.ClasspathBeanDefinitionRegistryLocation;
import org.springframework.beans.factory.xml.XmlBeanFactory;

/**
 * Tests for pooling invoker interceptor
 * TODO need to make these tests stronger: it's hard to
 * make too many assumptions about a pool
 * @author Rod Johnson
 * @version $Id: CommonsPoolTargetSourceTests.java,v 1.2 2003-12-19 15:49:58 johnsonr Exp $
 */
public class CommonsPoolTargetSourceTests extends TestCase {

	/** Initial count value set in bean factory XML */
	private static final int INITIAL_COUNT = 10;

	private XmlBeanFactory beanFactory;
	
	public CommonsPoolTargetSourceTests(String s) {
		super(s);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// Load from classpath, NOT a file path
		InputStream is = getClass().getResourceAsStream("commonsPoolTests.xml");
		this.beanFactory = new XmlBeanFactory(is, new ClasspathBeanDefinitionRegistryLocation("commonsPoolTests.xml"));
	}
	
	/**
	 * We must simulate container shutdown, which should clear
	 * threads
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() {
		// Will call pool.close()
		this.beanFactory.destroySingletons();
	}

	private void testFunctionality(String name) {
		SideEffectBean pooled = (SideEffectBean) beanFactory.getBean(name);
		assertEquals(INITIAL_COUNT, pooled.getCount() );
		pooled.doWork();
		assertEquals(INITIAL_COUNT + 1, pooled.getCount() );
		
		pooled = (SideEffectBean) beanFactory.getBean(name);
		// Just check that it works--we can't make assumptions
		// about the count
		pooled.doWork();
		//assertEquals(INITIAL_COUNT + 1, apartment.getCount() );
	}
	
	public void testFunctionality() {
		testFunctionality("pooled");
	}
	
	public void testFunctionalityWithNoInterceptors() {
		testFunctionality("pooledNoInterceptors");
	}
	
	public void testConfigMixin() {
		SideEffectBean pooled = (SideEffectBean) beanFactory.getBean("pooledWithMixin");
		assertEquals(INITIAL_COUNT, pooled.getCount() );
		PoolingConfig conf = (PoolingConfig) beanFactory.getBean("pooledWithMixin");
		// TODO one invocation from setup
		//assertEquals(1, conf.getInvocations());
		pooled.doWork();
	//	assertEquals("No objects active", 0, conf.getActive());
		assertEquals("Correct target source", 25, conf.getMaxSize());
//		assertTrue("Some free", conf.getFree() > 0);
		//assertEquals(2, conf.getInvocations());
		assertEquals(25, conf.getMaxSize());
	}

}
