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

package org.springframework.web.servlet.mvc;

import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring Controller implementation that wraps a servlet instance which it manages
 * internally. Such a wrapped servlet is not known outside of this controller;
 * its entire lifecycle is covered here (in contrast to ServletForwardingController).
 *
 * <p>Useful to invoke an existing servlet via Spring's dispatching infrastructure,
 * for example to apply Spring HandlerInterceptors to its requests. This will work
 * even in a Servlet 2.2 container that does not support Servlet filters.
 *
 * <p>In particular, the main intent of this controller is to allow for applying
 * Spring's OpenSessionInViewInterceptor or OpenPersistenceManagerInViewInterceptor
 * to Struts actions in a Servlet 2.2 container. The Struts ActionServlet will be
 * wrapped by this controller in such a scenario, rather than defined in web.xml.
 * You then need to map "/*.do" (or whatever pattern you choose for your Struts actions)
 * onto this controller, which will in turn forward to the Struts ActionServlet.
 *
 * <p>Note that Struts has a special requirement in that it parses web.xml to
 * find its servlet mapping. Therefore, you need to specify the DispatcherServlet's
 * servlet name as "servletName" on this controller, so that Struts finds the
 * DispatcherServlet's mapping (thinking that it refers to the ActionServlet).
 *
 * <p>In a Servlet 2.3 container, when not using Spring's own web MVC framework,
 * it is recommended to use classic servlet mapping in combination with a filter,
 * for example Spring's OpenSessionInViewFilter or OpenPersistenceManagerInViewFilter.
 *
 * <p><b>Example:</b> a DispatcherServlet XML context, forwarding "*.do" to the Struts
 * ActionServlet wrapped by a ServletWrappingController. All such requests will go
 * through the configured HandlerInterceptor chain (e.g. an OpenSessionInViewInterceptor).
 * From the Struts point of view, everything will work as usual.
 *
 * <pre>
 * &lt;bean id="urlMapping" class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping"&gt;
 *   &lt;property name="interceptors"&gt;
 *     &lt;list&gt;
 *       &lt;ref bean="openSessionInViewInterceptor"/&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 *   &lt;property name="mappings"&gt;
 *     &lt;props&gt;
 *       &lt;prop key="*.do"&gt;strutsWrappingController&lt;/prop&gt;
 *     &lt;/props&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="strutsWrappingController" class="org.springframework.web.servlet.mvc.ServletWrappingController"&gt;
 *   &lt;property name="servletClass"&gt;
 *     &lt;value&gt;org.apache.struts.action.ActionServlet&lt;/value&gt;
 *   &lt;/property&gt;
 *   &lt;property name="servletName"&gt;
 *     &lt;value&gt;action&lt;/value&gt;
 *   &lt;/property&gt;
 *   &lt;property name="initParameters"&gt;
 *     &lt;props&gt;
 *       &lt;prop key="config"&gt;/WEB-INF/struts-config.xml&lt;/prop&gt;
 *     &lt;/props&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * Thanks to Keith Garry Boyce for pointing out the issue with Struts in a
 * Servlet 2.2 container, and for providing a prototype for accessing Struts
 * through Spring's web dispatching infrastructure!
 *
 * @author Juergen Hoeller
 * @since 1.1.1
 * @see ServletForwardingController
 * @see org.springframework.orm.hibernate.support.OpenSessionInViewInterceptor
 * @see org.springframework.orm.hibernate.support.OpenSessionInViewFilter
 * @see org.springframework.orm.jdo.support.OpenPersistenceManagerInViewInterceptor
 * @see org.springframework.orm.jdo.support.OpenPersistenceManagerInViewFilter
 */
public class ServletWrappingController extends AbstractController
    implements BeanNameAware, InitializingBean, DisposableBean {

	private Class servletClass;

	private String servletName;

	private Properties initParameters = new Properties();

	private String beanName;

	private Servlet servletInstance;


	/**
	 * Set the class of the servlet to wrap.
	 * Needs to implement <code>javax.servlet.Servlet</code>.
	 * @see javax.servlet.Servlet
	 */
	public void setServletClass(Class servletClass) {
		this.servletClass = servletClass;
	}

	/**
	 * Set the name of the servlet to wrap.
	 * Default is the bean name of this controller.
	 */
	public void setServletName(String servletName) {
		this.servletName = servletName;
	}

	/**
	 * Specify init parameters for the servlet to wrap,
	 * as name-value pairs.
	 */
	public void setInitParameters(Properties initParameters) {
		this.initParameters = initParameters;
	}

	public void setBeanName(String name) {
		this.beanName = name;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.servletClass == null) {
			throw new IllegalArgumentException("servletClass is required");
		}
		if (!Servlet.class.isAssignableFrom(this.servletClass)) {
			throw new IllegalArgumentException("servletClass [" + this.servletClass.getName() +
			    "] needs to implement interface [javax.servlet.Servlet]");
		}
		if (this.servletName == null) {
			this.servletName = this.beanName;
		}
		this.servletInstance = (Servlet) this.servletClass.newInstance();
		this.servletInstance.init(new DelegatingServletConfig());
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		this.servletInstance.service(request, response);
		return null;
	}

	public void destroy() {
		this.servletInstance.destroy();
	}


	/**
	 * Internal implementation of the ServletConfig interface, to be passed
	 * to the wrapped servlet. Delegates to ServletWrappingController fields
	 * and methods to provide init parameters and other environment info.
	 */
	private class DelegatingServletConfig implements ServletConfig {

		public String getServletName() {
			return servletName;
		}

		public ServletContext getServletContext() {
			return getWebApplicationContext().getServletContext();
		}

		public String getInitParameter(String paramName) {
			return initParameters.getProperty(paramName);
		}

		public Enumeration getInitParameterNames() {
			return initParameters.keys();
		}
	}

}
