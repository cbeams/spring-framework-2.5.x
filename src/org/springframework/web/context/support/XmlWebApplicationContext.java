/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.context.support;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.AbstractXmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.ui.context.support.AbstractXmlUiApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.NestedWebApplicationContext;
import org.springframework.web.context.RootWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * WebApplicationContext implementation that takes configuration from an XML document.
 * Implements both RootWebApplicationContext (for use with ContextLoader) and
 * NestedWebApplicationContext (for use with FrameworkServlet).
 *
 * <p>Supports various servlet context init parameters for config file lookup.
 * By default, the lookup occurs in the web app's WEB-INF directory, looking for
 * "WEB-INF/applicationContext.xml" for the root context, and
 * "WEB-INF/test-servlet.xml" for a namespaced context with the name "test-servlet"
 * (like for a DispatcherServlet instance with the web.xml servlet name "test").
 *
 * <p>Config locations can be overridden via the "contextConfigLocation" context-param
 * in web.xml for the root context, respectively via "contextConfigLocationPrefix" and
 * "contextConfigLocationSuffix" for child contexts (getting applied to the namespace).
 *
 * <p>A config location can consist of multiple names of XML files, separated by any
 * number of commas and spaces, like "applicationContext1.xml, applicationContext2.xml".
 * For example, a root application context definition can be collected from multiple
 * XML files by simply setting multiple names as "contextConfigLocation" context-param.
 * The bean definitions in the files are not allowed to overlap.
 *
 * <p>Interprets resource paths as servlet context resources, i.e. as paths beneath
 * the web application root. Absolute paths, e.g. for files outside the web app root,
 * can be accessed via "file:" URLs.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.context.ContextLoader#initContext
 * @see org.springframework.web.servlet.FrameworkServlet#createWebApplicationContext
 */
public class XmlWebApplicationContext extends AbstractXmlUiApplicationContext
		implements RootWebApplicationContext, NestedWebApplicationContext {

	/**
	 * Any number of these characters are considered delimiters
	 * between multiple context paths in a config location.
	 */
	public static final String CONFIG_LOCATION_DELIMITERS = ",; ";

	/**
	 * Name of servlet context parameter that can specify the config location
	 * for the root context, falling back to DEFAULT_CONFIG_LOCATION.
	 */
	public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";

	/**
	 * Name of servlet context parameter that can specify the config location prefix
	 * for namespaced contexts, falling back to DEFAULT_CONFIG_LOCATION_PREFIX.
	 */
	public static final String CONFIG_LOCATION_PREFIX_PARAM = "contextConfigLocationPrefix";

	/**
	 * Name of servlet context parameter that can specify the config location suffix
	 * for namespaced contexts, falling back to DEFAULT_CONFIG_LOCATION_SUFFIX.
	 */
	public static final String CONFIG_LOCATION_SUFFIX_PARAM = "contextConfigLocationSuffix";

	/** Default prefix for config locations, followed by the namespace */
	public static final String DEFAULT_CONFIG_LOCATION_PREFIX = "/WEB-INF/";

	/** Default suffix for config locations, following the namespace */
	public static final String DEFAULT_CONFIG_LOCATION_SUFFIX = ".xml";

	/** Default config location for the root context. */
	public static final String DEFAULT_CONFIG_LOCATION =
	    DEFAULT_CONFIG_LOCATION_PREFIX + "applicationContext" + DEFAULT_CONFIG_LOCATION_SUFFIX;


	/** Servlet context that this context runs in */
	private ServletContext servletContext;

	/** Namespace of this context, or null if root */
	private String namespace = null;

	/** Path from which the configuration was loaded */
	private String[] configLocations;


	public void initRootContext(ServletContext servletContext) throws BeansException {
		initRootContext(servletContext, null);
	}


	/* (non-Javadoc)
	 * @see org.springframework.web.context.RootWebApplicationContext#initRootContext(javax.servlet.ServletContext, org.springframework.context.ApplicationContext)
	 */
	public void initRootContext(ServletContext servletContext, ApplicationContext parent)
				throws BeansException {
		
		this.servletContext = servletContext;
		if (parent != null)
			setParent(parent);
		
		setDisplayName("Root WebApplicationContext");

		String configLocation = this.servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
		if (configLocation != null) {
			this.configLocations = StringUtils.tokenizeToStringArray(configLocation, CONFIG_LOCATION_DELIMITERS, true, true);
		}
		else {
			this.configLocations = new String[] {DEFAULT_CONFIG_LOCATION};
		}
		logger.info("Using config locations [" + StringUtils.arrayToCommaDelimitedString(this.configLocations) + "]");

		refresh();
		
	}
	
	
	public void initNestedContext(ServletContext servletContext, String namespace,
															 WebApplicationContext parent, Object owner) throws BeansException {
		this.servletContext = servletContext;
		this.namespace = namespace;
		setParent(parent);
		setDisplayName("WebApplicationContext for namespace '" + namespace + "'");

		String configLocationPrefix = this.servletContext.getInitParameter(CONFIG_LOCATION_PREFIX_PARAM);
		String prefix = (configLocationPrefix != null) ? configLocationPrefix : DEFAULT_CONFIG_LOCATION_PREFIX;
		String configLocationSuffix = this.servletContext.getInitParameter(CONFIG_LOCATION_SUFFIX_PARAM);
		String suffix = (configLocationSuffix != null) ? configLocationSuffix : DEFAULT_CONFIG_LOCATION_SUFFIX;
		this.configLocations = new String[] {prefix + getNamespace() + suffix};
		logger.info("Using config locations [" + StringUtils.arrayToCommaDelimitedString(this.configLocations) + "]");

		refresh();
	}

	public ServletContext getServletContext() {
		return this.servletContext;
	}

	public String getNamespace() {
		return this.namespace;
	}

	/**
	 * Return the URL or path of the configuration.
	 */
	protected String[] getConfigLocations() {
		return this.configLocations;
	}

	/**
	 * Initialize the config locations for the current namespace.
	 * This can be overridden in subclasses for custom config lookup.
	 */

	protected void loadBeanDefinitions(AbstractXmlBeanDefinitionReader reader) throws BeansException, IOException {
		for (int i = 0; i < this.configLocations.length; i++) {
			reader.loadBeanDefinitions(getResourceAsStream(this.configLocations[i]));
		}
	}

	/**
	 * This implementation supports file paths beneath the root of the web application.
	 */
	protected InputStream getResourceByPath(String path) throws IOException {
		if (path != null && !path.startsWith("/")) {
			// the Servlet spec requires that resource paths start with a slash,
			// even if many containers accept paths without leading slash too
			path = "/" + path;
		}
		return getServletContext().getResourceAsStream(path);
	}

	/**
	 * This implementation returns the real path of the root directory of the
	 * web application that this WebApplicationContext is associated with.
	 * @see org.springframework.context.ApplicationContext#getResourceBasePath
	 * @see javax.servlet.ServletContext#getRealPath
	 */
	public String getResourceBasePath() {
		return getServletContext().getRealPath("/");
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
