/**
 * Generic framework code included with
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 * This code is free to use and modify. However, please
 * acknowledge the source and include the above URL in each
 * class using or derived from this code.
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */

package org.springframework.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.OrderComparator;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.util.WebUtils;

/**
 * Concrete front controller for use within the web MVC framework.
 * Dispatches to registered handlers for processing a web request.
 *
 * <p>This class and the MVC approach it delivers is discussed in Chapter 12 of
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 *
 * <p>This servlet is very flexible: It can be used with just about any workflow,
 * with the installation of the appropriate adapter classes. It offers the
 * following functionality that distinguishes it from other MVC frameworks:
 * <ul>
 * <li>It is based around a JavaBeans configuration mechanism.
 * <li>It can use any HandlerMapping implementation - whether standard, or provided
 * as part of an application - to control the routing of requests to handler objects.
 * Additional HandlerMapping objects can be added through defining beans in the
 * servlet's application context that implement the HandlerMapping interface in this
 * package. HandlerMappings can be given any bean name (they are tested by type).
 * <li>It can use any HandlerAdapter (additional HandlerAdapter objects can be added
 * through the application context).
 * <li>Its view resolution strategy can be specified via a ViewResolver implementation.
 * Standard implementations support mapping URLs to bean names, and explicit mappings.
 * <li>Its locale resolution strategy is determined by a LocaleResolver implementation.
 * Out-of-the-box implementations work via HTTP accept header, cookie, or session.
 * <li>Its theme resolution strategy is determined by a ThemeResolver implementation.
 * Implementations for a fixed theme and for cookie and session storage are included.
 * </ul>
 *
 * <p>A web application can use any number of dispatcher servlets.
 * Each servlet will operate in its own namespace. Only the root application context,
 * and any config objects set for the application as a whole, will be shared.
 *
 * @see HandlerMapping
 * @see HandlerAdapter
 * @see ViewResolver
 * @see LocaleResolver
 * @see org.springframework.web.context.WebApplicationContext
 * @see org.springframework.web.context.ContextLoaderListener
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Revision: 1.5 $
 */
public class DispatcherServlet extends FrameworkServlet {

	/**
	 * Well-known name for the LocaleResolver object in the bean factory for
	 * this namespace.
	 */
	public static final String LOCALE_RESOLVER_BEAN_NAME = "localeResolver";

	/**
	 * Well-known name for the MultipartResolver object in the bean factory for
	 * this namespace.
	 */
	public static final String MULTIPART_RESOLVER_BEAN_NAME = "multipartResolver";

	/**
	 * Well-known name for the ViewResolver object in the bean factory for
	 * this namespace.
	 */
	public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";

	/**
	 * Well-known name for the ThemeResolver object in the bean factory for
	 * this namespace.
	 */
	public static final String THEME_RESOLVER_BEAN_NAME = "themeResolver";

	/**
	 * Request attribute to hold current web application context.
	 * Otherwise only the global web app context is obtainable by tags etc.
	 */
	public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";

	/**
	 * Request attribute to hold current locale, retrievable by views.
	 * @see org.springframework.web.servlet.support.RequestContext
	 */
	public static final String LOCALE_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".LOCALE";

	/**
	 * Request attribute to hold current multipart resolver, retrievable by views/binders.
	 * @see org.springframework.web.servlet.support.RequestContextUtils
	 */
	public static final String MULTIPART_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".MULTIPART";

	/**
	 * Request attribute to hold current theme, retrievable by views.
	 * @see org.springframework.web.servlet.support.RequestContext
	 */
	public static final String THEME_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME";

	/**
	 * Additional logger for use when no mapping handlers are found for a request. 
	 */
	protected final Log pageNotFoundLogger =
		LogFactory.getLog("org.springframework.web.servlet.PageNotFound");

	/** LocaleResolver used by this servlet */
	private LocaleResolver localeResolver;

	/** MultipartResolver used by this servlet */
	private MultipartResolver multipartResolver;

	/** ThemeResolver used by this servlet */
	private ThemeResolver themeResolver;

	/** List of HandlerMappings */
	private List handlerMappings;

	/** List of HandlerAdapters */
	private List handlerAdapters;

	/** ViewResolver used by this servlet */
	private ViewResolver viewResolver;


