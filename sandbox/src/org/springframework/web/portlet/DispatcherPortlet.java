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

package org.springframework.web.portlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.OrderComparator;
import org.springframework.web.portlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.portlet.theme.FixedThemeResolver;
import org.springframework.web.portlet.view.InternalResourceViewResolver;

/**
 * Concrete front controller for use within the portlet MVC framework.
 * Dispatches to registered handlers for processing portlet requests.
 *
 * <p>This class and the MVC approach it delivers is based on the discussion in Chapter 12 of
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 *
 * <p>This portlet is very flexible: It can be used with just about any workflow,
 * with the installation of the appropriate adapter classes. It offers the
 * following functionality that distinguishes it from other MVC frameworks:
 *
 * <ul>
 * <li>It is based around a JavaBeans configuration mechanism.
 *
 * <li>It can use any HandlerMapping implementation - whether standard, or provided
 * as part of an application - to control the routing of requests to handler objects.
 * Additional HandlerMapping objects can be added through defining beans in the
 * servlet's application context that implement the HandlerMapping interface in this
 * package. HandlerMappings can be given any bean name (they are tested by type).
 *
 * <li>It can use any HandlerAdapter. Default is SimpleControllerHandlerAdapter;
 * additional HandlerAdapter objects can be added through the application context.
 * Like HandlerMappings, HandlerAdapters can be given any bean name (tested by type).
 *
 * <li>Its view resolution strategy can be specified via a ViewResolver implementation.
 * Standard implementations support mapping URLs to bean names, and explicit mappings.
 * The ViewResolver bean name is "viewResolver"; default is InternalResourceViewResolver.
 * TODO: URL to bean viewresolver doesn't makes sense for portlets
 *
 * <li>Its theme resolution strategy is determined by a ThemeResolver implementation.
 * Implementations for a fixed theme and for cookie and session storage are included.
 * The ThemeResolver bean name is "themeResolver"; default is FixedThemeResolver.
 * </ul>
 * TODO: does theme make sense?
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: DispatcherPortlet.java,v 1.1 2004-04-29 13:54:22 dkopylenko Exp $
 * @see HandlerMapping
 * @see HandlerAdapter
 * @see ViewResolver
 * @see ThemeResolver
 * @see org.springframework.web.portlet.context.PortletApplicationContext
 * @see org.springframework.web.portlet.context.FrameworkPortlet
 */
public class DispatcherPortlet extends FrameworkPortlet {

	/**
	 * Well-known name for the ThemeResolver object in the bean factory for this namespace.
	 * TODO: do we need this?
	 */
	public static final String THEME_RESOLVER_BEAN_NAME = "themeResolver";

	/**
	 * Well-known name for the ExceptionResolver object in the bean factory for this namespace.
	 */
	public static final String EXCEPTION_RESOLVER_BEAN_NAME = "exceptionResolver";

	/**
	 * Well-known name for the ViewResolver object in the bean factory for this namespace.
	 */
	public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";

	/**
	 * Request attribute to hold current portlet application context.
	 * Otherwise only the global portlet app context is obtainable by tags etc.
	 */
	public static final String PORTLET_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherPortlet.class.getName() + ".CONTEXT";

	/**
	 * Request attribute to hold current locale, retrievable by views.
	 * @see org.springframework.web.servlet.support.RequestContext
	 */
	public static final String LOCALE_RESOLVER_ATTRIBUTE = DispatcherPortlet.class.getName() + ".LOCALE";

	/**
	 * Request attribute to hold current theme, retrievable by views.
	 * TODO do we need this?
	 * @see org.springframework.web.servlet.support.RequestContext
	 */
	public static final String THEME_RESOLVER_ATTRIBUTE = DispatcherPortlet.class.getName() + ".THEME";

	/**
	 * Additional logger for use when no mapping handlers are found for a request.
	 * TODO what to do about this?
	 */
	protected static final Log pageNotFoundLogger = LogFactory.getLog("org.springframework.web.servlet.PageNotFound");

