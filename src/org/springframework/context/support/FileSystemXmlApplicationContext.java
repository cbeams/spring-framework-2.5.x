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

package org.springframework.context.support;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Standalone XML application context, taking the context definition files
 * from the file system or from URLs. Mainly useful for test harnesses,
 * but also for standalone environments.
 *
 * <p>Treats resource paths as file system resources, when using
 * ApplicationContext.getResource. Resource paths are considered relative
 * to the current VM working directory, even if they start with a slash.
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files. This can be leveraged to
 * deliberately override certain bean definitions via an extra XML file.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getResource
 * @see #getResourceByPath
 */
public class FileSystemXmlApplicationContext extends AbstractXmlApplicationContext {

	private String[] configLocations;

	/**
	 * Create a new FileSystemXmlApplicationContext, loading the definitions
	 * from the given XML file.
	 * @param configLocation file path
	 */
	public FileSystemXmlApplicationContext(String configLocation) throws BeansException {
		this.configLocations = new String[] {configLocation};
		refresh();
	}

	/**
	 * Create a new FileSystemXmlApplicationContext, loading the definitions
	 * from the given XML files.
	 * @param configLocations array of file paths
	 */
	public FileSystemXmlApplicationContext(String[] configLocations) throws BeansException {
		this.configLocations = configLocations;
		refresh();
	}

	/**
	 * Create a new FileSystemXmlApplicationContext with the given parent,
	 * loading the definitions from the given XML files.
	 * @param configLocations array of file paths
	 * @param parent the parent context
	 */
	public FileSystemXmlApplicationContext(String[] configLocations, ApplicationContext parent)
			throws BeansException {
		super(parent);
		this.configLocations = configLocations;
		refresh();
	}

	protected String[] getConfigLocations() {
		return this.configLocations;
	}

	/**
	 * Resolve resource paths as file system paths.
	 * <p>Note: Even if a given path starts with a slash, it will get
	 * interpreted as relative to the current VM working directory.
	 * This is consisted with the semantics in a Servlet container.
	 * @param path path to the resource
	 * @return Resource handle
	 */
	protected Resource getResourceByPath(String path) {
		if (path != null && path.startsWith("/")) {
			path = path.substring(1);
		}
		return new FileSystemResource(path);
	}

}
