/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.web.portlet.handler;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSecurityException;

/**
 * Interceptor that checks the authorization of the current user via the
 * user's roles, as evaluated by PortletRequest's isUserInRole method.
 *
 * @author John A. Lewis
 * @since 2.0
 * @see javax.portlet.PortletRequest#isUserInRole
 */
public class UserRoleAuthorizationInterceptor extends HandlerInterceptorAdapter {

	private String[] authorizedRoles;

	/**
	 * Set the roles that this interceptor should treat as authorized.
	 * @param authorizedRoles array of role names
	 */
	public final void setAuthorizedRoles(String[] authorizedRoles) {
		this.authorizedRoles = authorizedRoles;
	}

	public final boolean preHandle(PortletRequest request, PortletResponse response, Object handler)
			throws PortletException {
		if (this.authorizedRoles != null) {
			for (int i = 0; i < this.authorizedRoles.length; i++) {
				if (request.isUserInRole(this.authorizedRoles[i])) {
					return true;
				}
			}
		}
		handleNotAuthorized(request, response, handler);
		return false;
	}

	/**
	 * Handle a request that is not authorized according to this interceptor.
	 * Default implementation throws a new PortletSecurityException.
	 * <p>This method can be overridden to write a custom message, forward or
	 * redirect to some error page or login page, or throw a PortletException.
	 * @param request current portlet request
	 * @param response current portlet response
	 * @param handler chosen handler to execute, for type and/or instance evaluation
	 * @throws javax.portlet.PortletException
	 */
	protected void handleNotAuthorized(PortletRequest request, PortletResponse response, Object handler)
			throws PortletException {
		throw new PortletSecurityException("");
		
	}

}
