/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.interceptor;

import java.io.InputStream;

import junit.framework.TestCase;

import org.springframework.beans.factory.xml.XmlBeanFactory;

/**
 * Tests for pooling invoker interceptor
 * TODO need to make these tests stronger: it's hard to
 * make too many assumptions about a pool
 * @author Rod Johnson
 * @version $Id: PoolingInvokerInterceptorTests.java,v 1.1 2003-10-08 08:13:04 johnsonr Exp $
 */
public class PoolingInvokerInterceptorTests extends TestCase {

	/** Initial count value set in bean factory XML */
	private static final int INITIAL_COUNT = 10;

	private XmlBeanFactory beanFactory;
	
	public PoolingInvokerInterceptorTests(String s) {
		super(s);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// Load from classpath, NOT a file path
		InputStream is = getClass().getResourceAsStream("poolInvokerTests.xml");
		this.beanFactory = new XmlBeanFactory(is);
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

	public void testFunctionality() {
		SideEffectBean pooled = (SideEffectBean) beanFactory.getBean("pooled");
		assertEquals(INITIAL_COUNT, pooled.getCount() );
		pooled.doWork();
		assertEquals(INITIAL_COUNT + 1, pooled.getCount() );
		
		pooled = (SideEffectBean) beanFactory.getBean("pooled");
		// Just check that it works--we can't make assumptions
		// about the count
		pooled.doWork();
		//assertEquals(INITIAL_COUNT + 1, apartment.getCount() );
	}
	

}
