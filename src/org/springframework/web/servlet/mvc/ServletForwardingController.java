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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UrlPathHelper;

/**
 * Spring Controller implementation that forwards to a named servlet,
 * i.e. the "servlet-name" in web.xml rather than a URL path mapping.
 * A target servlet doesn't even need a "servlet-mapping" in web.xml
 * in the first place: A "servlet" declaration is sufficient.
 *
 * <p>Useful to invoke an existing servlet via Spring's dispatching infrastructure,
 * for example to apply Spring HandlerInterceptors to its requests. This will work
 * even in a Servlet 2.2 container that does not support Servlet filters.
 *
 * <p>In particular, the main intent of this controller is to allow for applying
 * Spring's OpenSessionInViewInterceptor or OpenPersistenceManagerInViewInterceptor
 * to servlets in a Servlet 2.2 container. The specified "servlet-name" will
 * simply refer to a custom servlet definition in web.xml in such a scenario.
 * You then need to map "/myservlet" (or whatever path you choose for your servlet)
 * onto this controller, which will in turn forward to your servlet.
 *
 * <p>In a Servlet 2.3 container, when not using Spring's own web MVC framework,
 * it is recommended to use classic servlet mapping in combination with a filter,
 * for example Spring's OpenSessionInViewFilter or OpenPersistenceManagerInViewFilter.
 *
 * <p><b>Example:</b> web.xml, mapping all "/myservlet" requests to a Spring dispatcher.
 * Also defines a custom "myServlet", but <i>without</i> servlet mapping.
 *
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;myServlet&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;mypackage.TestServlet&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 *
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;myDispatcher&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;org.springframework.web.servlet.DispatcherServlet&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 *
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;myDispatcher&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;/myservlet&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;</pre>
 *
 * <b>Example:</b> myDispatcher-servlet.xml, in turn forwarding "/myservlet" to your
 * servlet (identified by servlet name). All such requests will go through the
 * configured HandlerInterceptor chain (e.g. an OpenSessionInViewInterceptor).
 * From the servlet point of view, everything will work as usual.
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
 *       &lt;prop key="/myservlet"&gt;myServletForwardingController&lt;/prop&gt;
 *     &lt;/props&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="myServletForwardingController" class="org.springframework.web.servlet.mvc.ServletForwardingController"&gt;
 *   &lt;property name="servletName"&gt;&lt;value&gt;myServlet&lt;/value&gt;&lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Juergen Hoeller
 * @since 1.1.1
 * @see ServletWrappingController
 * @see org.springframework.orm.hibernate.support.OpenSessionInViewInterceptor
 * @see org.springframework.orm.hibernate.support.OpenSessionInViewFilter
 * @see org.springframework.orm.jdo.support.OpenPersistenceManagerInViewInterceptor
 * @see org.springframework.orm.jdo.support.OpenPersistenceManagerInViewFilter
 */
public class ServletForwardingController extends AbstractController implements BeanNameAware {

	private String servletName;

	private String beanName;

	/**
	 * Set  the name of the servlet to forward to,
	 * i.e. the "servlet-name" of the target servlet in web.xml.
	 * <p>Default is the bean name of this controller.
	 */
	public void setServletName(String servletName) {
		this.servletName = servletName;
	}

	public void setBeanName(String name) {
		this.beanName = name;
	}

	protected void initApplicationContext() {
		if (this.servletName == null) {
			this.servletName = this.beanName;
		}
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		RequestDispatcher rd = getServletContext().getNamedDispatcher(this.servletName);
		if (rd == null) {
			throw new ServletException("No servlet with name '" + this.servletName + "' defined in web.xml");
		}
		// if already included, include again, else forward
		if (request.getAttribute(UrlPathHelper.INCLUDE_URI_REQUEST_ATTRIBUTE) != null) {
			rd.include(request, response);
			if (logger.isDebugEnabled()) {
				logger.debug("Included servlet [" + this.servletName +
						"] in ServletForwardingController '" + this.beanName + "'");
			}
		}
		else {
			rd.forward(request, response);
			if (logger.isDebugEnabled()) {
				logger.debug("Forwarded to servlet [" + this.servletName +
						"] in ServletForwardingController '" + this.beanName + "'");
			}
		}
		return null;
	}

}
