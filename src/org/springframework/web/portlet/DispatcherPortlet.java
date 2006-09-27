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

package org.springframework.web.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.UnavailableException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.portlet.context.PortletRequestAttributes;
import org.springframework.web.portlet.multipart.MultipartActionRequest;
import org.springframework.web.portlet.multipart.PortletMultipartResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewRendererServlet;
import org.springframework.web.servlet.ViewResolver;

/**
 * Central dispatcher for use within the Portlet MVC framework, e.g. for web UI
 * controllers. Dispatches to registered handlers for processing a portlet request.
 *
 * <p>This portlet is very flexible: It can be used with just about any workflow,
 * with the installation of the appropriate adapter classes. It offers the
 * following functionality that distinguishes it from other request-driven
 * portlet MVC frameworks:
 *
 * <ul>
 * <li>It is based around a JavaBeans configuration mechanism.
 *
 * <li>It can use any HandlerMapping implementation - whether standard, or provided
 * as part of an application - to control the routing of requests to handler objects.
 * Default is PortletModeHandlerMapping. HandlerMapping objects can be define as beans
 * in the portlet's application context that implement the HandlerMapping interface.
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
 * <li>Its strategy for resolving multipart requests is determined by a
 * PortletMultipartResolver implementation. An implementation for Jakarta Commons
 * FileUpload is included. The MultipartResolver bean name is "portletMultipartResolver";
 * default is none.
 * </ul>
 *
 * <p><b>A web application can use any number of DispatcherPortlets.</b> Each portlet
 * will operate in its own namespace. Only the root application context will be shared.
 *
 * @author William G. Thompson, Jr.
 * @author John A. Lewis
 * @author Juergen Hoeller
 * @author Nick Lothian
 * @author Rainer Schmitz
 * @since 2.0
 * @see org.springframework.web.context.ContextLoaderListener
 * @see HandlerMapping
 * @see org.springframework.web.portlet.handler.PortletModeHandlerMapping
 * @see HandlerAdapter
 * @see org.springframework.web.portlet.mvc.SimpleControllerHandlerAdapter
 * @see org.springframework.web.portlet.mvc.Controller
 * @see HandlerExceptionResolver
 * @see org.springframework.web.portlet.handler.SimpleMappingExceptionResolver
 * @see org.springframework.web.servlet.ViewResolver
 * @see org.springframework.web.servlet.view.InternalResourceViewResolver
 * @see org.springframework.web.servlet.ViewRendererServlet
 */
public class DispatcherPortlet extends FrameworkPortlet {

	/**
	 * Well-known name for the PortletMultipartResolver object in the bean factory for this namespace.
	 */
	public static final String MULTIPART_RESOLVER_BEAN_NAME = "portletMultipartResolver";

	/**
	 * Well-known name for the HandlerMapping object in the bean factory for this namespace.
	 * Only used when "detectAllHandlerMappings" is turned off.
	 * @see #setDetectAllViewResolvers
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
	 * @see #setDetectAllViewResolvers
	 */
	public static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerExceptionResolver";

	/**
	 * Well-known name for the ViewResolver object in the bean factory for this namespace.
	 */
	public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";

	/**
	 * Default URL to ViewRendererServlet. This bridge servlet is used to convert
	 * portlet render requests to servlet requests in order to leverage the view support
	 * in the <code>org.springframework.web.view</code> package.
	 */
	public static final String DEFAULT_VIEW_RENDERER_URL = "/WEB-INF/servlet/view";

	/**
	 * Request attribute to hold the currently chosen HandlerExecutionChain.
	 * Only used for internal optimizations.
	 */
	public static final String HANDLER_EXECUTION_CHAIN_ATTRIBUTE =
			DispatcherPortlet.class.getName() + ".HANDLER";

