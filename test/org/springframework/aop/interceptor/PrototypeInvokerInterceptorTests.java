/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.interceptor;

import java.io.InputStream;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;

/**
 * 
 * @author Rod Johnson
 * @version $Id: PrototypeInvokerInterceptorTests.java,v 1.1 2003-10-06 09:45:52 johnsonr Exp $
 */
public class PrototypeInvokerInterceptorTests extends TestCase {
	
	/** Initial count value set in bean factory XML */
	private static final int INITIAL_COUNT = 10;
	
	private BeanFactory beanFactory;
	
	public PrototypeInvokerInterceptorTests(String s) {
		super(s);
	}
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// Load from classpath, NOT a file path
		InputStream is = getClass().getResourceAsStream("prototypeInvokerTests.xml");
		this.beanFactory = new XmlBeanFactory(is);
	}
	
	
	/**
	 * Test that multiple invocations of the prototype bean will result
	 * in no change to visible state, as a new instance is used.
	 * With the singleton, there will be change.
	 */
	public void testPrototypeAndSingletonBehaveDifferently() {
		SideEffectBean singleton = (SideEffectBean) beanFactory.getBean("singleton");
		assertEquals(INITIAL_COUNT, singleton.getCount() );
		singleton.doWork();
		assertEquals(INITIAL_COUNT + 1, singleton.getCount() );
		
		SideEffectBean prototype = (SideEffectBean) beanFactory.getBean("prototype");
		assertEquals(INITIAL_COUNT, prototype.getCount() );
		singleton.doWork();
		assertEquals(INITIAL_COUNT, prototype.getCount() );
	}


}
