package org.springframework.web.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.springframework.core.io.AbstractResource;

/**
 * Resource implementation for ServletContext resources,
 * interpreting relative paths within the web application root.
 *
 * <p>Always supports stream access, but only allows java.io.File
 * access when the web application archive is expanded.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see javax.servlet.ServletContext#getResourceAsStream
 * @see javax.servlet.ServletContext#getRealPath
 */
public class ServletContextResource extends AbstractResource {

	private final ServletContext servletContext;

	private final String path;

	/**
	 * Create a new ServletContextResource.
	 * @param servletContext the ServletContext to load from
	 * @param path the path of the resource
	 */
	public ServletContextResource(ServletContext servletContext, String path) {
		this.servletContext = servletContext;
		this.path = path;
	}

	/**
	 * This implementation delegates to ServletContext.getResourceAsStream,
	 * but throws a FileNotFoundException if not found.
	 * @see javax.servlet.ServletContext#getResourceAsStream
	 */
	public InputStream getInputStream() throws IOException {
		InputStream is = this.servletContext.getResourceAsStream(this.path);
		if (is == null) {
			throw new FileNotFoundException("Could not open " + getDescription());
		}
		return is;
	}

	/**
	 * This implementation delegates to ServletContext.getRealPath,
	 * but throws a FileNotFoundException if not found or not resolvable.
	 * @see javax.servlet.ServletContext#getRealPath
	 */
	public File getFile() throws IOException {
		String realPath = this.servletContext.getRealPath(this.path);
		if (realPath != null) {
			return new File(realPath);
		}
		else {
			throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path - " +
																			"web application archive not expanded?");
		}
	}

	public String getDescription() {
		return "resource [" + this.path + "] of ServletContext '" + this.servletContext.getServletContextName() + "'";
	}

}
