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

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.scope.RequestScope;
import org.springframework.web.context.scope.SessionScope;

/**
 * Static ApplicationContext for Portlet environments. This implementation is intended
 * for testing, not for use in production applications.
 *
 * <p>Interprets resource paths as portlet context resources, that is, as paths beneath
 * the portlet application root. Absolute paths, for example for files outside the portlet
 * app root, can be accessed via "file:" URLs, as implemented by AbstractApplicationContext.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.0
 */
public class StaticPortletApplicationContext extends StaticApplicationContext
		implements ConfigurablePortletApplicationContext {

	private ServletContext servletContext;
	
	private PortletContext portletContext;

	private PortletConfig portletConfig;

	private String namespace;


	public StaticPortletApplicationContext() {
		setDisplayName("Root Portlet ApplicationContext");
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
		return portletContext;
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
			setDisplayName("Portlet ApplicationContext for namespace '" + namespace + "'");
		}
	}

	protected String getNamespace() {
		return this.namespace;
	}

	public void setConfigLocations(String[] configLocations) {
		throw new UnsupportedOperationException("StaticPortletApplicationContext does not support configLocations");
	}


	/**
	 * Register PortletContextAwareProcessor.
	 * @see PortletContextAwareProcessor
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		beanFactory.registerScope(SCOPE_REQUEST, new RequestScope());
		beanFactory.registerScope(SCOPE_SESSION, new SessionScope(false));
		beanFactory.registerScope(SCOPE_GLOBAL_SESSION, new SessionScope(true));

		beanFactory.addBeanPostProcessor(new PortletContextAwareProcessor(this.portletContext, this.portletConfig));
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

}
