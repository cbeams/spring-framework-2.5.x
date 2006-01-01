/*
 * Copyright 2002-2005 the original author or authors.
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
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.UiApplicationContextUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ServletContextAware;

/**
 * AbstractRefreshableApplicationContext subclass that implements the
 * ConfigurableWebApplicationContext interface for web environments.
 * Pre-implements a "configLocation" property, to be populated through the
 * ConfigurableWebApplicationContext interface on web application startup.
 *
 * <p>This class is as easy to subclass as AbstractRefreshableApplicationContext:
 * All you need to implements is the <code>loadBeanDefinitions</code> method;
 * see the superclass javadoc for details. Note that implementations are supposed
 * to load bean definitions from the files specified by the locations returned
 * by the <code>getConfigLocations</code> method.
 *
 * <p>Interprets resource paths as servlet context resources, i.e. as paths beneath
 * the web application root. Absolute paths, e.g. for files outside the web app root,
 * can be accessed via "file:" URLs, as implemented by AbstractApplicationContext.
 *
 * <p>In addition to the special beans detected by AbstractApplicationContext,
 * this class detects a ThemeSource bean in the context, with the name
 * "themeSource".
 *
 * <p><b>This is the web context to be subclassed for a different bean definition format.</b>
 * Such a context implementation can be specified as "contextClass" context-param
 * for ContextLoader or "contextClass" init-param for FrameworkServlet, replacing
 * the default XmlWebApplicationContext. It would automatically receive the
 * "contextConfigLocation" context-param or init-param, respectively.
 *
 * <p>Note that WebApplicationContext implementations are generally supposed
 * to configure themselves based on the configuration received through the
 * ConfigurableWebApplicationContext interface. In contrast, a standalone
 * application context might allow for configuration in custom startup code
 * (for example, GenericApplicationContext).
 *
 * @author Juergen Hoeller
 * @since 1.1.3
 * @see #loadBeanDefinitions
 * @see #getConfigLocations
 * @see org.springframework.web.context.ConfigurableWebApplicationContext#setConfigLocations
 * @see ServletContextResourcePatternResolver
 * @see org.springframework.context.support.AbstractApplicationContext
 * @see org.springframework.ui.context.ThemeSource
 * @see XmlWebApplicationContext
 * @see org.springframework.context.support.GenericApplicationContext
 */
public abstract class AbstractRefreshableWebApplicationContext extends AbstractRefreshableApplicationContext
		implements ConfigurableWebApplicationContext{

	/** Servlet context that this context runs in */
	private ServletContext servletContext;

	/** Namespace of this context, or <code>null</code> if root */
	private String namespace;

	/** Paths to XML configuration files */
	private String[] configLocations;

	/** the ThemeSource for this ApplicationContext */
	private ThemeSource themeSource;


	public AbstractRefreshableWebApplicationContext() {
		setDisplayName("Root WebApplicationContext");
	}


	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public ServletContext getServletContext() {
		return this.servletContext;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
		if (namespace != null) {
			setDisplayName("WebApplicationContext for namespace '" + namespace + "'");
		}
	}

	protected String getNamespace() {
		return this.namespace;
	}

	public void setConfigLocations(String[] locations) {
		this.configLocations = new String[locations.length];
		for (int i = 0; i < locations.length; i++) {
			this.configLocations[i] = resolvePath(locations[i]);
		}
	}

	protected String[] getConfigLocations() {
		return this.configLocations;
	}


	/**
	 * Sets a default config location if no explicit config location specified.
	 * @see #getDefaultConfigLocations
	 * @see #setConfigLocations
	 */
	public void refresh() throws BeansException {
		if (ObjectUtils.isEmpty(getConfigLocations())) {
			setConfigLocations(getDefaultConfigLocations());
		}
		super.refresh();
	}

	/**
	 * Return the default config locations to use, for the case where no explicit
	 * config locations have been specified.
	 * <p>Default implementation returns null, requiring explicit config locations.
	 * @see #setConfigLocations
	 */
	protected String[] getDefaultConfigLocations() {
		return null;
	}

	/**
	 * Register ServletContextAwareProcessor.
	 * @see ServletContextAwareProcessor
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext));
		beanFactory.ignoreDependencyInterface(ServletContextAware.class);
	}

	/**
	 * Resolve the given path, replacing placeholders with corresponding
	 * system property values if necessary. Applied to config locations.
	 * @param path the original file path
	 * @return the resolved file path
	 * @see org.springframework.util.SystemPropertyUtils#resolvePlaceholders
	 */
	protected String resolvePath(String path) {
		return SystemPropertyUtils.resolvePlaceholders(path);
	}

	/**
	 * This implementation supports file paths beneath the root of the ServletContext.
	 * @see ServletContextResource
	 */
	protected Resource getResourceByPath(String path) {
		return new ServletContextResource(this.servletContext, path);
	}

	/**
	 * This implementation supports pattern matching in unexpanded WARs too.
	 * @see ServletContextResourcePatternResolver
	 */
	protected ResourcePatternResolver getResourcePatternResolver() {
		return new ServletContextResourcePatternResolver(this);
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
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append("; ");
		sb.append("config locations [");
		sb.append(StringUtils.arrayToCommaDelimitedString(this.configLocations));
		sb.append("]");
		return sb.toString();
	}

}
