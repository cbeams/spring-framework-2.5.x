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

package org.springframework.web.portlet;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.portlet.support.PortletController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Workflow interface that allows for customized controller execution chains.
 * Applications can register any number of existing or custom interceptors
 * for certain groups of contollers, to add common preprocessing behavior
 * without needing to modify each controller implementation.
 *
 * <p>A PortletControllerInterceptor gets called before the appropriate controller
 * triggers the execution of the controller itself. This mechanism can be used
 * for a large field of preprocessing aspects, e.g. for authorization checks,
 * Its main purpose is to allow for factoring out repetitive controller code.
 *
 * <p>Typically an interceptor chain is defined per PortletControllerMapping bean,
 * sharing its granularity. To be able to apply a certain interceptor chain
 * to a group of controllers, one needs to map the desired controllers via one
 * PortletControllerMapping bean. The interceptors themselves are defined as beans
 * in the portlet application context, referenced by the mapping bean definition
 * via its "interceptors" property (in XML: a &lt;list&gt; of &lt;ref&gt;).
 *
 * <p>As a basic guideline, fine-grained controller-related preprocessing tasks are
 * candidates for PortletControllerInterceptor implementations, especially factored-out
 * common controller code and authorization checks.
 *
 * @author William G. Thompson, Jr.
 * @see PortletControllerExecutionChain#getInterceptors
 * @see org.springframework.web.portlet.PortletControllerInterceptorAdapter
 */
public interface PortletControllerInterceptor {

	/**
	 * Intercept the execution of a controller. Called after PortletControllerMapping determined
	 * an appropriate controller object, but before DispatcherPortlet invokes the controller.
	 * <p>DispatcherPortlet processes a controller in an execution chain, consisting
	 * of any number of interceptors, with the controller itself at the end.
	 * With this method, each interceptor can decide to abort the execution chain,
	 * typically sending a portlet error or writing a custom response.
	 * @param request current portlet request
	 * @param response current portlet response
	 * @param controller chosen controller to execute, for type and/or instance evaluation
	 * @return <code>true</code> if the execution chain should proceed with the
	 * next interceptor respectively the controller itself. Else, DispatcherPortlet
	 * assumes that this interceptor has already dealt with the response itself.
	 * @throws Exception in case of errors
	 */
	boolean preController(PortletRequest request, PortletResponse response,
										PortletController controller) throws Exception;

	/**
	 * Intercept the execution of a controller. Called after DispatcherPortlet actually
	 * invoked the controller, but before it renders the view.
	 * Can expose additional model objects to the view via the given ModelAndView.
	 * <p>DispatcherPortlet processes a controller in an execution chain, consisting
	 * of any number of interceptors, with the controller itself at the end.
	 * With this method, each interceptor can post-process an execution,
	 * getting applied in inverse order of the execution chain.
	 * @param request current portlet request
	 * @param response current portlet response
	 * @param controller chosen controller to execute, for type and/or instance examination
	 * @param modelAndView the ModelAndView that the controller returned, can also be null
	 * @throws Exception in case of errors
	 */
	void postController(RenderRequest request, RenderResponse response,
									PortletController controller, ModelAndView modelAndView) throws Exception;

	
	/**
	 * Callback after completion of request processing, i.e. after rendering the view.
	 * Will be called on any outcome of controller execution, thus allows for proper
	 * resource cleanup.
	 * <p>Note: Will only be called if this interceptor's preHandle method has
	 * successfully completed and returned true!
	 * @param request current portlet request
	 * @param response current portlet response
	 * @param controller chosen controller to execute, for type and/or instance examination
	 * @param ex exception thrown on controller execution
	 * @throws Exception in case of errors
	 */
	void afterCompletion(PortletRequest request, PortletResponse response,
											 PortletController controller, Exception ex) throws Exception;

}