	/** ThemeResolver used by this portlet */
	private ThemeResolver themeResolver;

	/** List of HandlerMappings used by this portlet */
	private List handlerMappings;

	/** List of HandlerAdapters used by this portlet */
	private List handlerAdapters;

	/** List of HandlerExceptionResolvers used by this portlet */
	private List handlerExceptionResolvers;

	/** ViewResolver used by this portlet */
	private ViewResolver viewResolver;


	/**
	 * Overridden method, invoked after any bean properties have been set and the
	 * PortletApplicationContext and BeanFactory for this namespace is available.
	 * <p>Loads HandlerMapping and HandlerAdapter objects, and configures a
	 * ViewResolver.
	 */
	protected void initFrameworkPortlet() throws PortletException, BeansException {
		initThemeResolver();
		initHandlerMappings();
		initHandlerAdapters();
		initHandlerExceptionResolvers();
		initViewResolver();
	}

	private void initThemeResolver() throws BeansException {
		try {
			this.themeResolver = (ThemeResolver) getPortletApplicationContext().getBean(THEME_RESOLVER_BEAN_NAME);
			logger.info("Loaded theme resolver [" + this.themeResolver + "]");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// we need to use the default
			this.themeResolver = new FixedThemeResolver();
			logger.info("Unable to locate theme resolver with name '" + THEME_RESOLVER_BEAN_NAME +
			            "': using default [" + this.themeResolver + "]");
		}
	}

	/**
	 * Initialize the HandlerMappings used by this class.
	 * If no HandlerMapping beans are defined in the BeanFactory
	 * for this namespace, we default to BeanNameUrlHandlerMapping.
	 * TODO need multiple list for PortletModes and new default mapping
	 */
	private void initHandlerMappings() throws BeansException {
		// find all HandlerMappings in the ApplicationContext
		Map matchingBeans = getPortletApplicationContext().getBeansOfType(HandlerMapping.class, true, false);
		this.handlerMappings = new ArrayList(matchingBeans.values());
		// Ensure we have at least one HandlerMapping, by registering
		// a default HandlerMapping if no other mappings are found.
		if (this.handlerMappings.isEmpty()) {
		    // TODO implement default portlet handler mapping
			//BeanNameUrlHandlerMapping hm = new BeanNameUrlHandlerMapping();
			//hm.setApplicationContext(getPortletApplicationContext());
			//this.handlerMappings.add(hm);
			logger.info("No HandlerMappings found in portlet '" + getPortletName() + "': using default");
		}
		else {
			// we keep HandlerMappings in sorted order
			Collections.sort(this.handlerMappings, new OrderComparator());
		}
	}

	/**
	 * Initialize the HandlerAdapters used by this class.
	 * If no HandlerAdapter beans are defined in the BeanFactory
	 * for this namespace, we default to SimpleControllerHandlerAdapter.
	 */
	private void initHandlerAdapters() throws BeansException {
		// find all HandlerAdapters in the ApplicationContext
		Map matchingBeans = getPortletApplicationContext().getBeansOfType(HandlerAdapter.class, true, false);
		this.handlerAdapters = new ArrayList(matchingBeans.values());
		// Ensure we have at least one HandlerAdapter, by registering
		// a default HandlerAdapter if no other adapters are found.
		if (this.handlerAdapters.isEmpty()) {
			this.handlerAdapters.add(new SimpleControllerHandlerAdapter());
			// TODO do we need this handlerapapter??
			//this.handlerAdapters.add(new ThrowawayControllerHandlerAdapter());
			logger.info("No HandlerAdapters found in porltet '" + getPortletName() + "': using defaults");
		}
		else {
			// we keep HandlerAdapters in sorted order
			Collections.sort(this.handlerAdapters, new OrderComparator());
		}
	}

