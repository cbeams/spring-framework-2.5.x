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

package org.springframework.web.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.RequestHandledEvent;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Base servlet for servlets within the web framework. Allows integration
 * with an application context, in a JavaBean-based overall solution.
 *
 * <p>This class offers the following functionality:
 * <ul>
 * <li>Uses a WebApplicationContext to access a BeanFactory. The servlet's
 * configuration is determined by beans in the servlet's namespace.
 * <li>Publishes events on request processing, whether or not a request is
 * successfully handled.
 * </ul>
 *
 * <p>Subclasses must implement doService() to handle requests. Because this extends
 * HttpServletBean rather than HttpServlet directly, bean properties are mapped
 * onto it. Subclasses can override initFrameworkServlet() for custom initialization.
 *
 * <p>Regards a "contextClass" parameter at the servlet init-param level,
 * falling back to the default context class (XmlWebApplicationContext) if not found.
 * With the default FrameworkServlet, a context class needs to implement
 * ConfigurableWebApplicationContext.
 *
 * <p>Passes a "contextConfigLocation" servlet init-param to the context instance,
 * parsing it into potentially multiple file paths which can be separated by any
 * number of commas and spaces, like "test-servlet.xml, myServlet.xml".
 * If not explicitly specified, the context implementation is supposed to build a
 * default location from the namespace of the servlet.
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files, at least when using one of
 * Spring's default ApplicationContext implementations. This can be leveraged
 * to deliberately override certain bean definitions via an extra XML file.
 *
 * <p>The default namespace is "'servlet-name'-servlet", e.g. "test-servlet" for a
 * servlet-name "test" (leading to a "/WEB-INF/test-servlet.xml" default location
 * with XmlWebApplicationContext). The namespace can also be set explicitly via
 * the "namespace" servlet init-param.
 *
 * @author Rod Johnson
 * @version $Revision: 1.14 $
 * @see #doService
 * @see #initFrameworkServlet
 * @see #setContextClass
 * @see #setContextConfigLocation
 * @see #setNamespace
 */
public abstract class FrameworkServlet extends HttpServletBean {

	/**
	 * Suffix for namespace bean factory names. If a servlet of this class is
	 * given the name 'test' in a context, the namespace used by the servlet will
	 * resolve to 'test-servlet'.
	 */
	public static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";

	/**
	 * Default context class for FrameworkServlet.
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	public static final Class DEFAULT_CONTEXT_CLASS = XmlWebApplicationContext.class;

	/**
	 * Prefix for the ServletContext attribute for the web application context.
	 * The completion is the servlet name.
	 */
	public static final String SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT.";


	/** Custom context class */
	private Class contextClass = DEFAULT_CONTEXT_CLASS;

	/** Namespace for this servlet */
	private String namespace;

	/** Explicit context config location */
	private String contextConfigLocation;

	/** Should we publish the context as a ServletContext attribute? */
	private boolean publishContext = true;

	/** WebApplicationContext for this servlet */
	private WebApplicationContext webApplicationContext;


	/**
	 * Set a custom context class. This class must be of type WebApplicationContext,
	 * with the default FrameworkServlet implementing ConfigurableWebApplicationContext.
	 * @see #createWebApplicationContext
	 */
	public final void setContextClass(Class contextClass) {
		this.contextClass = contextClass;
	}

	/**
	 * Return the custom context class.
	 */
	public Class getContextClass() {
		return contextClass;
	}

	/**
	 * Set a custom namespace for this servlet,
	 * to be used for building a default context config location.
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * Return the namespace for this servlet, falling back to default scheme if
	 * no custom namespace was set: e.g. "test-servlet" for a servlet named "test".
	 */
	public String getNamespace() {
		return (namespace != null) ? namespace : getServletName() + FrameworkServlet.DEFAULT_NAMESPACE_SUFFIX;
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
	 * Set whether to publish this servlet's context as a ServletContext attribute.
	 * Default is true.
	 * @param publishContext whether we should publish this servlet's
	 * WebApplicationContext as a ServletContext attribute, available to
	 * all objects in this web container. Default is true. This is especially
	 * handy during testing, although it is debatable whether it's good practice
	 * to let other application objects access the context this way.
	 */
	public final void setPublishContext(boolean publishContext) {
		this.publishContext = publishContext;
	}

	/**
	 * Return whether to publish this servlet's context as a ServletContext attribute.
	 */
	public boolean isPublishContext() {
		return publishContext;
	}

	/**
	 * Return this servlet's WebApplicationContext.
	 */
	public final WebApplicationContext getWebApplicationContext() {
		return webApplicationContext;
	}

	/**
	 * Return the ServletContext attribute name for this servlet's
	 * WebApplicationContext.
	 */
	public final String getServletContextAttributeName() {
		return SERVLET_CONTEXT_PREFIX + getServletName();
	}


	/**
	 * Overridden method of HttpServletBean, invoked after any bean properties
	 * have been set. Creates this servlet's WebApplicationContext.
	 */
	protected final void initServletBean() throws ServletException, BeansException {
		long startTime = System.currentTimeMillis();
		if (logger.isInfoEnabled()) {
			logger.info("Framework servlet '" + getServletName() + "' init");
		}

		try {
			this.webApplicationContext = initWebApplicationContext();
			initFrameworkServlet();
		}
		catch (ServletException ex) {
			logger.error("Context initialization failed", ex);
			throw ex;
		}
		catch (BeansException ex) {
			logger.error("Context initialization failed", ex);
			throw ex;
		}

		if (logger.isInfoEnabled()) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			logger.info("Framework servlet '" + getServletName() + "' init completed in " + elapsedTime + " ms");
		}
	}

