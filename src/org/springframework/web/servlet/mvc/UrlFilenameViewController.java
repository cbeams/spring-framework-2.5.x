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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

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
public class UrlFilenameViewController extends AbstractController {

	private String prefix = "";

	private String suffix = "";

	/** Request URI String --> view name String */
	private final Map viewNameCache = Collections.synchronizedMap(new HashMap());


	/**
	 * Set the prefix that gets prepended to the request URL filename
	 * to build a view name.
	 */
	public void setPrefix(String prefix) {
		this.prefix = (prefix != null ? prefix : "");
	}

	/**
	 * Set the suffix that gets appended to the request URL filename
	 * to build a view name.
	 */
	public void setSuffix(String suffix) {
		this.suffix = (suffix != null ? suffix : "");
	}


	/**
	 * Returns a ModelAndView with the view name being the URL filename,
	 * with prefix/suffix applied when appropriate.
	 * @see #getFilenameFromRequestURI
	 * @see #setPrefix
	 * @see #setSuffix
	 */
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		String uri = request.getRequestURI();
		String viewName = (String) this.viewNameCache.get(uri);
		if (viewName == null) {
			viewName = this.prefix + getFilenameFromRequestURI(uri) + this.suffix;
			this.viewNameCache.put(uri, viewName);
		}
		return new ModelAndView(viewName);
	}

	/**
	 * Extract the URL filename from the given request URI. Correctly resolves nested paths such
	 * as /products/view.html as well.
	 * @param uri the request URI (e.g. "/index.html")
	 * @return the extracted URI filename (e.g. "index")
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	protected String getFilenameFromRequestURI(String uri) {
		int begin = (uri.startsWith("/") ? 1 : 0);
		int end = uri.indexOf(';');
		if (end == -1) {
			end = uri.indexOf('?');
			if (end == -1) {
				end = uri.length();
			}
		}
		String filename = uri.substring(begin, end);
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex != -1) {
			filename = filename.substring(0, dotIndex);
		}
		return filename;
	}
}
