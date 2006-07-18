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
package org.springframework.osgi.context;

import java.lang.ref.WeakReference;

import org.osgi.framework.Bundle;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;

/**
 * ResourceLoader implementation that resolves paths inside an OSGi bundle, for
 * use outside a OSGi ApplicationContext. Will use the bundle classpath for
 * resource loading for any unqualified resource string.
 * 
 * Also understands the "bundle:" resource prefix for explicit loading of
 * resources from the bundle. When the bundle prefix is used the target resource
 * must be contained within the bundle (or attached fragments), the classpath is
 * not searched.
 * 
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 * 
 */
public class OsgiBundleResourceLoader extends DefaultResourceLoader {

	public static final String BUNDLE_URL_PREFIX = "bundle:";
	private static final char PREFIX_SEPARATOR = ':';
	private static final String ABSOLUTE_PATH_PREFIX = "/";

	/**
	 * Weak reference to the bundle (in case the bundle gets stopped and the
	 * loader is still used).
	 */
	private WeakReference bundle;

	public OsgiBundleResourceLoader(Bundle bundle) {
		this.bundle = new WeakReference(bundle);
	}

	protected Resource getResourceByPath(String path) {
		return super.getResourceByPath(path);
	}

	protected Bundle getBundleReference() {
		Bundle b = (Bundle) bundle.get();
		if (b == null)
			throw new IllegalStateException("bundle has been undeployed");
		
		return b;
	}

	/**
	 * Implementation of getResource that delegates to the bundle for any
	 * unqualified resource reference or a reference starting with "bundle:"
	 */
	public Resource getResource(String location) {
		Assert.notNull(location, "location is required");
		if (location.startsWith(BUNDLE_URL_PREFIX)) {
			return getResourceFromBundle(location.substring(BUNDLE_URL_PREFIX.length()));
		}
		else if (location.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX)) {
			return getResourceFromBundleClasspath(location.substring(ResourceLoader.CLASSPATH_URL_PREFIX.length()));
		}
		else if (isRelativePath(location)) {
			return getResourceFromBundleClasspath(location);
		}
		else {
			return super.getResource(location);
		}
	}

	/**
	 * Resolves a resource from *this bundle only*. Only the bundle and its
	 * attached fragments are searched for the given resource.
	 * 
	 * @param bundleRelativePath
	 * @return
	 */
	protected Resource getResourceFromBundle(String bundleRelativePath) {
		return new UrlResource(getBundleReference().getEntry(bundleRelativePath));
	}

	/**
	 * Resolves a resource from the bundle's classpath. This will find resources
	 * in this bundle and also in imported packages from other bundles.
	 * 
	 * @param bundleRelativePath
	 * @return
	 */
	protected Resource getResourceFromBundleClasspath(String bundleRelativePath) {
		return new UrlResource(getBundleReference().getResource(bundleRelativePath));
	}

	protected boolean isRelativePath(String locationPath) {
		return ((locationPath.indexOf(PREFIX_SEPARATOR) == -1) && !locationPath.startsWith(ABSOLUTE_PATH_PREFIX));
	}

}
