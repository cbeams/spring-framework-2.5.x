package org.springframework.web.context.support;

import java.io.File;

import javax.servlet.ServletContext;

import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.WebUtils;

/**
 * Convenient superclass for application objects running in a
 * web application context.
 * @author Juergen Hoeller
 * @since 28.08.2003
 */
public abstract class WebApplicationObjectSupport extends ApplicationObjectSupport {

	protected Class requiredContextClass() {
		return WebApplicationContext.class;
	}

	/**
	 * Return the current application context as WebApplicationContext.
	 */
	protected final WebApplicationContext getWebApplicationContext() {
		return (WebApplicationContext) getApplicationContext();
	}

	/**
	 * Return the current ServletContext.
	 */
	protected final ServletContext getServletContext() {
		return getWebApplicationContext().getServletContext();
	}

	/**
	 * Return the temporary directory for the current web application,
	 * as provided by the servlet container.
	 * @return the File representing the temporary directory
	 */
	protected File getTempDir() {
		return WebUtils.getTempDir(getServletContext());
	}

}
