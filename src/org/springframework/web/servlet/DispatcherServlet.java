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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.OrderComparator;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.mvc.throwaway.ThrowawayControllerHandlerAdapter;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * Concrete front controller for use within the web MVC framework.
 * Dispatches to registered handlers for processing a web request.
 *
 * <p>This class and the MVC approach it delivers is discussed in Chapter 12 of
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 *
 * <p>This servlet is very flexible: It can be used with just about any workflow,
 * with the installation of the appropriate adapter classes. It offers the
 * following functionality that distinguishes it from other MVC frameworks:
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
 * <p>A web application can use any number of dispatcher servlets. Each servlet will
 * operate in its own namespace. Only the root application context will be shared.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see HandlerMapping
 * @see HandlerAdapter
 * @see ViewResolver
 * @see MultipartResolver
 * @see LocaleResolver
 * @see ThemeResolver
 * @see org.springframework.web.context.WebApplicationContext
 * @see org.springframework.web.context.ContextLoaderListener
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
	 * @see #setDetectAllViewResolvers
	 */
	public static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";

	/**
	 * Well-known name for the HandlerExceptionResolver object in the bean factory for this
	 * namespace. Only used when "detectAllHandlerExceptionResolvers" is turned off.
	 * @see #setDetectAllViewResolvers
	 */
	public static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerExceptionResolver";

	/**
	 * Well-known name for the ViewResolver object in the bean factory for this namespace.
	 * Only used when "detectAllViewResolvers" is turned off.
	 * @see #setDetectAllViewResolvers
	 */
	public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";

	/**
	 * Request attribute to hold current web application context.
	 * Otherwise only the global web app context is obtainable by tags etc.
	 */
	public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";

	/**
	 * Request attribute to hold current multipart resolver, retrievable by views/binders.
	 * @see org.springframework.web.servlet.support.RequestContextUtils
	 */
	public static final String MULTIPART_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".MULTIPART";

	/**
	 * Request attribute to hold current locale, retrievable by views.
	 * @see org.springframework.web.servlet.support.RequestContext
	 */
	public static final String LOCALE_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".LOCALE";

	/**
	 * Request attribute to hold current theme, retrievable by views.
	 * @see org.springframework.web.servlet.support.RequestContext
	 */
	public static final String THEME_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME";

	/**
	 * Log category to use when no mapped handler is found for a request.
	 */
	public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

	/**
	 * Additional logger to use when no mapped handler is found for a request.
	 */
	protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);


	/** Detect all HandlerMappings or just expect "handlerMapping" bean? */
	private boolean detectAllHandlerMappings = true;

	/** Detect all HandlerExceptionResolvers or just expect "handlerExceptionResolver" bean? */
	private boolean detectAllHandlerExceptionResolvers = true;

	/** Detect all ViewResolvers or just expect "viewResolver" bean? */
	private boolean detectAllViewResolvers = true;

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
	 * <p>Default is true. Turn this off if you want this servlet to use a
	 * single HandlerMapping, despite multiple HandlerMapping beans being
	 * defined in the context.
	 */
	public void setDetectAllHandlerMappings(boolean detectAllHandlerMappings) {
		this.detectAllHandlerMappings = detectAllHandlerMappings;
	}

	/**
	 * Set whether to detect all HandlerExceptionResolver beans in this servlet's context.
	 * Else, just a single bean with name "handlerExceptionResolver" will be expected.
	 * <p>Default is true. Turn this off if you want this servlet to use a
	 * single HandlerExceptionResolver, despite multiple HandlerExceptionResolver
	 * beans being defined in the context.
	 */
	public void setDetectAllHandlerExceptionResolvers(boolean detectAllHandlerExceptionResolvers) {
		this.detectAllHandlerExceptionResolvers = detectAllHandlerExceptionResolvers;
	}

	/**
	 * Set whether to detect all ViewResolver beans in this servlet's context.
	 * Else, just a single bean with name "viewResolver" will be expected.
	 * <p>Default is true. Turn this off if you want this servlet to use a
	 * single ViewResolver, despite multiple ViewResolver beans being
	 * defined in the context.
	 */
	public void setDetectAllViewResolvers(boolean detectAllViewResolvers) {
		this.detectAllViewResolvers = detectAllViewResolvers;
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
			this.multipartResolver =
					(MultipartResolver) getWebApplicationContext().getBean(MULTIPART_RESOLVER_BEAN_NAME);
			if (logger.isInfoEnabled()) {
				logger.info("Using MultipartResolver [" + this.multipartResolver + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// default is no multipart resolver
			this.multipartResolver = null;
			if (logger.isInfoEnabled()) {
				logger.info("Unable to locate MultipartResolver with name ["	+ MULTIPART_RESOLVER_BEAN_NAME +
						"]: no multipart handling provided");
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
			this.localeResolver = (LocaleResolver) getWebApplicationContext().getBean(LOCALE_RESOLVER_BEAN_NAME);
			if (logger.isInfoEnabled()) {
				logger.info("Using LocaleResolver [" + this.localeResolver + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// we need to use the default
			this.localeResolver = new AcceptHeaderLocaleResolver();
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
			this.themeResolver = (ThemeResolver) getWebApplicationContext().getBean(THEME_RESOLVER_BEAN_NAME);
			if (logger.isInfoEnabled()) {
				logger.info("Using ThemeResolver [" + this.themeResolver + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// we need to use the default
			this.themeResolver = new FixedThemeResolver();
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
			// find all HandlerMappings in the ApplicationContext,
			// including ancestor contexts
			Map matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					getWebApplicationContext(), HandlerMapping.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerMappings = new ArrayList(matchingBeans.values());
				// we keep HandlerMappings in sorted order
				Collections.sort(this.handlerMappings, new OrderComparator());
			}
		}
		else {
			try {
				Object hm = getWebApplicationContext().getBean(HANDLER_MAPPING_BEAN_NAME);
				this.handlerMappings = Collections.singletonList(hm);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerMapping later.
			}
		}

		// Ensure we have at least one HandlerMapping, by registering
		// a default HandlerMapping if no other mappings are found.
		if (this.handlerMappings == null) {
			BeanNameUrlHandlerMapping hm = new BeanNameUrlHandlerMapping();
			hm.setApplicationContext(getWebApplicationContext());
			this.handlerMappings = Collections.singletonList(hm);
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
		// find all HandlerAdapters in the ApplicationContext
		Map matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
				getWebApplicationContext(), HandlerAdapter.class, true, false);
		if (!matchingBeans.isEmpty()) {
			this.handlerAdapters = new ArrayList(matchingBeans.values());
			// we keep HandlerAdapters in sorted order
			Collections.sort(this.handlerAdapters, new OrderComparator());
		}
		else {
			// Ensure we have at least some HandlerAdapters, by registering
			// default HandlerAdapters if no other adapters are found.
			this.handlerAdapters = new ArrayList(2);
			this.handlerAdapters.add(new SimpleControllerHandlerAdapter());
			this.handlerAdapters.add(new ThrowawayControllerHandlerAdapter());
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
			// find all HandlerExceptionResolvers in the ApplicationContext
			Map matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					getWebApplicationContext(), HandlerExceptionResolver.class, true, false);
			this.handlerExceptionResolvers = new ArrayList(matchingBeans.values());
			// we keep HandlerExceptionResolvers in sorted order
			Collections.sort(this.handlerExceptionResolvers, new OrderComparator());
		}
		else {
			try {
				Object her = getWebApplicationContext().getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME);
				this.handlerExceptionResolvers = Collections.singletonList(her);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, no HandlerExceptionResolver is fine too.
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
			// find all ViewResolvers in the ApplicationContext
			Map matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					getWebApplicationContext(), ViewResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.viewResolvers = new ArrayList(matchingBeans.values());
				// we keep ViewResolvers in sorted order
				Collections.sort(this.viewResolvers, new OrderComparator());
			}
		}
		else {
			try {
				Object vr = getWebApplicationContext().getBean(VIEW_RESOLVER_BEAN_NAME);
				this.viewResolvers = Collections.singletonList(vr);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default ViewResolver later.
			}
		}

		// Ensure we have at least one ViewResolver, by registering
		// a default ViewResolver if no other resolvers are found.
		if (this.viewResolvers == null) {
			InternalResourceViewResolver vr = new InternalResourceViewResolver();
			vr.setApplicationContext(getWebApplicationContext());
			this.viewResolvers = Collections.singletonList(vr);
			if (logger.isInfoEnabled()) {
				logger.info("No ViewResolvers found in servlet '" + getServletName() + "': using default");
			}
		}
	}


	/**
	 * Obtain and use the handler for this method.
	 * The handler will be obtained by applying the servlet's HandlerMappings in order.
	 * The HandlerAdapter will be obtained by querying the servlet's
	 * installed HandlerAdapters to find the first that supports the handler class.
	 * Both doGet() and doPost() are handled by this method.
	 * It's up to HandlerAdapters to decide which methods are acceptable.
	 */
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("DispatcherServlet with name '" + getServletName() + "' received request for [" +
					request.getRequestURI() + "]");
		}

		// make framework objects available for handlers
		request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
		request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
		request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);

		HttpServletRequest processedRequest = request;
		HandlerExecutionChain mappedHandler = null;
		int interceptorIndex = -1;
		try {
			ModelAndView mv = null;
			try {
				// Convert the request into a multipart request, and make multipart resolver available.
				// If no multipart resolver is set, simply use the existing request.
				if (this.multipartResolver != null && this.multipartResolver.isMultipart(request)) {
					if (request instanceof MultipartHttpServletRequest) {
						logger.info("Request is already a MultipartHttpServletRequest - if not in a forward, " +
								"this typically results from an additional MultipartFilter in web.xml");
					}
					else {
						request.setAttribute(MULTIPART_RESOLVER_ATTRIBUTE, this.multipartResolver);
						processedRequest = this.multipartResolver.resolveMultipart(request);
					}
				}

				mappedHandler = getHandler(processedRequest);
				if (mappedHandler == null || mappedHandler.getHandler() == null) {
					// if we didn't find a handler
					if (pageNotFoundLogger.isWarnEnabled()) {
						pageNotFoundLogger.warn("No mapping for [" + request.getRequestURI() +
								"] in DispatcherServlet with name '" + getServletName() + "'");
					}
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
					return;
				}

				// apply preHandle methods of registered interceptors
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

				// actually invoke the handler
				HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
				mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

				// apply postHandle methods of registered interceptors
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
				ModelAndView exMv = null;
				Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
				for (Iterator it = this.handlerExceptionResolvers.iterator(); exMv == null && it.hasNext();) {
					HandlerExceptionResolver resolver = (HandlerExceptionResolver) it.next();
					exMv = resolver.resolveException(request, response, handler, ex);
				}
				if (exMv != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("HandlerExceptionResolver returned ModelAndView [" + exMv + "] for exception");
					}
					logger.warn("Handler execution resulted in exception - forwarding to resolved error view", ex);
					mv = exMv;
				}
				else {
					throw ex;
				}
			}

			// did the handler return a view to render?
			if (mv != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Will render view in DispatcherServlet with name '" + getServletName() + "'");
				}
				Locale locale = this.localeResolver.resolveLocale(processedRequest);
				response.setLocale(locale);
				render(mv, processedRequest, response, locale);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Null ModelAndView returned to DispatcherServlet with name '" +
							getServletName() + "': assuming HandlerAdapter completed request handling");
				}
			}

			triggerAfterCompletion(mappedHandler, interceptorIndex, processedRequest, response, null);
		}
		catch (Exception ex) {
			triggerAfterCompletion(mappedHandler, interceptorIndex, processedRequest, response, ex);
		}
		finally {
			// clean up any resources used by a multipart request
			if (processedRequest instanceof MultipartHttpServletRequest && processedRequest != request) {
				this.multipartResolver.cleanupMultipart((MultipartHttpServletRequest) processedRequest);
			}
		}
	}

	/**
	 * Override HttpServlet's getLastModified to evaluate the Last-Modified
	 * value of the mapped handler.
	 */
	protected long getLastModified(HttpServletRequest request) {
		try {
			HandlerExecutionChain mappedHandler = getHandler(request);
			if (mappedHandler == null || mappedHandler.getHandler() == null) {
				// ignore -> will reappear on doService
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
			// ignore -> will reappear on doService
			logger.debug("Exception thrown in getLastModified", ex);
			return -1;
		}
	}

	/**
	 * Return the handler for this request.
	 * Try all handler mappings in order.
	 * @return the handler, or null if no handler could be found
	 */
	private HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		Iterator it = this.handlerMappings.iterator();
		while (it.hasNext()) {
			HandlerMapping hm = (HandlerMapping) it.next();
			if (logger.isDebugEnabled()) {
				logger.debug("Testing handler map [" + hm  + "] in DispatcherServlet with name '" +
						getServletName() + "'");
			}
			HandlerExecutionChain handler = hm.getHandler(request);
			if (handler != null)
				return handler;
		}
		return null;
	}

	/**
	 * Return the HandlerAdapter for this handler class.
	 * @throws ServletException if no HandlerAdapter can be found for the handler.
	 * This is a fatal error.
	 */
	private HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
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
	 * Render the given ModelAndView. This is the last stage in handling a request.
	 * It may involve resolving the view by name.
	 * @throws Exception if there's a problem rendering the view
	 */
	private void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response, Locale locale)
	    throws Exception {
		View view = null;
		if (mv.isReference()) {
			// we need to resolve the view name
			for (Iterator it = this.viewResolvers.iterator(); it.hasNext() && view == null;) {
				ViewResolver viewResolver = (ViewResolver) it.next();
				view = viewResolver.resolveViewName(mv.getViewName(), locale);
			}
			if (view == null) {
				throw new ServletException("Could not resolve view with name '" + mv.getViewName() +
						"' in servlet with name '" + getServletName() + "'");
			}
		}
		else {
			// no need to lookup: the ModelAndView object contains the actual View object
			view = mv.getView();
			if (view == null) {
				throw new ServletException("ModelAndView [" + mv + "] neither contains a view name nor a " +
						"View object in servlet with name '" + getServletName() + "'");
			}
		}
		view.render(mv.getModel(), request, response);
	}

	/**
	 * Trigger afterCompletion callbacks on the mapped HandlerInterceptors.
	 * Will just invoke afterCompletion for all interceptors whose preHandle
	 * invocation has successfully completed and returned true.
	 * @param mappedHandler the mapped HandlerExecutionChain
	 * @param interceptorIndex index of last interceptor that successfully completed
	 * @param ex Exception thrown on handler execution, or null if none
	 * @see HandlerInterceptor#afterCompletion
	 */
	private void triggerAfterCompletion(
			HandlerExecutionChain mappedHandler, int interceptorIndex,
			HttpServletRequest request, HttpServletResponse response, Exception ex)
			throws Exception {

		// apply afterCompletion methods of registered interceptors
		Exception currEx = ex;

		if (mappedHandler != null) {
			if (mappedHandler.getInterceptors() != null) {
				for (int i = interceptorIndex; i >=0; i--) {
					HandlerInterceptor interceptor = mappedHandler.getInterceptors()[i];
					try {
						interceptor.afterCompletion(request, response, mappedHandler.getHandler(), ex);
					}
					catch (Exception ex2) {
						if (currEx != null) {
							logger.error("Exception overridden by HandlerInterceptor.afterCompletion exception", currEx);
						}
						currEx = ex2;
					}
				}
			}
		}

		if (currEx != null) {
			throw currEx;
		}
	}

}
