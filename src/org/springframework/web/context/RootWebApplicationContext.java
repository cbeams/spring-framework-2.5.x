package org.springframework.web.context;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.context.config.ConfigurableApplicationContext;

/**
 * Interface to be implemented by root web application contexts.
 * Expected by ContextLoader.
 * @author Juergen Hoeller
 * @since 05.12.2003
 * @see ContextLoader
 */
public interface RootWebApplicationContext extends WebApplicationContext, ConfigurableApplicationContext {

	/**
	 * Context attribute to bind root WebApplicationContext to on successful startup.
	 */
	String WEB_APPLICATION_CONTEXT_ATTRIBUTE_NAME = WebApplicationContext.class + ".ROOT";

	/**
	 * Initialize this root web application context.
	 * @param servletContext the current ServletContext
	 * @throws BeansException in case of initialization errors
	 */
	void initRootContext(ServletContext servletContext) throws BeansException;

}
