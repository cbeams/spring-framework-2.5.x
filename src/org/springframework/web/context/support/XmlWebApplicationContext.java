/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.web.context.support;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.UiApplicationContextUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * WebApplicationContext implementation that takes configuration from an XML document.
 *
 * <p>By default, the configuration will be taken from "/WEB-INF/applicationContext.xml"
 * for the root context, and "/WEB-INF/test-servlet.xml" for a context with the namespace
 * "test-servlet" (like for a DispatcherServlet instance with the servlet-name "test").
 *
 * <p>The config location defaults can be overridden via setConfigLocations,
 * respectively via the "contextConfigLocation" parameters of ContextLoader and
 * FrameworkServlet. Config locations can either denote concrete files like
 * "/WEB-INF/context.xml" or Ant-style patterns like "/WEB-INF/*-context.xml"
 * (see PathMatcher javadoc for pattern details).
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files. This can be leveraged to
 * deliberately override certain bean definitions via an extra XML file.
 *
 * <p>Interprets resource paths as servlet context resources, i.e. as paths beneath
 * the web application root. Absolute paths, e.g. for files outside the web app root,
 * can be accessed via "file:" URLs, as implemented by AbstractApplicationContext.
 *
 * <p>In addition to the special beans detected by AbstractApplicationContext,
 * this class detects a ThemeSource bean in the context, with the name
 * "themeSource".
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setNamespace
 * @see #setConfigLocations
 * @see #getResourcePatternResolver
 * @see org.springframework.web.context.ContextLoader#initWebApplicationContext
 * @see org.springframework.web.servlet.FrameworkServlet#initWebApplicationContext
 * @see org.springframework.context.support.AbstractApplicationContext#getResource
 * @see org.springframework.ui.context.ThemeSource
 */
public class XmlWebApplicationContext extends AbstractXmlApplicationContext
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

	/** the ThemeSource for this ApplicationContext */
	private ThemeSource themeSource;


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

	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext));
		beanFactory.ignoreDependencyType(ServletContext.class);
	}

	/**
	 * This implementation supports file paths beneath the root of the web application.
	 */
	protected Resource getResourceByPath(String path) {
		return new ServletContextResource(this.servletContext, path);
	}

	/**
	 * Initialize the theme capability.
	 */
	protected void onRefresh() {
		this.themeSource = UiApplicationContextUtils.initThemeSource(this);
	}

	public Theme getTheme(String themeName) {
		return this.themeSource.getTheme(themeName);
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
