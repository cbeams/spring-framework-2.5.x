/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.servlet.mvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple <code>Controller</code> implementation that transforms the virtual
 * path of a URL into a view name and returns that view.
 * 
 * <p>Can optionally prepend a {@link #setPrefix prefix} and/or append a
 * {@link #setSuffix suffix} to build the viewname from the URL filename.
 *
 * <p>Find below some examples:
 *
 * <ol>
 *		<li><code>"/index" -> "index"</code></li>
 *		<li><code>"/index.html" -> "index"</code></li>
 *		<li><code>"/index.html"</code> + prefix <code>"pre_"</code> and suffix <code>"_suf" -> "pre_index_suf"</code></li>
 * 		<li><code>"/products/view.html" -> "products/view"</code></li>
 * </ol>
 *
 * <p>Thanks to David Barri for suggesting prefix/suffix support!
 *
 * @author Alef Arendsen
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see #setPrefix
 * @see #setSuffix
 */
public class UrlFilenameViewController extends AbstractUrlViewController {

	private final UrlPathHelper urlPathHelper = new UrlPathHelper();

	private String prefix = "";

	private String suffix = "";

	/** Request URL path String --> view name String */
	private final Map viewNameCache = Collections.synchronizedMap(new HashMap());


	/**
	 * Set the prefix to prepend to the request URL filename
	 * to build a view name.
	 */
	public void setPrefix(String prefix) {
		this.prefix = (prefix != null ? prefix : "");
	}

	/**
	 * Return the prefix to prepend to the request URL filename.
	 */
	protected String getPrefix() {
		return prefix;
	}

	/**
	 * Set the suffix to append to the request URL filename
	 * to build a view name.
	 */
	public void setSuffix(String suffix) {
		this.suffix = (suffix != null ? suffix : "");
	}

	/**
	 * Return the suffix to append to the request URL filename.
	 */
	protected String getSuffix() {
		return suffix;
	}


	/**
	 * Returns view name based on the URL filename,
	 * with prefix/suffix applied when appropriate.
	 * @see #extractViewNameFromUrlPath
	 * @see #setPrefix
	 * @see #setSuffix
	 */
	protected String getViewNameForRequest(HttpServletRequest request) {
		String urlPath = extractOperableUrl(request);
		String viewName = (String) this.viewNameCache.get(urlPath);
		if (viewName == null) {
			viewName = extractViewNameFromUrlPath(urlPath);
			viewName = postProcessViewName(viewName);
			this.viewNameCache.put(urlPath, viewName);
		}
		return viewName;
	}

	protected String extractOperableUrl(HttpServletRequest request) {
		String urlPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		if(!StringUtils.hasText(urlPath)) {
			urlPath = this.urlPathHelper.getPathWithinApplication(request);
		}
		return urlPath;
	}

	/**
	 * Extract the URL filename from the given request URI.
	 * @param uri the request URI; for example <code>"/index.html"</code>
	 * @return the extracted URI filename; for example <code>"index"</code>
	 */
	protected String extractViewNameFromUrlPath(String uri) {
		int start = (uri.charAt(0) == '/' ? 1 : 0);
		int lastIndex = uri.lastIndexOf(".");
		int end = (lastIndex < 0 ? uri.length() : lastIndex);
		return uri.substring(start, end);
	}

	/**
	 * Build the full view name based on the given view name
	 * as indicated by the URL path.
	 * <p>The default implementation simply applies prefix and suffix.
	 * This can be overridden, for example, to manipulate upper case
	 * / lower case, etc.
	 * @param viewName the original view name, as indicated by the URL path
	 * @return the full view name to use
	 * @see #getPrefix()
	 * @see #getSuffix()
	 */
	protected String postProcessViewName(String viewName) {
		return getPrefix() + viewName + getSuffix();
	}

}
