package org.springframework.web.context;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.config.ConfigurableApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Performs the actual initialization work for the root application context.
 * Called by ContextLoaderListener and ContextLoaderServlet.
 *
 * <p>Regards a "contextClass" parameter at the servlet context resp. web.xml root level,
 * falling back to the default context class (XmlWebApplicationContext) if not found.
 *
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @since 17.02.2003
 * @see ContextLoaderListener
 * @see ContextLoaderServlet
 * @see XmlWebApplicationContext
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

	private final Log logger = LogFactory.getLog(ContextLoader.class);

	/**
	 * Parent context to web application context, which may optionally be
	 * loaded/initialized by the loadParentContext() template method 
	 */
	protected ApplicationContext parentContext;
	
	/**
	 * Initialize Spring's web application context for the given servlet context,
	 * regarding the "contextClass" servlet context init parameter.
	 * @param servletContext current servlet context
	 * @return the new WebApplicationContext
	 */
	public WebApplicationContext initContext(ServletContext servletContext) throws BeansException {
		servletContext.log("Loading root WebApplicationContext");
		WebApplicationContext wac = createContext(servletContext);
		logger.info("Using context class [" + wac.getClass().getName() + "] for root WebApplicationContext");
		WebApplicationContextUtils.publishWebApplicationContext(wac);
		return wac;
	}

	/**
	 * Instantiate the root WebApplicationContext for this loader, either a default
	 * XmlWebApplicationContext or a custom context class if specified.
	 * This implementation expects custom contexts to implement NestedWebApplicationContext.
	 * Can be overridden in subclasses.
	 * @throws BeansException if the context couldn't be initialized
	 * @see #CONTEXT_CLASS_PARAM
	 * @see #DEFAULT_CONTEXT_CLASS
	 * @see RootWebApplicationContext
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	protected WebApplicationContext createContext(ServletContext servletContext) throws BeansException {

		String contextClass = servletContext.getInitParameter(CONTEXT_CLASS_PARAM);
		RootWebApplicationContext wac = null;
		try {
			loadParentContext(servletContext);
			
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Class clazz = (contextClass != null ? Class.forName(contextClass, true, cl) : DEFAULT_CONTEXT_CLASS);
			if (!RootWebApplicationContext.class.isAssignableFrom(clazz)) {
				throw new ApplicationContextException("Context class [" + contextClass + "] is not RootWebApplicationContext");
			}
			wac = (RootWebApplicationContext) clazz.newInstance();
			wac.initRootContext(servletContext, this.parentContext);
		}
		catch (BeansException ex) {
			handleException("Failed to initialize application context", ex);
		}
		catch (ClassNotFoundException ex) {
			handleException("Failed to load config class [" + contextClass + "]", ex);
		}
		catch (InstantiationException ex) {
			handleException("Failed to instantiate config class [" + contextClass +
			                "]: does it have a public no arg constructor?", ex);
		}
		catch (IllegalAccessException ex) {
			handleException("Illegal access while finding or instantiating config class [" +
			                contextClass + "]: does it have a public no arg constructor?", ex);
		}
		return wac;
	}

	/**
	 * Close Spring's web application context for the given servlet context.
	 * @param servletContext current servlet context
	 */
	public void closeContext(ServletContext servletContext) throws ApplicationContextException {
		servletContext.log("Closing root WebApplicationContext");
		WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		if (wac instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) wac).close();
		}
	}
	
	/**
	 * Template method which may be overriden by a subclass to load or obtain
	 * an ApplicationContext instance which will be used as the parent context
	 * of the WebApplicationContext if it is not null. This method should set
	 * the parentContext field.
	 * 
	 * @param servletContext
	 * @throws BeansException if the context couldn't be initialized
	 */
	protected void loadParentContext(ServletContext servletContext) throws BeansException {
		// empty default impl.
	}
	
	/**
	 * Log and throw an appropriate exception.
	 */
	protected void handleException(String msg, Throwable ex) throws BeansException {
		logger.error(msg, ex);
		if (ex instanceof BeansException) {
			throw (BeansException) ex;
		}
		else {
			throw new ApplicationContextException(msg, ex);
		}
	}

}
