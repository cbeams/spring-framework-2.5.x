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

package org.springframework.web.servlet.support;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.context.Theme;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ThemeResolver;

/**
 * Utility class for easy access to request-specific state
 * which has been set by the DispatcherServlet.
 *
 * <p>Supports lookup of current WebApplicationContext, LocaleResolver,
 * Locale, ThemeResolver, Theme, and MultipartResolver.
 *
 * @author Juergen Hoeller
 * @since 03.03.2003
 * @see RequestContext
 * @see org.springframework.web.servlet.DispatcherServlet
 */
public abstract class RequestContextUtils {

	/**
	 * Look for the WebApplicationContext associated with the DispatcherServlet
	 * that has initiated request processing.
	 * @param request current HTTP request
	 * @return the request-specific web application context
	 * @throws IllegalStateException if no servlet-specific context has been found
	 */
	public static WebApplicationContext getWebApplicationContext(ServletRequest request)
	    throws IllegalStateException {
		return getWebApplicationContext(request, null);
	}

	/**
	 * Look for the WebApplicationContext associated with the DispatcherServlet
	 * that has initiated request processing, and for the global context if none
	 * was found associated with the current request. This method is useful to
	 * allow components outside the framework, such as JSP tag handlers,
	 * to access the most specific application context available.
	 * @param request current HTTP request
	 * @param servletContext current servlet context
	 * @return the request-specific WebApplicationContext, or the global one
	 * if no request-specific context has been found
	 * @throws IllegalStateException if neither a servlet-specific nor a
	 * global context has been found
	 */
	public static WebApplicationContext getWebApplicationContext(ServletRequest request,
	                                                             ServletContext servletContext)
	    throws IllegalStateException {
		WebApplicationContext webApplicationContext = (WebApplicationContext) request.getAttribute(
				DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		if (webApplicationContext == null) {
			if (servletContext == null) {
				throw new IllegalStateException("No WebApplicationContext found: not in a DispatcherServlet request?");
			}
			webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			if (webApplicationContext == null) {
				throw new IllegalStateException("No WebApplicationContext found: no ContextLoaderListener registered?");
			}
		}
		return webApplicationContext;
	}

	/**
	 * Return the LocaleResolver that has been bound to the request by the
	 * DispatcherServlet.
	 * @param request current HTTP request
	 * @return the current LocaleResolver
	 * @throws IllegalStateException if no LocaleResolver has been found
	 */
	public static LocaleResolver getLocaleResolver(HttpServletRequest request) throws IllegalStateException {
		LocaleResolver localeResolver = (LocaleResolver) request.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE);
		if (localeResolver == null) {
			throw new IllegalStateException("No LocaleResolver found: not in a DispatcherServlet request?");
		}
		return localeResolver;
	}

	/**
	 * Retrieves the current locale from the given request,
	 * using the LocaleResolver bound to the request by the DispatcherServlet.
	 * @param request current HTTP request
	 * @return the current locale
	 * @throws IllegalStateException if no LocaleResolver has been found
	 */
	public static Locale getLocale(HttpServletRequest request) throws IllegalStateException {
		return getLocaleResolver(request).resolveLocale(request);
	}

	/**
	 * Return the ThemeResolver that has been bound to the request by the
	 * DispatcherServlet.
	 * @param request current HTTP request
	 * @return the current ThemeResolver
	 * @throws IllegalStateException if no ThemeResolver has been found
	 */
	public static ThemeResolver getThemeResolver(HttpServletRequest request) throws IllegalStateException {
		ThemeResolver themeResolver = (ThemeResolver) request.getAttribute(DispatcherServlet.THEME_RESOLVER_ATTRIBUTE);
		if (themeResolver == null) {
			throw new IllegalStateException("No ThemeResolver found: not in a DispatcherServlet request?");
		}
		return themeResolver;
	}

	/**
	 * Retrieves the current theme from the given request, using the
	 * ThemeResolver bound to the request by the DispatcherServlet.
	 * @param request current HTTP request
	 * @return the current theme
	 * @throws IllegalStateException if no ThemeResolver has been found
	 */
	public static Theme getTheme(HttpServletRequest request) throws IllegalStateException {
		WebApplicationContext context = getWebApplicationContext(request);
		String themeName = getThemeResolver(request).resolveThemeName(request);
		return context.getTheme(themeName);
	}

	/**
	 * Return the MultipartResolver that has been bound to the request by the
	 * DispatcherServlet.
	 * @param request current HTTP request
	 * @return the current MultipartResolver, or null if not a multipart request
	 */
	public static MultipartResolver getMultipartResolver(ServletRequest request) {
		return (MultipartResolver) request.getAttribute(DispatcherServlet.MULTIPART_RESOLVER_ATTRIBUTE);
	}

}
