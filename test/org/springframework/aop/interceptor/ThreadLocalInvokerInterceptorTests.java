/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.interceptor;

import java.io.InputStream;

import junit.framework.TestCase;

import org.springframework.beans.factory.xml.XmlBeanFactory;

/**
 * 
 * @author Rod Johnson
 * @version $Id: ThreadLocalInvokerInterceptorTests.java,v 1.1 2003-11-24 20:45:06 johnsonr Exp $
 */
public class ThreadLocalInvokerInterceptorTests extends TestCase {

	/** Initial count value set in bean factory XML */
	private static final int INITIAL_COUNT = 10;

	private XmlBeanFactory beanFactory;
	
	public ThreadLocalInvokerInterceptorTests(String s) {
		super(s);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// Load from classpath, NOT a file path
		InputStream is = getClass().getResourceAsStream("threadLocalInvokerTests.xml");
		this.beanFactory = new XmlBeanFactory(is);
	}
	
	/**
	 * We must simulate container shutdown, which should clear
	 * threads
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() {
		this.beanFactory.destroySingletons();
	}

	public void testReuseInSameThread() {
		SideEffectBean apartment = (SideEffectBean) beanFactory.getBean("apartment");
		assertEquals(INITIAL_COUNT, apartment.getCount() );
		apartment.doWork();
		assertEquals(INITIAL_COUNT + 1, apartment.getCount() );
		
		apartment = (SideEffectBean) beanFactory.getBean("apartment");
		assertEquals(INITIAL_COUNT + 1, apartment.getCount() );
	}
	
	/**
	 * Relies on introduction
	 *
	 */
	public void testCanGetStatsViaMixin() {
		ThreadLocalInvokerStats stats = (ThreadLocalInvokerStats) beanFactory.getBean("apartment");
		// TODO dodgy: effect of advice creation
		assertEquals(1, stats.getInvocations());
		SideEffectBean apartment = (SideEffectBean) beanFactory.getBean("apartment");
		apartment.doWork();
		assertEquals(2, stats.getInvocations());
		assertEquals(1, stats.getHits());
		apartment.doWork();
		assertEquals(3, stats.getInvocations());
		assertEquals(2, stats.getHits());
		// Only one thread so only one object can have been bound
		assertEquals(1, stats.getObjects());
	}
	

	public void testNewThreadHasOwnInstance() throws InterruptedException {
		SideEffectBean apartment = (SideEffectBean) beanFactory.getBean("apartment");
		assertEquals(INITIAL_COUNT, apartment.getCount() );
		apartment.doWork();
		apartment.doWork();
		apartment.doWork();
		assertEquals(INITIAL_COUNT + 3, apartment.getCount() );
	
		class Runner implements Runnable {
			public SideEffectBean mine;
			public void run() {
				this.mine = (SideEffectBean) beanFactory.getBean("apartment");
				assertEquals(INITIAL_COUNT, mine.getCount() );
				mine.doWork();
				assertEquals(INITIAL_COUNT + 1, mine.getCount() );
			}
		}
		Runner r = new Runner();
		Thread t = new Thread(r);
		t.start();
		t.join();
		
		assertNotNull(r);
		
		// Check it didn't affect the other thread's copy
		assertEquals(INITIAL_COUNT + 3, apartment.getCount() );
		
		// When we use other thread's copy in this thread 
		// it should behave like ours
		assertEquals(INITIAL_COUNT + 3, r.mine.getCount() );
		
		// Bound to two threads
		assertEquals(2, ((ThreadLocalInvokerStats) apartment).getObjects());
	}

}
