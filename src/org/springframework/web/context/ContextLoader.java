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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Performs the actual initialization work for the root application context.
 * Called by ContextLoaderListener and ContextLoaderServlet.
 *
 * <p>Regards a "contextClass" parameter at the web.xml context-param level,
 * falling back to the default context class (XmlWebApplicationContext) if not found.
 * With the default ContextLoader, a context class needs to implement
 * ConfigurableWebApplicationContext.
 *
 * <p>Passes a "contextConfigLocation" context-param to the context instance,
 * parsing it into potentially multiple file paths which can be separated by any
 * number of commas and spaces, like "applicationContext1.xml, applicationContext2.xml".
 * If not explicitly specified, the context implementation is supposed to use a
 * default location (with XmlWebApplicationContext: "/WEB-INF/applicationContext.xml").
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files, at least when using one of
 * Spring's default ApplicationContext implementations. This can be leveraged
 * to deliberately override certain bean definitions via an extra XML file.
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
	 * Config param for the root WebApplicationContext implementation class to use:
	 * "contextClass"
	 */
	public static final String CONTEXT_CLASS_PARAM = "contextClass";

	/**
	 * Default context class for ContextLoader.
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	public static final Class DEFAULT_CONTEXT_CLASS = XmlWebApplicationContext.class;

	/**
	 * Name of servlet context parameter that can specify the config location
	 * for the root context, falling back to the implementation's default else.
	 * @see org.springframework.web.context.support.XmlWebApplicationContext#DEFAULT_CONFIG_LOCATION
	 */
	public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";

	private final Log logger = LogFactory.getLog(ContextLoader.class);


	/**
	 * Initialize Spring's web application context for the given servlet context,
	 * regarding the "contextClass" and "contextConfigLocation" context-params.
	 * @param servletContext current servlet context
	 * @return the new WebApplicationContext
	 * @throws BeansException if the context couldn't be initialized
	 * @see #CONTEXT_CLASS_PARAM
	 * @see #CONFIG_LOCATION_PARAM
	 */
	public WebApplicationContext initWebApplicationContext(ServletContext servletContext) throws BeansException {
		servletContext.log("Loading root WebApplicationContext");
		try {
			ApplicationContext parent = loadParentContext(servletContext);
			WebApplicationContext wac = createWebApplicationContext(servletContext, parent);
			logger.info("Using context class [" + wac.getClass().getName() + "] for root WebApplicationContext");
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
			if (logger.isInfoEnabled()) {
				logger.info("Published root WebApplicationContext [" + wac +
										"] as ServletContext attribute with name [" +
										WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
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
	 * Instantiate the root WebApplicationContext for this loader, either a default
	 * XmlWebApplicationContext or a custom context class if specified.
	 * This implementation expects custom contexts to implement ConfigurableWebApplicationContext.
	 * Can be overridden in subclasses.
	 * @throws BeansException if the context couldn't be initialized
	 * @see #CONTEXT_CLASS_PARAM
	 * @see #DEFAULT_CONTEXT_CLASS
	 * @see ConfigurableWebApplicationContext
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	protected WebApplicationContext createWebApplicationContext(ServletContext servletContext, ApplicationContext parent)
			throws BeansException {
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
				throw new ApplicationContextException(
					"Custom context class [" + contextClassName + "] is not of type ConfigurableWebApplicationContext");
			}
		}
		ConfigurableWebApplicationContext wac =
		    (ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);
		wac.setParent(parent);
		wac.setServletContext(servletContext);
		String configLocation = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
		if (configLocation != null) {
			wac.setConfigLocations(
				StringUtils.tokenizeToStringArray(
					configLocation, ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS, true, true));
		}
		wac.refresh();
		return wac;
	}

	/**
	 * Template method which may be overridden by a subclass to load or obtain
	 * an ApplicationContext instance which will be used as the parent context
	 * of the root WebApplicationContext if it is not null.
	 * @param servletContext
	 * @return the parent application context, or null if none
	 * @throws BeansException if the context couldn't be initialized
	 */
	protected ApplicationContext loadParentContext(ServletContext servletContext) throws BeansException {
		return null;
	}

	/**
	 * Close Spring's web application context for the given servlet context.
	 * @param servletContext current servlet context
	 */
	public void closeWebApplicationContext(ServletContext servletContext) throws ApplicationContextException {
		servletContext.log("Closing root WebApplicationContext");
		Object wac = servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		if (wac instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) wac).close();
		}
	}

}
