package org.springframework.web.context.support;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.ui.context.support.StaticUiApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * Static WebApplicationContext implementation for testing.
 * Not for use in production applications.
 */
public class StaticWebApplicationContext extends StaticUiApplicationContext implements WebApplicationContext {

	private String namespace;

	private ServletContext servletContext;

	public StaticWebApplicationContext() {
	}

	public StaticWebApplicationContext(ApplicationContext parent, String namespace) {
		super(parent);
		this.namespace = namespace;
	}

	public String getNamespace() {
		return namespace;
	}

	/**
	 * Normally this would cause loading, but this class doesn't rely on loading.
	 * @see WebApplicationContext#setServletContext(ServletContext)
	 */
	public void setServletContext(ServletContext servletContext) throws ApplicationContextException, BeansException {
		this.servletContext = servletContext;
		refresh();
		WebApplicationContextUtils.publishConfigObjects(this);
		// Expose as a ServletContext object
		WebApplicationContextUtils.publishWebApplicationContext(this);
	}
	

	public ServletContext getServletContext() {
		return servletContext;
	}

}
