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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * 
 * @author Rod Johnson
 * @since 02-Dec-02
 */
public class BeanFactoryBootstrapTests extends TestCase {
	
	private Properties _savedProps;

	/** How to test many singletons? */
	public void testGetInstanceWithNullPropertiesFails() throws BeansException {
		System.setProperties(null);
		BeanFactoryBootstrap.reinitialize();
		try {
			BeanFactoryBootstrap bsb = BeanFactoryBootstrap.getInstance();
			fail("Should have failed with no system properties");
		}
		catch (BootstrapException ex) {
			// OK
		}
	}
	
	public void testGetInstanceWithUnknownBeanFactoryClassFails() throws BeansException {
		System.setProperties(null);
		Properties p = new Properties();
		p.put(BeanFactoryBootstrap.BEAN_FACTORY_BEAN_NAME + ".class",
		"org.springframework.beans.factory.support.xxxxXmlBeanFactory");
		
		System.setProperties(p);
		BeanFactoryBootstrap.reinitialize();
		try {
			BeanFactoryBootstrap bsb = BeanFactoryBootstrap.getInstance();
			fail("Should have failed with invalid class");
		}
		catch (BootstrapException ex) {
			// OK
		}
	}
	
	public void testGetInstanceWithMistypedBeanFactoryClassFails() throws BeansException {
		System.setProperties(null);
		Properties p = new Properties();
		p.put(BeanFactoryBootstrap.BEAN_FACTORY_BEAN_NAME + ".class",
		"java.awt.Point");
		
		System.setProperties(p);
		BeanFactoryBootstrap.reinitialize();
		try {
			BeanFactoryBootstrap bsb = BeanFactoryBootstrap.getInstance();
			fail("Should have failed with mistyped class");
		}
		catch (BootstrapException ex) {
			// OK
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
//	public void testXmlBeanFactory() throws Exception {
//		Properties p = new Properties();
//		p.put(BeanFactoryBootstrap.BEAN_FACTORY_BEAN_NAME + ".class", 
//		"XmlBeanFactory");
//		p.put(BeanFactoryBootstrap.BEAN_FACTORY_BEAN_NAME + ".url", 
//		"c:/checkouts/book/framework/src/org/springframework/beans/factory/support/bs.xml");
//		
//		
//		System.setProperties(p);
//		System.getProperties().list(System.out);
//		
//		BeanFactoryBootstrap.reinitialize();
//
//		try {
//			BeanFactoryBootstrap bsb = BeanFactoryBootstrap.getInstance();
//			
//			BeanFactory bf1 = BeanFactoryBootstrap.getInstance().getBeanFactory();
//			BeanFactory bf2 = BeanFactoryBootstrap.getInstance().getBeanFactory();
//			assertTrue("Two instances identical", bf1==bf2);
//			
//			System.out.println("Got bean factory");
//			assertNotNull("Bsb instance is not null", bsb);
//			TestBean tb = (TestBean) bsb.getBeanFactory().getBean("test");
//			assertNotNull("Test bean is not null", tb);
//			System.out.println(tb);
//			assertTrue("Property set", tb.getFoo().equals("bar"));
//		}
//		catch (Exception ex) {
//			ex.printStackTrace();
//			throw ex;
//		}
//	}
	
	
	public void testDummyBeanFactory() throws Exception {
		Properties p = new Properties();
		p.put(BeanFactoryBootstrap.BEAN_FACTORY_BEAN_NAME + ".class",
		"org.springframework.beans.factory.access.BeanFactoryBootstrapTests$DummyBeanFactory");
		
		
		System.setProperties(p);
		//System.getProperties().list(System.out);
		
		BeanFactoryBootstrap.reinitialize();

		try {
			BeanFactoryBootstrap bsb = BeanFactoryBootstrap.getInstance();
			assertNotNull("Bsb instance is not null", bsb);
			assertTrue("Is dummy", bsb.getBeanFactory() instanceof DummyBeanFactory);
			TestBean tb = (TestBean) bsb.getBeanFactory().getBean("test");
			assertNotNull("Test bean is not null", tb);
			//assertTrue("Property set", tb.getFoo().equals("bar"));
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public static class DummyBeanFactory implements BeanFactory {
		
		public Map m = new HashMap();
		
		 {
			m.put("test", new TestBean());
			m.put("s", new String());
		}
		
		public Object getBean(String name) {
			Object bean = m.get(name);
			if (bean == null)
				throw new NoSuchBeanDefinitionException(name, "no message");
			return bean;
		}

		public Object getBean(String name, Class requiredType) {
			return getBean(name);
		}

		public boolean containsBean(String name) {
			return m.containsKey(name);
		}

		public boolean isSingleton(String name) {
			return true;
		}

		public String[] getAliases(String name) {
			throw new UnsupportedOperationException("getAliases");
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// save and restore System properties, which get destroyed for the tests
		_savedProps = System.getProperties();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		System.setProperties(_savedProps);
	}

}
