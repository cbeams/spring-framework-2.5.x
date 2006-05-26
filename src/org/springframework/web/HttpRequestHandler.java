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

package org.springframework.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Plain handler interface for components that process HTTP requests,
 * analogous to a Servlet. Only throws ServletException and IOException,
 * to allow for usage within any HttpServlet. Essentially the direct
 * equivalent of an HttpServlet, reduced to a handle method.
 *
 * <p>The easiest way to expose an HttpRequestHandler bean in Spring style
 * is to define it in Spring's root web application context and define
 * an HttpRequestHandlerServlet in <code>web.xml</code>, pointing at the
 * target HttpRequestHandler bean through its servlet-name which needs
 * to match the target bean name.
 *
 * <p>Supported as a handler type within Spring's DispatcherServlet,
 * being able to leverage the dispatcher's advanced mapping and interception
 * facilities. This is the recommended way of exposing an HttpRequestHandler,
 * while keeping the handler implementations free of direct dependencies
 * on a DispatcherServlet environment.
 *
 * <p>Typically implemented to generate binary responses directly,
 * with no separate view resource involved. This differentiates it from
 * a Controller within Spring's Web MVC framework. The lack of a ModelAndView
 * return value gives a clearer signature to callers other than the
 * DispatcherServlet, indicating there will never be a view to render.
 *
 * <p>As of Spring 2.0, Spring's HTTP-based remote exporters, such as
 * HttpInvokerServiceExporter and HessianServiceExporter, implement this
 * interface rather than the more extensive Controller interface,
 * for minimal dependencies on Spring-specific web infrastructure.
 *
 * <p>Note that RequestHandlers can optionally implement the LastModified
 * interface, just like Controllers can, provided that they run within
 * Spring's DispatcherServlet.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.web.context.support.HttpRequestHandlerServlet
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.web.servlet.ModelAndView
 * @see org.springframework.web.servlet.mvc.Controller
 * @see org.springframework.web.servlet.mvc.LastModified
 * @see org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter
 * @see org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter
 * @see org.springframework.remoting.caucho.HessianServiceExporter
 * @see org.springframework.remoting.caucho.BurlapServiceExporter
 */
public interface HttpRequestHandler {

	/**
	 * Process the given request, generating a response.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws ServletException in case of general errors
	 * @throws IOException in case of I/O errors
	 */
	void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException;

}
