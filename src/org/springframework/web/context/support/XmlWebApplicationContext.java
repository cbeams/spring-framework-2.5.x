/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.context.support;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.ui.context.support.AbstractXmlUiApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

/**
 * WebApplicationContext implementation that takes configuration from an XML document.
 *
 * <p>Supports various servlet context init parameters for config file lookup.
 * By default, the lookup occurs in the web app's WEB-INF directory, looking for
 * "WEB-INF/applicationContext.xml" for the root context, and
 * "WEB-INF/test-servlet.xml" for a namespaced context with the name "test-servlet"
 * (like for a DispatcherServlet instance with the web.xml servlet name "test").
 *
 * <p>A config location can consist of multiple names of XML files, separated by any
 * number of commas and spaces, like "applicationContext1.xml, applicationContext2.xml".
 * For example, a root application context definition can be collected from multiple
 * XML files by simply setting multiple names as "contextConfigLocation" context-param.
 * Note that bean definitions in a </i>later</i> XML file will override ones of the
 * same name in a previous file.
 *
 * <p>Interprets (file) paths as servlet context resources, i.e. as paths beneath
 * the web application root. Thus, absolute paths, i.e. files outside the web app
 * root, should be accessed via "file:" URLs.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.context.ContextLoader#initContext
 * @see org.springframework.web.servlet.FrameworkServlet#getNamespace
 */
public class XmlWebApplicationContext extends AbstractXmlUiApplicationContext	implements WebApplicationContext {

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


	/** Namespace of this context, or null if root */
	private String namespace = null;

	/** Servlet context that this context runs in */
	private ServletContext servletContext;

	/** Path from which the configuration was loaded */
	private String[] configLocations;


	/**
	 * Create a new root web application context, for use in an entire web application.
	 * This context will be the parent for individual servlet contexts.
	 */
	public XmlWebApplicationContext() {
		setDisplayName("Root WebApplicationContext");
	}
	
	/** 
	 * Create a new child WebApplicationContext.
	 */
	public XmlWebApplicationContext(ApplicationContext parent, String namespace) {
		super(parent);
		this.namespace = namespace;
		setDisplayName("WebApplicationContext for namespace [" + namespace + "]");
	}

	/**
	 * Return the namespace of this context, or null if root.
	 */
	public String getNamespace() {
		return this.namespace;
	}

	/**
	 * Initialize and attach to the given context.
	 * @param servletContext ServletContext to use to load configuration,
	 * and in which this web application context should be set as an attribute
	 */
	public void setServletContext(ServletContext servletContext) throws BeansException {
		this.servletContext = servletContext;
		this.configLocations = initConfigLocations();
		logger.info("Using config location [" + StringUtils.arrayToCommaDelimitedString(this.configLocations) + "]");
		refresh();
		
		if (this.namespace == null) {
			// We're the root context
			WebApplicationContextUtils.publishConfigObjects(this);
			// Expose as a ServletContext object
			WebApplicationContextUtils.publishWebApplicationContext(this);
		}	
	}

	public ServletContext getServletContext() {
		return this.servletContext;
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
	 * <p>Default implementation returns the namespace with the default prefix
	 * "WEB-INF/" and suffix ".xml", if a namespace is set. For the root context,
	 * the "configLocation" servlet context parameter is used, falling back to
	 * "WEB-INF/applicationContext.xml" if no parameter is found.
	 * @return the URL or path of the configuration
	 */
	protected String[] initConfigLocations() {
		if (getNamespace() != null) {
			String configLocationPrefix = this.servletContext.getInitParameter(CONFIG_LOCATION_PREFIX_PARAM);
			String prefix = (configLocationPrefix != null) ? configLocationPrefix : DEFAULT_CONFIG_LOCATION_PREFIX;
			String configLocationSuffix = this.servletContext.getInitParameter(CONFIG_LOCATION_SUFFIX_PARAM);
			String suffix = (configLocationSuffix != null) ? configLocationSuffix : DEFAULT_CONFIG_LOCATION_SUFFIX;
			return new String[] {prefix + getNamespace() + suffix};
		}
		else {
			String configLocation = this.servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
			if (configLocation != null) {
				return StringUtils.tokenizeToStringArray(configLocation, CONFIG_LOCATION_DELIMITERS, true, true);
			}
			else {
				return new String[] {DEFAULT_CONFIG_LOCATION};
			}
		}
	}

	protected void loadBeanDefinitions(XmlBeanFactory beanFactory) throws BeansException, IOException {
		for (int i = 0; i < this.configLocations.length; i++) {
			beanFactory.loadBeanDefinitions(getResourceAsStream(this.configLocations[i]));
		}
	}

	/**
	 * This implementation supports file paths beneath the root
	 * of the web application.
	 */
	protected InputStream getResourceByPath(String path) throws IOException {
		if (path != null && !path.startsWith("/")) {
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
