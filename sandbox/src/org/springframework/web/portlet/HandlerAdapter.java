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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * MVC framework SPI interface, allowing parameterization of core MVC workflow.
 *
 * <p>Interface that must be implemented for each handler type to handle a request.
 * This interface is used to allow the DispatcherPortlet to be indefinitely
 * extensible. The DispatcherPortlet accesses all installed handlers through this
 * interface, meaning that it does not contain code specific to any handler type.
 *
 * <p>Note that a handler can be of type Object. This is to enable handlers from
 * other frameworks to be integrated with this framework without custom coding.
 *
 * <p>This interface is not intended for application developers. It is available
 * to handlers who want to develop their own web workflow.
 *
 * <p>Note: Implementations can implement the Ordered interface to be able to
 * specify a sorting order and thus a priority for getting applied by
 * DispatcherPortlet. Non-Ordered instances get treated as lowest priority.
 *
 * @author Rod Johnson
 * TODO provide a simple portlet controller
 * @see org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter
 */
public interface HandlerAdapter {
	
	/**
	 * Given a handler instance, return whether or not this HandlerAdapter can
	 * support it. Usually HandlerAdapters will base the decision on the handler
	 * type. HandlerAdapters will normally support only one handler type.
	 * <p>A typical implementation:<br><br>
	 * <code>
	 * return handler != null && MyHandler.class.isAssignableFrom(handler.getClass());
	 * </code>
	 * @param handler handler object to check
	 * @return whether or not this object can use the given handler
	 */
	boolean supports(Object handler); 
	
	/**
	 * Use the given handler to handle this Render request.
	 * The workflow that is required may vary widely.
	 * @param request current portlet request
	 * @param response current portlet response
	 * @param handler handler to use. This object must have previously been passed
	 * to the supports() method of this interface, which must have returned true.
	 * Implementations that generate output themselves (and return null
	 * from this method) may encounter IOExceptions.
	 * @throws Exception in case of errors
	 * @return ModelAndView object with the name of the view and the required
	 * model data, or null if the request has been handled directly
	 */
	ModelAndView handle(RenderRequest request, RenderResponse response, Object handler) throws Exception;

	/**
	 * Use the given handler to handle this Action request.
	 * The workflow that is required may vary widely.
	 * @param request current portlet request
	 * @param response current portlet response
	 * @param handler handler to use. This object must have previously been passed
	 * to the supports() method of this interface, which must have returned true.
	 * Implementations that generate output themselves (and return null
	 * from this method) may encounter IOExceptions.
	 * @throws Exception in case of errors
	 */
	void handle(ActionRequest request, ActionResponse response, Object handler) throws Exception;

}
