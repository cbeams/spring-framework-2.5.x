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

package org.springframework.web.servlet.handler;

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.web.context.ServletContextAware;

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
 * <p><b>Alternatively, consider wrapping a Servlet with Spring's
 * ServletWrappingController.</b> This is particularly appropriate for
 * existing Servlet classes, allowing to specify Servlet initialization
 * parameters etc.
 *
 * @author Juergen Hoeller
 * @since 1.1.5
 * @see javax.servlet.Servlet
 * @see javax.servlet.ServletConfig
 * @see SimpleServletHandlerAdapter
 * @see org.springframework.web.servlet.HttpServletBean
 * @see org.springframework.web.servlet.mvc.ServletWrappingController
 */
public class SimpleServletPostProcessor implements DestructionAwareBeanPostProcessor, ServletContextAware {

	private ServletContext servletContext;

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Servlet) {
			try {
				((Servlet) bean).init(new DelegatingServletConfig(beanName, this.servletContext));
			}
			catch (ServletException ex) {
				throw new BeanInitializationException("Servlet.init threw exception", ex);
			}
		}
		return bean;
	}

	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		if (bean instanceof Servlet) {
			((Servlet) bean).destroy();
		}
	}


	/**
	 * Internal implementation of the ServletConfig interface, to be passed
	 * to the wrapped servlet. Delegates to ServletWrappingController fields
	 * and methods to provide init parameters and other environment info.
	 */
	private static class DelegatingServletConfig implements ServletConfig {

		private final String servletName;

		private final ServletContext servletContext;

		public DelegatingServletConfig(String servletName, ServletContext servletContext) {
			this.servletName = servletName;
			this.servletContext = servletContext;
		}

		public String getServletName() {
			return servletName;
		}

		public ServletContext getServletContext() {
			return servletContext;
		}

		public String getInitParameter(String paramName) {
			return null;
		}

		public Enumeration getInitParameterNames() {
			return Collections.enumeration(Collections.EMPTY_SET);
		}
	}

}
