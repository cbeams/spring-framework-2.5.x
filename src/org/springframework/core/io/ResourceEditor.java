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

package org.springframework.core.io;

import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;

/**
 * Editor for Resource descriptors, to automatically convert String locations
 * (e.g. "file:C:/myfile.txt" or "classpath:myfile.txt") to Resource properties
 * instead of using a String location property.
 *
 * <p>The path may contain ${...} placeholders, to be resolved as
 * system properties: e.g. ${user.dir}.
 *
 * <p>Delegates to a ResourceLoader, by default a DefaultResourceLoader.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see Resource
 * @see ResourceLoader
 * @see DefaultResourceLoader
 * @see org.springframework.util.SystemPropertyUtils#resolvePlaceholders
 * @see System#getProperty(String)
 */
public class ResourceEditor extends PropertyEditorSupport {

	private final ResourceLoader resourceLoader;


	/**
	 * Create a new ResourceEditor with a DefaultResourceLoader.
	 * @see DefaultResourceLoader
	 */
	public ResourceEditor() {
		this.resourceLoader = new DefaultResourceLoader();
	}

	/**
	 * Create a new ResourceEditor with the given ResourceLoader.
	 * @param resourceLoader the ResourceLoader to use
	 */
	public ResourceEditor(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}


	public void setAsText(String text) {
		if (StringUtils.hasText(text)) {
			String locationToUse = resolvePath(text).trim();
			setValue(this.resourceLoader.getResource(locationToUse));
		}
		else {
			setValue(null);
		}
	}

	/**
	 * Resolve the given path, replacing placeholders with
	 * corresponding system property values if necessary.
	 * @param path the original file path
	 * @return the resolved file path
	 * @see org.springframework.util.SystemPropertyUtils#resolvePlaceholders
	 */
	protected String resolvePath(String path) {
		return SystemPropertyUtils.resolvePlaceholders(path);
	}

}
