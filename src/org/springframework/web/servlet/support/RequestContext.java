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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.ui.context.Theme;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.EscapedErrors;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

/**
 * Context holder for request-specific state, like current web application
 * context, current locale, current theme, and potential binding errors.
 * Provides easy access to localized messages and Errors instances.
 *
 * <p>Suitable for exposition to views, and usage within JSP's "useBean" tag,
 * JSP scriptlets, JSTL EL, Velocity templates, etc. Necessary for views
 * that do not have access to the servlet request, like Velocity templates.
 *
 * <p>Can be instantiated manually, or automatically exposed to views as
 * model attribute via AbstractView's requestContextAttribute property.
 *
 * @author Juergen Hoeller
 * @since 03.03.2003
 * @see org.springframework.web.servlet.view.AbstractView#setRequestContextAttribute
 * @see org.springframework.web.servlet.view.UrlBasedViewResolver#setRequestContextAttribute
 */
public class RequestContext {

	private final HttpServletRequest request;

	private final Map model;

	private final WebApplicationContext webApplicationContext;

	private final Locale locale;

	private final Theme theme;

	private boolean defaultHtmlEscape;

	private UrlPathHelper urlPathHelper;

	private Map errorsMap;


	/**
	 * Create a new RequestContext for the given request,
	 * using the request attributes for Errors retrieval.
	 * <p>This only works with InternalResourceViews, as Errors instances
	 * are part of the model and not normally exposed as request attributes.
	 * It will typically be used within JSPs or custom tags.
	 * @param request current HTTP request
	 */
	public RequestContext(HttpServletRequest request) {
		this(request, null);
	}

	/**
	 * Create a new RequestContext for the given request,
	 * using the given model attributes for Errors retrieval.
	 * <p>This works with all View implementations.
	 * It will typically be used by View implementations.
	 * @param request current HTTP request
	 * @param model the model attributes for the current view
	 */
	public RequestContext(HttpServletRequest request, Map model) {
		this.request = request;
		this.model = model;
		this.webApplicationContext = RequestContextUtils.getWebApplicationContext(request);
		this.locale = RequestContextUtils.getLocale(request);
		this.theme = RequestContextUtils.getTheme(request);
		this.defaultHtmlEscape = WebUtils.isDefaultHtmlEscape(this.webApplicationContext.getServletContext());
		this.urlPathHelper = new UrlPathHelper();
	}

	/**
	 * Return the underlying HttpServletRequest.
	 * Only intended for cooperating classes in this package.
	 */
	protected HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * Return the current WebApplicationContext.
	 */
	public WebApplicationContext getWebApplicationContext() {
		return webApplicationContext;
	}

	/**
	 * Return the current locale.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Return the current theme.
	 */
	public Theme getTheme() {
		return theme;
	}


	/**
	 * (De)activate default HTML escaping for messages and errors, for the scope
	 * of this RequestContext. The default is the application-wide setting
	 * (the "defaultHtmlEscape" context-param in web.xml).
	 * @see org.springframework.web.util.WebUtils#isDefaultHtmlEscape
	 */
	public void setDefaultHtmlEscape(boolean defaultHtmlEscape) {
		this.defaultHtmlEscape = defaultHtmlEscape;
	}

	/**
	 * Is default HTML escaping active?
	 */
	public boolean isDefaultHtmlEscape() {
		return defaultHtmlEscape;
	}

