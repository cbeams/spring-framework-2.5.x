package org.springframework.web.context;

import javax.servlet.ServletContext;

/**
 * Interface to be implemented by any object that wishes to be notified
 * of the ServletContext (typically determined by the WebApplicationContext)
 * that it runs in.
 *
 * <p>Can be implemented instead of ApplicationContextAware (casting the
 * context to WebApplicationContext) if all an object needs is a reference
 * to the ServletContext.
 *
 * @author Juergen Hoeller
 * @since 12.03.2004
 * @see org.springframework.context.ApplicationContextAware
 */
public interface ServletContextAware {

	/**
	 * Set the ServletContext that this object runs in.
	 * <p>Invoked after population of normal bean properties but before an init
	 * callback like InitializingBean's afterPropertiesSet or a custom init-method.
	 * Invoked after ApplicationContextAware's setApplicationContext.
	 * @param servletContext ServletContext object to be used by this object
	 */
	void setServletContext(ServletContext servletContext);

}
