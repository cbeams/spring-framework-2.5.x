/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.access;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.SimpleJndiBeanFactoryLocator;
import org.springframework.beans.factory.support.BootstrapException;
import org.springframework.jndi.support.SimpleNamingContextBuilder;

/**
 * @author Rod Johnson
 * @author colin sampaleanu
 * @version $Id: SimpleJndiBeanFactoryLocatorTests.java,v 1.1 2004-01-27 00:03:45 colins Exp $
 */
public class SimpleJndiBeanFactoryLocatorTests extends TestCase {
	
	public static final String BEAN_FACTORY_PATH_ENVIRONMENT_KEY = "java:comp/env/ejb/BeanFactoryPath";

	public void testBeanFactoryPathRequiredFromJndiEnvironment() throws Exception {
		// Set up initial context but don't bind anything
		SimpleNamingContextBuilder sncb = SimpleNamingContextBuilder.emptyActivatedContextBuilder();

		SimpleJndiBeanFactoryLocator jbfl = createLocator();
		try {
			jbfl.useFactory(BEAN_FACTORY_PATH_ENVIRONMENT_KEY);
			fail();
		}
		catch (BootstrapException ex) {
			// Check for helpful JNDI message
			assertTrue(ex.getMessage().indexOf(BEAN_FACTORY_PATH_ENVIRONMENT_KEY) != -1);
		}
	}
	
	public void testBeanFactoryPathFromJndiEnvironmentNotFound() throws Exception  {
		SimpleNamingContextBuilder sncb = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		
		String bogusPath = "/RUBBISH/com/xxxx/framework/server/test1.xml";
	
		// Set up initial context
		sncb.bind(BEAN_FACTORY_PATH_ENVIRONMENT_KEY, bogusPath);

		SimpleJndiBeanFactoryLocator jbfl = createLocator();
		try {
			jbfl.useFactory(BEAN_FACTORY_PATH_ENVIRONMENT_KEY);
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
		sncb.bind(BEAN_FACTORY_PATH_ENVIRONMENT_KEY, nonXmlPath);

		SimpleJndiBeanFactoryLocator jbfl = createLocator();
		try {
			jbfl.useFactory(BEAN_FACTORY_PATH_ENVIRONMENT_KEY);
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
		sncb.bind(BEAN_FACTORY_PATH_ENVIRONMENT_KEY, path);

		SimpleJndiBeanFactoryLocator jbfl = createLocator();
		BeanFactory bf = jbfl.useFactory(BEAN_FACTORY_PATH_ENVIRONMENT_KEY).getFactory();
		assertTrue(bf.containsBean("rod"));
	}

	public void testBeanFactoryPathFromJndiEnvironmentWithMultipleFiles() throws Exception {
		SimpleNamingContextBuilder sncb = SimpleNamingContextBuilder.emptyActivatedContextBuilder();

		String path = "/org/springframework/beans/factory/xml/collections.xml /org/springframework/beans/factory/xml/parent.xml";

		// Set up initial context
		sncb.bind(BEAN_FACTORY_PATH_ENVIRONMENT_KEY, path);

		SimpleJndiBeanFactoryLocator jbfl = createLocator();
		BeanFactory bf = jbfl.useFactory(BEAN_FACTORY_PATH_ENVIRONMENT_KEY).getFactory();
		assertTrue(bf.containsBean("rod"));
		assertTrue(bf.containsBean("inheritedTestBean"));
	}

	/**
	 * Allows locator impl to be overriden so we can test subclasses as well
	 * @return
	 */
	private SimpleJndiBeanFactoryLocator createLocator() {
		SimpleJndiBeanFactoryLocator jbfl = new SimpleJndiBeanFactoryLocator();
		return jbfl;
	}
	
}
