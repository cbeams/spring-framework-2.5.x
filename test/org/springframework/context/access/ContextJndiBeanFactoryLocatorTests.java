/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.context.access;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.JndiBeanFactoryLocator;
import org.springframework.beans.factory.access.JndiBeanFactoryLocatorTests;
import org.springframework.context.ApplicationContext;
import org.springframework.jndi.support.SimpleNamingContextBuilder;

/**
 * @author Colin Sampaleanu
 * @version $Id: ContextJndiBeanFactoryLocatorTests.java,v 1.1 2004-02-13 17:54:14 jhoeller Exp $
 */
public class ContextJndiBeanFactoryLocatorTests extends JndiBeanFactoryLocatorTests {

	/**
	 * Override default imple to use JndiBeanFactoryLocator instead of Simple variant
	 */
	private JndiBeanFactoryLocator createLocator() {
		JndiBeanFactoryLocator jbfl = new ContextJndiBeanFactoryLocator();
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

		JndiBeanFactoryLocator jbfl = createLocator();
		BeanFactory bf = jbfl.useBeanFactory(BEAN_FACTORY_PATH_ENVIRONMENT_KEY)
				.getFactory();
		assertTrue(bf instanceof ApplicationContext);
	}

}
