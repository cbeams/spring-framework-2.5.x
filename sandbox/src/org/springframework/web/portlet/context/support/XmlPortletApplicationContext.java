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
package org.springframework.web.portlet.context.support;

import javax.portlet.PortletContext;
import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.UiApplicationContextUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.portlet.context.ConfigurablePortletApplicationContext;


/**
 * PortletApplicationContext implementation that takes configuration from an XML document.
 *
 * <p>By default, the configuration will be take from "/WEB-INF/test-portlet.xml" for a 
 * context with the namespace "test-portlet" (like for a DispatcherPortlet instance with
 * the portlet-name "test").
 *
 * <p>The config location defaults can be overridden via setConfigLocations,
 * respectively via the "contextConfigLocation" parameters of ContextLoader and
 * FrameworkPortlet. Config locations can either denote concrete files like
 * "/WEB-INF/context.xml" or Ant-style patterns like "/WEB-INF/*-context.xml"
 * (see PathMatcher javadoc for pattern details).
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files. This can be leveraged to
 * deliberately override certain bean definitions via an extra XML file.
 *
 * <p>Interprets resource paths as portlet context resources, i.e. as paths beneath
 * the web application root. Absolute paths, e.g. for files outside the portlet app root,
 * can be accessed via "file:" URLs, as implemented by AbstractApplicationContext.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author William G. Thompson, Jr.
 * @author Nick Lothian
 * @see #setNamespace
 * @see #setConfigLocations
 * @see org.springframework.web.portlet.FrameworkPortlet#initPortletApplicationContext
 * @see org.springframework.util.PathMatcher#retrieveMatchingFiles
 * @see org.springframework.context.support.AbstractApplicationContext#getResource
 */

public class XmlPortletApplicationContext extends AbstractXmlApplicationContext implements
        ConfigurablePortletApplicationContext {

	/** Default config location for the root context */
	//public static final String DEFAULT_CONFIG_LOCATION = "/WEB-INF/applicationContext.xml";
    // TODO default doesn't make sense anymore ??? since root is loaded by WAC

	/** Default prefix for building a config location for a namespace */
	public static final String DEFAULT_CONFIG_LOCATION_PREFIX = "/WEB-INF/";

	/** Default suffix for building a config location for a namespace */
	public static final String DEFAULT_CONFIG_LOCATION_SUFFIX = ".xml";

	/** Portlet context that this context runs in */
	private PortletContext portletContext;

	/** Namespace of this context, or null if root */
	private String namespace = null;

	/** Paths to XML configuration files */
	private String[] configLocations;
	
	/** the ThemeSource for this ApplicationContext. Required for Spring TagLibs */
	private ThemeSource themeSource;
	
	private ServletContext servletContext;
	

	public void setPortletContext(PortletContext portletContext) {
		this.portletContext = portletContext;
	}

	public PortletContext getPortletContext() {
		return this.portletContext;
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
			setDisplayName("XmlPortletApplicationContext for namespace '" + this.namespace + "'");
			if (this.configLocations == null || this.configLocations.length == 0) {
				this.configLocations = new String[] {DEFAULT_CONFIG_LOCATION_PREFIX + this.namespace +
						DEFAULT_CONFIG_LOCATION_SUFFIX};
			}
		}
		else {
		    // TODO throw BeansException?  only load PAC...
//			setDisplayName("Root XmlPortletApplicationContext");
//			if (this.configLocations == null || this.configLocations.length == 0) {
//				this.configLocations = new String[] {DEFAULT_CONFIG_LOCATION};
//			}
		}
		super.refresh();
	}

	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		beanFactory.addBeanPostProcessor(new PortletContextAwareProcessor(this.portletContext));
		beanFactory.ignoreDependencyType(PortletContext.class);
	}

	/**
	 * This implementation supports file paths beneath the root of the web application.
	 */
	protected Resource getResourceByPath(String path) {
		if (path != null && !path.startsWith("/")) {
			// the Servlet spec requires that resource paths start with a slash,
			// even if many containers accept paths without leading slash too
			path = "/" + path;
		}
		return new PortletContextResource(this.portletContext, path);
	}


	/**
	 * Return diagnostic information.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString() + "; ");
		sb.append("config locations=[" + StringUtils.arrayToCommaDelimitedString(this.configLocations) + "]; ");
		return sb.toString();
	}

	/**
	 * <p>This is required to use the Spring Tag Libraries. Should be used with
	 * caution as a Portal will normally supply equivalent functionality</p>
	 *
	 * @see org.springframework.ui.context.ThemeSource#getTheme(java.lang.String)
	 */
	public Theme getTheme(String themeName) {
		return this.themeSource.getTheme(themeName);
	}
	
	/**
	 * <p>Required by Spring Tag Libraries</p>
	 *
	 * @see org.springframework.web.context.WebApplicationContext#getServletContext()
	 */
	public ServletContext getServletContext() {
		return this.servletContext;
	}
	
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	/**
	 * @see org.springframework.context.support.AbstractApplicationContext#onRefresh()
	 */
	   protected void onRefresh() throws BeansException {
	       this.themeSource = UiApplicationContextUtils.initThemeSource(this);
	   }
}
