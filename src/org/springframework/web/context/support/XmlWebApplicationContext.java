/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.context.support;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
import org.springframework.ui.context.support.AbstractXmlUiApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.util.ServletContextResource;

/**
 * WebApplicationContext implementation that takes configuration from an XML document.
 *
 * <p>By default, the configuration will be taken from "/WEB-INF/applicationContext.xml"
 * for the root context, and "/WEB-INF/test-servlet.xml" for a context with the namespace
 * "test-servlet" (like for a DispatcherServlet instance with the web.xml servlet-name "test").
 * These config location defaults can be overridden via setConfigLocations, respectively
 * via the "contextConfigLocation" parameters of ContextLoader and FrameworkServlet.
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files. This can be leveraged to
 * deliberately override certain bean definitions via an extra XML file.
 *
 * <p>Interprets resource paths as servlet context resources, i.e. as paths beneath
 * the web application root. Absolute paths, e.g. for files outside the web app root,
 * can be accessed via "file:" URLs, as implemented by AbstractApplicationContext.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setNamespace
 * @see #setConfigLocations
 * @see org.springframework.web.context.ContextLoader#initWebApplicationContext
 * @see org.springframework.web.servlet.FrameworkServlet#initWebApplicationContext
 * @see org.springframework.context.support.AbstractApplicationContext#getResource
 */
public class XmlWebApplicationContext extends AbstractXmlUiApplicationContext
		implements ConfigurableWebApplicationContext {

	/** Default config location for the root context */
	public static final String DEFAULT_CONFIG_LOCATION = "/WEB-INF/applicationContext.xml";

	/** Default prefix for building a config location for a namespace */
	public static final String DEFAULT_CONFIG_LOCATION_PREFIX = "/WEB-INF/";

	/** Default suffix for building a config location for a namespace */
	public static final String DEFAULT_CONFIG_LOCATION_SUFFIX = ".xml";


	/** Servlet context that this context runs in */
	private ServletContext servletContext;

	/** Namespace of this context, or null if root */
	private String namespace = null;

	/** Paths to XML configuration files */
	private String[] configLocations;


	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public ServletContext getServletContext() {
		return this.servletContext;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	protected String getNamespace() {
		return this.namespace;
	}

	public void setConfigLocations(String[] configLocations) {
		this.configLocations = configLocations;
	}

	protected String[] getConfigLocations() {
		return this.configLocations;
	}


	public void refresh() throws BeansException {
		if (this.namespace != null) {
			setDisplayName("XmlWebApplicationContext for namespace '" + this.namespace + "'");
			if (this.configLocations == null || this.configLocations.length == 0) {
				this.configLocations = new String[] {DEFAULT_CONFIG_LOCATION_PREFIX + this.namespace +
						DEFAULT_CONFIG_LOCATION_SUFFIX};
			}
		}
		else {
			setDisplayName("Root XmlWebApplicationContext");
			if (this.configLocations == null || this.configLocations.length == 0) {
				this.configLocations = new String[] {DEFAULT_CONFIG_LOCATION};
			}
		}
		super.refresh();
	}

	/**
	 * Initialize the config locations for the current namespace.
	 * This can be overridden in subclasses for custom config lookup.
	 */
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
		for (int i = 0; i < this.configLocations.length; i++) {
			reader.loadBeanDefinitions(getResource(this.configLocations[i]));
		}
	}

	/**
	 * This implementation supports file paths beneath the root of the web application.
	 */
	protected Resource getResourceByPath(String path) throws IOException {
		if (path != null && !path.startsWith("/")) {
			// the Servlet spec requires that resource paths start with a slash,
			// even if many containers accept paths without leading slash too
			path = "/" + path;
		}
		return new ServletContextResource(this.servletContext, path);
	}

	/**
	 * Return diagnostic information.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString() + "; ");
		sb.append("config locations=[" + StringUtils.arrayToCommaDelimitedString(this.configLocations) + "]; ");
		return sb.toString();
	}

}
