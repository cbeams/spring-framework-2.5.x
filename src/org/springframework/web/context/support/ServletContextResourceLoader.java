package org.springframework.web.context.support;

import javax.servlet.ServletContext;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * ResourceLoader implementation that resolves paths as ServletContext
 * resources, for use outside a WebApplicationContext.
 * @author Juergen Hoeller
 * @since 28.04.2004
 * @see #getResourceByPath
 * @see org.springframework.web.servlet.HttpServletBean
 * @see org.springframework.web.filter.GenericFilterBean
 */
public class ServletContextResourceLoader extends DefaultResourceLoader {

	private final ServletContext servletContext;

	/**
	 * Create a new ServletContextResourceLoader.
	 * @param servletContext the ServletContext to resolve resources with.
	 */
	public ServletContextResourceLoader(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * This implementation supports file paths beneath the root of the web application.
	 */
	protected Resource getResourceByPath(String path) {
		return new ServletContextResource(this.servletContext, path);
	}

}
