package org.springframework.web.context.support;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.ui.context.support.StaticUiApplicationContext;
import org.springframework.web.context.NestedWebApplicationContext;
import org.springframework.web.context.RootWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * Static WebApplicationContext implementation for testing.
 * Not for use in production applications.
 */
public class StaticWebApplicationContext extends StaticUiApplicationContext
		implements RootWebApplicationContext, NestedWebApplicationContext {

	private ServletContext servletContext;

	private String namespace;

	public void initRootContext(ServletContext servletContext) {
		initRootContext(servletContext, null);
	}

	public void initRootContext(ServletContext servletContext, ApplicationContext parent) throws BeansException {
		this.servletContext = servletContext;
		if (parent != null)
			setParent(parent);
	}
	
	
	public void initNestedContext(ServletContext servletContext, String namespace,
																WebApplicationContext parent, Object owner) {
		this.servletContext = servletContext;
		this.namespace = namespace;
		setParent(parent);
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public String getNamespace() {
		return namespace;
	}


}
