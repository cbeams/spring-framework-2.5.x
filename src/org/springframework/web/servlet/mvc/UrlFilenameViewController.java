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

package org.springframework.web.servlet.mvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.util.WebUtils;

/**
 * Controller that transforms the virtual filename at the end of a URL
 * into a view name and returns that view. Can optionally prepend a prefix
 * and/or append a suffix to build the viewname from the URL filename.
 *
 * <p>Example: "/index" -> "index"
 * Example: "/index.html" -> "index"
 * Example: "/index.html" + prefix "pre_" and suffix "_suf" -> "pre_index_suf".
 *
 * <p>Thanks to David Barri for suggesting prefix/suffix support!
 *
 * @author Alef Arendsen
 * @author Juergen Hoeller
 * @see #setPrefix
 * @see #setSuffix
 */
public class UrlFilenameViewController extends AbstractUrlViewController {

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
	protected String getViewNameForUrlPath(String urlPath) {
		String viewName = (String) this.viewNameCache.get(urlPath);
		if (viewName == null) {
			viewName = extractViewNameFromUrlPath(urlPath);
			viewName = postProcessViewName(viewName);
			this.viewNameCache.put(urlPath, viewName);
		}
		return viewName;
	}

	/**
	 * Extract the URL filename from the given request URI.
	 * Delegates to <code>WebUtils.extractViewNameFromUrlPath(String)</code>.
	 * @param uri the request URI (e.g. "/index.html")
	 * @return the extracted URI filename (e.g. "index")
	 * @see org.springframework.web.util.WebUtils#extractFilenameFromUrlPath
	 */
	protected String extractViewNameFromUrlPath(String uri) {
		return WebUtils.extractFilenameFromUrlPath(uri);
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
