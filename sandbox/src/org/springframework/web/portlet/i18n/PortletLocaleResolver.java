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

package org.springframework.web.portlet.i18n;

import java.util.Locale;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.portlet.DispatcherPortlet;

/**
 * Implementation of LocaleResolver that looks up Locale in the portlet request.
 * This is set by the DispatcherPortlet for use with the ViewRendererServlet.
 * Locale resolution is handled by the portal application and is made available
 * in the PortletRequest.
 *
 * <p>Realize that portlet containers typically manage locale as one
 * of their features, so changing the locale through this resolver is
 * not supported.
 * 
 * @see org.springframework.web.portlet.LocaleResolver
 * @see javax.portlet.PortletRequest#getLocale
 * @author Juergen Hoeller
 * @author William G. Thompson, Jr.
 * @author John A. Lewis
 */
public class PortletLocaleResolver implements org.springframework.web.portlet.LocaleResolver {

    /* (non-Javadoc)
     * @see org.springframework.web.portlet.LocaleResolver#resolveLocale(javax.servlet.http.HttpServletRequest)
     */
	public Locale resolveLocale(HttpServletRequest request) {
	    // get portlet locale from request attribute
	    Locale locale = (Locale)request.getAttribute(DispatcherPortlet.PORTLET_LOCALE_ATTRIBUTE);
		// return portlet locale if available, otherwise return request locale
		return (locale != null ? locale : request.getLocale());
	}

    /* (non-Javadoc)
     * @see org.springframework.web.portlet.LocaleResolver#resolveLocale(javax.portlet.PortletRequest)
     */
    public Locale resolveLocale(PortletRequest request) {
	    // get portlet locale from request attribute
	    Locale locale = (Locale)request.getAttribute(DispatcherPortlet.PORTLET_LOCALE_ATTRIBUTE);
		// return portlet locale if available, otherwise return request locale
		return (locale != null ? locale : request.getLocale());
    }

    /* (non-Javadoc)
     * @see org.springframework.web.portlet.LocaleResolver#setLocale(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Locale)
     */
	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
	    // cannot set the locale in the portal -- the portal controls this
	}

    /* (non-Javadoc)
     * @see org.springframework.web.portlet.LocaleResolver#setLocale(javax.portlet.PortletRequest, javax.portlet.PortletResponse, java.util.Locale)
     */
    public void setLocale(PortletRequest request, PortletResponse response,
            Locale locale) {
	    // cannot set the locale in the portal -- the portal controls this
    }
}
