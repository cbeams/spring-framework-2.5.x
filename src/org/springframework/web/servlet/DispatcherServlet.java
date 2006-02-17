/*
 * Copyright 2002-2006 the original author or authors.
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.OrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.UrlPathHelper;

/**
 * Central dispatcher for use within the web MVC framework,
 * e.g. for web UI controllers or HTTP-based remote service exporters.
 * Dispatches to registered handlers for processing a web request.
 *
 * <p>This servlet is very flexible: It can be used with just about any workflow,
 * with the installation of the appropriate adapter classes. It offers the
 * following functionality that distinguishes it from other request-driven
 * web MVC frameworks:
 *
 * <ul>
 * <li>It is based around a JavaBeans configuration mechanism.
 *
 * <li>It can use any HandlerMapping implementation - whether standard, or provided
 * as part of an application - to control the routing of requests to handler objects.
 * Default is BeanNameUrlHandlerMapping. HandlerMapping objects can be define as beans
 * in the servlet's application context that implement the HandlerMapping interface.
 * HandlerMappings can be given any bean name (they are tested by type).
 *
 * <li>It can use any HandlerAdapter; this allows to use any handler interface.
 * Default is SimpleControllerHandlerAdapter, for Spring's Controller interface.
 * Additional HandlerAdapter objects can be added through the application context.
 * Like HandlerMappings, HandlerAdapters can be given any bean name (tested by type).
 *
 * <li>Its exception resolution strategy can be specified via a HandlerExceptionResolver,
 * for example mapping certain exceptions to error pages. Default is none.
 * Additional HandlerExceptionResolvers can be added through the application context.
 * HandlerExceptionResolver can be given any bean name (tested by type).
 *
 * <li>Its view resolution strategy can be specified via a ViewResolver implementation,
 * resolving symbolic view names into View objects. Default is InternalResourceViewResolver.
 * Additional ViewResolver objects can be added through the application context.
 * ViewResolvers can be given any bean name (tested by type).
 *
 * <li>Its strategy for resolving multipart requests is determined by a MultipartResolver
 * implementation. Implementations for Jakarta Commons FileUpload and Jason Hunter's COS
 * are included. The MultipartResolver bean name is "multipartResolver"; default is none.
 *
 * <li>Its locale resolution strategy is determined by a LocaleResolver implementation.
 * Out-of-the-box implementations work via HTTP accept header, cookie, or session.
 * The LocaleResolver bean name is "localeResolver"; default is AcceptHeaderLocaleResolver.
 *
 * <li>Its theme resolution strategy is determined by a ThemeResolver implementation.
 * Implementations for a fixed theme and for cookie and session storage are included.
 * The ThemeResolver bean name is "themeResolver"; default is FixedThemeResolver.
 * </ul>
 *
 * <p><b>A web application can use any number of DispatcherServlets.</b> Each servlet
 * will operate in its own namespace. Only the root application context will be shared.
 *
 * <p>This class and the MVC approach it delivers is discussed in Chapter 12 of
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002). Note that it is called <i>ControllerServlet</i> there;
 * it has been renamed since to emphasize its dispatching role and avoid confusion
 * with Controller objects that the DispatcherServlet will dispatch to.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.context.ContextLoaderListener
 * @see HandlerMapping
 * @see org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping
 * @see HandlerAdapter
 * @see org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter
 * @see org.springframework.web.servlet.mvc.Controller
 * @see HandlerExceptionResolver
 * @see org.springframework.web.servlet.handler.SimpleMappingExceptionResolver
 * @see ViewResolver
 * @see org.springframework.web.servlet.view.InternalResourceViewResolver
 * @see MultipartResolver
 * @see org.springframework.web.multipart.commons.CommonsMultipartResolver
 * @see LocaleResolver
 * @see org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
 * @see ThemeResolver
 * @see org.springframework.web.servlet.theme.FixedThemeResolver
 */
public class DispatcherServlet extends FrameworkServlet {

	/**
	 * Well-known name for the MultipartResolver object in the bean factory for this namespace.
	 */
	public static final String MULTIPART_RESOLVER_BEAN_NAME = "multipartResolver";

	/**
	 * Well-known name for the LocaleResolver object in the bean factory for this namespace.
	 */
	public static final String LOCALE_RESOLVER_BEAN_NAME = "localeResolver";

	/**
	 * Well-known name for the ThemeResolver object in the bean factory for this namespace.
	 */
	public static final String THEME_RESOLVER_BEAN_NAME = "themeResolver";

