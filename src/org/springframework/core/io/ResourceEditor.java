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

package org.springframework.core.io;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Editor for Resource descriptors, to convert String locations to Resource
 * properties automatically instead of using a String location property.
 *
 * <p>The path may contain ${...} placeholders, to be resolved as
 * system properties: e.g. ${user.dir}.
 *
 * <p>Delegates to a ResourceLoader, by default a DefaultResourceLoader.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see #PLACEHOLDER_PREFIX
 * @see #PLACEHOLDER_SUFFIX
 * @see Resource
 * @see ResourceLoader
 * @see DefaultResourceLoader
 * @see System#getProperty(String)
 */
public class ResourceEditor extends PropertyEditorSupport {

	protected static final Log logger = LogFactory.getLog(ResourceEditor.class);

	public static final String PLACEHOLDER_PREFIX = "${";

	public static final String PLACEHOLDER_SUFFIX = "}";

	private final ResourceLoader resourceLoader;

	/**
	 * Create new ResourceEditor with DefaultResourceLoader.
	 * @see DefaultResourceLoader
	 */
	public ResourceEditor() {
		this.resourceLoader = new DefaultResourceLoader();
	}

	/**
	 * Create new ResourceEditor with given ResourceLoader.
	 * @param resourceLoader the ResourceLoader to use
	 */
	public ResourceEditor(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void setAsText(String text) {
		String locationToUse = resolvePath(text).trim();
		setValue(this.resourceLoader.getResource(locationToUse));
	}

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
		int startIndex = buf.indexOf(PLACEHOLDER_PREFIX);
		while (startIndex != -1) {
			int endIndex = buf.indexOf(PLACEHOLDER_SUFFIX, startIndex + PLACEHOLDER_PREFIX.length());
			if (endIndex != -1) {
				String placeholder = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
				String propVal = System.getProperty(placeholder);
				if (propVal != null) {
					buf.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), propVal);
					startIndex = buf.indexOf(PLACEHOLDER_PREFIX, startIndex + propVal.length());
				}
				else {
					logger.warn("Could not resolve placeholder '" + placeholder +
					    "' in resource path [" + path + "] as system property");
					startIndex = buf.indexOf(PLACEHOLDER_PREFIX, endIndex + PLACEHOLDER_SUFFIX.length());
				}
			}
			else {
				startIndex = -1;
			}
		}
		return buf.toString();
	}

}