	/**
	 * Set the UrlPathHelper to use for context path and request URI decoding.
	 * Can be used to pass a shared UrlPathHelper instance in.
	 * <p>A default UrlPathHelper is always available.
	 */
	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		this.urlPathHelper = (urlPathHelper != null ? urlPathHelper : new UrlPathHelper());
	}

	/**
	 * Return the UrlPathHelper used for context path and request URI decoding.
	 * Can be used to configure the current UrlPathHelper.
	 * <p>A default UrlPathHelper is always available.
	 */
	public UrlPathHelper getUrlPathHelper() {
		return urlPathHelper;
	}


	/**
	 * Return the context path of the current request,
	 * i.e. the path that indicates the current web application.
	 * <p>Delegates to the UrlPathHelper for decoding.
	 * @see javax.servlet.http.HttpServletRequest#getContextPath
	 * @see #getUrlPathHelper
	 */
	public String getContextPath() {
		return this.urlPathHelper.getContextPath(this.request);
	}

	/**
	 * Return the request URI of the current request, i.e. the invoked URL
	 * without parameters. This is particularly useful as HTML form action target.
	 * <p><b>Note that you should create your RequestContext instance <i>before</i>
	 * forwarding to your JSP view, if you intend to determine the request URI!</b>
	 * The optimal way to do so is to set "requestContextAttribute" on your view.
	 * Else, you'll get the URI of your JSP rather than the one of your controller.
	 * <p>Side note: As alternative for an HTML form action, either specify
	 * an empty "action" or omit the "action" attribute completely. This is
	 * not covered by the HTML spec, though, but known to work reliably on
	 * all current browsers.
	 * <p>Delegates to the UrlPathHelper for decoding.
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI
	 * @see #getUrlPathHelper
	 * @see org.springframework.web.servlet.view.AbstractView#setRequestContextAttribute
	 * @see org.springframework.web.servlet.view.UrlBasedViewResolver#setRequestContextAttribute
	 */
	public String getRequestUri() {
		return this.urlPathHelper.getRequestUri(this.request);
	}


	/**
	 * Retrieve the message for the given code, using the defaultHtmlEscape setting.
	 * @param code code of the message
	 * @param defaultMessage String to return if the lookup fails
	 * @return the message
	 */
	public String getMessage(String code, String defaultMessage) {
		return getMessage(code, null, defaultMessage, this.defaultHtmlEscape);
	}

	/**
	 * Retrieve the message for the given code, using the defaultHtmlEscape setting.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @param defaultMessage String to return if the lookup fails
	 * @return the message
	 */
	public String getMessage(String code, Object[] args, String defaultMessage) {
		return getMessage(code, args, defaultMessage, this.defaultHtmlEscape);
	}

	/**
	 * Retrieve the message for the given code.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @param defaultMessage String to return if the lookup fails
	 * @param htmlEscape HTML escape the message?
	 * @return the message
	 */
	public String getMessage(String code, Object[] args, String defaultMessage, boolean htmlEscape) {
		String msg = this.webApplicationContext.getMessage(code, args, defaultMessage, this.locale);
		return (htmlEscape ? HtmlUtils.htmlEscape(msg) : msg);
	}

	/**
	 * Retrieve the message for the given code, using the defaultHtmlEscape setting.
	 * @param code code of the message
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getMessage(String code) throws NoSuchMessageException {
		return getMessage(code, null, this.defaultHtmlEscape);
	}

	/**
	 * Retrieve the message for the given code, using the defaultHtmlEscape setting.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getMessage(String code, Object[] args) throws NoSuchMessageException {
		return getMessage(code, args, this.defaultHtmlEscape);
	}

	/**
	 * Retrieve the message for the given code.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @param htmlEscape HTML escape the message?
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getMessage(String code, Object[] args, boolean htmlEscape) throws NoSuchMessageException {
		String msg = this.webApplicationContext.getMessage(code, args, this.locale);
		return (htmlEscape ? HtmlUtils.htmlEscape(msg) : msg);
	}

	/**
	 * Retrieve the given MessageSourceResolvable (e.g. an ObjectError instance),
	 * using the defaultHtmlEscape setting.
	 * @param resolvable the MessageSourceResolvable
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getMessage(MessageSourceResolvable resolvable) throws NoSuchMessageException {
		return getMessage(resolvable, this.defaultHtmlEscape);
	}

	/**
	 * Retrieve the given MessageSourceResolvable (e.g. an ObjectError instance).
	 * @param resolvable the MessageSourceResolvable
	 * @param htmlEscape HTML escape the message?
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getMessage(MessageSourceResolvable resolvable, boolean htmlEscape) throws NoSuchMessageException {
		String msg = this.webApplicationContext.getMessage(resolvable, this.locale);
		return (htmlEscape ? HtmlUtils.htmlEscape(msg) : msg);
	}


	/**
	 * Retrieve the theme message for the given code.
	 * <p>Note that theme messages are never HTML-escaped, as they typically
	 * denote theme-specific resource paths and not client-visible messages.
	 * @param code code of the message
	 * @param defaultMessage String to return if the lookup fails
	 * @return the message
	 */
	public String getThemeMessage(String code, String defaultMessage) {
		return this.theme.getMessageSource().getMessage(code, null, defaultMessage, this.locale);
	}

	/**
	 * Retrieve the theme message for the given code.
	 * <p>Note that theme messages are never HTML-escaped, as they typically
	 * denote theme-specific resource paths and not client-visible messages.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @param defaultMessage String to return if the lookup fails
	 * @return the message
	 */
	public String getThemeMessage(String code, String[] args, String defaultMessage) {
		return this.theme.getMessageSource().getMessage(code, args, defaultMessage, this.locale);
	}

	/**
	 * Retrieve the theme message for the given code.
	 * <p>Note that theme messages are never HTML-escaped, as they typically
	 * denote theme-specific resource paths and not client-visible messages.
	 * @param code code of the message
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getThemeMessage(String code) throws NoSuchMessageException {
		return this.theme.getMessageSource().getMessage(code, null, this.locale);
	}

	/**
	 * Retrieve the theme message for the given code.
	 * <p>Note that theme messages are never HTML-escaped, as they typically
	 * denote theme-specific resource paths and not client-visible messages.
	 * @param code code of the message
	 * @param args arguments for the message, or null if none
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getThemeMessage(String code, String[] args) throws NoSuchMessageException {
		return this.theme.getMessageSource().getMessage(code, args, this.locale);
	}

	/**
	 * Retrieve the given MessageSourceResolvable in the current theme.
	 * <p>Note that theme messages are never HTML-escaped, as they typically
	 * denote theme-specific resource paths and not client-visible messages.
	 * @param resolvable the MessageSourceResolvable
	 * @return the message
	 * @throws org.springframework.context.NoSuchMessageException if not found
	 */
	public String getThemeMessage(MessageSourceResolvable resolvable) throws NoSuchMessageException {
		return this.theme.getMessageSource().getMessage(resolvable, this.locale);
	}


	/**
	 * Retrieve the Errors instance for the given bind object,
	 * using the defaultHtmlEscape setting.
	 * @param name name of the bind object
	 * @return the Errors instance, or null if not found
	 */
	public Errors getErrors(String name) {
		return getErrors(name, this.defaultHtmlEscape);
	}

	/**
	 * Retrieve the Errors instance for the given bind object.
	 * @param name name of the bind object
	 * @param htmlEscape create an Errors instance with automatic HTML escaping?
	 * @return the Errors instance, or null if not found
	 */
	public Errors getErrors(String name, boolean htmlEscape) {
		if (this.errorsMap == null) {
			this.errorsMap = new HashMap();
		}
		Errors errors = (Errors) this.errorsMap.get(name);
		boolean put = false;
		if (errors == null) {
			errors = retrieveErrors(name);
			if (errors == null) {
				return null;
			}
			put = true;
		}
		if (htmlEscape && !(errors instanceof EscapedErrors)) {
			errors = new EscapedErrors(errors);
			put = true;
		}
		else if (!htmlEscape && errors instanceof EscapedErrors) {
			errors = ((EscapedErrors) errors).getSource();
			put = true;
		}
		if (put) {
			this.errorsMap.put(name, errors);
		}
		return errors;
	}

	/**
	 * Retrieve the Errors instance for the given bind object,
	 * either from the model or from the request attributes.
	 */
	private Errors retrieveErrors(String name) {
		String key = BindException.ERROR_KEY_PREFIX + name;
		if (this.model != null) {
			return (Errors) this.model.get(key);
		}
		else {
			return (Errors) this.request.getAttribute(key);
		}
	}

	/**
	 * Create a BindStatus for the given bind object,
	 * using the defaultHtmlEscape setting.
	 * @param path the bean and property path for which values and errors
	 * will be resolved (e.g. "person.age")
	 * @return the new BindStatus instance
	 * @throws IllegalStateException if no corresponding Errors object found
	 */
	public BindStatus getBindStatus(String path) throws IllegalStateException {
		return new BindStatus(this, path, this.defaultHtmlEscape);
	}

	/**
	 * Create a BindStatus for the given bind object,
	 * using the defaultHtmlEscape setting.
	 * @param path the bean and property path for which values and errors
	 * will be resolved (e.g. "person.age")
	 * @param htmlEscape create a BindStatus with automatic HTML escaping?
	 * @return the new BindStatus instance
	 * @throws IllegalStateException if no corresponding Errors object found
	 */
	public BindStatus getBindStatus(String path, boolean htmlEscape) throws IllegalStateException {
		return new BindStatus(this, path, htmlEscape);
	}

}
