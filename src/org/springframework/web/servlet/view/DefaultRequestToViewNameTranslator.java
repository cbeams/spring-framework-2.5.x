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

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.util.UrlPathHelper;

/**
 * Default implementation of the {@link RequestToViewNameTranslator} interface that
 * simply transforms the URI of the incoming request into the view name.
 *
 * <p>The default transformation simply strips the leading slash and file extension of
 * the URI and returns the result as the view name with the configured
 * {@link #setPrefix prefix} and {@link #setSuffix suffix} added as appropriate.
 *
 * <p>Stripping of the leading slash and file extension can be disabled using the
 * {@link #setStripLeadingSlash} and {@link #setStripExtension} properties
 * respectively.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class DefaultRequestToViewNameTranslator implements RequestToViewNameTranslator {

	private static final String SLASH = "/";


	private String prefix = "";

	private String suffix = "";

	private String separator = "/";

	private boolean stripLeadingSlash = true;

	private boolean stripExtension = true;

	private UrlPathHelper urlPathHelper = new UrlPathHelper();


	/**
	 * Set the prefix to prepend to generated view names.
	 */
	public void setPrefix(String prefix) {
		this.prefix = (prefix == null ? "" : prefix);
	}

	/**
	 * Set the suffix to append to generated view names.
	 */
	public void setSuffix(String suffix) {
		this.suffix = (suffix == null ? "" : suffix);
	}

	/**
	 * Set the value that will replace '<code>/</code>' as the separator
	 * in the view name. The default behavior simply leaves '<code>/</code>'
	 * as the separator.
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * Set whether or not leadng slashes should be stripped from the URI when
	 * generating the view name. Default is "true".
	 */
	public void setStripLeadingSlash(boolean stripLeadingSlash) {
		this.stripLeadingSlash = stripLeadingSlash;
	}

	/**
	 * Set whether or not file extensions should be stripped from the URI when
	 * generating the view name. Default is "true".
	 */
	public void setStripExtension(boolean stripExtension) {
		this.stripExtension = stripExtension;
	}

	/**
	 * Set if URL lookup should always use the full path within the current servlet
	 * context. Else, the path within the current servlet mapping is used
	 * if applicable (i.e. in the case of a ".../*" servlet mapping in web.xml).
	 * Default is "false".
	 * @see org.springframework.web.util.UrlPathHelper#setAlwaysUseFullPath
	 */
	public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
		this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
	}

	/**
	 * Set if context path and request URI should be URL-decoded.
	 * Both are returned <i>undecoded</i> by the Servlet API,
	 * in contrast to the servlet path.
	 * <p>Uses either the request encoding or the default encoding according
	 * to the Servlet spec (ISO-8859-1).
	 * <p>Note: Setting this to "true" requires JDK 1.4 if the encoding differs
	 * from the VM's platform default encoding, as JDK 1.3's URLDecoder class
	 * does not offer a way to specify the encoding.
	 * @see org.springframework.web.util.UrlPathHelper#setUrlDecode
	 */
	public void setUrlDecode(boolean urlDecode) {
		this.urlPathHelper.setUrlDecode(urlDecode);
	}

	/**
	 * Set the UrlPathHelper to use for resolution of lookup paths.
	 * <p>Use this to override the default UrlPathHelper with a custom subclass,
	 * or to share common UrlPathHelper settings across multiple web components.
	 */
	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		this.urlPathHelper = urlPathHelper;
	}


	/**

	 * Translates the request URI of the incoming {@link HttpServletRequest} to the
	 * view name based on the configured parameters.
	 */
	public final String getViewName(HttpServletRequest request) {
		String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
		return this.prefix + transformPath(lookupPath) + this.suffix;
	}

	/**
	 * Transform the request URI (in the context of the webapp) stripping
	 * slashes and extensions, and replacing the separator as required.
	 * @param lookupPath the lookup path for the current request,
	 * as determined by the UrlPathHelper
	 * @return the transformed path, with slashes and extensions stripped
	 * if desired
	 */
	protected String transformPath(String lookupPath) {
		String path = lookupPath;

		if (this.stripLeadingSlash && path.startsWith(SLASH)) {
			path = path.substring(1);
		}

		if (this.stripExtension) {
			path = StringUtils.stripFilenameExtension(path);
		}

		if (!SLASH.equals(this.separator)) {
			path = StringUtils.replace(path, SLASH, this.separator);
		}

		return path;
	}

}
