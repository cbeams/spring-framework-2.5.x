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

import java.util.HashSet;
import java.util.Set;

import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;

import org.springframework.web.portlet.context.support.PortletApplicationObjectSupport;

/**
 * Convenient superclass for any kind of web content generator,
 * like AbstractController and WebContentInterceptor. Can also be
 * used for custom handlers that have their own HandlerAdapter.
 *
 * <p>Supports portlet cache control options. The usage of corresponding
 * portlet headers can be determined via the "useExpiresHeader" and
 * "userCacheControlHeader" properties.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author William G. Thompson, Jr.
 * @see org.springframework.web.portlet.mvc.AbstractController
 * @see org.springframework.web.portlet.mvc.WebContentInterceptor
 */
public abstract class PortletContentGenerator extends PortletApplicationObjectSupport {

	/** Set of supported PortletModes. VIEW, EDIT and HELP by default. */
	private Set	supportedPortletModes;

	private boolean requireSession = false;

	/**
	 * Create a new PortletContextGenerator supporting EDIT, VIEW and HELP modes.
	 */
	public PortletContentGenerator() {
		this.supportedPortletModes = new HashSet();
		this.supportedPortletModes.add(PortletMode.EDIT);
		this.supportedPortletModes.add(PortletMode.VIEW);
		this.supportedPortletModes.add(PortletMode.HELP);
	}

	/**
	 * Set the PortletModes that this content generator should support.
	 * Default is VIEW, EDIT and HELP.
	 */
	public final void setSupportedMethods(String[] supportedPortletModesArray) {
		if (supportedPortletModesArray == null || supportedPortletModesArray.length == 0) {
			throw new IllegalArgumentException("supportedPortletModes must not be empty");
		}
		this.supportedPortletModes.clear();
		for (int i = 0; i < supportedPortletModesArray.length; i++) {
			this.supportedPortletModes.add(supportedPortletModesArray[i]);
		}
	}

	/**
	 * Set if a session should be required to handle requests.
	 * TODO do portlets always have a session?
	 */
	public final void setRequireSession(boolean requireSession) {
		this.requireSession = requireSession;
	}

	/**
	 * Check and prepare the given request and response according to the settings
	 * of this generator. Checks for supported methods and a required session,
	 * and applies the specified number of cache seconds.
	 * @param request current portlet request
	 * @param response current portlet response
	 * @throws PortletException if the request cannot be handled because a check failed
	 */
	protected final void checkAndPrepare(PortletRequest request, PortletResponse response)
		throws PortletException {
	    
		// check whether we should support the request method
		PortletMode portletMode = request.getPortletMode();
		if (!this.supportedPortletModes.contains(portletMode)) {
			logger.info("Disallowed RenderRequest for PortletMode: '" + portletMode + "'");
			throw new PortletException("This resource does not support RenderRequest for PortletMode: '" + portletMode + "'");
		}

		// check whether session was required
		PortletSession session = request.getPortletSession(false);
		if (this.requireSession && session == null) {
			throw new PortletException("This resource requires a pre-existing PortletSession: none was found");
		}

	}

}
