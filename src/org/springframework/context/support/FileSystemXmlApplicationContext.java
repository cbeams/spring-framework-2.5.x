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
 * <p>The config location defaults can be overridden via setConfigLocations,
 * respectively via the "contextConfigLocation" parameters of ContextLoader and
 * FrameworkServlet. Config locations can either denote concrete files like
 * "/myfiles/context.xml" or Ant-style patterns like "/myfiles/*-context.xml"
 * (see PathMatcher javadoc for pattern details).
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files. This can be leveraged to
 * deliberately override certain bean definitions via an extra XML file.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getResource
 * @see #getResourceByPath
 * @see #getResourcePatternResolver
 */
public class FileSystemXmlApplicationContext extends AbstractXmlApplicationContext {

	private String[] configLocations;

	/**
	 * Create a new FileSystemXmlApplicationContext, loading the definitions
	 * from the given XML file and automatically refreshing the context.
	 * @param configLocation file path
	 */
	public FileSystemXmlApplicationContext(String configLocation) throws BeansException {
		this(new String[] {configLocation});
	}

	/**
	 * Create a new FileSystemXmlApplicationContext, loading the definitions
	 * from the given XML files and automatically refreshing the context.
	 * @param configLocations array of file paths
	 */
	public FileSystemXmlApplicationContext(String[] configLocations) throws BeansException {
		this(configLocations, true);
	}

	/**
	 * Create a new FileSystemXmlApplicationContext, loading the definitions
	 * from the given XML files.
	 * @param configLocations array of file paths
	 * @param refresh whether to automatically refresh the context,
	 * loading all bean definitions and creating all singletons.
	 * Alternatively, call refresh manually after further configuring the context.
	 * @see #refresh
	 */
	public FileSystemXmlApplicationContext(String[] configLocations, boolean refresh) throws BeansException {
		this.configLocations = configLocations;
		if (refresh) {
			refresh();
		}
	}

	/**
	 * Create a new FileSystemXmlApplicationContext with the given parent,
	 * loading the definitions from the given XML files and automatically
	 * refreshing the context.
	 * @param configLocations array of file paths
	 * @param parent the parent context
	 */
	public FileSystemXmlApplicationContext(String[] configLocations, ApplicationContext parent)
			throws BeansException {
		this(configLocations, true, parent);
	}

	/**
	 * Create a new FileSystemXmlApplicationContext with the given parent,
	 * loading the definitions from the given XML files.
	 * @param configLocations array of file paths
	 * @param refresh whether to automatically refresh the context,
	 * loading all bean definitions and creating all singletons.
	 * Alternatively, call refresh manually after further configuring the context.
	 * @param parent the parent context
	 * @see #refresh
	 */
	public FileSystemXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent)
			throws BeansException {
		super(parent);
		this.configLocations = configLocations;
		if (refresh) {
			refresh();
		}
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