	/**
	 * Well-known name for the HandlerMapping object in the bean factory for this namespace.
	 * Only used when "detectAllHandlerMappings" is turned off.
	 * @see #setDetectAllHandlerMappings
	 */
	public static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";

	/**
	 * Well-known name for the HandlerAdapter object in the bean factory for this namespace.
	 * Only used when "detectAllHandlerAdapters" is turned off.
	 * @see #setDetectAllHandlerAdapters
	 */
	public static final String HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter";

	/**
	 * Well-known name for the HandlerExceptionResolver object in the bean factory for this
	 * namespace. Only used when "detectAllHandlerExceptionResolvers" is turned off.
	 * @see #setDetectAllHandlerExceptionResolvers
	 */
	public static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerExceptionResolver";

	/**
	 * Well-known name for the ViewResolver object in the bean factory for this namespace.
	 * Only used when "detectAllViewResolvers" is turned off.
	 * @see #setDetectAllViewResolvers
	 */
	public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";


	/**
	 * Request attribute to hold the currently chosen HandlerExecutionChain.
	 * Only used for internal optimizations.
	 */
	public static final String HANDLER_EXECUTION_CHAIN_ATTRIBUTE = DispatcherServlet.class.getName() + ".HANDLER";

	/**
	 * Request attribute to hold current web application context.
	 * Otherwise only the global web app context is obtainable by tags etc.
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getWebApplicationContext
	 */
	public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";

	/**
	 * Request attribute to hold current multipart resolver, retrievable by views/binders.
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getMultipartResolver
	 */
	public static final String MULTIPART_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".MULTIPART";

	/**
	 * Request attribute to hold current locale, retrievable by views.
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getLocaleResolver
	 */
	public static final String LOCALE_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".LOCALE";

	/**
	 * Request attribute to hold current theme, retrievable by views.
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getThemeResolver
	 */
	public static final String THEME_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME";


	/**
	 * Log category to use when no mapped handler is found for a request.
	 */
	public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

	/**
	 * Name of the class path resource (relative to the DispatcherServlet class)
	 * that defines DispatcherServlet's default strategy names.
	 */
	private static final String DEFAULT_STRATEGIES_PATH = "DispatcherServlet.properties";


