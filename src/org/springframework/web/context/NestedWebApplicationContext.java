package org.springframework.web.context;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.context.config.ConfigurableApplicationContext;

/**
 * Interface to be implemented by nested web application contexts.
 * Expected for example by FrameworkServlet.
 * @author Juergen Hoeller
 * @since 05.12.2003
 * @see org.springframework.web.servlet.FrameworkServlet
 */
public interface NestedWebApplicationContext extends WebApplicationContext, ConfigurableApplicationContext {

	/**
	 * Initialize this nested web application context.
	 * @param servletContext the current ServletContext
	 * @param namespace the namespace for the child context
	 * @param parent the parent, or null if none
	 * @param owner the owner component like a Servlet, or null if none
	 * @throws BeansException in case of initialization errors
	 */
	void initNestedContext(ServletContext servletContext, String namespace,
												 WebApplicationContext parent, Object owner) throws BeansException;

	/**
	 * Return the namespace of this context, or null if root.
	 */
	String getNamespace();

}
