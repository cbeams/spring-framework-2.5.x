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

package org.springframework.web.servlet.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * View that redirects to an internal or external URL,
 * exposing all model attributes as HTTP query parameters.
 *
 * <p>A URL for this view is supposed to be a HTTP redirect URL,
 * i.e. suitable for HttpServletResponse's sendRedirect method.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: RedirectView.java,v 1.7 2004-03-18 02:46:11 trisberg Exp $
 * @see javax.servlet.http.HttpServletResponse#sendRedirect
 */
public class RedirectView extends AbstractUrlBasedView {

	public static final String DEFAULT_ENCODING_SCHEME = "UTF-8";

	private boolean contextRelative = false;

	private String encodingScheme = DEFAULT_ENCODING_SCHEME;

	/**
	 * Constructor for use as a bean.
	 */
	public RedirectView() {
	}

	/**
	 * Create a new RedirectView with the given URL.
	 * @param url the URL to redirect to
	 */
	public RedirectView(String url) {
		setUrl(url);
	}

	/**
	 * Create a new RedirectView with the given URL.
	 * @param url the URL to redirect to
	 * @param contextRelative whether to interpret the given URL as
	 * relative to the current ServletContext
	 */
	public RedirectView(String url, boolean contextRelative) {
		setUrl(url);
		this.contextRelative = contextRelative;
	}

	/**
	 * Set whether to interpret the given URL as relative to the current
	 * ServletContext, i.e. as relative to the web application root.
	 * <p>Default is false: The URL will be intepreted as absolute, i.e.
	 * taken as-is. If true, the context path will be prepended to the URL.
	 * @see javax.servlet.http.HttpServletRequest#getContextPath
	 */
	public void setContextRelative(boolean contextRelative) {
		this.contextRelative = contextRelative;
	}

	/**
	 * Set the encoding scheme for this view.
	 */
	public void setEncodingScheme(String encodingScheme) {
		this.encodingScheme = encodingScheme;
	}

	/**
	 * Convert model to request parameters and redirect to the given URL.
	 * @see #appendQueryProperties
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		StringBuffer targetUrl = new StringBuffer();
		if (this.contextRelative) {
			targetUrl.append(request.getContextPath());
		}
		targetUrl.append(getUrl());
		appendQueryProperties(targetUrl, model, this.encodingScheme);
		response.sendRedirect(response.encodeRedirectURL(targetUrl.toString()));
	}

	/**
	 * Append query properties to the redirect URL.
	 * Stringifies, URL-encodes and formats model attributes as query properties.
	 * @param targetUrl the StringBuffer to append the properties to
	 * @param model Map that contains model attributes
	 * @param encodingScheme the encoding scheme to use
	 * @throws UnsupportedEncodingException if string encoding failed
	 * @see #queryProperties
	 */
	protected void appendQueryProperties(StringBuffer targetUrl, Map model, String encodingScheme)
			throws UnsupportedEncodingException {
		// if there are not already some parameters, we need a ?
		boolean first = (getUrl().indexOf('?') < 0);
		Iterator entries = queryProperties(model).entrySet().iterator();
		while (entries.hasNext()) {
			if (first) {
				targetUrl.append('?');
				first = false;
			}
			else {
				targetUrl.append('&');
			}
			Map.Entry entry = (Map.Entry) entries.next();
			String encodedKey = URLEncoder.encode(entry.getKey().toString());
			String encodedValue = (entry.getValue() != null ? URLEncoder.encode(entry.getValue().toString()) : "");
			targetUrl.append(new String(encodedKey.getBytes(encodingScheme), encodingScheme));
			targetUrl.append("=");
			targetUrl.append(new String(encodedValue.getBytes(encodingScheme), encodingScheme));
		}
	}

	/**
	 * Determine name-value pairs for query strings, which will be stringified,
	 * URL-encoded and formatted by buildRedirectUrl.
	 * <p>This implementation returns all model elements as-is.
	 * @see #appendQueryProperties
	 */
	protected Map queryProperties(Map model) {
		return model;
	}

}
