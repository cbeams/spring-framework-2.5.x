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

package org.springframework.autobuilds.integration.util;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Class used to load a web application definition, including possibly triggering
 * loading of a parent definition through a BeanFactoryLocator instance
 * 
 * @author colin
 */
public class ContextLoader
		extends
			org.springframework.web.context.ContextLoader {

	// --- statics

	protected static final Log log = LogFactory.getLog(ContextLoader.class);

	/**
	 * ? Config param for the root WebApplicationContext implementation class to
	 * use.
	 */

	public static final String LOCATOR_FACTORY_SELECTOR = "locatorFactorySelector";
	public static final String BEAN_FACTORY_LOCATOR_FACTORY_KEY = "parentContextKey";

	// --- attributes

	protected BeanFactoryReference _beanFactoryRef = null;

	/**
	 * Overrides method from superclass to implement loading of parent context
	 */
	protected ApplicationContext loadParentContext(ServletContext servletContext)
			throws BeansException {

		ApplicationContext parentContext = null;

		String locatorFactorySelector = servletContext
				.getInitParameter(LOCATOR_FACTORY_SELECTOR);
		String parentContextKey = servletContext
				.getInitParameter(BEAN_FACTORY_LOCATOR_FACTORY_KEY);

		try {
			if (locatorFactorySelector != null) {
				BeanFactoryLocator bfr = ContextSingletonBeanFactoryLocator
						.getInstance(locatorFactorySelector);

				log
						.info("Getting parent context definition: using parent context key of '"
								+ parentContextKey
								+ "' with BeanFactoryLocator");
				_beanFactoryRef = bfr.useBeanFactory(parentContextKey);
				parentContext = (ApplicationContext) _beanFactoryRef
						.getFactory();
			}
		} catch (BeansException ex) {
			throw ex;
		}

		return parentContext;
	}

	/**
	 * Close Spring's web application definition for the given servlet definition.
	 * 
	 * @param servletContext
	 *            current servlet definition
	 */
	public void closeContext(ServletContext servletContext)
			throws ApplicationContextException {
		servletContext.log("Closing root WebApplicationContext");

		WebApplicationContext wac = WebApplicationContextUtils
				.getRequiredWebApplicationContext(servletContext);
		ApplicationContext parent = wac.getParent();
		try {
			if (wac instanceof ConfigurableApplicationContext) {
				((ConfigurableApplicationContext) wac).close();
			}
		} finally {
			if (parent != null && _beanFactoryRef != null)
				_beanFactoryRef.release();
		}
	}

}