	/**
	 * Overridden method, invoked after any bean properties have been set and the
	 * WebApplicationContext and BeanFactory for this namespace is available.
	 * <p>Loads HandlerMapping and HandlerAdapter objects, and configures a
	 * ViewResolver and a LocaleResolver.
	 */
	protected void initFrameworkServlet() throws ServletException {
		initLocaleResolver();
		initThemeResolver();
		initMultipartResolver();
		initHandlerMappings();
		initHandlerAdapters();
		initViewResolver();
	}

	/**
	 * Initialize the LocaleResolver used by this class.
	 * If no bean is defined with the given name in the BeanFactory
	 * for this namespace, we default to AcceptHeaderLocaleResolver.
	 */
	private void initLocaleResolver() throws ServletException {
		try {
			this.localeResolver = (LocaleResolver) getWebApplicationContext().getBean(LOCALE_RESOLVER_BEAN_NAME);
			logger.info("Loaded locale resolver [" + this.localeResolver + "]");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default
			this.localeResolver = new AcceptHeaderLocaleResolver();
			logger.info("Unable to locate locale resolver with name '" + LOCALE_RESOLVER_BEAN_NAME + "': using default [" + this.localeResolver + "]");
		}
		catch (BeansException ex) {
			// We tried and failed to load the LocaleResolver specified by a bean
			throw new ServletException("Fatal error loading locale resolver with name '" + LOCALE_RESOLVER_BEAN_NAME + "': using default", ex);
		}
	}

	/**
	 * Initialize the MultipartResolver used by this class.
	 * If no bean is defined with the given name in the BeanFactory
	 * for this namespace, no multipart handling is provided.
	 */
	private void initMultipartResolver() throws ServletException {
		try {
			this.multipartResolver =
				(MultipartResolver) getWebApplicationContext().getBean(
					MULTIPART_RESOLVER_BEAN_NAME);
			logger.info(
				"Loaded multipart resolver [" + this.multipartResolver + "]");
		} catch (NoSuchBeanDefinitionException ex) {
			// default to no resolver
			this.multipartResolver = null;
			logger.info(
				"Unable to locate multipart resolver with name '"
					+ MULTIPART_RESOLVER_BEAN_NAME
					+ "': no multipart handling provided");
		} catch (BeansException ex) {
			// We tried and failed to load the MultipartResolver specified by a bean
			throw new ServletException(
				"Fatal error loading multipart resolver with name '"
					+ MULTIPART_RESOLVER_BEAN_NAME
					+ "': using default",
				ex);
		}
	}

	/**
	 * Initialize the LocaleResolver used by this class.
	 * If no bean is defined with the given name in the BeanFactory
	 * for this namespace, we default to a AcceptHeaderLocaleResolver.
	 */
	private void initThemeResolver() throws ServletException {
		try {
			this.themeResolver = (ThemeResolver) getWebApplicationContext().getBean(THEME_RESOLVER_BEAN_NAME);
			logger.info("Loaded theme resolver [" + this.themeResolver + "]");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default
			this.themeResolver = new FixedThemeResolver();
			logger.info("Unable to locate theme resolver with name '" + THEME_RESOLVER_BEAN_NAME + "': using default [" + this.themeResolver + "]");
		}
		catch (BeansException ex) {
			// We tried and failed to load the ThemeResolver specified by a bean
			throw new ServletException("Fatal error loading theme resolver with name '" + THEME_RESOLVER_BEAN_NAME + "': using default", ex);
		}
	}

	/**
	 * Initialize the HandlerMappings used by this class.
	 * If no HandlerMapping beans are defined in the BeanFactory
	 * for this namespace, we default to BeanNameUrlHandlerMapping.
	 */
	private void initHandlerMappings() throws ServletException {
		this.handlerMappings = new ArrayList();

		// Find all HandlerMappings in the ApplicationContext
		String[] hms = getWebApplicationContext().getBeanDefinitionNames(HandlerMapping.class);
		for (int i = 0; i < hms.length; i++) {
			initHandlerMapping(hms[i]);
			logger.info("Loaded handler mapping [" + hms[i] + "]");
		}

		// Ensure we have at least one HandlerMapping, by registering
		// a default HandlerMapping if no other mappings are found.
		if (this.handlerMappings.isEmpty()) {
			initDefaultHandlerMapping();
			logger.info("No HandlerMappings found in servlet '" + getServletName() + "': using default");
		}
		else {
			// We keep HandlerMappings in sorted order
			Collections.sort(this.handlerMappings, new OrderComparator());
		}
	}

