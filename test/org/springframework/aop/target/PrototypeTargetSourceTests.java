/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.target;

import java.io.InputStream;

import junit.framework.TestCase;

import org.springframework.aop.interceptor.SideEffectBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.ClasspathBeanDefinitionRegistryLocation;
import org.springframework.beans.factory.xml.XmlBeanFactory;

/**
 * 
 * @author Rod Johnson
 * @version $Id: PrototypeTargetSourceTests.java,v 1.2 2003-12-19 15:49:58 johnsonr Exp $
 */
public class PrototypeTargetSourceTests extends TestCase {
	
	/** Initial count value set in bean factory XML */
	private static final int INITIAL_COUNT = 10;
	
	private BeanFactory beanFactory;
	
	public PrototypeTargetSourceTests(String s) {
		super(s);
	}
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// Load from classpath, NOT a file path
		InputStream is = getClass().getResourceAsStream("prototypeTests.xml");
		this.beanFactory = new XmlBeanFactory(is, new ClasspathBeanDefinitionRegistryLocation( "prototypeTests.xml"));
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
		prototype.doWork();
		assertEquals(INITIAL_COUNT, prototype.getCount() );
	}


}
