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

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;

import org.springframework.ui.context.Theme;
import org.springframework.web.portlet.DispatcherPortlet;
import org.springframework.web.portlet.context.PortletApplicationContext;
import org.springframework.web.portlet.context.support.PortletApplicationContextUtils;
import org.springframework.web.portlet.ThemeResolver;

/**
 * Utility class for easy access to various request-specific state,
 * set by the DispatcherPortlet.
 *
 * @author Juergen Hoeller
 * @since 03.03.2003
 */
public abstract class RequestContextUtils {

	/**
	 * Look for the PortletApplicationContext associated with the portlet that has
	 * initiated request processing.
	 * @param request current portlet request
	 * @return the request-specific portlet application context
	 * @throws IllegalStateException if neither the portlet-specific nor global context has been found
	 */
	public static PortletApplicationContext getPortletApplicationContext(PortletRequest request)
	    throws IllegalStateException {
		return getPortletApplicationContext(request, null);
	}

	/**
	 * Look for the PortletApplicationContext associated with the controller portlet that has
	 * initiated request processing, and for the global context if none was found associated
	 * with the current request. This method is useful to allow components outside our framework,
	 * such as JSP tag handlers, to access the most specific application context available.
	 * @param request current portlet request
	 * @param portletContext current portlet context
	 * @return the request-specific or global web application context if no request-specific
	 * context has been set
	 * @throws IllegalStateException if neither a portlet-specific nor global context has been found
	 */
	public static PortletApplicationContext getPortletApplicationContext(PortletRequest request, PortletContext portletContext)
	    throws IllegalStateException {
		PortletApplicationContext webApplicationContext = (PortletApplicationContext) request.getAttribute(
				DispatcherPortlet.PORTLET_APPLICATION_CONTEXT_ATTRIBUTE);
		if (webApplicationContext == null) {
			if (portletContext == null) {
				throw new IllegalStateException("No PortletApplicationContext found: not in a DispatcherPortlet request?");
			}
			webApplicationContext = PortletApplicationContextUtils.getPortletApplicationContext(portletContext);
			if (webApplicationContext == null) {
				throw new IllegalStateException("No PortletApplicationContext found: no ContextLoaderListener registered?");
			}
		}
		return webApplicationContext;
	}

	/**
	 * Return the ThemeResolver that has been bound to the request by the DispatcherPortlet.
	 * @param request current HTTP request
	 * @return the current ThemeResolver
	 * @throws IllegalStateException if no ThemeResolver has been found
	 */
	public static ThemeResolver getThemeResolver(RenderRequest request) throws IllegalStateException {
		ThemeResolver themeResolver = (ThemeResolver) request.getAttribute(DispatcherPortlet.THEME_RESOLVER_ATTRIBUTE);
		if (themeResolver == null) {
			throw new IllegalStateException("No ThemeResolver found: not in a DispatcherPortlet request?");
		}
		return themeResolver;
	}

	/**
	 * Retrieves the current theme from the given request,
	 * using the ThemeResolver bound to the request by the DispatcherPortlet,
	 * and the current PortletApplicationContext.
	 * @param request current HTTP request
	 * @return the current theme
	 * @throws IllegalStateException if no ThemeResolver has been found
	 */
	public static Theme getTheme(RenderRequest request) throws IllegalStateException {
		PortletApplicationContext context = getPortletApplicationContext(request);
		String themeName = getThemeResolver(request).resolveThemeName(request);
		return context.getTheme(themeName);
	}

}
