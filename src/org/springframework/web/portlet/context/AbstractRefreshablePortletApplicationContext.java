/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.portlet.context;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.scope.RequestScope;
import org.springframework.web.context.scope.SessionScope;
import org.springframework.web.context.support.ServletContextAwareProcessor;

/**
 * AbstractRefreshableApplicationContext subclass that implements the
 * ConfigurablePortletApplicationContext interface for portlet environments.
 * Pre-implements a "configLocation" property, to be populated through the
 * ConfigurablePortletApplicationContext interface on portlet application startup.
 *
 * <p>This class is as easy to subclass as AbstractRefreshableApplicationContext:
 * All you need to implement is the <code>loadBeanDefinitions</code> method;
 * see the superclass javadoc for details. Note that implementations are supposed
 * to load bean definitions from the files specified by the locations returned
 * by the <code>getConfigLocations</code> method.
 *
 * <p>Interprets resource paths as portlet context resources, i.e. as paths beneath
 * the web application root. Absolute paths, e.g. for files outside the web app root,
 * can be accessed via "file:" URLs, as implemented by AbstractApplicationContext.
 *
 * <p>In addition to the special beans detected by AbstractApplicationContext,
 * this class detects a ThemeSource bean in the context, with the name
 * "themeSource".
 *
 * <p><b>This is the portlet context to be subclassed for a different bean definition format.</b>
 * Such a context implementation can be specified as "contextClass" context-param
 * for ContextLoader or "contextClass" init-param for FrameworkPortlet, replacing
 * the default XmlPortletApplicationContext. It would automatically receive the
 * "contextConfigLocation" context-param or init-param, respectively.
 *
 * <p>Note that PortletApplicationContext implementations are generally supposed
 * to configure themselves based on the configuration received through the
 * ConfigurablePortletApplicationContext interface. In contrast, a standalone
 * application context might allow for configuration in custom startup code
 * (for example, GenericApplicationContext).
 *
 * @author Juergen Hoeller
 * @author John A. Lewis
 * @since 2.0
 * @see #loadBeanDefinitions
 * @see #getConfigLocations
 * @see org.springframework.web.portlet.context.ConfigurablePortletApplicationContext#setConfigLocations
 * @see PortletContextResourcePatternResolver
 * @see org.springframework.context.support.AbstractApplicationContext
 * @see org.springframework.ui.context.ThemeSource
 * @see XmlPortletApplicationContext
 * @see org.springframework.context.support.GenericApplicationContext
 */
public abstract class AbstractRefreshablePortletApplicationContext extends AbstractRefreshableApplicationContext
		implements WebApplicationContext, ConfigurablePortletApplicationContext {

	/** Servlet context that this context runs in */
	private ServletContext servletContext;

	/** Portlet context that this context runs in */
	private PortletContext portletContext;

	/** Portlet config that this context runs in */
	private PortletConfig portletConfig;

	/** Namespace of this context, or null if root */
	private String namespace;

	/** Paths to XML configuration files */
	private String[] configLocations;


	public AbstractRefreshablePortletApplicationContext() {
		setDisplayName("Root PortletApplicationContext");
	}

	public void setParent(ApplicationContext parent) {
		super.setParent(parent);
		if (parent instanceof WebApplicationContext) {
			this.servletContext = ((WebApplicationContext) parent).getServletContext();
		}
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public void setPortletContext(PortletContext portletContext) {
		this.portletContext = portletContext;
	}

	public PortletContext getPortletContext() {
		return this.portletContext;
	}

	public void setPortletConfig(PortletConfig portletConfig) {
		this.portletConfig = portletConfig;
		if (portletConfig != null && this.portletContext == null) {
			this.portletContext = portletConfig.getPortletContext();
		}
	}

	public PortletConfig getPortletConfig() {
		return portletConfig;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
		if (namespace != null) {
			setDisplayName("PortletApplicationContext for namespace '" + namespace + "'");
		}
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


	/**
	 * Sets a default config location if no explicit config location specified.
	 * @see #getDefaultConfigLocations
	 * @see #setConfigLocations
	 */
	public void refresh() throws BeansException {
		if (this.configLocations == null || this.configLocations.length == 0) {
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
	 * Register PortletContextAwareProcessor.
	 * @see PortletContextAwareProcessor
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		beanFactory.registerScope(SCOPE_REQUEST, new RequestScope());
		beanFactory.registerScope(SCOPE_SESSION, new SessionScope(false));
		beanFactory.registerScope(SCOPE_GLOBAL_SESSION, new SessionScope(true));

		beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext));
		beanFactory.addBeanPostProcessor(new PortletContextAwareProcessor(this.portletContext, this.portletConfig));
		beanFactory.ignoreDependencyInterface(ServletContextAware.class);
		beanFactory.ignoreDependencyInterface(PortletContextAware.class);
		beanFactory.ignoreDependencyInterface(PortletConfigAware.class);
	}

	/**
	 * This implementation supports file paths beneath the root of the PortletContext.
	 * @see PortletContextResource
	 */
	protected Resource getResourceByPath(String path) {
		return new PortletContextResource(this.portletContext, path);
	}

	/**
	 * This implementation supports pattern matching in unexpanded WARs too.
	 * @see PortletContextResourcePatternResolver
	 */
	protected ResourcePatternResolver getResourcePatternResolver() {
		return new PortletContextResourcePatternResolver(this);
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
