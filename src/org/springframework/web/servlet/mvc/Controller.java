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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * Base Controller interface, representing a component that receives HttpServletRequest
 * and HttpServletResponse like a <code>HttpServlet</code> but is able to participate in
 * an MVC workflow. Comparable to the notion of a Struts <code>Action</code>
 *
 * <p>Any implementation of the Controller interface should be a
 * <i>reusable, threadsafe</i> class, capable of handling multiple
 * HTTP requests throughout the lifecycle of an application. To be able to
 * configure Controller in an easy, Controllers are usually JavaBeans.</p>
 *
 * <p><b><a name="workflow">Workflow</a>:</b><br>
 * After the DispatcherServlet has received a request and has done its work
 * to resolve locales, themes and things a like, it tries to resolve
 * a Controller, using a {@link org.springframework.web.servlet.HandlerMapping
 * HandlerMapping}. When a Controller has been found, the \
 * {@link #handleRequest(HttpServletRequest, HttpServletResponse) handleRequest()}
 * method will be invoked, which is responsible for handling the actual
 * request and - if applicable - returning an appropriate ModelAndView.
 * So actually, this method is the main entrypoint for the
 * {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet}
 * which delegates requests to controllers. This method - and also this interface -
 * should preferrably not be implemented by custom controllers <i>directly</i>, since
 * abstract controller also provided by this package already provide a lot
 * of functionality common for virtually all webapplications. A few examples of
 * those abstract controllers:
 * {@link AbstractController AbstractController},
 * {@link AbstractCommandController AbstractCommandController} and
 * {@link AbstractFormController AbstractFormController}.
 * </p>
 * <p>So basically any <i>direct</i> implementation of the Controller interface
 * just handles HttpServletRequests and should return a ModelAndView, to be
 * further used by the DispatcherServlet. Any additional functionality such
 * as optional validation, formhandling, etcetera should be obtained by
 * extended one of the abstract controller mentioned above.
 * </p>
 *
 * @author Rod Johnson
 * @see SimpleControllerHandlerAdapter
 */
public interface Controller {

	/**
	 * Process the request and return a ModelAndView object which the DispatcherServlet
	 * will render. A null return is not an error: It indicates that this object
	 * completed request processing itself, thus there is no ModelAndView to render.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render, or null if handled directly
	 * @throws Exception in case of errors
	 */
	ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