	/**
	 * Unlike the Servlet version of this class, we have to deal with the
	 * two-phase nature of the portlet request. To do this, we need to pass
	 * forward any exception that occurs during the action phase, so that
	 * it can be displayed in the render phase. The only direct way to pass
	 * things forward and preserve them for each render request is through
	 * render parameters, but these are limited to String objects and we need
	 * to pass the Exception itself. The only other way to do this is in the
	 * session. The bad thing about using the session is that we have no way
	 * of knowing when we are done re-rendering the request and so we don't
	 * know when we can remove the objects from the session. So we will end
	 * up polluting the session with an old exception when we finally leave
	 * the render phase of one request and move on to something else.
	 */
	public static final String ACTION_EXCEPTION_SESSION_ATTRIBUTE =
			DispatcherPortlet.class.getName() + ".ACTION_EXCEPTION";

	/**
	 * This render parameter is used to indicate forward to the render phase
	 * that an exception occurred during the action phase.
	 */
	public static final String ACTION_EXCEPTION_RENDER_PARAMETER = "actionException";

	/**
	 * Log category to use when no mapped handler is found for a request.
	 */
	public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.portlet.PageNotFound";

	/**
	 * Name of the class path resource (relative to the DispatcherPortlet class)
	 * that defines DispatcherPortet's default strategy names.
	 */
	private static final String DEFAULT_STRATEGIES_PATH = "DispatcherPortlet.properties";