	/**
	 * Initialize the HandlerExceptionResolver used by this class.
	 * If no bean is defined with the given name in the BeanFactory
	 * for this namespace, we default to no exception resolver.
	 */
	private void initHandlerExceptionResolvers() throws BeansException {
		// find all HandlerExceptionResolvers in the ApplicationContext
		Map matchingBeans = getPortletApplicationContext().getBeansOfType(HandlerExceptionResolver.class, true, false);
		this.handlerExceptionResolvers = new ArrayList(matchingBeans.values());
		// we keep HandlerExceptionResolvers in sorted order
		Collections.sort(this.handlerExceptionResolvers, new OrderComparator());
	}

	/**
	 * Initialize the ViewResolver used by this class.
	 * If no bean is defined with the given name in the BeanFactory
	 * for this namespace, we default to InternalResourceViewResolver.
	 */
	private void initViewResolver() throws BeansException {
		try {
			this.viewResolver = (ViewResolver) getPortletApplicationContext().getBean(VIEW_RESOLVER_BEAN_NAME);
			logger.info("Loaded view resolver [" + viewResolver + "]");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default
			InternalResourceViewResolver vr = new InternalResourceViewResolver();
			vr.setApplicationContext(getPortletApplicationContext());
			this.viewResolver = vr;
			logger.info("Unable to locate view resolver with name '" + VIEW_RESOLVER_BEAN_NAME +
									"': using default [" + this.viewResolver + "]");
		}
	}


