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

package org.springframework.core.io.support;

import java.io.IOException;

import org.springframework.core.io.AbstractPathResolvingPropertyEditor;

/**
 * Editor for Resource descriptor arrays, to automatically convert String
 * location patterns (e.g. "file:C:/my*.txt" or "classpath*:myfile.txt")
 * to Resource array properties.
 *
 * <p>The path may contain ${...} placeholders, to be resolved as
 * system properties: e.g. ${user.dir}.
 *
 * <p>Delegates to a ResourcePatternResolver, by default a
 * PathMatchingResourcePatternResolver.
 *
 * @author Juergen Hoeller
 * @since 1.1.2
 * @see #PLACEHOLDER_PREFIX
 * @see #PLACEHOLDER_SUFFIX
 * @see org.springframework.core.io.Resource
 * @see ResourcePatternResolver
 * @see PathMatchingResourcePatternResolver
 * @see System#getProperty(String)
 */
public class ResourceArrayPropertyEditor extends AbstractPathResolvingPropertyEditor {

	private final ResourcePatternResolver resourcePatternResolver;

	/**
	 * Create a new ResourceArrayPropertyEditor with a default
	 * PathMatchingResourcePatternResolver.
	 * @see PathMatchingResourcePatternResolver
	 */
	public ResourceArrayPropertyEditor() {
		this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
	}

	/**
	 * Create a new ResourceArrayPropertyEditor with the given ResourcePatternResolver.
	 * @param resourcePatternResolver the ResourcePatternResolver to use
	 */
	public ResourceArrayPropertyEditor(ResourcePatternResolver resourcePatternResolver) {
		this.resourcePatternResolver = resourcePatternResolver;
	}

	public void setAsText(String text) {
		String locationPatternToUse = resolvePath(text).trim();
		try {
			setValue(this.resourcePatternResolver.getResources(locationPatternToUse));
		}
		catch (IOException ex) {
			throw new IllegalArgumentException(
			    "Could not convert location pattern [" + locationPatternToUse + "] to Resource array");
		}
	}

}
