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

package org.springframework.web.portlet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * Workflow interface that allows for customized handler execution chains.
 * Applications can register any number of existing or custom interceptors
 * for certain groups of handlers, to add common preprocessing behavior
 * without needing to modify each handler implementation.
 *
 * <p>A HandlerInterceptor gets called before the appropriate HandlerAdapter
 * triggers the execution of the handler itself. This mechanism can be used
 * for a large field of preprocessing aspects, e.g. for authorization checks,
 * or common handler behavior like locale or theme changes. Its main purpose
 * is to allow for factoring out repetitive handler code.
 *
 * <p>Typically an interceptor chain is defined per HandlerMapping bean,
 * sharing its granularity. To be able to apply a certain interceptor chain
 * to a group of handlers, one needs to map the desired handlers via one
 * HandlerMapping bean. The interceptors themselves are defined as beans
 * in the application context, referenced by the mapping bean definition
 * via its "interceptors" property (in XML: a &lt;list&gt; of &lt;ref&gt;).
 *
 * <p>HandlerInterceptor is basically similar to a Servlet 2.3 Filter, but in
 * contrast to the latter it just allows custom pre-processing with the option
 * of prohibiting the execution of the handler itself, and custom post-processing.
 * Filters are more powerful, for example they allow for exchanging the request
 * and response objects that are handed down the chain. Note that a filter
 * gets configured in web.xml, a HandlerInterceptor in the application context.
 *
 * <p>As a basic guideline, fine-grained handler-related preprocessing tasks are
 * candidates for HandlerInterceptor implementations, especially factored-out
 * common handler code and authorization checks. On the other hand, a Filter
 * is well-suited for request content and view content handling, like multipart
 * forms and GZIP compression. This typically shows when one needs to map the
 * filter to certain content types (e.g. images), or to all requests.  Be aware
 * that filters cannot be applied to portlet requests (they only operate on
 * servlet requests), so for portlet requests interceptors are essential. 
 *
 * @author Juergen Hoeller
 * @author John A. Lewis
 * @since 2.0
 * @see HandlerExecutionChain#getInterceptors
 * @see org.springframework.web.portlet.HandlerMapping
 * @see org.springframework.web.portlet.handler.AbstractHandlerMapping#setInterceptors
 * @see org.springframework.web.portlet.HandlerExecutionChain
 */
public interface HandlerInterceptor {

	/**
	 * Intercept the execution of a handler. Called after HandlerMapping determined
	 * an appropriate handler object, but before HandlerAdapter invokes the handler.
	 * <p>DispatcherPortlet processes a handler in an execution chain, consisting
	 * of any number of interceptors, with the handler itself at the end.
	 * With this method, each interceptor can decide to abort the execution chain,
	 * typically throwing an exception or writing a custom response.
	 * @param request current portlet request
	 * @param response current portlet response
	 * @param handler chosen handler to execute, for type and/or instance evaluation
	 * @return <code>true</code> if the execution chain should proceed with the
	 * next interceptor or the handler itself. Else, DispatcherPortlet assumes
	 * that this interceptor has already dealt with the response itself.
	 * @throws Exception in case of errors
	 */
	boolean preHandleAction(ActionRequest request, ActionResponse response, Object handler)
	    throws Exception;

	/**
	 * Callback after completion of request processing, that is, after rendering
	 * the view. Will be called on any outcome of handler execution, thus allows
	 * for proper resource cleanup.
	 * <p>Note: Will only be called if this interceptor's <code>preHandleAction</code>
	 * method has successfully completed and returned <code>true</code>!
	 * @param request current portlet request
	 * @param response current portlet response
	 * @param handler chosen handler to execute, for type and/or instance examination
	 * @param ex exception thrown on handler execution, if any (only included as
	 * additional context information for the case where a handler threw an exception;
	 * request execution may have failed even when this argument is <code>null</code>)
	 * @throws Exception in case of errors
	 */
	void afterActionCompletion(
			ActionRequest request, ActionResponse response, Object handler, Exception ex)
	    throws Exception;

	/**
	 * Intercept the execution of a handler. Called after HandlerMapping determined
	 * an appropriate handler object, but before HandlerAdapter invokes the handler.
	 * <p>DispatcherPortlet processes a handler in an execution chain, consisting
	 * of any number of interceptors, with the handler itself at the end.
	 * With this method, each interceptor can decide to abort the execution chain,
	 * typically throwing an exception or writing a custom response.
	 * @param request current portlet request
	 * @param response current portlet response
	 * @param handler chosen handler to execute, for type and/or instance evaluation
	 * @return <code>true</code> if the execution chain should proceed with the
	 * next interceptor or the handler itself. Else, DispatcherPortlet assumes
	 * that this interceptor has already dealt with the response itself.
	 * @throws Exception in case of errors
	 */
	boolean preHandleRender(RenderRequest request, RenderResponse response, Object handler)
	    throws Exception;

	/**
	 * Intercept the execution of a handler. Called after HandlerAdapter actually
	 * invoked the handler, but before the DispatcherPortlet renders the view.
	 * Can expose additional model objects to the view via the given ModelAndView.
	 * <p>DispatcherPortlet processes a handler in an execution chain, consisting
	 * of any number of interceptors, with the handler itself at the end.
	 * With this method, each interceptor can post-process an execution,
	 * getting applied in inverse order of the execution chain.
	 * @param request current portlet render request
	 * @param response current portlet render response
	 * @param handler chosen handler to execute, for type and/or instance examination
	 * @param modelAndView the ModelAndView that the handler returned, can also be null
	 * @throws Exception in case of errors
	 */
	void postHandleRender(
			RenderRequest request, RenderResponse response, Object handler, ModelAndView modelAndView)
			throws Exception;

	/**
	 * Callback after completion of request processing, that is, after rendering
	 * the view. Will be called on any outcome of handler execution, thus allows
	 * for proper resource cleanup.
	 * <p>Note: Will only be called if this interceptor's <code>preHandleRender</code>
	 * method has successfully completed and returned <code>true</code>!
	 * @param request current portlet request
	 * @param response current portlet response
	 * @param handler chosen handler to execute, for type and/or instance examination
	 * @param ex exception thrown on handler execution, if any (only included as
	 * additional context information for the case where a handler threw an exception;
	 * request execution may have failed even when this argument is <code>null</code>)
	 * @throws Exception in case of errors
	 */
	void afterRenderCompletion(
			RenderRequest request, RenderResponse response, Object handler, Exception ex)
			throws Exception;

}
