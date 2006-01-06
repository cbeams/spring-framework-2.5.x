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
 * @author Rob Harrop
 * @since 2.0M2
 */
public class DefaultRequestToViewNameTranslator implements RequestToViewNameTranslator {

	private static final String SLASH = "/";

	private UrlPathHelper pathHelper = new UrlPathHelper();

	private String separator =SLASH;

	private boolean stripExtensions = true;

	private boolean stripLeadingSlashes = true;

	private static final String DOT = ".";

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public void setStripExtensions(boolean stripExtensions) {
		this.stripExtensions = stripExtensions;
	}

	public void setStripLeadingSlashes(boolean stripLeadingSlashes) {
		this.stripLeadingSlashes = stripLeadingSlashes;
	}

	public void setPathHelper(UrlPathHelper pathHelper) {
		this.pathHelper = pathHelper;
	}

	public final String translate(HttpServletRequest request) {
		String pathInMapping = this.pathHelper.getPathWithinServletMapping(request);
		return transformPath(pathInMapping);
	}

	protected String transformPath(String pathInMapping) {
		String path = pathInMapping;

		if(this.stripLeadingSlashes && path.startsWith(SLASH)) {
			path = path.substring(1);
		}

		if(this.stripExtensions && path.indexOf(DOT) > -1) {
			path = path.substring(0, path.lastIndexOf(DOT));
		}

		return !SLASH.equals(this.separator) ? path : StringUtils.replace(path, SLASH, this.separator);
	}
}
