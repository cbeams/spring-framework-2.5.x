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

package org.springframework.web.struts;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Struts 1.1 PlugIn that loads a Spring application context for the Struts
 * ActionServlet. This context will automatically refer to the root
 * WebApplicationContext (loaded by ContextLoaderListener/Servlet) as parent.
 *
 * <p>The default namespace of the WebApplicationContext is the name of the
 * Struts ActionServlet, suffixed with "-servlet" (e.g. "action-servlet").
 * The default location of the XmlWebApplicationContext configuration file
 * is therefore "/WEB-INF/action-servlet.xml".
 *
 * <pre>
 * &lt;plug-in className="org.springframework.web.struts.ContextLoaderPlugIn"/&gt;</pre>
 *
 * The location of the context configuration files can be customized
 * through the "contextConfigLocation" setting, analogous to the root
 * WebApplicationContext and FrameworkServlet contexts.
 *
 * <pre>
 * &lt;plug-in className="org.springframework.web.struts.ContextLoaderPlugIn"&gt;
 *   &lt;set-property property="contextConfigLocation" value="/WEB-INF/action-servlet.xml /WEB-INF/myContext.xml"/&gt;
 * &lt;/plug-in&gt;</pre>
 *
 * Beans defined in the ContextLoaderPlugin context can be accessed
 * from conventional Struts Actions, via fetching the WebApplicationContext
 * reference from the ServletContext. ActionSupport and DispatchActionSupport
 * are pre-built convenience classes that provide easy access to the context.
 *
 * <p>It is normally preferable to access Spring's root WebApplicationContext
 * in such scenarios, though: A shared middle tier should be defined there
 * rather than in a ContextLoaderPlugin context, for access by any web component.
 * ActionSupport and DispatchActionSupport auto-detect the root context too.
 *
 * <p>A special usage of this PlugIn is to define Struts Actions themselves
 * as beans, typically wiring them with middle tier components defined in the
 * root context. Such Actions will then be delegated to by proxy definitions
 * in the Struts configuration, using the DelegatingActionProxy class or
 * the DelegatingRequestProcessor.
 *
 * <p>Note that you can use a single ContextLoaderPlugIn for all Struts modules.
 * That context can in turn be loaded from multiple XML files, for example split
 * according to Struts modules. Alternatively, define one ContextLoaderPlugIn per
 * Struts module, specifying appropriate "contextConfigLocation" parameters.
 *
 * <p>Note: The idea of delegating to Spring-managed Struts Actions originated in
 * Don Brown's <a href="http://struts.sourceforge.net/struts-spring">Spring Struts Plugin</a>.
 * ContextLoaderPlugIn and DelegatingActionProxy constitute a clean-room
 * implementation of the same idea, essentially superseding the original plugin.
 * Many thanks to Don Brown and Matt Raible for the original work, and for the
 * agreement to reimplement the idea in standard Spring!
 *
 * @author Juergen Hoeller
 * @since 05.04.2004
 * @see #SERVLET_CONTEXT_PREFIX
 * @see ActionSupport
 * @see DispatchActionSupport
 * @see DelegatingActionProxy
 * @see DelegatingRequestProcessor
 * @see DelegatingTilesRequestProcessor
 * @see org.springframework.web.context.ContextLoaderListener
 * @see org.springframework.web.context.ContextLoaderServlet
 * @see org.springframework.web.servlet.FrameworkServlet
 */
public class ContextLoaderPlugIn implements PlugIn {

	/**
	 * Suffix for WebApplicationContext namespaces. If a Struts ActionServlet is
	 * given the name "action" in a context, the namespace used by this PlugIn will
	 * resolve to "action-servlet".
	 */
	public static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";

	/**
	 * Default context class for ContextLoaderPlugIn.
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	public static final Class DEFAULT_CONTEXT_CLASS = XmlWebApplicationContext.class;

	/**
	 * Prefix for the ServletContext attribute for the WebApplicationContext.
	 * The completion is the Struts module name.
	 */
	public static final String SERVLET_CONTEXT_PREFIX = ContextLoaderPlugIn.class.getName() + ".CONTEXT.";


	protected final Log logger = LogFactory.getLog(getClass());

	/** Custom WebApplicationContext class */
	private Class contextClass = DEFAULT_CONTEXT_CLASS;

	/** Namespace for this servlet */
	private String namespace;

	/** Explicit context config location */
	private String contextConfigLocation;

	/** The Struts ActionServlet that this PlugIn is registered with */
	private ActionServlet actionServlet;

	/** The Struts ModuleConfig that this PlugIn is registered with */
	private ModuleConfig moduleConfig;

	/** WebApplicationContext for the ActionServlet */
	private WebApplicationContext webApplicationContext;


