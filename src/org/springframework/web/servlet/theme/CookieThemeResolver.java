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

package org.springframework.web.servlet.theme;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.WebUtils;

/**
 * Implementation of ThemeResolver that uses a cookie sent back to the user
 * in case of a custom setting, with a fallback to the default theme.
 * This is especially useful for stateless applications without user sessions.
 *
 * <p>Custom controllers can thus override the user's theme by calling
 * <code>setThemeName</code>, e.g. responding to a certain theme change request.
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @since 17.06.2003
 * @see #setThemeName
 */
public class CookieThemeResolver extends AbstractThemeResolver {

	/**
	 * Name of the request attribute that holds the theme name. Only used
	 * for overriding a cookie value if the theme has been changed in the
	 * course of the current request! Use RequestContext.getTheme() to
	 * retrieve the current theme in controllers or views.
	 * @see org.springframework.web.servlet.support.RequestContext#getTheme
	 */
	public static final String THEME_REQUEST_ATTRIBUTE_NAME = CookieThemeResolver.class.getName() + ".THEME";

	public static final String DEFAULT_COOKIE_NAME = CookieThemeResolver.class.getName() + ".THEME";

	public static final String DEFAULT_COOKIE_PATH = "/";

	public static final int DEFAULT_COOKIE_MAX_AGE = Integer.MAX_VALUE;


	private String cookieName = DEFAULT_COOKIE_NAME;

	private int cookieMaxAge = DEFAULT_COOKIE_MAX_AGE;

	private String cookiePath = DEFAULT_COOKIE_PATH;


	/**
	 * Use the given name for theme cookies, containing the theme name.
	 */
	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

	/**
	 * Return the name for theme cookies.
	 */
	public String getCookieName() {
		return cookieName;
	}

	/**
	 * Use the given path for theme cookies.
	 * The cookie is only visible for URLs in the path and below. 
	 */
	public void setCookiePath(String cookiePath) {
		this.cookiePath = cookiePath;
	}

	/**
	 * Return the path for theme cookies.
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


	public String resolveThemeName(HttpServletRequest request) {
		// check theme for preparsed resp. preset theme
		String theme = (String) request.getAttribute(THEME_REQUEST_ATTRIBUTE_NAME);
		if (theme != null)
			return theme;

		// retrieve cookie value
		Cookie cookie = WebUtils.getCookie(request, getCookieName());

		if (cookie != null) {
			return cookie.getValue();
		}

		// fallback
		return getDefaultThemeName();
	}

	public void setThemeName(HttpServletRequest request, HttpServletResponse response, String themeName) {
		Cookie cookie = null;
		if (themeName != null) {
			// set request attribute and add cookie
			request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, themeName);
			cookie = new Cookie(getCookieName(), themeName);
			cookie.setPath(getCookiePath());
			cookie.setMaxAge(getCookieMaxAge());
		}
		else {
			// set request attribute to fallback theme and remove cookie
			request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, getDefaultThemeName());
			cookie = new Cookie(getCookieName(), "");
			cookie.setPath(getCookiePath());
			cookie.setMaxAge(0);
		}
		response.addCookie(cookie);
	}

}
