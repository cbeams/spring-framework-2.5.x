/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.access;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.jndi.support.SimpleNamingContextBuilder;

/**
 * @author Rod Johnson
 * @author Colin Sampaleanu
 * @version $Id: JndiBeanFactoryLocatorTests.java,v 1.1 2004-02-13 17:54:14 jhoeller Exp $
 */
public class JndiBeanFactoryLocatorTests extends TestCase {
	
	public static final String BEAN_FACTORY_PATH_ENVIRONMENT_KEY = "java:comp/env/ejb/BeanFactoryPath";

	public void testBeanFactoryPathRequiredFromJndiEnvironment() throws Exception {
		// Set up initial context but don't bind anything
		SimpleNamingContextBuilder sncb = SimpleNamingContextBuilder.emptyActivatedContextBuilder();

		JndiBeanFactoryLocator jbfl = createLocator();
		try {
			jbfl.useBeanFactory(BEAN_FACTORY_PATH_ENVIRONMENT_KEY);
			fail();
		}
		catch (BootstrapException ex) {
			// Check for helpful JNDI message
			assertTrue(ex.getMessage().indexOf(BEAN_FACTORY_PATH_ENVIRONMENT_KEY) != -1);
		}
	}
	
	public void testBeanFactoryPathFromJndiEnvironmentNotFound() throws Exception  {
		SimpleNamingContextBuilder sncb = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		
		String bogusPath = "RUBBISH/com/xxxx/framework/server/test1.xml";
	
		// Set up initial context
		sncb.bind(BEAN_FACTORY_PATH_ENVIRONMENT_KEY, bogusPath);

		JndiBeanFactoryLocator jbfl = createLocator();
		try {
			jbfl.useBeanFactory(BEAN_FACTORY_PATH_ENVIRONMENT_KEY);
			fail();
		}
		catch (BeansException ex) {
			// Check for helpful JNDI message
			assertTrue(ex.getMessage().indexOf(bogusPath) != -1);
		}
	}
	
	public void testBeanFactoryPathFromJndiEnvironmentNotValidXml() throws Exception {
		SimpleNamingContextBuilder sncb = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
	
		String nonXmlPath = "com/xxxx/framework/server/SlsbEndpointBean.class";

		// Set up initial context
		sncb.bind(BEAN_FACTORY_PATH_ENVIRONMENT_KEY, nonXmlPath);

		JndiBeanFactoryLocator jbfl = createLocator();
		try {
			jbfl.useBeanFactory(BEAN_FACTORY_PATH_ENVIRONMENT_KEY);
			fail();
		}
		catch (BeansException ex) {
			// Check for helpful JNDI message
			assertTrue(ex.getMessage().indexOf(nonXmlPath) != -1);
		}
	}
	
	public void testBeanFactoryPathFromJndiEnvironmentWithSingleFile() throws Exception {
		SimpleNamingContextBuilder sncb = SimpleNamingContextBuilder.emptyActivatedContextBuilder();

		String path = "org/springframework/beans/factory/xml/collections.xml";

		// Set up initial context
		sncb.bind(BEAN_FACTORY_PATH_ENVIRONMENT_KEY, path);

		JndiBeanFactoryLocator jbfl = createLocator();
		BeanFactory bf = jbfl.useBeanFactory(BEAN_FACTORY_PATH_ENVIRONMENT_KEY).getFactory();
		assertTrue(bf.containsBean("rod"));
	}

	public void testBeanFactoryPathFromJndiEnvironmentWithMultipleFiles() throws Exception {
		SimpleNamingContextBuilder sncb = SimpleNamingContextBuilder.emptyActivatedContextBuilder();

		String path = "/org/springframework/beans/factory/xml/collections.xml /org/springframework/beans/factory/xml/parent.xml";

		// Set up initial context
		sncb.bind(BEAN_FACTORY_PATH_ENVIRONMENT_KEY, path);

		JndiBeanFactoryLocator jbfl = createLocator();
		BeanFactory bf = jbfl.useBeanFactory(BEAN_FACTORY_PATH_ENVIRONMENT_KEY).getFactory();
		assertTrue(bf.containsBean("rod"));
		assertTrue(bf.containsBean("inheritedTestBean"));
	}

	/**
	 * Allows locator impl to be overriden so we can test subclasses as well
	 * @return
	 */
	private JndiBeanFactoryLocator createLocator() {
		JndiBeanFactoryLocator jbfl = new JndiBeanFactoryLocator();
		return jbfl;
	}
	
}
