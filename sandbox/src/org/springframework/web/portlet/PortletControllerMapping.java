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

/**
 * Interface to be implemented by objects that define a mapping between
 * portlet requests and controller objects.
 *
 * <p>This class can be implemented by application developers, although this
 * is not necessary, as PortletModeControllerMapping
 * is included in the framework.
 *
 * <p>PortletControllerMapping implementations can support mapped interceptors but do
 * not have to. A controller will always be wrapped in a PortletControllerExecutionChain
 * instance, optionally accompanied by some PortletControllerInterceptor instances.
 * The DispatcherPortlet will first call each PortletControllerInterceptor's preController
 * method in the given order, finally invoking the controller itself if all
 * preController methods have returned "true".
 *
 * <p>Note: Implementations can implement the Ordered interface to be able
 * to specify a sorting order and thus a priority for getting applied by
 * DispatcherPortlet. Non-Ordered instances get treated as lowest priority.
 *
 * @author William G. Thompson, Jr.
 * @see org.springframework.core.Ordered
 */
public interface PortletControllerMapping {
	
	/**
	 * Return a controller and any interceptors for this request. The choice may be made
	 * on PortletMode, session state, or any factor the implementing class chooses.
	 * 
	 * <p>Returns null if no match was found. This is not an error. The
	 * DispatcherServlet will query all registered ControllerMapping beans to find
	 * a match, and only decide there is an error if none can find a controller.
	 * @param request current portlet request
	 * @return a PortletControllerExecutionChain instance containing controller object and
	 * any interceptors, or null if no mapping found
	 * @throws Exception if there is an internal error
	 */
	PortletControllerExecutionChain getPortletController(PortletRequest request) throws Exception;

}
