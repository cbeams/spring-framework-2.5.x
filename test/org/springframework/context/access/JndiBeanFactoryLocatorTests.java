/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.context.access;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.SimpleJndiBeanFactoryLocator;
import org.springframework.beans.factory.access.SimpleJndiBeanFactoryLocatorTests;
import org.springframework.context.ApplicationContext;
import org.springframework.jndi.support.SimpleNamingContextBuilder;

/**
 * @author colin sampaleanu
 * @version $Id: JndiBeanFactoryLocatorTests.java,v 1.1 2004-01-27 00:03:47 colins Exp $
 */
public class JndiBeanFactoryLocatorTests extends SimpleJndiBeanFactoryLocatorTests {

	/**
	 * Override default imple to use JndiBeanFactoryLocator instead of Simple variant
	 */
	private SimpleJndiBeanFactoryLocator createLocator() {
		SimpleJndiBeanFactoryLocator jbfl = new JndiBeanFactoryLocator();
		return jbfl;
	}

	/**
	 * Do an extra test to make sure we are actually working with an ApplicationContext,
	 * not a BeanFactory
	 */
	public void testBeanFactoryPathFromJndiEnvironmentWithSingleFile()
			throws Exception {
		SimpleNamingContextBuilder sncb = SimpleNamingContextBuilder
				.emptyActivatedContextBuilder();

		String path = "/org/springframework/beans/factory/xml/collections.xml";

		// Set up initial context
		sncb.bind(BEAN_FACTORY_PATH_ENVIRONMENT_KEY, path);

		SimpleJndiBeanFactoryLocator jbfl = createLocator();
		BeanFactory bf = jbfl.useFactory(BEAN_FACTORY_PATH_ENVIRONMENT_KEY)
				.getFactory();
		assertTrue(bf instanceof ApplicationContext);
	}

}
