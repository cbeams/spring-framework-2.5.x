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

package org.springframework.core.io;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for PropertyEditors that need
 * to resolve placeholders in paths.
 *
 * <p>A path may contain ${...} placeholders, to be resolved as
 * system properties: e.g. ${user.dir}.
 *
 * @author Juergen Hoeller
 * @since 1.1.2
 * @see #PLACEHOLDER_PREFIX
 * @see #PLACEHOLDER_SUFFIX
 * @see System#getProperty(String)
 */
public class AbstractPathResolvingPropertyEditor extends PropertyEditorSupport {

	public static final String PLACEHOLDER_PREFIX = "${";

	public static final String PLACEHOLDER_SUFFIX = "}";

	protected static final Log logger = LogFactory.getLog(AbstractPathResolvingPropertyEditor.class);

	/**
	 * Resolve the given path, replacing ${...} placeholders with
	 * corresponding system property values if necessary.
	 * @param path the original file path
	 * @return the resolved file path
	 * @see #PLACEHOLDER_PREFIX
	 * @see #PLACEHOLDER_SUFFIX
	 */
	protected String resolvePath(String path) {
		StringBuffer buf = new StringBuffer(path);

		// The following code does not use JDK 1.4's StringBuffer.indexOf(String)
		// method to retain JDK 1.3 compatibility. The slight loss in performance
		// is not really relevant, as this code will typically just run on startup.

		int startIndex = path.indexOf(PLACEHOLDER_PREFIX);
		while (startIndex != -1) {
			int endIndex = buf.toString().indexOf(PLACEHOLDER_SUFFIX, startIndex + PLACEHOLDER_PREFIX.length());
			if (endIndex != -1) {
				String placeholder = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
				String propVal = System.getProperty(placeholder);
				if (propVal != null) {
					buf.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), propVal);
					startIndex = buf.toString().indexOf(PLACEHOLDER_PREFIX, startIndex + propVal.length());
				}
				else {
					logger.warn("Could not resolve placeholder '" + placeholder +
					    "' in resource path [" + path + "] as system property");
					startIndex = buf.toString().indexOf(PLACEHOLDER_PREFIX, endIndex + PLACEHOLDER_SUFFIX.length());
				}
			}
			else {
				startIndex = -1;
			}
		}

		return buf.toString();
	}

}
