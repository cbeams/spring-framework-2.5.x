/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.ejb.support;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.BootstrapException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.jndi.support.SimpleNamingContextBuilder;

/**
 * @author Rod Johnson
 * @author colin sampaleanu
 * @version $Id: XmlApplicationContextBeanFactoryLoaderTests.java,v 1.1 2004-01-18 00:09:29 colins Exp $
 */
public class XmlApplicationContextBeanFactoryLoaderTests extends TestCase {

	public void testBeanFactoryPathRequiredFromJndiEnvironment() throws Exception {
		// Set up initial context but don't bind anything
		SimpleNamingContextBuilder sncb = SimpleNamingContextBuilder.emptyActivatedContextBuilder();

		XmlApplicationContextBeanFactoryLoader xbfl = new XmlApplicationContextBeanFactoryLoader();
		try {
			xbfl.loadBeanFactory();
			fail();
		}
		catch (BootstrapException ex) {
			// Check for helpful JNDI message
			assertTrue(ex.getMessage().indexOf(XmlBeanFactoryLoader.BEAN_FACTORY_PATH_ENVIRONMENT_KEY) != -1);
		}
	}
	
	public void testBeanFactoryPathFromJndiEnvironmentNotFound() throws Exception  {
		SimpleNamingContextBuilder sncb = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		
		String bogusPath = "/RUBBISH/com/xxxx/framework/server/test1.xml";
	
		// Set up initial context
		sncb.bind(XmlApplicationContextBeanFactoryLoader.BEAN_FACTORY_PATH_ENVIRONMENT_KEY, bogusPath);

		XmlApplicationContextBeanFactoryLoader xbfl = new XmlApplicationContextBeanFactoryLoader();
		try {
			xbfl.loadBeanFactory();
			fail();
		}
		catch (BootstrapException ex) {
			// Check for helpful JNDI message
			assertTrue(ex.getMessage().indexOf(bogusPath) != -1);
		}
	}
	
	public void testBeanFactoryPathFromJndiEnvironmentNotValidXml() throws Exception {
		SimpleNamingContextBuilder sncb = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
	
		String nonXmlPath = "/com/xxxx/framework/server/SlsbEndpointBean.class";

		// Set up initial context
		sncb.bind(XmlApplicationContextBeanFactoryLoader.BEAN_FACTORY_PATH_ENVIRONMENT_KEY, nonXmlPath);

		XmlApplicationContextBeanFactoryLoader xbfl = new XmlApplicationContextBeanFactoryLoader();
		try {
			xbfl.loadBeanFactory();
			fail();
		}
		catch (BootstrapException ex) {
			// Check for helpful JNDI message
			assertTrue(ex.getMessage().indexOf(nonXmlPath) != -1);
		}
	}
	
	public void testBeanFactoryPathFromJndiEnvironmentWithSingleFile() throws Exception {
		SimpleNamingContextBuilder sncb = SimpleNamingContextBuilder.emptyActivatedContextBuilder();

		String path = "/org/springframework/beans/factory/xml/collections.xml";

		// Set up initial context
		sncb.bind(XmlBeanFactoryLoader.BEAN_FACTORY_PATH_ENVIRONMENT_KEY, path);

		XmlApplicationContextBeanFactoryLoader xbfl = new XmlApplicationContextBeanFactoryLoader();
		BeanFactory bf = xbfl.loadBeanFactory();
		assertTrue(bf.containsBean("rod"));
	}

	public void testBeanFactoryPathFromJndiEnvironmentWithMultipleFiles() throws Exception {
		SimpleNamingContextBuilder sncb = SimpleNamingContextBuilder.emptyActivatedContextBuilder();

		String path = "/org/springframework/beans/factory/xml/collections.xml /org/springframework/beans/factory/xml/parent.xml";

		// Set up initial context
		sncb.bind(XmlBeanFactoryLoader.BEAN_FACTORY_PATH_ENVIRONMENT_KEY, path);

		XmlApplicationContextBeanFactoryLoader xbfl = new XmlApplicationContextBeanFactoryLoader();
		BeanFactory bf = xbfl.loadBeanFactory();
		assertTrue(bf.containsBean("rod"));
		assertTrue(bf.containsBean("inheritedTestBean"));
	}

}