	/**
	 * Initialize and publish the WebApplicationContext for this servlet.
	 * Delegates to createWebApplicationContext for actual creation.
	 * Can be overridden in subclasses.
	 * @throws BeansException if the context couldn't be initialized
	 * @see #createWebApplicationContext
	 */
	protected WebApplicationContext initWebApplicationContext() throws BeansException {
		getServletContext().log("Initializing WebApplicationContext for servlet '" + getServletName() + "'");
		ServletContext servletContext = getServletConfig().getServletContext();
		WebApplicationContext parent = WebApplicationContextUtils.getWebApplicationContext(servletContext);

		WebApplicationContext wac = createWebApplicationContext(parent);
		if (logger.isInfoEnabled()) {
			logger.info("Using context class '" + wac.getClass().getName() + "' for servlet '" + getServletName() + "'");
		}

		if (this.publishContext) {
			// Publish the context as a servlet context attribute
			String attName = getServletContextAttributeName();
			servletContext.setAttribute(attName, wac);
			if (logger.isInfoEnabled()) {
				logger.info("Published WebApplicationContext of servlet '" + getServletName() +
										"' as ServletContext attribute with name [" + attName + "]");
			}
		}
		return wac;
	}

	/**
	 * Instantiate the WebApplicationContext for this servlet, either a default
	 * XmlWebApplicationContext or a custom context class if set. This implementation
	 * expects custom contexts to implement ConfigurableWebApplicationContext.
	 * Can be overridden in subclasses.
	 * @throws BeansException if the context couldn't be initialized
	 * @see #setContextClass
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent)
			throws BeansException {

		if (logger.isInfoEnabled()) {
			logger.info("Servlet with name '" + getServletName() +
									"' will try to create custom WebApplicationContext context of class '" +
									getContextClass().getName() + "'" + " using parent context [" + parent + "]");
		}
		if (!ConfigurableWebApplicationContext.class.isAssignableFrom(getContextClass())) {
			throw new ApplicationContextException("Fatal initialization error in servlet with name '" +
																						getServletName() + "': custom WebApplicationContext class [" +
																						getContextClass().getName() +
																						"] is not of type ConfigurableWebApplicationContext");
		}

		ConfigurableWebApplicationContext wac =
				(ConfigurableWebApplicationContext) BeanUtils.instantiateClass(getContextClass());
		wac.setParent(parent);
		wac.setServletContext(getServletContext());
		wac.setNamespace(getNamespace());
		if (this.contextConfigLocation != null) {
			wac.setConfigLocations(
			    StringUtils.tokenizeToStringArray(this.contextConfigLocation,
			                                      ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS,
			                                      true, true));
		}
		wac.refresh();
		return wac;
	}

	public void destroy() {
		getServletContext().log("Closing WebApplicationContext for servlet '" + getServletName() + "'");
		if (getWebApplicationContext() instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) getWebApplicationContext()).close();
		}
	}

	/**
	 * It's up to each subclass to decide whether or not it supports a request method.
	 * It should throw a Servlet exception if it doesn't support a particular request type.
	 * This might commonly be done with GET for forms, for example
	 */
	protected final void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
		serviceWrapper(request, response);
	}

	/**
	 * It's up to each subclass to decide whether or not it supports a request method.
	 * It should throw a Servlet exception if it doesn't support a particular request type.
	 * This might commonly be done with GET for forms, for example
	 */
	protected final void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
		serviceWrapper(request, response);
	}

	/**
	 * Handle this request, publishing an event regardless of the outcome.
	 * The actually event handling is performed by the abstract doService() method.
	 * Both doGet() and doPost() are handled by this method.
	 */
	private void serviceWrapper(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

		long startTime = System.currentTimeMillis();
		Exception failureCause = null;
		try {
			doService(request, response);
		}
		catch (ServletException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (IOException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (RuntimeException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (Exception ex) {
			failureCause = ex;
			throw new ServletException(ex.getMessage(), ex);
		}
		finally {
			long processingTime = System.currentTimeMillis() - startTime;
			// whether or not we succeeded, publish an event
			if (failureCause != null) {
				logger.error("Could not complete request", failureCause);
				this.webApplicationContext.publishEvent(
				    new RequestHandledEvent(this, request.getRequestURI(), processingTime, request.getRemoteAddr(),
				                            request.getMethod(), getServletConfig().getServletName(), failureCause));
			}
			else {
				logger.debug("Successfully completed request");
				this.webApplicationContext.publishEvent(
				    new RequestHandledEvent(this, request.getRequestURI(), processingTime, request.getRemoteAddr(),
				                            request.getMethod(), getServletConfig().getServletName()));
			}
		}
	}


	/**
	 * This method will be invoked after any bean properties have been set and
	 * the WebApplicationContext has been loaded. The default implementation is empty;
	 * subclasses may override this method to perform any initialization they require.
	 * @throws ServletException in case of an initialization exception
	 * @throws BeansException if thrown by ApplicationContext methods
	 */
	protected void initFrameworkServlet() throws ServletException, BeansException {
	}

	/**
	 * Subclasses must implement this method to do the work of request handling.
	 * The contract is the same as that for the doGet or doPost methods of HttpServlet.
	 * This class intercepts calls to ensure that event publication takes place.
	 * @see javax.servlet.http.HttpServlet#doGet
	 * @see javax.servlet.http.HttpServlet#doPost
	 * @throws Exception in case of any kind of processing failure
	 */
	protected abstract void doService(HttpServletRequest request, HttpServletResponse response)
	    throws Exception;

}
