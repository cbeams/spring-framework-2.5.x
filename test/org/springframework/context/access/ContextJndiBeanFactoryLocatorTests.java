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

package org.springframework.context.access;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.JndiBeanFactoryLocator;
import org.springframework.beans.factory.access.JndiBeanFactoryLocatorTests;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

/**
 * @author Colin Sampaleanu
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