	/**
	 * Return the handler for this request.
	 * Try all handler mappings in order.
	 * @return the handler, or null if no handler could be found
	 */
	private HandlerExecutionChain getHandler(PortletRequest request) throws Exception {
		Iterator itr = this.handlerMappings.iterator();
		while (itr.hasNext()) {
			HandlerMapping hm = (HandlerMapping) itr.next();
			logger.debug("Testing handler map [" + hm  + "] in DispatcherPortlet with name '" + getPortletName() + "'");
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
	private HandlerAdapter getHandlerAdapter(Object handler) throws PortletException {
		Iterator itr = this.handlerAdapters.iterator();
		while (itr.hasNext()) {
			HandlerAdapter ha = (HandlerAdapter) itr.next();
			logger.debug("Testing handler adapter [" + ha + "]");
			if (ha.supports(handler)) {
				return ha;
			}
		}
		throw new PortletException("No adapter for handler [" + handler +
		                           "]: Does your handler implement a supported interface like Controller?");
	}

	/**
	 * Render the given ModelAndView. This is the last stage in handling a request.
	 * It may involve resolving the view by name.
	 * @throws Exception if there's a problem rendering the view
	 */
	private void render(ModelAndView mv, RenderRequest request, RenderResponse response, Locale locale)
	    throws Exception {
		View view = null;
		if (mv.isReference()) {
			// we need to resolve this view name
			view = this.viewResolver.resolveViewName(mv.getViewName(), locale);
		}
		else {
			// no need to lookup: the ModelAndView object contains the actual View object
			view = mv.getView();
		}
		if (view == null) {
			throw new PortletException("Error in ModelAndView object or View resolution encountered by portlet with name '" +
																 getPortletName() + "': View to render cannot be null with ModelAndView [" + mv + "]");
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
	private void triggerAfterCompletion(HandlerExecutionChain mappedHandler, int interceptorIndex,
            PortletRequest request, PortletResponse response, Exception ex) throws Exception {
        // apply afterCompletion methods of registered interceptors
        Exception currEx = ex;
        if (mappedHandler != null) {
            if (mappedHandler.getInterceptors() != null) {
                for (int i = interceptorIndex; i >= 0; i--) {
                    HandlerInterceptor interceptor = mappedHandler.getInterceptors()[i];
                    try {
                        interceptor.afterCompletion(request, response, mappedHandler.getHandler(), ex);
                    } catch (Exception ex2) {
                        if (currEx != null) {
                            logger.error("Exception overridden by HandlerInterceptor.afterCompletion exception", currEx);
                        }
                        currEx = ex2;
                    }
                }
            }
        }
        if (currEx != null) { throw currEx; }
    }

	/**
	 * Obtain and use the handler for this method.
	 * The handler will be obtained by applying the portlet's HandlerMappings in order.
	 * The HandlerAdapter will be obtained by querying the portlets's
	 * installed HandlerAdapters to find the first that supports the handler class.
	 * All ActionRequests are handled by this method.
	 * It's up to HandlerAdapters to decide which PortletModes are acceptable.
	 */
    protected void doActionService(ActionRequest request, ActionResponse response) throws Exception {
        logger.debug("DispatcherPortlet with name '" + getPortletName() + "' in context [" + request.getContextPath() 
                + "] received an Action request.");

        // make framework objects available for handlers
        request.setAttribute(PORTLET_APPLICATION_CONTEXT_ATTRIBUTE, getPortletApplicationContext());

        ActionRequest processedRequest = request;
        HandlerExecutionChain mappedHandler = null;
        int interceptorIndex = -1;
        try {
                mappedHandler = getHandler(processedRequest);
                if (mappedHandler == null || mappedHandler.getHandler() == null) {
                    // if we didn't find a handler
                    pageNotFoundLogger.warn("No mapping for ActionRequest in DispatcherPortlet with name '" + getPortletName() + "'"
                            + " in context [" + request.getContextPath() + "].");
                    // TODO how to indicate to portal we have a problem
                    // response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
                ha.handle(processedRequest, response, mappedHandler.getHandler());

            triggerAfterCompletion(mappedHandler, interceptorIndex, processedRequest, response, null);
        } catch (Exception ex) {
            triggerAfterCompletion(mappedHandler, interceptorIndex, processedRequest, response, ex);
        }
    }

	/**
	 * Obtain and use the handler for this method.
	 * The handler will be obtained by applying the portlet's HandlerMappings in order.
	 * The HandlerAdapter will be obtained by querying the portlets's
	 * installed HandlerAdapters to find the first that supports the handler class.
	 * All RenderRequests are handled by this method.
	 * It's up to HandlerAdapters to decide which PortletModes are acceptable.
	 */
    protected void doRenderService(RenderRequest request, RenderResponse response) throws Exception {
        logger.debug("DispatcherPortlet with name '" + getPortletName() + "' in context [" + request.getContextPath() 
                + "] received a Render request.");

        // make framework objects available for handlers
        request.setAttribute(PORTLET_APPLICATION_CONTEXT_ATTRIBUTE, getPortletApplicationContext());
        request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);

        RenderRequest processedRequest = request;
        HandlerExecutionChain mappedHandler = null;
        int interceptorIndex = -1;
        try {
            ModelAndView mv = null;
            try {
                mappedHandler = getHandler(processedRequest);
                if (mappedHandler == null || mappedHandler.getHandler() == null) {
                    // if we didn't find a handler
                    pageNotFoundLogger.warn("No mapping for RenderRequest in DispatcherPortlet with name '" + getPortletName() + "'"
                            + " in context [" + request.getContextPath() + "].");
                    // TODO how to indicate to portal we have a problem
                    // response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
            } catch (ModelAndViewDefiningException ex) {
                logger.debug("ModelAndViewDefiningException encountered", ex);
                mv = ex.getModelAndView();
            } catch (Exception ex) {
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
                } else {
                    throw ex;
                }
            }

            // did the handler return a view to render?
            if (mv != null) {
                logger.debug("Will render view in DispatcherPortlet with name '" + getPortletName() + "'");
                render(mv, processedRequest, response, request.getLocale());
            } else {
                logger.debug("Null ModelAndView returned to DispatcherPortlet with name '" + getPortletName()
                        + "': assuming HandlerAdapter completed request handling");
            }

            triggerAfterCompletion(mappedHandler, interceptorIndex, processedRequest, response, null);
        } catch (Exception ex) {
            triggerAfterCompletion(mappedHandler, interceptorIndex, processedRequest, response, ex);
        }
    }

}
