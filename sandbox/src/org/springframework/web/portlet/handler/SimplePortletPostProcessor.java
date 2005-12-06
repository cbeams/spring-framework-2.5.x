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

package org.springframework.web.portlet.handler;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.web.portlet.context.PortletContextAware;

/**
 * Bean post-processor that applies initialization and destruction callbacks
 * to beans that implement the Servlet interface.
 *
 * <p>After initialization of the bean instance, the Servlet <code>init</code>
 * method will be called with a ServletConfig that contains the bean name
 * of the Servlet and the ServletContext that it is running in.
 *
 * <p>Before destruction of the bean instance, the Servlet <code>destroy</code>
 * will be called.
 *
 * <p><b>Note that this post-processor does not support Servlet initialization
 * parameters.</b> Bean instances that implement the Servlet interface are
 * supposed to be configured like any other Spring bean, that is, through
 * constructor arguments or bean properties.
 *
 * <p>For reuse of a Servlet implementation in a plain Servlet container and as
 * a bean in a Spring context, consider deriving from Spring's HttpServletBean
 * base class that applies Servlet initialization parameters as bean properties,
 * supporting both initialization styles.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see javax.portlet.Portlet
 * @see javax.portlet.PortletConfig
 * @see org.springframework.web.portlet.handler.SimplePortletHandlerAdapter
 * @see org.springframework.web.portlet.PortletBean
 */
public class SimplePortletPostProcessor implements DestructionAwareBeanPostProcessor, PortletContextAware {

	private boolean useSharedPortletConfig = true;

	private PortletContext portletContext;

	private PortletConfig portletConfig;


	/**
	 * Set whether to use the shared PortletConfig object passed in
	 * through <code>setPortletConfig</code>, if available.
	 * <p>Default is "true". Turn this setting to "false" to pass in
	 * a mock PortletConfig object with the bean name as servlet name,
	 * holding the current PortletContext.
	 * @see #setPortletConfig
	 */
	public void setUseSharedPortletConfig(boolean useSharedPortletConfig) {
		this.useSharedPortletConfig = useSharedPortletConfig;
	}

	public void setPortletContext(PortletContext portletContext) {
		this.portletContext = portletContext;
	}

	public void setPortletConfig(PortletConfig portletConfig) {
		this.portletConfig = portletConfig;
	}


	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Portlet) {
			PortletConfig config = this.portletConfig;
			if (config == null || !this.useSharedPortletConfig) {
				config = new DelegatingPortletConfig(beanName, this.portletContext);
			}
			try {
				((Portlet) bean).init(config);
			}
			catch (PortletException ex) {
				throw new BeanInitializationException("Portlet.init threw exception", ex);
			}
		}
		return bean;
	}

	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		if (bean instanceof Portlet) {
			((Portlet) bean).destroy();
		}
	}


	/**
	 * Internal implementation of the PortletConfig interface, to be passed
	 * to the wrapped servlet.
	 */
	private static class DelegatingPortletConfig implements PortletConfig {

		private final String portletName;

		private final PortletContext portletContext;

		public DelegatingPortletConfig(String portletName, PortletContext portletContext) {
			this.portletName = portletName;
			this.portletContext = portletContext;
		}

		public String getPortletName() {
			return portletName;
		}

		public PortletContext getPortletContext() {
			return portletContext;
		}

		public String getInitParameter(String paramName) {
			return null;
		}

		public Enumeration getInitParameterNames() {
			return Collections.enumeration(Collections.EMPTY_SET);
		}

		public ResourceBundle getResourceBundle(Locale locale) {
			return null;
		}
	}

}
