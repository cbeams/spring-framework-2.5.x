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

package org.springframework.web.context;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Performs the actual initialization work for the root application context.
 * Called by ContextLoaderListener and ContextLoaderServlet.
 * 
 * <p>Looks for a "contextClass" parameter at the web.xml context-param level
 * to specify the context class type, falling back to the default of
 * {@link XmlWebApplicationContext} if not found. With the default ContextLoader
 * implementation, any context class specified needs to implement
 * ConfigurableWebApplicationContext.
 *
 * <p>Passes a "contextConfigLocation" context-param to the context instance,
 * parsing it into potentially multiple file paths which can be separated by
 * any number of commas and spaces, like "applicationContext1.xml,
 * applicationContext2.xml". If not explicitly specified, the context
 * implementation is supposed to use a default location (with
 * XmlWebApplicationContext: "/WEB-INF/applicationContext.xml").
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files, at least when using one of
 * Spring's default ApplicationContext implementations. This can be leveraged
 * to deliberately override certain bean definitions via an extra XML file.
 *
 * <p>Above and beyond loading the root application context, this class can
 * optionally load or obtain and hook up a shared parent context to the root
 * application context. See the
 * {@link #loadParentContext(ServletContext)} method for more information.
 *
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @since 17.02.2003
 * @see ContextLoaderListener
 * @see ContextLoaderServlet
 * @see ConfigurableWebApplicationContext
 * @see org.springframework.web.context.support.XmlWebApplicationContext
 */
public class ContextLoader {

	/**
	 * Config param for the root WebApplicationContext implementation class to
	 * use: "contextClass"
	 */
	public static final String CONTEXT_CLASS_PARAM = "contextClass";

	/**
	 * Default context class for ContextLoader.
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	public static final Class DEFAULT_CONTEXT_CLASS = XmlWebApplicationContext.class;

	/**
	 * Name of servlet context parameter that can specify the config location
	 * for the root context, falling back to the implementation's default
	 * otherwise.
	 * @see org.springframework.web.context.support.XmlWebApplicationContext#DEFAULT_CONFIG_LOCATION
	 */
	public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";

	/**
	 * Optional servlet context parameter used only when obtaining a parent
	 * context using the default implementation of
	 * {@link #loadParentContext(ServletContext servletContext)}. Specifies the
	 * 'selector' used in the
	 * {@link ContextSingletonBeanFactoryLocator#getInstance(String selector)}
	 * method call used to obtain the BeanFactoryLocator instance from which the
	 * parent context is obtained.
	 * <p>This will normally be set to <code>classpath*:beanRefContext.xml</code>
	 * to match the default applied for the
	 * {@link ContextSingletonBeanFactoryLocator#getInstance()}method.
	 */
	public static final String LOCATOR_FACTORY_SELECTOR_PARAM = "locatorFactorySelector";

	/**
	 * Optional servlet context parameter used only when obtaining a parent
	 * context using the default implementation of
	 * {@link #loadParentContext(ServletContext servletContext)}. Specifies the
	 * 'factoryKey' used in the
	 * {@link BeanFactoryLocator#useBeanFactory(String factoryKey)}method call
	 * used to obtain the parent application context from the BeanFactoryLocator
	 * instance.
	 */
	public static final String BEAN_FACTORY_LOCATOR_FACTORY_KEY_PARAM = "parentContextKey";


	private final Log logger = LogFactory.getLog(ContextLoader.class);

	/**
	 * Holds BeanFactoryReference when loading parent factory via
	 * ContextSingletonBeanFactoryLocator.
	 */
	protected BeanFactoryReference beanFactoryRef = null;


	/**
	 * Initialize Spring's web application context for the given servlet
	 * context, regarding the "contextClass" and "contextConfigLocation"
	 * context-params.
	 * @param servletContext current servlet context
	 * @return the new WebApplicationContext
	 * @throws BeansException if the context couldn't be initialized
	 * @see #CONTEXT_CLASS_PARAM
	 * @see #CONFIG_LOCATION_PARAM
	 */
	public WebApplicationContext initWebApplicationContext(ServletContext servletContext)
			throws BeansException {

		long startTime = System.currentTimeMillis();
		if (logger.isInfoEnabled()) {
			logger.info("Root WebApplicationContext: initialization started");
		}
		servletContext.log("Loading Spring root WebApplicationContext");

		try {
			// Determine parent for root web application context, if any.
			ApplicationContext parent = loadParentContext(servletContext);

			WebApplicationContext wac = createWebApplicationContext(servletContext, parent);
			servletContext.setAttribute(
					WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);

			if (logger.isInfoEnabled()) {
				logger.info("Using context class [" + wac.getClass().getName() +
						"] for root WebApplicationContext");
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Published root WebApplicationContext [" + wac +
						"] as ServletContext attribute with name [" +
						WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
			}

			if (logger.isInfoEnabled()) {
				long elapsedTime = System.currentTimeMillis() - startTime;
				logger.info("Root WebApplicationContext: initialization completed in " + elapsedTime + " ms");
			}

			return wac;
		}
		catch (RuntimeException ex) {
			logger.error("Context initialization failed", ex);
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ex);
			throw ex;
		}
		catch (Error err) {
			logger.error("Context initialization failed", err);
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, err);
			throw err;
		}
	}

	/**
	 * Instantiate the root WebApplicationContext for this loader, either a
	 * default XmlWebApplicationContext or a custom context class if specified.
	 * <p>This implementation expects custom contexts to implement
	 * ConfigurableWebApplicationContext. Can be overridden in subclasses.
	 * @param servletContext current servlet context
	 * @param parent the parent ApplicationContext to use, or null if none
	 * @throws BeansException if the context couldn't be initialized
	 * @see #CONTEXT_CLASS_PARAM
	 * @see #DEFAULT_CONTEXT_CLASS
	 * @see ConfigurableWebApplicationContext
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	protected WebApplicationContext createWebApplicationContext(
			ServletContext servletContext, ApplicationContext parent) throws BeansException {

		String contextClassName = servletContext.getInitParameter(CONTEXT_CLASS_PARAM);
		Class contextClass = DEFAULT_CONTEXT_CLASS;
		if (contextClassName != null) {
			try {
				contextClass = Class.forName(contextClassName, true, Thread.currentThread().getContextClassLoader());
			}
			catch (ClassNotFoundException ex) {
				throw new ApplicationContextException("Failed to load context class [" + contextClassName + "]", ex);
			}
			if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
				throw new ApplicationContextException("Custom context class [" + contextClassName +
						"] is not of type ConfigurableWebApplicationContext");
			}
		}

		ConfigurableWebApplicationContext wac =
				(ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);
		wac.setParent(parent);
		wac.setServletContext(servletContext);
		String configLocation = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
		if (configLocation != null) {
			wac.setConfigLocations(StringUtils.tokenizeToStringArray(configLocation,
					ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS));
		}

		wac.refresh();
		return wac;
	}

	/**
	 * Template method with default implementation (which may be overridden by a
	 * subclass), to load or obtain an ApplicationContext instance which will be
	 * used as the parent context of the root WebApplicationContext. If the
	 * return value from the method is null, no parent context is set.
	 * <p>The main reason to load a parent context here is to allow multiple root
	 * web application contexts to all be children of a shared EAR context, or
	 * alternately to also share the same parent context that is visible to
	 * EJBs. For pure web applications, there is usually no need to worry about
	 * having a parent context to the root web application context.
	 * <p>The default implementation uses ContextSingletonBeanFactoryLocator,
	 * configured via {@link #LOCATOR_FACTORY_SELECTOR_PARAM} and
	 * {@link #BEAN_FACTORY_LOCATOR_FACTORY_KEY_PARAM}, to load a parent context
	 * which will be shared by all other users of ContextsingletonBeanFactoryLocator
	 * which also use the same configuration parameters.
	 * @param servletContext current servlet context
	 * @return the parent application context, or null if none
	 * @throws BeansException if the context couldn't be initialized
	 * @see org.springframework.beans.factory.access.BeanFactoryLocator
	 * @see org.springframework.context.access.ContextSingletonBeanFactoryLocator
	 */
	protected ApplicationContext loadParentContext(ServletContext servletContext)
			throws BeansException {

		ApplicationContext parentContext = null;

		String locatorFactorySelector = servletContext.getInitParameter(LOCATOR_FACTORY_SELECTOR_PARAM);
		String parentContextKey = servletContext.getInitParameter(BEAN_FACTORY_LOCATOR_FACTORY_KEY_PARAM);

		if (locatorFactorySelector != null) {
			BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance(locatorFactorySelector);

			if (logger.isInfoEnabled()) {
				logger.info("Getting parent context definition: using parent context key of '"
						+ parentContextKey + "' with BeanFactoryLocator");
			}

			this.beanFactoryRef = locator.useBeanFactory(parentContextKey);
			parentContext = (ApplicationContext) this.beanFactoryRef.getFactory();
		}

		return parentContext;
	}

	/**
	 * Close Spring's web application context for the given servlet context. If
	 * the default {@link #loadParentContext(ServletContext)}implementation,
	 * which uses ContextSingletonBeanFactoryLocator, has loaded any shared
	 * parent context, release one reference to that shared parent context.
	 * <p>If overriding {@link #loadParentContext(ServletContext)}, you may have
	 * to override this method as well.
	 * @param servletContext current servlet context
	 */
	public void closeWebApplicationContext(ServletContext servletContext)
			throws ApplicationContextException {

		servletContext.log("Closing Spring root WebApplicationContext");
		Object wac = servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		ApplicationContext parent = null;
		if (wac instanceof ApplicationContext) {
			parent = ((ApplicationContext) wac).getParent();
		}
		try {
			if (wac instanceof ConfigurableApplicationContext) {
				((ConfigurableApplicationContext) wac).close();
			}
		}
		finally {
			if (parent != null && this.beanFactoryRef != null) {
				this.beanFactoryRef.release();
			}
		}
	}

}
