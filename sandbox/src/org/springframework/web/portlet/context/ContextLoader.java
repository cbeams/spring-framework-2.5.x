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

package org.springframework.web.portlet.context;

import javax.portlet.PortletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.portlet.FrameworkPortlet;
import org.springframework.web.portlet.context.support.XmlPortletApplicationContext;

/**
 * Performs the actual initialization work for the root application context.
 * Called by FrameworkPortlet.
 * 
 * <p>Regards a "contextClass" parameter at the web.xml context-param level,
 * falling back to the default context class (XmlPortletApplicationContext) if not found.
 * With the default ContextLoader, a context class needs to implement
 * ConfigurablePortletApplicationContext.
 *
 * <p>Passes a "contextConfigLocation" context-param to the context instance,
 * parsing it into potentially multiple file paths which can be separated by any
 * number of commas and spaces, like "applicationContext1.xml, applicationContext2.xml".
 * If not explicitly specified, the context implementation is supposed to use a
 * default location (with XmlPortletApplicationContext: "/WEB-INF/applicationContext.xml").
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files, at least when using one of
 * Spring's default ApplicationContext implementations. This can be leveraged
 * to deliberately override certain bean definitions via an extra XML file.
 *
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @since 17.02.2003
 * @see FrameworkPortlet
 * @see ConfigurablePortletApplicationContext
 * @see org.springframework.web.portlet.context.support.XmlPortletApplicationContext
 */
public class ContextLoader {

	/**
	 * Config param for the root PortletApplicationContext implementation class to use:
	 * "contextClass"
	 */
	public static final String CONTEXT_CLASS_PARAM = "contextClass";

	/**
	 * Default context class for ContextLoader.
	 * @see org.springframework.web.portlet.context.support.XmlPortletApplicationContext
	 */
	public static final Class DEFAULT_CONTEXT_CLASS = XmlPortletApplicationContext.class;

	/**
	 * Name of servlet context parameter that can specify the config location
	 * for the root context, falling back to DEFAULT_CONFIG_LOCATION.
	 */
	public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";

	private final Log logger = LogFactory.getLog(ContextLoader.class);

	/**
	 * Initialize Spring's portlet application context for the given portlet context,
	 * regarding the "contextClass" and "contextConfigLocation" context-params.
	 * @param portletContext current portlet context
	 * @return the new PortletApplicationContext
	 * @throws BeansException if the context couldn't be initialized
	 * @see #CONTEXT_CLASS_PARAM
	 * @see #CONFIG_LOCATION_PARAM
	 */
	public PortletApplicationContext initPortletApplicationContext(PortletContext portletContext) throws BeansException {
		portletContext.log("Loading root PortletApplicationContext");
		try {
			ApplicationContext parent = loadParentContext(portletContext);
			PortletApplicationContext pac = createPortletApplicationContext(portletContext, parent);
			logger.info("Using context class [" + pac.getClass().getName() + "] for root PortletApplicationContext");
			portletContext.setAttribute(PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE, pac);
			if (logger.isInfoEnabled()) {
				logger.info("Published root PortletApplicationContext [" + pac +
										"] as PortletContext attribute with name [" +
										PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE + "]");
			}
			return pac;
		}
		catch (RuntimeException ex) {
			logger.error("Context initialization failed", ex);
			portletContext.setAttribute(PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE, ex);
			throw ex;
		}
		catch (Error err) {
			logger.error("Context initialization failed", err);
			portletContext.setAttribute(PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE, err);
			throw err;
		}
	}

	/**
	 * Instantiate the root PortletApplicationContext for this loader, either a default
	 * XmlPortletApplicationContext or a custom context class if specified.
	 * This implementation expects custom contexts to implement ConfigurablePortletApplicationContext.
	 * Can be overridden in subclasses.
	 * @throws BeansException if the context couldn't be initialized
	 * @see #CONTEXT_CLASS_PARAM
	 * @see #DEFAULT_CONTEXT_CLASS
	 * @see ConfigurablePortletApplicationContext
	 * @see org.springframework.web.portlet.context.support.XmlPortletApplicationContext
	 */
	protected PortletApplicationContext createPortletApplicationContext(PortletContext portletContext, ApplicationContext parent)
			throws BeansException {
		String contextClassName = portletContext.getInitParameter(CONTEXT_CLASS_PARAM);
		Class contextClass = DEFAULT_CONTEXT_CLASS;
		if (contextClassName != null) {
			try {
				contextClass = Class.forName(contextClassName, true, Thread.currentThread().getContextClassLoader());
			}
			catch (ClassNotFoundException ex) {
				throw new ApplicationContextException("Failed to load context class [" + contextClassName + "]", ex);
			}
			if (!ConfigurablePortletApplicationContext.class.isAssignableFrom(contextClass)) {
				throw new ApplicationContextException(
					"Custom context class [" + contextClassName + "] is not of type ConfigurablePortletApplicationContext");
			}
		}
		ConfigurablePortletApplicationContext cpac =
		    (ConfigurablePortletApplicationContext) BeanUtils.instantiateClass(contextClass);
		cpac.setParent(parent);
		cpac.setPortletContext(portletContext);
		String configLocation = portletContext.getInitParameter(CONFIG_LOCATION_PARAM);
		if (configLocation != null) {
			cpac.setConfigLocations(
				StringUtils.tokenizeToStringArray(
					configLocation, ConfigurablePortletApplicationContext.CONFIG_LOCATION_DELIMITERS, true, true));
		}
		cpac.refresh();
		return cpac;
	}

	/**
	 * Template method which may be overridden by a subclass to load or obtain
	 * an ApplicationContext instance which will be used as the parent context
	 * of the root PortletApplicationContext if it is not null.
	 * @param portletContext
	 * @return the parent application context, or null if none
	 * @throws BeansException if the context couldn't be initialized
	 */
	protected ApplicationContext loadParentContext(PortletContext portletContext) throws BeansException {
		return null;
	}

	/**
	 * Close Spring's portlet application context for the given portlet context.
	 * @param portletContext current portlet context
	 */
	public void closePortletApplicationContext(PortletContext portletContext) throws ApplicationContextException {
		portletContext.log("Closing root PortletApplicationContext");
		Object pac = portletContext.getAttribute(PortletApplicationContext.ROOT_PORTLET_APPLICATION_CONTEXT_ATTRIBUTE);
		if (pac instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) pac).close();
		}
	}

}