	/**
	 * Additional logger to use when no mapped handler is found for a request.
	 */
	protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);

	private static final Properties defaultStrategies;

	static {
		// Load default strategy implementations from properties file.
		// This is currently strictly internal and not meant to be customized
		// by application developers.
		try {
			ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, DispatcherPortlet.class);
			defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not load 'DispatcherPortlet.properties': " + ex.getMessage());
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

	/** URL that points to the ViewRendererServlet */
	private String viewRendererUrl = DEFAULT_VIEW_RENDERER_URL;


	/** MultipartResolver used by this servlet */
	private PortletMultipartResolver multipartResolver;

	/** List of HandlerMappings used by this portlet */
	private List handlerMappings;

	/** List of HandlerAdapters used by this portlet */
	private List handlerAdapters;

	/** List of HandlerExceptionResolvers used by this portlet */
	private List handlerExceptionResolvers;

	/** List of ViewResolvers used by this portlet */
	private List viewResolvers;


	/**
	 * Set whether to detect all HandlerMapping beans in this portlet's context.
	 * Else, just a single bean with name "handlerMapping" will be expected.
	 * <p>Default is true. Turn this off if you want this portlet to use a
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
	 * Set whether to detect all HandlerExceptionResolver beans in this portlet's context.
	 * Else, just a single bean with name "handlerExceptionResolver" will be expected.
	 * <p>Default is true. Turn this off if you want this portlet to use a
	 * single HandlerExceptionResolver, despite multiple HandlerExceptionResolver
	 * beans being defined in the context.
	 */
	public void setDetectAllHandlerExceptionResolvers(boolean detectAllHandlerExceptionResolvers) {
		this.detectAllHandlerExceptionResolvers = detectAllHandlerExceptionResolvers;
	}

	/**
	 * Set whether to detect all ViewResolver beans in this portlet's context.
	 * Else, just a single bean with name "viewResolver" will be expected.
	 * <p>Default is true. Turn this off if you want this portlet to use a
	 * single ViewResolver, despite multiple ViewResolver beans being
	 * defined in the context.
	 */
	public void setDetectAllViewResolvers(boolean detectAllViewResolvers) {
		this.detectAllViewResolvers = detectAllViewResolvers;
	}

	/**
	 * Set the ViewRendererServlet.  This servlet is used to ultimately render
	 * all views in the portlet application.
	 */
	public void setViewRendererUrl(String viewRendererUrl) {
		this.viewRendererUrl = viewRendererUrl;
	}


	/**
	 * Overridden method, invoked after any bean properties have been set and the
	 * PortletApplicationContext and BeanFactory for this namespace is available.
	 * <p>Loads HandlerMapping and HandlerAdapter objects, and configures a
	 * ViewResolver and a LocaleResolver.
	 */
	protected void initFrameworkPortlet() throws PortletException, BeansException {
		initMultipartResolver();
		initHandlerMappings();
		initHandlerAdapters();
		initHandlerExceptionResolvers();
		initViewResolvers();
	}

	/**
	 * Initialize the PortletMultipartResolver used by this class.
	 * If no valid bean is defined with the given name in the BeanFactory
	 * for this namespace, no multipart handling is provided.
	 */
	private void initMultipartResolver() throws BeansException {
		try {
			this.multipartResolver = (PortletMultipartResolver)
					getPortletApplicationContext().getBean(MULTIPART_RESOLVER_BEAN_NAME, PortletMultipartResolver.class);
			if (logger.isInfoEnabled()) {
				logger.info("Using MultipartResolver [" + this.multipartResolver + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Default is no multipart resolver.
			this.multipartResolver = null;
			if (logger.isInfoEnabled()) {
				logger.info("Unable to locate PortletMultipartResolver with name '"	+ MULTIPART_RESOLVER_BEAN_NAME +
						"': no multipart request handling provided");
			}
		}
	}

	/**
	 * Initialize the HandlerMappings used by this class.
	 * If no HandlerMapping beans are defined in the BeanFactory
	 * for this namespace, we default to PortletModeHandlerMapping.
	 */
	private void initHandlerMappings() throws BeansException {
		if (this.detectAllHandlerMappings) {
			// Find all HandlerMappings in the ApplicationContext,
			// including ancestor contexts.
			Map matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					getPortletApplicationContext(), HandlerMapping.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerMappings = new ArrayList(matchingBeans.values());
				// We keep HandlerMappings in sorted order.
				Collections.sort(this.handlerMappings, new OrderComparator());
			}
		}
		else {
			try {
				Object hm = getPortletApplicationContext().getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
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
				logger.info("No HandlerMappings found in portlet '" + getPortletName() + "': using default");
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
					getPortletApplicationContext(), HandlerAdapter.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerAdapters = new ArrayList(matchingBeans.values());
				// We keep HandlerAdapters in sorted order.
				Collections.sort(this.handlerAdapters, new OrderComparator());
			}
		}
		else {
			try {
				Object ha = getPortletApplicationContext().getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
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
				logger.info("No HandlerAdapters found in portlet '" + getPortletName() + "': using default");
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
					getPortletApplicationContext(), HandlerExceptionResolver.class, true, false);
			this.handlerExceptionResolvers = new ArrayList(matchingBeans.values());
			// We keep HandlerExceptionResolvers in sorted order.
			Collections.sort(this.handlerExceptionResolvers, new OrderComparator());
		}
		else {
			try {
				Object her = getPortletApplicationContext().getBean(
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
					getPortletApplicationContext(), ViewResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.viewResolvers = new ArrayList(matchingBeans.values());
				// We keep ViewResolvers in sorted order.
				Collections.sort(this.viewResolvers, new OrderComparator());
			}
		}
		else {
			try {
				Object vr = getPortletApplicationContext().getBean(VIEW_RESOLVER_BEAN_NAME, ViewResolver.class);
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
				logger.info("No ViewResolvers found in portlet '" + getPortletName() + "': using default");
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
					"DispatcherPortlet needs exactly 1 strategy for interface [" + strategyInterface.getName() + "]");
		}
		return strategies.get(0);
	}

	/**
	 * Create a List of default strategy objects for the given strategy interface.
	 * <p>The default implementation uses the "DispatcherPortlet.properties" file
	 * (in the same package as the DispatcherPortlet class) to determine the class names.
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
					Class clazz = ClassUtils.forName(classNames[i], getClass().getClassLoader());
					Object strategy = createDefaultStrategy(clazz);
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
					"Could not find DispatcherPortlet's default strategy class for interface [" + key + "]", ex);
		}
	}

	/**
	 * Create a default strategy.
	 * Default implementation uses <code>ApplicationContext.createBean</code>.
	 * @param clazz the strategy implementation class to instantiate
	 * @return the fully configured strategy instance
	 * @see #getPortletApplicationContext()
	 * @see org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#createBean
	 */
	protected Object createDefaultStrategy(Class clazz) {
		return getPortletApplicationContext().getAutowireCapableBeanFactory().createBean(
				clazz, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
	}


	/**
	 * Process the actual dispatching to the handler for action requests.
	 * <p>The handler will be obtained by applying the portlet's HandlerMappings in order.
	 * The HandlerAdapter will be obtained by querying the portlet's installed
	 * HandlerAdapters to find the first that supports the handler class.
	 * @param request current portlet action request
	 * @param response current portlet Action response
	 * @throws Exception in case of any kind of processing failure
	 */
	protected void doActionService(ActionRequest request, ActionResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("DispatcherPortlet with name '" + getPortletName() + "' received action request");
		}

		ActionRequest processedRequest = request;
		HandlerExecutionChain mappedHandler = null;
		int interceptorIndex = -1;

		// Expose current Locale as LocaleContext.
		LocaleContextHolder.setLocaleContext(new SimpleLocaleContext(request.getLocale()));

		// Expose current RequestAttributes to current thread.
		PortletRequestAttributes requestAttributes = new PortletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(requestAttributes);

		if (logger.isDebugEnabled()) {
			logger.debug("Bound request context to thread: " + request);
		}

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
					if (!interceptor.preHandleAction(processedRequest, response, mappedHandler.getHandler())) {
						triggerAfterActionCompletion(mappedHandler, interceptorIndex, processedRequest, response, null);
						return;
					}
					interceptorIndex = i;
				}
			}

			// Actually invoke the handler.
			HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
			ha.handleAction(processedRequest, response, mappedHandler.getHandler());

			// Trigger after-completion for successful outcome.
			triggerAfterActionCompletion(mappedHandler, interceptorIndex, processedRequest, response, null);
		}

		catch (Exception ex) {
			// Trigger after-completion for thrown exception.
			triggerAfterActionCompletion(mappedHandler, interceptorIndex, processedRequest, response, ex);
			// Forward the exception to the render phase to be displayed.
			logger.debug("Caught exception during action phase - forwarding to render phase", ex);
			PortletSession session = request.getPortletSession();
			session.setAttribute(ACTION_EXCEPTION_SESSION_ATTRIBUTE, ex);
			response.setRenderParameter(ACTION_EXCEPTION_RENDER_PARAMETER, ex.toString());
		}
		catch (Error err) {
			PortletException ex =
					new PortletException("Error occured during request processing: " + err.getMessage(), err);
			// Trigger after-completion for thrown exception.
			triggerAfterActionCompletion(mappedHandler, interceptorIndex, processedRequest, response, ex);
			throw ex;
		}

		finally {
			// Clean up any resources used by a multipart request.
			if (processedRequest instanceof MultipartActionRequest && processedRequest != request) {
				this.multipartResolver.cleanupMultipart((MultipartActionRequest) processedRequest);
			}

			// Reset thread-bound RequestAttributes.
			requestAttributes.requestCompleted();
			RequestContextHolder.setRequestAttributes(null);

			// Reset thread-bound LocaleContext.
			LocaleContextHolder.setLocaleContext(null);

			if (logger.isDebugEnabled()) {
				logger.debug("Cleared thread-bound request context: " + request);
			}
		}
	}

	/**
	 * Process the actual dispatching to the handler for render requests.
	 * <p>The handler will be obtained by applying the portlet's HandlerMappings in order.
	 * The HandlerAdapter will be obtained by querying the portlet's installed
	 * HandlerAdapters to find the first that supports the handler class.
	 * @param request current portlet render request
	 * @param response current portlet render response
	 * @throws Exception in case of any kind of processing failure
	 */
	protected void doRenderService(RenderRequest request, RenderResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("DispatcherPortlet with name '" + getPortletName() + "' received render request");
		}

		RenderRequest processedRequest = request;
		HandlerExecutionChain mappedHandler = null;
		int interceptorIndex = -1;

		// Expose current Locale as LocaleContext.
		LocaleContextHolder.setLocaleContext(new SimpleLocaleContext(request.getLocale()));

		try {
			ModelAndView mv = null;
			try {
				// Check for forwarded exception from the action phase
				PortletSession session = request.getPortletSession(false);
				if (session != null) {
					if (request.getParameter(ACTION_EXCEPTION_RENDER_PARAMETER) != null) {
						Exception ex = (Exception) session.getAttribute(ACTION_EXCEPTION_SESSION_ATTRIBUTE);
						if (ex != null) {
							logger.debug("Render phase found exception caught during action phase - rethrowing it");
							throw ex;
						}
					}
					else {
						session.removeAttribute(ACTION_EXCEPTION_SESSION_ATTRIBUTE);
					}
				}
				
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
						if (!interceptor.preHandleRender(processedRequest, response, mappedHandler.getHandler())) {
							triggerAfterRenderCompletion(mappedHandler, interceptorIndex, processedRequest, response, null);
							return;
						}
						interceptorIndex = i;
					}
				}

				// Actually invoke the handler.
				HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
				mv = ha.handleRender(processedRequest, response, mappedHandler.getHandler());

				// Apply postHandle methods of registered interceptors.
				if (mappedHandler.getInterceptors() != null) {
					for (int i = mappedHandler.getInterceptors().length - 1; i >= 0; i--) {
						HandlerInterceptor interceptor = mappedHandler.getInterceptors()[i];
						interceptor.postHandleRender(processedRequest, response, mappedHandler.getHandler(), mv);
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
					logger.debug("Null ModelAndView returned to DispatcherPortlet with name '" +
							getPortletName() + "': assuming HandlerAdapter completed request handling");
				}
			}

			// Trigger after-completion for successful outcome.
			triggerAfterRenderCompletion(mappedHandler, interceptorIndex, processedRequest, response, null);
		}

		catch (Exception ex) {
			// Trigger after-completion for thrown exception.
			triggerAfterRenderCompletion(mappedHandler, interceptorIndex, processedRequest, response, ex);
			throw ex;
		}
		catch (Error err) {
			PortletException ex =
					new PortletException("Error occured during request processing: " + err.getMessage(), err);
			// Trigger after-completion for thrown exception.
			triggerAfterRenderCompletion(mappedHandler, interceptorIndex, processedRequest, response, ex);
			throw ex;
		}

		finally {
			// Reset thread-bound LocaleContext.
			LocaleContextHolder.setLocaleContext(null);
		}
	}


	/**
	 * Convert the request into a multipart request, and make multipart resolver available.
	 * If no multipart resolver is set, simply use the existing request.
	 * @param request current HTTP request
	 * @return the processed request (multipart wrapper if necessary)
	 */
	protected ActionRequest checkMultipart(ActionRequest request) throws MultipartException {
		if (this.multipartResolver != null && this.multipartResolver.isMultipart(request)) {
			if (request instanceof MultipartActionRequest) {
				logger.debug("Request is already a MultipartActionRequest - probably in a forward");
			}
			else {
				return this.multipartResolver.resolveMultipart(request);
			}
		}
		// If not returned before: return original request.
		return request;
	}

	/**
	 * Return the HandlerExecutionChain for this request.
	 * Try all handler mappings in order.
	 * @param request current portlet request
	 * @param cache whether to cache the HandlerExecutionChain in a request attribute
	 * @return the HandlerExceutionChain, or null if no handler could be found
	 */
	protected HandlerExecutionChain getHandler(PortletRequest request, boolean cache) throws Exception {
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
				logger.debug("Testing handler map [" + hm + "] in DispatcherPortlet with name '" +
						getPortletName() + "'");
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
	 * No handler found -> throw appropriate exception.
	 * @param request current portlet request
	 * @param response current portlet response
	 */
	protected void noHandlerFound(PortletRequest request, PortletResponse response) throws PortletException {
		if (pageNotFoundLogger.isWarnEnabled()) {
			pageNotFoundLogger.warn("No mapping found for current request " +
					"in DispatcherPortlet with name '" + getPortletName() + "'" +
					" mode '" + request.getPortletMode() + "'" +
					" type '" + (request instanceof ActionRequest ? "action" : "render") + "'" +
					" session '" + request.getRequestedSessionId() + "'" +
					" user '" + getUsernameForRequest(request) + "'");
		}
		throw new UnavailableException("No handler found for request");
	}

	/**
	 * Return the HandlerAdapter for this handler object.
	 * @param handler the handler object to find an adapter for
	 * @throws PortletException if no HandlerAdapter can be found for the handler.
	 * This is a fatal error.
	 */
	protected HandlerAdapter getHandlerAdapter(Object handler) throws PortletException {
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
		throw new PortletException("No adapter for handler [" + handler +
				"]: Does your handler implement a supported interface like Controller?");
	}

	/**
	 * Determine an error ModelAndView via the registered HandlerExceptionResolvers.
	 * @param request current portlet request
	 * @param response current portlet response
	 * @param handler the executed handler, or null if none chosen at the time of
	 * the exception (for example, if multipart resolution failed)
	 * @param ex the exception that got thrown during handler execution
	 * @return a corresponding ModelAndView to forward to
	 * @throws Exception if no error ModelAndView found
	 */
	protected ModelAndView processHandlerException(
			RenderRequest request, RenderResponse response, Object handler, Exception ex)
			throws Exception {

		ModelAndView exMv = null;
		for (Iterator it = this.handlerExceptionResolvers.iterator(); exMv == null && it.hasNext();) {
			HandlerExceptionResolver resolver = (HandlerExceptionResolver) it.next();
			exMv = resolver.resolveException(request, response, handler, ex);
		}
		if (exMv != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("HandlerExceptionResolver returned ModelAndView [" + exMv + "] for exception");
			}
			logger.warn("Handler execution resulted in exception - forwarding to resolved error view", ex);
			return exMv;
		}
		else {
			throw ex;
		}
	}

	/**
	 * Trigger afterCompletion callbacks on the mapped HandlerInterceptors.
	 * Will just invoke afterCompletion for all interceptors whose preHandle
	 * invocation has successfully completed and returned true.
	 * @param mappedHandler the mapped HandlerExecutionChain
	 * @param interceptorIndex index of last interceptor that successfully completed
	 * @param ex Exception thrown on handler execution, or null if none
	 * @see HandlerInterceptor#afterRenderCompletion
	 */
	private void triggerAfterActionCompletion(HandlerExecutionChain mappedHandler, int interceptorIndex,
			ActionRequest request, ActionResponse response, Exception ex)
			throws Exception {

		// Apply afterCompletion methods of registered interceptors.
		if (mappedHandler != null) {
			if (mappedHandler.getInterceptors() != null) {
				for (int i = interceptorIndex; i >= 0; i--) {
					HandlerInterceptor interceptor = mappedHandler.getInterceptors()[i];
					try {
						interceptor.afterActionCompletion(request, response, mappedHandler.getHandler(), ex);
					}
					catch (Throwable ex2) {
						logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
					}
				}
			}
		}
	}


	/**
	 * Render the given ModelAndView. This is the last stage in handling a request.
	 * It may involve resolving the view by name.
	 * @param mv the ModelAndView to render
	 * @param request current portlet render request
	 * @param response current portlet render response
	 * @throws Exception if there's a problem rendering the view
	 */
	protected void render(ModelAndView mv, RenderRequest request, RenderResponse response) throws Exception {
		View view = null;
		if (mv.isReference()) {
			// We need to resolve the view name.
			view = resolveViewName(mv.getViewName(), mv.getModelInternal(), request);
			if (view == null) {
				throw new PortletException("Could not resolve view with name '" + mv.getViewName() +
						"' in portlet with name '" + getPortletName() + "'");
			}
		}
		else {
			// No need to lookup: the ModelAndView object contains the actual View object.
			Object viewObject = mv.getView();
			if (viewObject == null) {
				throw new PortletException("ModelAndView [" + mv + "] neither contains a view name nor a " +
						"View object in portlet with name '" + getPortletName() + "'");
			}
			if (!(viewObject instanceof View)) {
				throw new PortletException(
						"View object [" + viewObject + "] is not an instance of [org.springframework.web.servlet.View] - " +
						"DispatcherPortlet does not support any other view types");
			}
			view = (View) viewObject;
		}

		if (view == null) {
			throw new PortletException("Could not resolve view with name '" + mv.getViewName() +
					"' in portlet with name '" + getPortletName() + "'");
		}

		// Set the content type on the response if needed and if possible.
		// The Portlet spec requires the content type to be set on the RenderResponse;
		// it's not sufficient to let the View set it on the ServletResponse.
		if (response.getContentType() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Portlet response content type already set to [" + response.getContentType() + "]");
			}
		}
		else {
			// No Portlet content type specified yet -> use the view-determined type.
			String contentType = view.getContentType();
			if (contentType != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Setting portlet response content type to view-determined type [" + contentType + "]");
				}
				response.setContentType(contentType);
			}
		}

		// Expose Portlet ApplicationContext to view objects.
		request.setAttribute(ViewRendererServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, getPortletApplicationContext());

		// These attributes are required by the ViewRendererServlet.
		request.setAttribute(ViewRendererServlet.VIEW_ATTRIBUTE, view);
		request.setAttribute(ViewRendererServlet.MODEL_ATTRIBUTE, mv.getModel());

		// Unclude the content of the view in the render response.
		getPortletContext().getRequestDispatcher(this.viewRendererUrl).include(request, response);
	}

	/**
	 * Resolve the given view name into a View object (to be rendered).
	 * <p>Default implementations asks all ViewResolvers of this dispatcher.
	 * Can be overridden for custom resolution strategies, potentially based
	 * on specific model attributes or request parameters.
	 * @param viewName the name of the view to resolve
	 * @param model the model to be passed to the view
	 * @param request current portlet render request
	 * @return the View object, or null if none found
	 * @throws Exception if the view cannot be resolved
	 * (typically in case of problems creating an actual View object)
	 * @see ViewResolver#resolveViewName
	 */
	protected View resolveViewName(String viewName, Map model, RenderRequest request) throws Exception {
		for (Iterator it = this.viewResolvers.iterator(); it.hasNext();) {
			ViewResolver viewResolver = (ViewResolver) it.next();
			View view = viewResolver.resolveViewName(viewName, request.getLocale());
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
	 * @param ex Exception thrown on handler execution, or null if none
	 * @see HandlerInterceptor#afterRenderCompletion
	 */
	private void triggerAfterRenderCompletion(HandlerExecutionChain mappedHandler, int interceptorIndex,
			RenderRequest request, RenderResponse response, Exception ex)
			throws Exception {

		// Apply afterCompletion methods of registered interceptors.
		if (mappedHandler != null) {
			if (mappedHandler.getInterceptors() != null) {
				for (int i = interceptorIndex; i >= 0; i--) {
					HandlerInterceptor interceptor = mappedHandler.getInterceptors()[i];
					try {
						interceptor.afterRenderCompletion(request, response, mappedHandler.getHandler(), ex);
					}
					catch (Throwable ex2) {
						logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
					}
				}
			}
		}
	}

}
