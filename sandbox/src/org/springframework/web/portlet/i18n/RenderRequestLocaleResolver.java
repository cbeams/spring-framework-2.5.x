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

package org.springframework.web.portlet.i18n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.portlet.PortletLocaleResolver;

/**
 * Implementation of LocaleResolver that looks up Locale in the HttpRequest.
 * This is set by the DispatcherPortlet for use with the ViewRendererServlet.
 * Locale resolution is handled by the portal application and is made available
 * in the RenderRequest.
 *
 * @author Juergen Hoeller
 * @author William G. Thompson, Jr.
 */
public class RenderRequestLocaleResolver implements PortletLocaleResolver {

	/**
	 * Name of the HttpRequest attribute that holds the Locale returned
	 * from RenderRequest.getLocale().  This is used by ViewRendererServlet
	 * and set by DispatcherPortlet. 
	 * <p>
	 * Use RequestContext.getLocale() to retrieve the current locale in
	 * controllers or views.
	 * @see org.springframework.web.servlet.support.RequestContext#getLocale
	 */
	public static final String LOCALE_RENDER_REQUEST_ATTRIBUTE = RenderRequestLocaleResolver.class.getName() + ".LOCALE";

	public Locale resolveLocale(HttpServletRequest request) {
	    Locale locale = (Locale)request.getAttribute(LOCALE_RENDER_REQUEST_ATTRIBUTE);
		// specific locale, or fallback to request locale?
		return (locale != null ? locale : request.getLocale());
	}

	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
	    // local set by portal or portlet prefs?
	}

    public String getLocaleAttribute() {
        return LOCALE_RENDER_REQUEST_ATTRIBUTE;
    }

}
