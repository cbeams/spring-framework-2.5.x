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

import org.springframework.web.portlet.support.PortletController;

/**
 * PortletController execution chain, consisting of contoller object and any
 * preprocessing interceptors. Returned by PortletControllerMapping's
 * getPortletController method.
 * @author William G. Thompson, Jr.
 * @see PortletControllerMapping#getPortletController
 */
public class PortletControllerExecutionChain {

	private PortletController controller;

	private PortletControllerInterceptor[] interceptors;

	/**
	 * Create new PortletControllerExecutionChain.
	 * @param controller the controller object to execute
	 */
	public PortletControllerExecutionChain(PortletController controller) {
		this.controller = controller;
	}

	/**
	 * Create new PortletControllerExecutionChain.
	 * @param controller the PortletController object to execute
	 * @param interceptors the array of interceptors to apply
	 * (in the given order) before the controller itself executes
	 */
	public PortletControllerExecutionChain(PortletController controller, PortletControllerInterceptor[] interceptors) {
		this.controller = controller;
		this.interceptors = interceptors;
	}

	/**
	 * Return the controller object to execute.
	 * @return the contoller object (should not be null)
	 */
	public PortletController getPortletController() {
		return controller;
	}

	/**
	 * Return the array of interceptors to apply (in the given order)
	 * before the controller itself executes.
	 * @return the array of PortletControllerInterceptors instances (may be null)
	 */
	public PortletControllerInterceptor[] getInterceptors() {
		return interceptors;
	}

}