	/**
	 * Initialize the given HandlerMapping object.
	 * @param beanName bean name in the current web application context
	 */
	private void initHandlerMapping(String beanName) throws ServletException {
		try {
			HandlerMapping hm = (HandlerMapping) getWebApplicationContext().getBean(beanName);
			this.handlerMappings.add(hm);
		}
		catch (ApplicationContextException ex) {
			// We don't need to catch NoSuchBeanDefinitionException:
			// we got the name from the bean factory.
			throw new ServletException("Error initializing HandlerMapping bean '" + beanName + "': " + ex.getMessage(), ex);
		}
	}

	/**
	 * Initialize the default HandlerMapping object, a BeanNameUrlHandlerMapping.
	 */
	private void initDefaultHandlerMapping() throws ServletException {
		try {
			HandlerMapping hm = new BeanNameUrlHandlerMapping();
			hm.setApplicationContext(getWebApplicationContext());
			this.handlerMappings.add(hm);
		}
		catch (ApplicationContextException ex) {
			throw new ServletException("Error initializing default HandlerMapping: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Initialize the HandlerAdapters used by this class.
	 * If no HandlerAdapter beans are defined in the BeanFactory
	 * for this namespace, we default to SimpleControllerHandlerAdapter.
	 */
	private void initHandlerAdapters() throws ServletException {
		this.handlerAdapters = new ArrayList();

		String[] has = getWebApplicationContext().getBeanDefinitionNames(HandlerAdapter.class);
		for (int i = 0; i < has.length; i++) {
			initHandlerAdapter(has[i]);
			logger.info("Loaded handler adapter [" + has[i] + "]");
		}

		// Ensure we have at least one HandlerAdapter, by registering
		// a default HandlerAdapter if no other adapters are found.
		if (this.handlerAdapters.isEmpty()) {
			initDefaultHandlerAdapter();
			logger.info("No HandlerAdapters found in servlet '" + getServletName() + "': using default");
		}
		else {
			// We keep HandlerAdapters in sorted order
			Collections.sort(this.handlerAdapters, new OrderComparator());
		}
	}

	/**
	 * Initialize the handler bean with the given name in the bean factory.
	 * @param beanName bean name in the current web application context
	 * @throws ServletException if there is an error trying to instantiate and initialize the handler bean
	 */
	private void initHandlerAdapter(String beanName) throws ServletException {
		try {
			HandlerAdapter ha = (HandlerAdapter) getWebApplicationContext().getBean(beanName);
			this.handlerAdapters.add(ha);
		}
		catch (BeansException ex) {
			// We don't need to catch NoSuchBeanDefinitionException:
			// we got the name from the bean factory.
			throw new ServletException("Error initializing HandlerAdapter bean '" + beanName + "': " + ex.getMessage(), ex);
		}
	}

	/**
	 * Initialize the default HandlerAdapter, a SimpleControllerHandlerAdapter.
	 */
	private void initDefaultHandlerAdapter() throws ServletException {
		try {
			HandlerAdapter ha = new SimpleControllerHandlerAdapter();
			ha.setApplicationContext(getWebApplicationContext());
			this.handlerAdapters.add(ha);
		}
		catch (ApplicationContextException ex) {
			throw new ServletException("Error initializing default HandlerAdapter: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Initialize the ViewResolver used by this class.
	 * If no bean is defined with the given name in the BeanFactory
	 * for this namespace, we default to InternalResourceViewResolver.
	 */
	private void initViewResolver() throws ServletException {
		try {
			this.viewResolver = (ViewResolver) getWebApplicationContext().getBean(VIEW_RESOLVER_BEAN_NAME);
			logger.info("Loaded view resolver [" + viewResolver + "]");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default
			this.viewResolver = new InternalResourceViewResolver();
			try {
				this.viewResolver.setApplicationContext(getWebApplicationContext());
			} catch (ApplicationContextException ex2) {
				throw new ServletException("Fatal error initializing default ViewResolver");
			}
			logger.info("Unable to locate view resolver with name '" + VIEW_RESOLVER_BEAN_NAME + "': using default [" + this.viewResolver + "]");
		}
		catch (BeansException ex) {
			// We tried and failed to load the ViewResolver specified by a bean
			throw new ServletException("Fatal error loading view resolver: bean with name '" + VIEW_RESOLVER_BEAN_NAME + "' is required in servlet '" + getServletName()  + "': using default", ex);
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
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.debug("DispatcherServlet with name '" + getServletName() + "' received request for [" + WebUtils.getRequestUri(request) + "]");

		// Make framework objects available for handlers
		request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
		request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
		request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);

		// Convert the request into a multipart request, and make multipart resolver available.
		// If no multipart resolver is set, simply use the existing request.
		HttpServletRequest processedRequest = request;
		if (this.multipartResolver != null && this.multipartResolver.isMultipart(request)) {
			request.setAttribute(MULTIPART_RESOLVER_ATTRIBUTE, this.multipartResolver);
			processedRequest = this.multipartResolver.resolveMultipart(request);
		}

		try {
			HandlerExecutionChain mappedHandler = getHandler(processedRequest);
			if (mappedHandler == null || mappedHandler.getHandler() == null) {
				// if we didn't find a handler
				pageNotFoundLogger.warn("No mapping for [" + WebUtils.getRequestUri(processedRequest) + "] in DispatcherServlet with name '" + getServletName() + "'");
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			// Apply preHandle methods of registered interceptors
			if (mappedHandler.getInterceptors() != null) {
				for (int i = 0; i < mappedHandler.getInterceptors().length; i++) {
					HandlerInterceptor interceptor = mappedHandler.getInterceptors()[i];
					if (!interceptor.preHandle(processedRequest, response, mappedHandler.getHandler())) {
						return;
					}
				}
			}

			// Actually invoke the handler
			HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
			ModelAndView mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

			// Apply postHandle methods of registered interceptors
			if (mappedHandler.getInterceptors() != null) {
				for (int i = mappedHandler.getInterceptors().length - 1; i >=0 ; i--) {
					HandlerInterceptor interceptor = mappedHandler.getInterceptors()[i];
					interceptor.postHandle(processedRequest, response, mappedHandler.getHandler());
				}
			}

			// Did the handler return a view to render?
			if (mv != null) {
				logger.debug("Will render view in DispatcherServlet with name '" + getServletName() + "'");
				Locale locale = this.localeResolver.resolveLocale(processedRequest);
				response.setLocale(locale);
				render(mv, processedRequest, response, locale);
			}
			else {
				logger.debug("Null ModelAndView returned to DispatcherServlet with name '" + getServletName() + "': assuming HandlerAdapter completed request handling");
			}
		}
		finally {
			// Clean up any resources used by a multipart request.
			if (this.multipartResolver != null && this.multipartResolver.isMultipart(request)) {
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
			HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
			long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
			logger.debug("Last-Modified value for [" + WebUtils.getRequestUri(request) + "] is [" + lastModified + "]");
			return lastModified;
		}
		catch (ServletException ex) {
			// ignore -> will reappear on doService
			logger.debug("Exception thrown on getLastModified", ex);
			return -1;
		}
	}

	/**
	 * Return the handler for this request.
	 * Try all handler mappings in order.
	 * @return the handler, or null if no handler could be found
	 */
	private HandlerExecutionChain getHandler(HttpServletRequest request) throws ServletException {
		Iterator itr = this.handlerMappings.iterator();
		while (itr.hasNext()) {
			HandlerMapping hm = (HandlerMapping) itr.next();
			logger.debug("Testing handler map [" + hm  + "] in DispatcherServlet with name '" + getServletName() + "'");
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
		Iterator itr = this.handlerAdapters.iterator();
		while (itr.hasNext()) {
			HandlerAdapter ha = (HandlerAdapter) itr.next();
			logger.debug("Testing handler adapter [" + ha + "]");
			if (ha.supports(handler)) {
				return ha;
			}
		}
		throw new ServletException("No adapter for handler " + handler);
	}

	/**
	 * Render the given ModelAndView. This is the last stage in handling a request.
	 * It may involve resolving the view by name.
	 * @throws ServletException if the view cannot be resolved.
	 * @throws IOException if there's a problem rendering the view
	 */
	private void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response, Locale locale)
	    throws ServletException, IOException {
		View view = null;
		if (mv.isReference()) {
			// We need to resolve this view name
			view = this.viewResolver.resolveViewName(mv.getViewName(), locale);
		}
		else {
			// No need to lookup: the ModelAndView object contains the actual view
			view = mv.getView();
		}
		if (view == null) {
			throw new ServletException("Error in ModelAndView object or View resolution encountered by servlet with name '" + getServletName() + "'. View cannot be null in render with ModelAndView=[" + mv + "]");
		}
		view.render(mv.getModel(), request, response);
	}

}
