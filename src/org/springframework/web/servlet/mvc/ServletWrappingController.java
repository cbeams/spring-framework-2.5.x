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

import org.springframework.web.servlet.ModelAndView;

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
 * <p>In particular, the main intent of this controller is to allow to apply
 * Spring's OpenSessionInViewInterceptor or OpenPersistenceManagerInViewInterceptor
 * to Struts actions in a Servlet 2.2 container. The specified "servlet-name" will
 * simply refer to a Struts ActionServlet definition in web.xml in such a scenario.
 * You then need to map "/*.do" (or whatever pattern you choose for your Struts actions)
 * onto this controller, which will in turn forward to the Struts ActionServlet.
 *
 * <p>In a Servlet 2.3 container, when not using Spring's own web MVC framework,
 * it is recommended to use classic servlet mapping in combination with a filter,
 * for example Spring's OpenSessionInViewFilter or OpenPersistenceManagerInViewFilter.
 *
 * <p><b>Example:</b> web.xml, mapping all *.do requests to a Spring dispatcher.
 * Also defines a Struts ActionServlet, but <i>without</i> servlet mapping.
 * All remaining Struts configuration is as usual, just like if the *.do mapping
 * pointed directly at the ActionServlet.
 *
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;action&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;org.apache.struts.action.ActionServlet&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 *
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;dispatcher&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;org.springframework.web.servlet.DispatcherServlet&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 *
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;dispatcher&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;*.do&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;</pre>
 *
 * <b>Example:</b> dispatcher-servlet.xml, in turn forwarding *.do to the Struts
 * ActionServlet (identified by servlet name). All such requests will go to the
 * configured HandlerInterceptor chain (e.g. an OpenSessionInViewInterceptor).
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
 *   &lt;property name="servletName"&gt;&lt;value&gt;action&lt;/value&gt;&lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * Thanks to Keith Garry Boyce for pointing out the issue with Struts in a
 * Servlet 2.2 container, and for suggesting to access Struts through Spring's
 * web dispatching infrastructure!
 *
 * @author Juergen Hoeller
 * @since 1.1.1
 * @see org.springframework.orm.hibernate.support.OpenSessionInViewInterceptor
 * @see org.springframework.orm.hibernate.support.OpenSessionInViewFilter
 * @see org.springframework.orm.jdo.support.OpenPersistenceManagerInViewInterceptor
 * @see org.springframework.orm.jdo.support.OpenPersistenceManagerInViewFilter
 */
public class ServletWrappingController extends AbstractController {

	private String servletName;

	/**
	 * Specify the name of the servlet to forward to,
	 * i.e. the "servlet-name" of the target servlet in web.xml.
	 */
	public void setServletName(String servletName) {
		this.servletName = servletName;
	}

	protected void initApplicationContext() {
		if (this.servletName == null) {
			throw new IllegalArgumentException("servletName is required");
		}
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		RequestDispatcher rd = getServletContext().getNamedDispatcher(this.servletName);
		if (rd == null) {
			throw new ServletException("No servlet with name '" + this.servletName + "' defined in web.xml");
		}
		rd.forward(request, response);
		return null;
	}

}
