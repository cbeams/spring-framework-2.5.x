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

import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.web.portlet.PortletControllerExecutionChain;
import org.springframework.web.portlet.PortletControllerInterceptor;
import org.springframework.web.portlet.PortletControllerMapping;
import org.springframework.web.portlet.context.support.PortletApplicationObjectSupport;

/**
 * Abstract base class for PortletControllerMapping implementations.
 * Supports ordering, a default controller, and controller interceptors.
 * @author Juergen Hoeller
 * @author William G. Thompson, Jr.
 * @see #getPortletControllerInternal
 * @see org.springframework.web.portlet.PortletControllerInterceptor
 */
public abstract class AbstractPortletControllerMapping extends PortletApplicationObjectSupport
    implements PortletControllerMapping, Ordered {

	protected final Log logger = LogFactory.getLog(getClass());

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private PortletController defaultPortletController;

	private PortletControllerInterceptor[] interceptors;


	public final void setOrder(int order) {
	  this.order = order;
	}

	public final int getOrder() {
	  return order;
	}

	/**
	 * Set the default controller for this controller mapping.
	 * This controller will be returned if no specific mapping was found.
	 * Default is null.
	 * @param defaultPortletController default controller instance, or null if none
	 */
	public final void setDefaultPortletController(PortletController defaultPortletController) {
		this.defaultPortletController = defaultPortletController;
		logger.info("Default mapping to controller [" + this.defaultPortletController + "]");
	}

	/**
	 * Return the default controller for this controller mapping.
	 * @return the default controller instance, or null if none
	 */
	protected final Object getDefaultPortletController() {
		return defaultPortletController;
	}

	/**
	 * Set the controller interceptors to apply for all controllers mapped by
	 * this controller mapping.
	 * @param interceptors array of controller interceptors, or null if none
	 */
	public final void setInterceptors(PortletControllerInterceptor[] interceptors) {
		this.interceptors = interceptors;
	}


	/**
	 * Look up a controller for the given request, falling back to the default
	 * controller if no specific one is found.
	 * @param request current portlet request
	 * @return the looked up controller instance, or the default controller
	 * @see #getPortletControllerInternal
	 */
	public final PortletControllerExecutionChain getPortletController(PortletRequest request) throws Exception {
		PortletController controller = getPortletControllerInternal(request);
		if (controller == null) {
			controller = this.defaultPortletController;
		}
		if (controller == null) {
			return null;
		}
		return new PortletControllerExecutionChain(controller, this.interceptors);
	}

	/**
	 * Lookup a controller for the given request, returning null if no specific
	 * one is found. This method is called by getPortletController, a null return value
	 * will lead to the default controller, if one is set.
	 * @param request current portlet request
	 * @return the looked up controller instance, or null
	 * @throws Exception if there is an internal error
	 */
	protected abstract PortletController getPortletControllerInternal(PortletRequest request) throws Exception;

}
