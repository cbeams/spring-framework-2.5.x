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

package org.springframework.instrument.classloading;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Subclass of ShadowingClassLoader that overrides attempts to
 * locate certain files.
 *
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0
 */
public class ResourceOverridingShadowingClassLoader extends ShadowingClassLoader {
	
	private static final Enumeration<URL> EMPTY_URL_ENUMERATION = new Enumeration<URL>() {

		public boolean hasMoreElements() {
			return false;
		}

		public URL nextElement() {
			throw new UnsupportedOperationException("Should not be called. I am empty");
		}
	};
	
	/**
	 * Key is asked for value: value is actual value
	 */
	private Map<String, String> overrides = new HashMap<String, String>(); 
	
	public ResourceOverridingShadowingClassLoader(ClassLoader loader) {
		super(loader);
	}
	
	/**
	 * Return the resource (if any) at the new path
	 * on an attempt to locate a resource at the old path
	 * @param oldPath path requested
	 * @param newPath path looked up
	 */
	public void override(String oldPath, String newPath) {
		overrides.put(oldPath, newPath);
	}
	
	/**
	 * Ensure that a resource with the given path is not found
	 * @param oldPath path of resource to hide even if it exists
	 * in the parent class loader
	 */
	public void suppress(String oldPath) {
		overrides.put(oldPath, null);
	}
	
	public void copyOverrides(ResourceOverridingShadowingClassLoader other) {
		overrides.putAll(other.overrides);
	}


	@Override
	public URL getResource(String requestedPath) {
		if (overrides.containsKey(requestedPath)) {
			String overriddenPath = overrides.get(requestedPath);
			return overriddenPath == null ? 
					null : 
					super.getResource(overriddenPath);
		}
		else {
			return super.getResource(requestedPath);
		}
	}

	@Override
	public InputStream getResourceAsStream(String requestedPath) {
		if (overrides.containsKey(requestedPath)) {
			String overriddenPath = overrides.get(requestedPath);
			return overriddenPath == null ? 
					null : 
					super.getResourceAsStream(overriddenPath);
		}
		else {
			return super.getResourceAsStream(requestedPath);
		}
	}
	
	@Override
	public Enumeration<URL> getResources(String requestedPath) throws IOException {
		if (overrides.containsKey(requestedPath)) {
			String overriddenLocation = overrides.get(requestedPath);
			return overriddenLocation == null ? 
					EMPTY_URL_ENUMERATION :
					super.getResources(overriddenLocation);
		}
		else {
			return super.getResources(requestedPath);
		}
	}

}
