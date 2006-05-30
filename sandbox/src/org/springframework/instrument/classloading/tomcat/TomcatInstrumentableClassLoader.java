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

package org.springframework.instrument.classloading.tomcat;

import java.lang.instrument.ClassFileTransformer;

import org.apache.catalina.loader.ResourceEntry;
import org.apache.catalina.loader.WebappClassLoader;

import org.springframework.instrument.classloading.InstrumentationRegistry;
import org.springframework.instrument.classloading.support.WeavingTransformer;

/**
 * Extension of tomcat default classloader which adds instrumentation to loaded
 * classes without the need of using an agent.
 * 
 * @author Costin Leau
 * @since 2.0
 */
public class TomcatInstrumentableClassLoader extends WebappClassLoader implements InstrumentationRegistry {

	/** Use an internal weavingTransformer */
	private final WeavingTransformer weavingTransformer;


	public TomcatInstrumentableClassLoader() {
		super();
		this.weavingTransformer = new WeavingTransformer();
	}

	public TomcatInstrumentableClassLoader(ClassLoader classLoader) {
		super(classLoader);
		this.weavingTransformer = new WeavingTransformer(classLoader);
	}


	public void addClassFileTransformer(ClassFileTransformer cft) {
		this.weavingTransformer.addClassFileTransformer(cft);
	}

	@Override
	protected ResourceEntry findResourceInternal(String name, String path) {
		ResourceEntry entry = super.findResourceInternal(name, path);
		// Postpone String parsing as much as possible (it is slow).
		if (entry != null && entry.binaryContent != null && path.endsWith(".class")) {
			byte[] transformed = weavingTransformer.transformIfNecessary(name, entry.binaryContent, null);
			entry.binaryContent = transformed;
		}
		return entry;
	}

	/**
	 * Return a new clone of the existing classloader w/ transforming turned off.
	 */
	public ClassLoader getThrowawayClassLoader() {
		// copy properties
		TomcatInstrumentableClassLoader tempClassLoader = new TomcatInstrumentableClassLoader(this.parent);
		// copy protected fields
		tempClassLoader.allPermission = this.allPermission;
		tempClassLoader.delegate = this.delegate;
		tempClassLoader.files = this.files;
		tempClassLoader.hasExternalRepositories = this.hasExternalRepositories;
		tempClassLoader.jarFiles = this.jarFiles;
		tempClassLoader.jarNames = this.jarNames;
		tempClassLoader.jarPath = this.jarPath;
		tempClassLoader.jarRealFiles = this.jarRealFiles;
		tempClassLoader.lastJarAccessed = this.lastJarAccessed;
		tempClassLoader.lastModifiedDates = this.lastModifiedDates;
		tempClassLoader.loaderDir = this.loaderDir;
		tempClassLoader.loaderPC = this.loaderPC;
		tempClassLoader.needConvert = this.needConvert;
		tempClassLoader.notFoundResources = this.notFoundResources;
		tempClassLoader.parent = this.parent;
		tempClassLoader.paths = this.paths;
		tempClassLoader.permissionList = this.permissionList;
		tempClassLoader.repositories = this.repositories;
		tempClassLoader.repositoryURLs = this.repositoryURLs;
		//tempClassLoader.resourceEntries = this.resourceEntries;
		tempClassLoader.resources = this.resources;
		tempClassLoader.securityManager = this.securityManager;
		tempClassLoader.started = this.started;
		tempClassLoader.system = this.system;
		// copy the rest of the fields through methods
		tempClassLoader.setAntiJARLocking(this.getAntiJARLocking());

		if (tempClassLoader.hasExternalRepositories) {
			/*// get URLClassLoader internal URLs and add it to the new temp class
			for (URL url : retrieveURLs()) {
				tempClassLoader.addURL(url);
			}*/
			WebappClassLoader.log.warn("copy URLS also");
		}

		return tempClassLoader;
	}

	/**
	 * Get super URLClassloader internal URLs. This is required since Tomcat's
	 * WebappClassLoader rewrites this method and add's extra URLs. The current
	 * implementation uses reflection in order to avoid too much coupling with
	 * WebappClassLoader internals.
	 *
	protected URL[] retrieveURLs() {
		return (URL[]) ReflectionUtils.invokeMethod("getURLs", URLClassLoader.class, this, null, (Class[]) null);
	}
	*/


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TomcatInstrumentedClassLoader\r\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
