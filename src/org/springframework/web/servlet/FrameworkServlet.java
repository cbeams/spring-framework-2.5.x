/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.config.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.RequestHandledEvent;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.util.WebUtils;

/**
 * Base servlet for servlets within the web framework. Allows integration
 * with an application context, in a JavaBean-based overall solution.
 *
 * <p>This class offers the following functionality:
 * <ul>
 * <li>Uses a WebApplicationContext to access a BeanFactory. The servlet's
 * configuration is determined by the beans in the namespace 'servlet-name'-servlet,
 * if not overridden via the namespace property.
 * <li>Publishes events on request processing, whether or not a request is
 * successfully handled.
 * </ul>
 *
 * <p>Subclasses must implement doService() to handle requests. Because this extends
 * HttpServletBean rather than HttpServlet directly, bean properties are mapped
 * onto it. Subclasses can override initFrameworkServlet() for custom initialization.
  *
 * @author Rod Johnson
 * @version $Revision: 1.5 $
 * @see #doService
 * @see #initFrameworkServlet
 */
public abstract class FrameworkServlet extends HttpServletBean {

	/**
	 * Suffix for namespace bean factory names. If a servlet of this class is
	 * given the name 'test' in a context, the namespace used by the servlet will
	 * resolve to 'test-servlet'.
	 */
	public static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";

	/**
	 * Prefix for the ServletContext attribute for the web application context.
	 * The completion is the servlet name.
	 */
	public static final String SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT.";


	/** Namespace for this servlet */
	private String namespace;

	/** Custom context class */
	private String contextClass;

	/**
	 * Should we publish the context as a ServletContext attribute?
	 */
	private boolean publishContext = true;

	/** WebApplicationContext for this servlet */
	private WebApplicationContext webApplicationContext;


	/**
	 * Set a custom namespace for this servlet.
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
	 * Set a custom context class name. This class must be of type WebApplicationContext,
	 * and must implement a constructor taking two arguments:
	 * a parent WebApplicationContext (the root), and the current namespace as String.
	 * @param className name of custom context class to use
	 */
	public final void setContextClass(String className) {
		this.contextClass = className;
	}

	/**
	 * Return the custom context class name, or null if none set.
	 */
	public String getContextClass() {
		return contextClass;
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
	protected final void initServletBean() throws ServletException {
		long startTime = System.currentTimeMillis();
		logger.info("Framework servlet '" + getServletName() + "' init");
		this.webApplicationContext = createWebApplicationContext();
		initFrameworkServlet();
		long elapsedTime = System.currentTimeMillis() - startTime;
		logger.info("Framework servlet '" + getServletName() + "' init completed in " + elapsedTime + " ms");
	}

	/**
	 * Create the WebApplicationContext for this web app,
	 * either an XmlWebApplicationContext or a custom context class if set.
	 * Can be overridden in subclasses.
	 * @throws ServletException if the context object can't be found
	 * @see #setContextClass
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	protected WebApplicationContext createWebApplicationContext() throws ServletException {
		getServletContext().log("Loading WebApplicationContext for servlet '" + getServletName() + "'");
		ServletContext sc = getServletConfig().getServletContext();
		WebApplicationContext parent = WebApplicationContextUtils.getWebApplicationContext(sc);
		String namespace = getNamespace();

		WebApplicationContext wac = (this.contextClass != null) ?
				instantiateCustomWebApplicationContext(this.contextClass, parent, namespace) :
				new XmlWebApplicationContext(parent, namespace);
		logger.info("Loading WebApplicationContext for servlet '" + getServletName() + "': using context class '" + wac.getClass().getName() + "'");
		try {
			wac.setServletContext(sc);
		}
		catch (BeansException ex) {
			handleException("Failed to initialize application context", ex);
		}

		if (this.publishContext) {
			// Publish the context as a servlet context attribute
			String attName = getServletContextAttributeName();
			sc.setAttribute(attName, wac);
			logger.info("Bound context of servlet '" + getServletName() + "' in global ServletContext with name '" + attName + "'");
		}
		return wac;
	}

	/**
	 * Try to instantiate a custom web application context.
	 */
	private WebApplicationContext instantiateCustomWebApplicationContext(String className, WebApplicationContext parent, String namespace) throws ServletException {
		logger.info("Servlet with name '" + getServletName() + "' will try to create custom WebApplicationContext context of class '" + className + "'");
		WebApplicationContext wac = null;
		try {
			Class clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
			if (!WebApplicationContext.class.isAssignableFrom(clazz)) {
				throw new ServletException("Fatal initialization error in servlet with name '" + getServletName() + "': custom WebApplicationContext class '" + className + "' must implement WebApplicationContext");
			}
			Constructor constructor = clazz.getConstructor( new Class[] { ApplicationContext.class, String.class} );
			wac = (WebApplicationContext) constructor.newInstance(new Object[] { parent, namespace} );
		}
		catch (ClassNotFoundException ex) {
			handleException("Failed to find custom context class", ex);
		}
		catch (InstantiationException ex) {
			handleException("Failed to instantiate custom context", ex);
		}
		catch (NoSuchMethodException ex) {
			handleException("Failed to find constructor for custom context (must define a constructor taking ApplicationContext parent and String namespace)", ex);
		}
		catch (InvocationTargetException ex) {
			handleException("Failed to invoke constructor for custom context", ex);
		}
		catch (IllegalAccessException ex) {
			handleException("Failed to access constructor for custom context", ex);
		}
		return wac;
	}

	/**
	 * Log and throw an appropriate exception.
	 */
	private void handleException(String msg, Throwable ex) throws ServletException {
		logger.error(msg + " for servlet '" + getServletName() + "'", ex);
		if (ex instanceof Error) {
			throw (Error) ex;
		}
		else {
			throw new ServletException(msg, ex);
		}
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
		finally {
			long processingTime = System.currentTimeMillis() - startTime;
			// Whether or not we succeeded, publish an event
			if (failureCause != null) {
				logger.error("Could not complete request", failureCause);
				this.webApplicationContext.publishEvent(
				    new RequestHandledEvent(this, WebUtils.getRequestUri(request), processingTime, request.getRemoteAddr(),
				                            request.getMethod(), getServletConfig().getServletName(), failureCause));
			}
			else {
				logger.debug("Successfully completed request");
				this.webApplicationContext.publishEvent(
				    new RequestHandledEvent(this, WebUtils.getRequestUri(request), processingTime, request.getRemoteAddr(),
				                            request.getMethod(), getServletConfig().getServletName()));
			}
		}
	}


	/**
	 * Subclasses must implement this method to perform any initialization they require.
	 * The implementation may be empty. This method will be invoked after any bean properties
	 * have been set and WebApplicationContext and BeanFactory have been loaded.
	 * @throws ServletException in case of an initialization exception
	 */
	protected abstract void initFrameworkServlet() throws ServletException;

	/**
	 * Subclasses must implement this method to do the work of request handling.
	 * The contract is the same as that for the doGet() or doPost() method of HttpServlet.
	 * This class intercepts calls to ensure that event publication takes place.
	 * @see javax.servlet.http.HttpServlet#doGet
	 */
	protected abstract void doService(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException;

}
