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

package org.springframework.core.io.support;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;

/**
 * ResourcePatternResolver which can retrieve multiple class path resources
 * with the same name. For example, "classpath:/beans.xml" will find all
 * beans.xml files in the class path, be it in "classes" directories or in
 * JAR files. This is particularly useful for auto-detecting config files.
 *
 * <p>For all other resource locations, this resolver returns a single resource
 * via the ResourceLoader that it uses.
 *
 * @author Juergen Hoeller
 * @since 23.06.2004
 */
public class ClassPathResourcePatternResolver implements ResourcePatternResolver {

	private final ResourceLoader resourceLoader;

	/**
	 * Create a new ClassPathResourcePatternResolver with a DefaultResourceLoader.
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public ClassPathResourcePatternResolver() {
		this.resourceLoader = new DefaultResourceLoader();
	}

	/**
	 * Create a new ClassPathResourcePatternResolver.
	 * @param resourceLoader ResourceLoader to load resources with
	 */
	public ClassPathResourcePatternResolver(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Return the ResourceLoader used by this ResourcePatternResolver.
	 */
	protected final ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public Resource[] getResources(String locationPattern) throws IOException {
		List result = new ArrayList();

		// check for class path resource (multiple resources for same name possible)
		if (locationPattern.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX)) {
			Enumeration resourceUrls = Thread.currentThread().getContextClassLoader().getResources(
					locationPattern.substring(ResourceLoader.CLASSPATH_URL_PREFIX.length()));
			while (resourceUrls.hasMoreElements()) {
				URL url = (URL) resourceUrls.nextElement();
				result.add(new UrlResource(url));
			}
		}

		// fall back to single resource
		else {
			result.add(this.resourceLoader.getResource(locationPattern));
		}

		return (Resource[]) result.toArray(new Resource[result.size()]);
	}

}
