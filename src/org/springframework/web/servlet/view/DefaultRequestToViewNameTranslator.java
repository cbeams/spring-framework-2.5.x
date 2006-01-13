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

package org.springframework.web.servlet.view;

import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Default implementation of the {@link RequestToViewNameTranslator} interface that
 * simply transforms the URI of the incoming request into the view name.
 * <p/>
 * The default transformation simply strips the leading slash and file extension of
 * the URI and returns the result as the view name with the configured
 * {@link #setPrefix prefix} and {@link #setSuffix suffix} added as appropriate.
 * <p/>
 * Stripping of the leading slash and file extension can be disabled using the
 * {@link #setStripLeadingSlashes} and {@link #setStripExtensions} properties
 * respectively.
 *
 * @author Rob Harrop
 * @since 2.0M2
 */
public class DefaultRequestToViewNameTranslator implements RequestToViewNameTranslator {

	/**
	 * The <code>/</code> character.
	 */
	private static final String SLASH = "/";

	/**
	 * The <code>.</code> character.
	 */
	private static final String DOT = ".";

	/**
	 * {@link UrlPathHelper} implementation used to split up the incoming request URI.
	 */
	private UrlPathHelper pathHelper = new UrlPathHelper();

	/**
	 * The separator to use in place of slashes when generating the view name.
	 */
	private String separator =SLASH;

	/**
	 * The prefix to add to any generated view names.
	 */
	private String prefix = "";

	/**
	 * The suffix to add to any generated view names.
	 */
	private String suffix = "";

	/**
	 * Should file extensions be stripped from the URL?
	 */
	private boolean stripExtensions = true;

	/**
	 * Should leading slashes be stripped from the URL?
	 */
	private boolean stripLeadingSlashes = true;

	/**
	 * Sets the prefix to prepend to generated view names.
	 */
	public void setPrefix(String prefix) {
		this.prefix = (prefix == null ? "" : prefix);
	}

	/**
	 * Sets the suffix to append to generated view names.
	 */
	public void setSuffix(String suffix) {
		this.suffix = (suffix == null ? "" : suffix);
	}

	/**
	 * Sets the value that will replace '<code>/</code>' as the separator
	 * in the view name. The default behaviour simply leaves '<code>/</code>'
	 * as the separator.
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * Sets whether or not file extensions should be stripped from the URI when
	 * generating the view name. Default is <code>true</code>.
	 */
	public void setStripExtensions(boolean stripExtensions) {
		this.stripExtensions = stripExtensions;
	}

	/**
	 * Sets whether or not leadng slashes should be stripped from the URI when
	 * generating the view name. Default is <code>true</code>.
	 */
	public void setStripLeadingSlashes(boolean stripLeadingSlashes) {
		this.stripLeadingSlashes = stripLeadingSlashes;
	}

	/**
	 * Sets the {@link UrlPathHelper} to use when manipulating the request URI
	 */
	public void setPathHelper(UrlPathHelper pathHelper) {
		this.pathHelper = pathHelper;
	}

	/**
	 * Translates the request URI of the incoming {@link HttpServletRequest} to the
	 * view name based on the configured parameters.
	 */
	public final String translate(HttpServletRequest request) {
		String pathInMapping = this.pathHelper.getPathWithinApplication(request);
		return this.prefix + transformPath(pathInMapping) + this.suffix;
	}

	/**
	 * Transforms the request URI (in the context of the webapp) stripping
	 * slashes and extensions, and replacing the separator as required.
	 */
	protected String transformPath(String pathInApplication) {
		String path = pathInApplication;

		if(this.stripLeadingSlashes && path.startsWith(SLASH)) {
			path = path.substring(1);
		}

		if(this.stripExtensions && path.indexOf(DOT) > -1) {
			path = path.substring(0, path.lastIndexOf(DOT));
		}

		return !SLASH.equals(this.separator) ? path : StringUtils.replace(path, SLASH, this.separator);
	}
}
