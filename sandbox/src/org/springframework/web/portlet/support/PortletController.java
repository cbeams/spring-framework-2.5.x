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

package org.springframework.web.portlet.support;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.AbstractFormController;

/**
 * TODO update this
 * Base PortletController interface, representing a component that receives RenderRequest
 * and RenderResponse like a <code>Portlet</code> but is able to participate in
 * an MVC workflow. Comparable to the notion of a Struts <code>Action</code>
 *
 * <p>Any implementation of the PortletController interface should be a
 * <i>reusable, threadsafe</i> class, capable of handling multiple
 * portlet requests throughout the lifecycle of an application. To be able to
 * configure PortletController in an easy way, Controllers are usually JavaBeans.</p>
 *
 * <p><b><a name="workflow">Workflow</a>:</b><br>
 * After the DispatcherPortlet has received a request and has done its work
 * to resolve themes and things a like, it tries to resolve
 * a PortletController, using a {@link org.springframework.web.portlet.HandlerMapping
 * HandlerMapping}. When a PortletController has been found, the \
 * {@link #handleRequest(RenderRequest, RenderResponse) handleRequest()}
 * method will be invoked, which is responsible for handling the actual
 * request and - if applicable - returning an appropriate ModelAndView.
 * So actually, this method is the main entrypoint for the
 * {@link org.springframework.web.portlet.DispatcherPortlet DispatcherPortlet}
 * which delegates requests to controllers. This method - and also this interface -
 * should preferrably not be implemented by custom controllers <i>directly</i>, since
 * abstract controller also provided by this package already provide a lot
 * of functionality common for virtually all portlet applications. A few examples of
 * those abstract controllers:
 * {@link AbstractController AbstractController},
 * {@link AbstractCommandController AbstractCommandController} and
 * {@link AbstractFormController AbstractFormController}.
 * </p>
 * <p>So basically any <i>direct</i> implementation of the PortletController interface
 * just handles RenderRequests and should return a ModelAndView, to be
 * further used by the DispatcherPortlet. Any additional functionality such
 * as optional validation, formhandling, etcetera should be obtained by
 * extended one of the abstract controller mentioned above.
 * </p>
 *
 * @author William G. Thompson, Jr.
 */
public interface PortletController {

	/**
	 * Process the request and return a ModelAndView object which the DispatcherPortlet
	 * will render. A null return is not an error: It indicates that this object
	 * completed request processing itself, thus there is no ModelAndView to render.
	 * @param request current portlet Render request
	 * @param response current portlet Render response
	 * @return a ModelAndView to render, or null if handled directly
	 * @throws Exception in case of errors
	 */
	ModelAndView handleRequest(RenderRequest request, RenderResponse response) throws Exception;
	
	/**
	 * Process the Action request.
	 * @param request current portlet Action request
	 * @param response current portlet Action response
	 * @throws Exception in case of errors
	 */
	void handleRequest(ActionRequest request, ActionResponse response) throws Exception;

}
