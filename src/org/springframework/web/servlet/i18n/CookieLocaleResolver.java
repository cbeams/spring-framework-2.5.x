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
import java.util.StringTokenizer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.util.WebUtils;

/**
 * Implementation of LocaleResolver that uses a cookie sent back to the user
 * in case of a custom setting, with a fallback to the accept header locale.
 * This is especially useful for stateless applications without user sessions.
 *
 * <p>Custom controllers can thus override the user's locale by calling
 * <code>setLocale</code>, e.g. responding to a certain locale change request.
 *
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @since 27.02.2003
 * @see #setLocale
 */
public class CookieLocaleResolver implements LocaleResolver {

	/**
	 * Name of the request attribute that holds the locale. Only used for
	 * overriding a cookie value if the locale has been changed in the
	 * course of the current request! Use RequestContext.getLocale() to
	 * retrieve the current locale in controllers or views.
	 * @see org.springframework.web.servlet.support.RequestContext#getLocale
	 */
	public static final String LOCALE_REQUEST_ATTRIBUTE_NAME = CookieLocaleResolver.class.getName() + ".LOCALE";

	public static final String DEFAULT_COOKIE_NAME = CookieLocaleResolver.class.getName() + ".LOCALE";

	public static final String DEFAULT_COOKIE_PATH = "/";

	public static final int DEFAULT_COOKIE_MAX_AGE = Integer.MAX_VALUE;


	private String cookieName = DEFAULT_COOKIE_NAME;

	private String cookiePath = DEFAULT_COOKIE_PATH;

	private int cookieMaxAge = DEFAULT_COOKIE_MAX_AGE;


	/**
	 * Use the given name for locale cookies.
	 */
	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

	/**
	 * Return the given name for locale cookies.
	 */
	public String getCookieName() {
		return cookieName;
	}

	/**
	 * Use the given path for locale cookies.
	 * The cookie is only visible for URLs in the path and below. 
	 */
	public void setCookiePath(String cookiePath) {
		this.cookiePath = cookiePath;
	}

	/**
	 * Return the path for locale cookies.
	 */
	public String getCookiePath() {
		return cookiePath;
	}

	/**
	 * Use the given maximum age, specified in seconds, for locale cookies.
	 * Useful special value: -1 ... not persistent, deleted when client shuts down
	 */
	public void setCookieMaxAge(int cookieMaxAge) {
		this.cookieMaxAge = cookieMaxAge;
	}

	/**
	 * Return the maximum age for locale cookies.
	 */
	public int getCookieMaxAge() {
		return cookieMaxAge;
	}


	public Locale resolveLocale(HttpServletRequest request) {
		// check locale for preparsed resp. preset locale
		Locale locale = (Locale) request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME);
		if (locale != null)
			return locale;

		// retrieve cookie value
		Cookie cookie = WebUtils.getCookie(request, getCookieName());

		if (cookie != null) {
			// parse cookie value
			String language = "";
			String country = "";
			String variant = "";

			StringTokenizer tokenizer = new StringTokenizer(cookie.getValue());
			if (tokenizer.hasMoreTokens())
				language = tokenizer.nextToken();
			if (tokenizer.hasMoreTokens())
				country = tokenizer.nextToken();
			if (tokenizer.hasMoreTokens())
				variant = tokenizer.nextToken();

			// evaluate results
			if (language != null) {
				locale = new Locale(language, country, variant);
				request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, locale);
				return locale;
			}
		}

		// fallback
		return request.getLocale();
	}

	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		Cookie cookie = null;
		if (locale != null) {
			// set request attribute and add cookie
			request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, locale);
			cookie = new Cookie(getCookieName(), locale.getLanguage() + " " + locale.getCountry() +
			                                     " " + locale.getVariant());
			cookie.setPath(getCookiePath());
			cookie.setMaxAge(getCookieMaxAge());
		}
		else {
			// set request attribute to fallback locale and remove cookie
			request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, request.getLocale());
			cookie = new Cookie(getCookieName(), "");
			cookie.setPath(getCookiePath());
			cookie.setMaxAge(0);
		}
		response.addCookie(cookie);
	}

}