	/**
	 * Additional logger to use when no mapped handler is found for a request.
	 */
	protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);

	private static final Properties defaultStrategies = new Properties();

	static {
		// Load default strategy implementations from properties file.
		// This is currently strictly internal and not meant to be customized
		// by application developers.
		try {
			ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, DispatcherServlet.class);
			InputStream is = resource.getInputStream();
			try {
				defaultStrategies.load(is);
			}
			finally {
				is.close();
			}
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not load 'DispatcherServlet.properties': " + ex.getMessage());
		}
	}


	/** Detect all HandlerMappings or just expect "handlerMapping" bean? */
	private boolean detectAllHandlerMappings = true;

	/** Detect all HandlerAdapters or just expect "handlerAdapter" bean? */
	private boolean detectAllHandlerAdapters = true;

	/** Detect all HandlerExceptionResolvers or just expect "handlerExceptionResolver" bean? */
	private boolean detectAllHandlerExceptionResolvers = true;

	/** Detect all ViewResolvers or just expect "viewResolver" bean? */
	private boolean detectAllViewResolvers = true;

	/** Perform cleanup of request attributes after include request? */
	private boolean cleanupAfterInclude = true;


	/** MultipartResolver used by this servlet */
	private MultipartResolver multipartResolver;

	/** LocaleResolver used by this servlet */
	private LocaleResolver localeResolver;

	/** ThemeResolver used by this servlet */
	private ThemeResolver themeResolver;

	/** List of HandlerMappings used by this servlet */
	private List handlerMappings;

	/** List of HandlerAdapters used by this servlet */
	private List handlerAdapters;

	/** List of HandlerExceptionResolvers used by this servlet */
	private List handlerExceptionResolvers;

	/** List of ViewResolvers used by this servlet */
	private List viewResolvers;


	/**
	 * Set whether to detect all HandlerMapping beans in this servlet's context.
	 * Else, just a single bean with name "handlerMapping" will be expected.
	 * <p>Default is "true". Turn this off if you want this servlet to use a
	 * single HandlerMapping, despite multiple HandlerMapping beans being
	 * defined in the context.
	 */
	public void setDetectAllHandlerMappings(boolean detectAllHandlerMappings) {
		this.detectAllHandlerMappings = detectAllHandlerMappings;
	}

	/**
	 * Set whether to detect all HandlerAdapter beans in this servlet's context.
	 * Else, just a single bean with name "handlerAdapter" will be expected.
	 * <p>Default is "true". Turn this off if you want this servlet to use a
	 * single HandlerAdapter, despite multiple HandlerAdapter beans being
	 * defined in the context.
	 */
	public void setDetectAllHandlerAdapters(boolean detectAllHandlerAdapters) {
		this.detectAllHandlerAdapters = detectAllHandlerAdapters;
	}

	/**
	 * Set whether to detect all HandlerExceptionResolver beans in this servlet's context.
	 * Else, just a single bean with name "handlerExceptionResolver" will be expected.
	 * <p>Default is "true". Turn this off if you want this servlet to use a
	 * single HandlerExceptionResolver, despite multiple HandlerExceptionResolver
	 * beans being defined in the context.
	 */
	public void setDetectAllHandlerExceptionResolvers(boolean detectAllHandlerExceptionResolvers) {
		this.detectAllHandlerExceptionResolvers = detectAllHandlerExceptionResolvers;
	}

	/**
	 * Set whether to detect all ViewResolver beans in this servlet's context.
	 * Else, just a single bean with name "viewResolver" will be expected.
	 * <p>Default is "true". Turn this off if you want this servlet to use a
	 * single ViewResolver, despite multiple ViewResolver beans being
	 * defined in the context.
	 */
	public void setDetectAllViewResolvers(boolean detectAllViewResolvers) {
		this.detectAllViewResolvers = detectAllViewResolvers;
	}

	/**
	 * Set whether to perform cleanup of request attributes after an include request,
	 * i.e. whether to reset the original state of all request attributes after the
	 * DispatcherServlet has processed within an include request. Else, just the
	 * DispatcherServlet's own request attributes will be reset, but not model
	 * attributes for JSPs or special attributes set by views (for example, JSTL's).
	 * <p>Default is "true", which is strongly recommended. Views should not rely on
	 * request attributes having been set by (dynamic) includes. This allows JSP views
	 * rendered by an included controller to use any model attributes, even with the
	 * same names as in the main JSP, without causing side effects. Only turn this
	 * off for special needs, for example to deliberately allow main JSPs to access
	 * attributes from JSP views rendered by an included controller.
	 */
	public void setCleanupAfterInclude(boolean cleanupAfterInclude) {
		this.cleanupAfterInclude = cleanupAfterInclude;
	}


	/**
	 * Overridden method, invoked after any bean properties have been set and the
	 * WebApplicationContext and BeanFactory for this namespace is available.
	 * <p>Loads HandlerMapping and HandlerAdapter objects, and configures a
	 * ViewResolver and a LocaleResolver.
	 */
	protected void initFrameworkServlet() throws ServletException, BeansException {
		initMultipartResolver();
		initLocaleResolver();
		initThemeResolver();
		initHandlerMappings();
		initHandlerAdapters();
		initHandlerExceptionResolvers();
		initViewResolvers();
	}

	/**
	 * Initialize the MultipartResolver used by this class.
	 * If no bean is defined with the given name in the BeanFactory
	 * for this namespace, no multipart handling is provided.
	 */
	private void initMultipartResolver() throws BeansException {
		try {
			this.multipartResolver = (MultipartResolver)
					getWebApplicationContext().getBean(MULTIPART_RESOLVER_BEAN_NAME, MultipartResolver.class);
			if (logger.isInfoEnabled()) {
				logger.info("Using MultipartResolver [" + this.multipartResolver + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Default is no multipart resolver.
			this.multipartResolver = null;
			if (logger.isInfoEnabled()) {
				logger.info("Unable to locate MultipartResolver with name '"	+ MULTIPART_RESOLVER_BEAN_NAME +
						"': no multipart request handling provided");
			}
		}
	}

	/**
	 * Initialize the LocaleResolver used by this class.
	 * If no bean is defined with the given name in the BeanFactory
	 * for this namespace, we default to AcceptHeaderLocaleResolver.
	 */
	private void initLocaleResolver() throws BeansException {
		try {
			this.localeResolver = (LocaleResolver)
					getWebApplicationContext().getBean(LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
			if (logger.isInfoEnabled()) {
				logger.info("Using LocaleResolver [" + this.localeResolver + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			this.localeResolver = (LocaleResolver) getDefaultStrategy(LocaleResolver.class);
			if (logger.isInfoEnabled()) {
				logger.info("Unable to locate LocaleResolver with name '" + LOCALE_RESOLVER_BEAN_NAME +
						"': using default [" + this.localeResolver + "]");
			}
		}
	}

	/**
	 * Initialize the ThemeResolver used by this class.
	 * If no bean is defined with the given name in the BeanFactory
	 * for this namespace, we default to a FixedThemeResolver.
	 */
	private void initThemeResolver() throws BeansException {
		try {
			this.themeResolver = (ThemeResolver)
					getWebApplicationContext().getBean(THEME_RESOLVER_BEAN_NAME, ThemeResolver.class);
			if (logger.isInfoEnabled()) {
				logger.info("Using ThemeResolver [" + this.themeResolver + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			this.themeResolver = (ThemeResolver) getDefaultStrategy(ThemeResolver.class);
			if (logger.isInfoEnabled()) {
				logger.info("Unable to locate ThemeResolver with name '" + THEME_RESOLVER_BEAN_NAME +
						"': using default [" + this.themeResolver + "]");
			}
		}
	}

	/**
	 * Initialize the HandlerMappings used by this class.
	 * If no HandlerMapping beans are defined in the BeanFactory
	 * for this namespace, we default to BeanNameUrlHandlerMapping.
	 */
	private void initHandlerMappings() throws BeansException {
		if (this.detectAllHandlerMappings) {
			// Find all HandlerMappings in the ApplicationContext,
			// including ancestor contexts.
			Map matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					getWebApplicationContext(), HandlerMapping.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerMappings = new ArrayList(matchingBeans.values());
				// We keep HandlerMappings in sorted order.
				Collections.sort(this.handlerMappings, new OrderComparator());
			}
		}
		else {
			try {
				Object hm = getWebApplicationContext().getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
				this.handlerMappings = Collections.singletonList(hm);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerMapping later.
			}
		}

		// Ensure we have at least one HandlerMapping, by registering
		// a default HandlerMapping if no other mappings are found.
		if (this.handlerMappings == null) {
			this.handlerMappings = getDefaultStrategies(HandlerMapping.class);
			if (logger.isInfoEnabled()) {
				logger.info("No HandlerMappings found in servlet '" + getServletName() + "': using default");
			}
		}
	}

	/**
	 * Initialize the HandlerAdapters used by this class.
	 * If no HandlerAdapter beans are defined in the BeanFactory
	 * for this namespace, we default to SimpleControllerHandlerAdapter.
	 */
	private void initHandlerAdapters() throws BeansException {
		if (this.detectAllHandlerAdapters) {
			// Find all HandlerAdapters in the ApplicationContext,
			// including ancestor contexts.
			Map matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					getWebApplicationContext(), HandlerAdapter.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerAdapters = new ArrayList(matchingBeans.values());
				// We keep HandlerAdapters in sorted order.
				Collections.sort(this.handlerAdapters, new OrderComparator());
			}
		}
		else {
			try {
				Object ha = getWebApplicationContext().getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
				this.handlerAdapters = Collections.singletonList(ha);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerAdapter later.
			}
		}

		// Ensure we have at least some HandlerAdapters, by registering
		// default HandlerAdapters if no other adapters are found.
		if (this.handlerAdapters == null) {
			this.handlerAdapters = getDefaultStrategies(HandlerAdapter.class);
			if (logger.isInfoEnabled()) {
				logger.info("No HandlerAdapters found in servlet '" + getServletName() + "': using default");
			}
		}
	}

	/**
	 * Initialize the HandlerExceptionResolver used by this class.
	 * If no bean is defined with the given name in the BeanFactory
	 * for this namespace, we default to no exception resolver.
	 */
	private void initHandlerExceptionResolvers() throws BeansException {
		if (this.detectAllHandlerExceptionResolvers) {
			// Find all HandlerExceptionResolvers in the ApplicationContext,
			// including ancestor contexts.
			Map matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					getWebApplicationContext(), HandlerExceptionResolver.class, true, false);
			this.handlerExceptionResolvers = new ArrayList(matchingBeans.values());
			// We keep HandlerExceptionResolvers in sorted order.
			Collections.sort(this.handlerExceptionResolvers, new OrderComparator());
		}
		else {
			try {
				Object her = getWebApplicationContext().getBean(
						HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
				this.handlerExceptionResolvers = Collections.singletonList(her);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, no HandlerExceptionResolver is fine too.
				this.handlerExceptionResolvers = getDefaultStrategies(HandlerExceptionResolver.class);
			}
		}
	}

	/**
	 * Initialize the ViewResolvers used by this class.
	 * If no ViewResolver beans are defined in the BeanFactory
	 * for this namespace, we default to InternalResourceViewResolver.
	 */
	private void initViewResolvers() throws BeansException {
		if (this.detectAllViewResolvers) {
			// Find all ViewResolvers in the ApplicationContext,
			// including ancestor contexts.
			Map matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					getWebApplicationContext(), ViewResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.viewResolvers = new ArrayList(matchingBeans.values());
				// We keep ViewResolvers in sorted order.
				Collections.sort(this.viewResolvers, new OrderComparator());
			}
		}
		else {
			try {
				Object vr = getWebApplicationContext().getBean(VIEW_RESOLVER_BEAN_NAME, ViewResolver.class);
				this.viewResolvers = Collections.singletonList(vr);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default ViewResolver later.
			}
		}

		// Ensure we have at least one ViewResolver, by registering
		// a default ViewResolver if no other resolvers are found.
		if (this.viewResolvers == null) {
			this.viewResolvers = getDefaultStrategies(ViewResolver.class);
			if (logger.isInfoEnabled()) {
				logger.info("No ViewResolvers found in servlet '" + getServletName() + "': using default");
			}
		}
	}


	/**
	 * Return the default strategy object for the given strategy interface.
	 * <p>Default implementation delegates to <code>getDefaultStrategies</code>,
	 * expecting a single object in the list.
	 * @param strategyInterface the strategy interface
	 * @return the corresponding strategy object
	 * @throws BeansException if initialization failed
	 * @see #getDefaultStrategies
	 */
	protected Object getDefaultStrategy(Class strategyInterface) throws BeansException {
		List strategies = getDefaultStrategies(strategyInterface);
		if (strategies.size() != 1) {
			throw new BeanInitializationException(
					"DispatcherServlet needs exactly 1 strategy for interface [" + strategyInterface.getName() + "]");
		}
		return strategies.get(0);
	}

	/**
	 * Create a List of default strategy objects for the given strategy interface.
	 * <p>The default implementation uses the "DispatcherServlet.properties" file
	 * (in the same package as the DispatcherServlet class) to determine the class names.
	 * It instantiates the strategy objects and satisifies ApplicationContextAware
	 * if necessary.
	 * @param strategyInterface the strategy interface
	 * @return the List of corresponding strategy objects
	 * @throws BeansException if initialization failed
	 */
	protected List getDefaultStrategies(Class strategyInterface) throws BeansException {
		String key = strategyInterface.getName();
		try {
			List strategies = null;
			String value = defaultStrategies.getProperty(key);
			if (value != null) {
				String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
				strategies = new ArrayList(classNames.length);
				for (int i = 0; i < classNames.length; i++) {
					Class clazz = Class.forName(classNames[i], true, getClass().getClassLoader());
					Object strategy = BeanUtils.instantiateClass(clazz);
					if (strategy instanceof ApplicationContextAware) {
						((ApplicationContextAware) strategy).setApplicationContext(getWebApplicationContext());
					}
					strategies.add(strategy);
				}
			}
			else {
				strategies = Collections.EMPTY_LIST;
			}
			return strategies;
		}
		catch (ClassNotFoundException ex) {
			throw new BeanInitializationException(
					"Could not find DispatcherServlet's default strategy class for interface [" + key + "]", ex);
		}
	}


	/**
	 * Expose the DispatcherServlet-specific request attributes and
	 * delegate to <code>doDispatch</code> for the actual dispatching.
	 * @see #doDispatch
	 */
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("DispatcherServlet with name '" + getServletName() + "' received request for [" +
					request.getRequestURI() + "]");
		}

		// Keep a snapshot of the request attributes in case of an include,
		// to be able to restore the original attributes after the include.
		Map attributesSnapshot = null;
		if (request.getAttribute(UrlPathHelper.INCLUDE_URI_REQUEST_ATTRIBUTE) != null) {
			logger.debug("Taking snapshot of request attributes before include");
			attributesSnapshot = new HashMap();
			Enumeration attrNames = request.getAttributeNames();
			while (attrNames.hasMoreElements()) {
				String attrName = (String) attrNames.nextElement();
				if (this.cleanupAfterInclude || attrName.startsWith(DispatcherServlet.class.getName())) {
					attributesSnapshot.put(attrName, request.getAttribute(attrName));
				}
			}
		}

		// Make framework objects available for handlers.
		request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
		request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
		request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);

		try {
			doDispatch(request, response);
		}
		finally {
			// Restore the original attribute snapshot, in case of an include.
			if (attributesSnapshot != null) {
				restoreAttributesAfterInclude(request, attributesSnapshot);
			}
		}
	}

	/**
	 * Process the actual dispatching to the handler.
	 * <p>The handler will be obtained by applying the servlet's HandlerMappings in order.
	 * The HandlerAdapter will be obtained by querying the servlet's installed
	 * HandlerAdapters to find the first that supports the handler class.
	 * <p>All HTTP methods are handled by this method. It's up to HandlerAdapters or
	 * handlers themselves to decide which methods are acceptable.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception in case of any kind of processing failure
	 */
	protected void doDispatch(final HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpServletRequest processedRequest = request;
		HandlerExecutionChain mappedHandler = null;
		int interceptorIndex = -1;

		// Expose current LocaleResolver and request as LocaleContext.
		LocaleContext previousLocaleContext = LocaleContextHolder.getLocaleContext();
		LocaleContextHolder.setLocaleContext(new LocaleContext() {
			public Locale getLocale() {
				return localeResolver.resolveLocale(request);
			}
		});

		try {
			ModelAndView mv = null;
			try {
				processedRequest = checkMultipart(request);

				// Determine handler for the current request.
				mappedHandler = getHandler(processedRequest, false);
				if (mappedHandler == null || mappedHandler.getHandler() == null) {
					noHandlerFound(processedRequest, response);
					return;
				}

				// Apply preHandle methods of registered interceptors.
				if (mappedHandler.getInterceptors() != null) {
					for (int i = 0; i < mappedHandler.getInterceptors().length; i++) {
						HandlerInterceptor interceptor = mappedHandler.getInterceptors()[i];
						if (!interceptor.preHandle(processedRequest, response, mappedHandler.getHandler())) {
							triggerAfterCompletion(mappedHandler, interceptorIndex, processedRequest, response, null);
							return;
						}
						interceptorIndex = i;
					}
				}

				// Actually invoke the handler.
				HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
				mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

				// Apply postHandle methods of registered interceptors.
				if (mappedHandler.getInterceptors() != null) {
					for (int i = mappedHandler.getInterceptors().length - 1; i >= 0; i--) {
						HandlerInterceptor interceptor = mappedHandler.getInterceptors()[i];
						interceptor.postHandle(processedRequest, response, mappedHandler.getHandler(), mv);
					}
				}
			}
			catch (ModelAndViewDefiningException ex) {
				logger.debug("ModelAndViewDefiningException encountered", ex);
				mv = ex.getModelAndView();
			}
			catch (Exception ex) {
				Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
				mv = processHandlerException(request, response, handler, ex);
			}

			// Did the handler return a view to render?
			if (mv != null && !mv.isEmpty()) {
				render(mv, processedRequest, response);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Null ModelAndView returned to DispatcherServlet with name '" +
							getServletName() + "': assuming HandlerAdapter completed request handling");
				}
			}

			// Trigger after-completion for successful outcome.
			triggerAfterCompletion(mappedHandler, interceptorIndex, processedRequest, response, null);
		}

		catch (Exception ex) {
			// Trigger after-completion for thrown exception.
			triggerAfterCompletion(mappedHandler, interceptorIndex, processedRequest, response, ex);
			throw ex;
		}
		catch (Error err) {
			ServletException ex = new NestedServletException("Handler processing failed", err);
			// Trigger after-completion for thrown exception.
			triggerAfterCompletion(mappedHandler, interceptorIndex, processedRequest, response, ex);
			throw ex;
		}

		finally {
			// Clean up any resources used by a multipart request.
			if (processedRequest instanceof MultipartHttpServletRequest && processedRequest != request) {
				this.multipartResolver.cleanupMultipart((MultipartHttpServletRequest) processedRequest);
			}
			// Reset thread-bound LocaleContext.
			LocaleContextHolder.setLocaleContext(previousLocaleContext);
		}
	}

	/**
	 * Override HttpServlet's <code>getLastModified</code> method to evaluate
	 * the Last-Modified value of the mapped handler.
	 */
	protected long getLastModified(HttpServletRequest request) {
		try {
			HandlerExecutionChain mappedHandler = getHandler(request, true);
			if (mappedHandler == null || mappedHandler.getHandler() == null) {
				// Ignore -> will reappear on doService.
				logger.debug("No handler found in getLastModified");
				return -1;
			}

			HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
			long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
			if (logger.isDebugEnabled()) {
				logger.debug("Last-Modified value for [" + request.getRequestURI() + "] is [" + lastModified + "]");
			}
			return lastModified;
		}
		catch (Exception ex) {
			// Ignore -> will reappear on doService.
			logger.debug("Exception thrown in getLastModified", ex);
			return -1;
		}
	}


	/**
	 * Convert the request into a multipart request, and make multipart resolver available.
	 * If no multipart resolver is set, simply use the existing request.
	 * @param request current HTTP request
	 * @return the processed request (multipart wrapper if necessary)
	 */
	protected HttpServletRequest checkMultipart(HttpServletRequest request) throws MultipartException {
		if (this.multipartResolver != null && this.multipartResolver.isMultipart(request)) {
			if (request instanceof MultipartHttpServletRequest) {
				logger.info("Request is already a MultipartHttpServletRequest - if not in a forward, " +
						"this typically results from an additional MultipartFilter in web.xml");
			}
			else {
				request.setAttribute(MULTIPART_RESOLVER_ATTRIBUTE, this.multipartResolver);
				return this.multipartResolver.resolveMultipart(request);
			}
		}
		// If not returned before: return original request.
		return request;
	}

	/**
	 * Return the HandlerExecutionChain for this request.
	 * Try all handler mappings in order.
	 * @param request current HTTP request
	 * @param cache whether to cache the HandlerExecutionChain in a request attribute
	 * @return the HandlerExceutionChain, or <code>null</code> if no handler could be found
	 */
	protected HandlerExecutionChain getHandler(HttpServletRequest request, boolean cache) throws Exception {
		HandlerExecutionChain handler =
				(HandlerExecutionChain) request.getAttribute(HANDLER_EXECUTION_CHAIN_ATTRIBUTE);
		if (handler != null) {
			if (!cache) {
				request.removeAttribute(HANDLER_EXECUTION_CHAIN_ATTRIBUTE);
			}
			return handler;
		}

		Iterator it = this.handlerMappings.iterator();
		while (it.hasNext()) {
			HandlerMapping hm = (HandlerMapping) it.next();
			if (logger.isDebugEnabled()) {
				logger.debug("Testing handler map [" + hm  + "] in DispatcherServlet with name '" +
						getServletName() + "'");
			}
			handler = hm.getHandler(request);
			if (handler != null) {
				if (cache) {
					request.setAttribute(HANDLER_EXECUTION_CHAIN_ATTRIBUTE, handler);
				}
				return handler;
			}
		}
		return null;
	}

	/**
	 * No handler found -> set appropriate HTTP response status.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws IOException if thrown by the HttpServletResponse
	 */
	protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (pageNotFoundLogger.isWarnEnabled()) {
			pageNotFoundLogger.warn("No mapping for [" + request.getRequestURI() +
					"] in DispatcherServlet with name '" + getServletName() + "'");
		}
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	/**
	 * Return the HandlerAdapter for this handler object.
	 * @param handler the handler object to find an adapter for
	 * @throws ServletException if no HandlerAdapter can be found for the handler.
	 * This is a fatal error.
	 */
	protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
		Iterator it = this.handlerAdapters.iterator();
		while (it.hasNext()) {
			HandlerAdapter ha = (HandlerAdapter) it.next();
			if (logger.isDebugEnabled()) {
				logger.debug("Testing handler adapter [" + ha + "]");
			}
			if (ha.supports(handler)) {
				return ha;
			}
		}
		throw new ServletException("No adapter for handler [" + handler +
				"]: Does your handler implement a supported interface like Controller?");
	}

	/**
	 * Determine an error ModelAndView via the registered HandlerExceptionResolvers.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the executed handler, or <code>null</code> if none chosen at the time of
	 * the exception (for example, if multipart resolution failed)
	 * @param ex the exception that got thrown during handler execution
	 * @return a corresponding ModelAndView to forward to
	 * @throws Exception if no error ModelAndView found
	 */
	protected ModelAndView processHandlerException(
			HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

		ModelAndView exMv = null;
		for (Iterator it = this.handlerExceptionResolvers.iterator(); exMv == null && it.hasNext();) {
			HandlerExceptionResolver resolver = (HandlerExceptionResolver) it.next();
			exMv = resolver.resolveException(request, response, handler, ex);
		}
		if (exMv != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Handler execution resulted in exception - forwarding to resolved error view: " + exMv, ex);
			}
			return exMv;
		}
		else {
			throw ex;
		}
	}

	/**
	 * Render the given ModelAndView. This is the last stage in handling a request.
	 * It may involve resolving the view by name.
	 * @param mv the ModelAndView to render
	 * @param request current HTTP servlet request
	 * @param response current HTTP servlet response
	 * @throws Exception if there's a problem rendering the view
	 */
	protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// Determine locale for request and apply it to the response.
		Locale locale = this.localeResolver.resolveLocale(request);
		response.setLocale(locale);

		View view = null;
		if (mv.isReference()) {
			// We need to resolve the view name.
			view = resolveViewName(mv.getViewName(), mv.getModelInternal(), locale, request);
			if (view == null) {
				throw new ServletException("Could not resolve view with name '" + mv.getViewName() +
						"' in servlet with name '" + getServletName() + "'");
			}
		}
		else {
			// No need to lookup: the ModelAndView object contains the actual View object.
			view = mv.getView();
			if (view == null) {
				throw new ServletException("ModelAndView [" + mv + "] neither contains a view name nor a " +
						"View object in servlet with name '" + getServletName() + "'");
			}
		}

		// Delegate to the View object for rendering.
		if (logger.isDebugEnabled()) {
			logger.debug("Rendering view [" + view + "] in DispatcherServlet with name '" + getServletName() + "'");
		}
		view.render(mv.getModelInternal(), request, response);
	}

	/**
	 * Resolve the given view name into a View object (to be rendered).
	 * <p>Default implementations asks all ViewResolvers of this dispatcher.
	 * Can be overridden for custom resolution strategies, potentially based
	 * on specific model attributes or request parameters.
	 * @param viewName the name of the view to resolve
	 * @param model the model to be passed to the view
	 * @param locale the current locale
	 * @param request current HTTP servlet request
	 * @return the View object, or <code>null</code> if none found
	 * @throws Exception if the view cannot be resolved
	 * (typically in case of problems creating an actual View object)
	 * @see ViewResolver#resolveViewName
	 */
	protected View resolveViewName(String viewName, Map model, Locale locale, HttpServletRequest request)
			throws Exception {

		for (Iterator it = this.viewResolvers.iterator(); it.hasNext();) {
			ViewResolver viewResolver = (ViewResolver) it.next();
			View view = viewResolver.resolveViewName(viewName, locale);
			if (view != null) {
				return view;
			}
		}
		return null;
	}

	/**
	 * Trigger afterCompletion callbacks on the mapped HandlerInterceptors.
	 * Will just invoke afterCompletion for all interceptors whose preHandle
	 * invocation has successfully completed and returned true.
	 * @param mappedHandler the mapped HandlerExecutionChain
	 * @param interceptorIndex index of last interceptor that successfully completed
	 * @param ex Exception thrown on handler execution, or <code>null</code> if none
	 * @see HandlerInterceptor#afterCompletion
	 */
	private void triggerAfterCompletion(
			HandlerExecutionChain mappedHandler, int interceptorIndex,
			HttpServletRequest request, HttpServletResponse response, Exception ex)
			throws Exception {

		// Apply afterCompletion methods of registered interceptors.
		if (mappedHandler != null) {
			if (mappedHandler.getInterceptors() != null) {
				for (int i = interceptorIndex; i >= 0; i--) {
					HandlerInterceptor interceptor = mappedHandler.getInterceptors()[i];
					try {
						interceptor.afterCompletion(request, response, mappedHandler.getHandler(), ex);
					}
					catch (Throwable ex2) {
						logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
					}
				}
			}
		}
	}

	/**
	 * Restore the request attributes after an include.
	 * @param request current HTTP request
	 * @param attributesSnapshot the snapshot of the request attributes
	 * before the include
	 */
	private void restoreAttributesAfterInclude(HttpServletRequest request, Map attributesSnapshot) {
		logger.debug("Restoring snapshot of request attributes after include");

		// Need to copy into separate Collection here, to avoid side effects
		// on the Enumeration when removing attributes.
		Set attrsToCheck = new HashSet();
		Enumeration attrNames = request.getAttributeNames();
		while (attrNames.hasMoreElements()) {
			String attrName = (String) attrNames.nextElement();
			if (this.cleanupAfterInclude || attrName.startsWith(DispatcherServlet.class.getName())) {
				attrsToCheck.add(attrName);
			}
		}

		// Iterate over the attributes to check, restoring the original value
		// or removing the attribute, respectively, if appropriate.
		for (Iterator it = attrsToCheck.iterator(); it.hasNext();) {
			String attrName = (String) it.next();
			Object attrValue = attributesSnapshot.get(attrName);
			if (attrValue != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Restoring original value of attribute [" + attrName + "] after include");
				}
				request.setAttribute(attrName, attrValue);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Removing attribute [" + attrName + "] after include");
				}
				request.removeAttribute(attrName);
			}
		}
	}

}
