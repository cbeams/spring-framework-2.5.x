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

package org.springframework.web.servlet.i18n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.util.WebUtils;

/**
 * Implementation of LocaleResolver that uses a locale attribute in the user's
 * session in case of a custom setting, with a fallback to the accept header locale.
 * This is most appropriate if the application needs user sessions anyway.
 *
 * <p>Custom controllers can override the user's locale by calling
 * <code>setLocale</code>, e.g. responding to a locale change request.
 *
 * @author Juergen Hoeller
 * @since 27.02.2003
 * @see #setLocale
 */
public class SessionLocaleResolver implements LocaleResolver {

	/**
	 * Name of the session attribute that holds the locale.
	 * Only used internally by this implementation.
	 * Use <code>RequestContext(Utils).getLocale()</code>
	 * to retrieve the current locale in controllers or views.
	 * @see org.springframework.web.servlet.support.RequestContext#getLocale
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getLocale
	 */
	public static final String LOCALE_SESSION_ATTRIBUTE_NAME = SessionLocaleResolver.class.getName() + ".LOCALE";

	public Locale resolveLocale(HttpServletRequest request) {
		Locale locale = (Locale) WebUtils.getSessionAttribute(request, LOCALE_SESSION_ATTRIBUTE_NAME);
		// specific locale, or fallback to accept header locale?
		return (locale != null ? locale : request.getLocale());
	}

	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		WebUtils.setSessionAttribute(request, LOCALE_SESSION_ATTRIBUTE_NAME, locale);
	}

}