	/**
	 * Set a custom context class by name. This class must be of type WebApplicationContext,
	 * when using the default ContextLoaderPlugIn implementation, the context class
	 * must also implement ConfigurableWebApplicationContext.
	 * @see #createWebApplicationContext
	 */
	public void setContextClassName(String contextClassName) throws ClassNotFoundException {
		this.contextClass = Class.forName(contextClassName, true, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * Set a custom context class. This class must be of type WebApplicationContext,
	 * when using the default ContextLoaderPlugIn implementation, the context class
	 * must also implement ConfigurableWebApplicationContext.
	 * @see #createWebApplicationContext
	 */
	public void setContextClass(Class contextClass) {
		this.contextClass = contextClass;
	}

	/**
	 * Return the custom context class.
	 */
	public Class getContextClass() {
		return contextClass;
	}

	/**
	 * Set a custom namespace for the ActionServlet,
	 * to be used for building a default context config location.
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * Return the namespace for the ActionServlet, falling back to default scheme if
	 * no custom namespace was set: e.g. "test-servlet" for a servlet named "test".
	 */
	public String getNamespace() {
		if (namespace != null) {
			return namespace;
		}
		if (this.actionServlet != null) {
			return this.actionServlet.getServletName() + DEFAULT_NAMESPACE_SUFFIX;
		}
		return null;
	}

	/**
	 * Set the context config location explicitly, instead of relying on the default
	 * location built from the namespace. This location string can consist of
	 * multiple locations separated by any number of commas and spaces.
	 */
	public void setContextConfigLocation(String contextConfigLocation) {
		this.contextConfigLocation = contextConfigLocation;
	}

	/**
	 * Return the explicit context config location, if any.
	 */
	public String getContextConfigLocation() {
		return contextConfigLocation;
	}


	/**
	 * Create the ActionServlet's WebApplicationContext.
	 */
	public final void init(ActionServlet actionServlet, ModuleConfig moduleConfig) throws ServletException {
		long startTime = System.currentTimeMillis();
		if (logger.isInfoEnabled()) {
			logger.info("Framework servlet '" + actionServlet.getServletName() + "' init");
		}

		this.actionServlet = actionServlet;
		this.moduleConfig = moduleConfig;
		try {
			this.webApplicationContext = initWebApplicationContext();
		}
		catch (RuntimeException ex) {
			logger.error("Context initialization failed", ex);
			throw ex;
		}
		onInit();

		if (logger.isInfoEnabled()) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			logger.info("Framework servlet '" + actionServlet.getServletName() +
									"' init completed in " + elapsedTime + " ms");
		}
	}

	/**
	 * Initialize and publish the WebApplicationContext for the ActionServlet.
	 * Delegates to createWebApplicationContext for actual creation.
	 * <p>Can be overridden in subclasses.
	 * @throws org.springframework.beans.BeansException if the context couldn't be initialized
	 * @throws IllegalStateException if there is already a context for the Struts ActionServlet
	 * @see #createWebApplicationContext
	 */
	protected WebApplicationContext initWebApplicationContext() throws BeansException, IllegalStateException {
		String servletName = this.actionServlet.getServletName();
		this.actionServlet.getServletContext().log("Initializing WebApplicationContext for servlet '" +
																							 servletName + "'");
		ServletContext servletContext = this.actionServlet.getServletContext();
		WebApplicationContext parent = WebApplicationContextUtils.getWebApplicationContext(servletContext);

		WebApplicationContext wac = createWebApplicationContext(parent);
		if (logger.isInfoEnabled()) {
			logger.info("Using context class '" + wac.getClass().getName() + "' for servlet '" + servletName + "'");
		}

		// publish the context as a servlet context attribute
		String modulePrefix = this.moduleConfig.getPrefix();
		String attrName = SERVLET_CONTEXT_PREFIX + modulePrefix;
		servletContext.setAttribute(attrName, wac);
		if (logger.isInfoEnabled()) {
			logger.info("Published WebApplicationContext of servlet '" + servletName + "' for module '" +
									modulePrefix + "' as ServletContext attribute with name [" + attrName + "]");
		}
		return wac;
	}

	/**
	 * Instantiate the WebApplicationContext for the ActionServlet, either a default
	 * XmlWebApplicationContext or a custom context class if set. This implementation
	 * expects custom contexts to implement ConfigurableWebApplicationContext.
	 * Can be overridden in subclasses.
	 * @throws org.springframework.beans.BeansException if the context couldn't be initialized
	 * @see #setContextClass
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent)
			throws BeansException {

		if (logger.isInfoEnabled()) {
			logger.info("Servlet with name '" + this.actionServlet.getServletName() +
									"' will try to create custom WebApplicationContext context of class '" +
									getContextClass().getName() + "'" + " using parent context [" + parent + "]");
		}
		if (!ConfigurableWebApplicationContext.class.isAssignableFrom(getContextClass())) {
			throw new ApplicationContextException("Fatal initialization error in servlet with name '" +
																						this.actionServlet.getServletName() +
																						"': custom WebApplicationContext class [" +
																						getContextClass().getName() +
																						"] is not of type ConfigurableWebApplicationContext");
		}

		ConfigurableWebApplicationContext wac =
				(ConfigurableWebApplicationContext) BeanUtils.instantiateClass(getContextClass());
		wac.setParent(parent);
		wac.setServletContext(this.actionServlet.getServletContext());
		wac.setNamespace(getNamespace());
		if (this.contextConfigLocation != null) {
			wac.setConfigLocations(
			    StringUtils.tokenizeToStringArray(this.contextConfigLocation,
			                                      ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS,
			                                      true, true));
		}
		wac.addBeanFactoryPostProcessor(
				new BeanFactoryPostProcessor() {
					public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
						beanFactory.addBeanPostProcessor(new ActionServletAwareProcessor(actionServlet));
					}
				}
		);
		wac.refresh();
		return wac;
	}

	/**
	 * Return the ActionServlet that this PlugIn is associated with.
	 */
	public final ActionServlet getActionServlet() {
		return actionServlet;
	}

	/**
	 * Return the ActionServlet's WebApplicationContext.
	 */
	public final WebApplicationContext getWebApplicationContext() {
		return webApplicationContext;
	}

	/**
	 * Callback for custom initialization after the context has been set up.
	 * @throws ServletException if initialization failed
	 */
	protected void onInit() throws ServletException {
	}


	/**
	 * Close the WebApplicationContext of the ActionServlet.
	 * @see org.springframework.context.ConfigurableApplicationContext#close
	 */
	public void destroy() {
		this.actionServlet.getServletContext().log("Closing WebApplicationContext of servlet '" +
																							 this.actionServlet.getServletName() + "'");
		if (this.webApplicationContext instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) this.webApplicationContext).close();
		}
	}

}
