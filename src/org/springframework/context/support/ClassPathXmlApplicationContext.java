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

package org.springframework.context.support;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * Standalone XML application context, taking the context definition
 * files from the class path. Mainly useful for test harnesses,
 * but also for application contexts embedded within JARs.
 *
 * <p>Treats resource paths as class path resources, when using
 * <code>getResource</code>. Only supports full class path resource
 * names that include the package path, like "mypackage/myresource.dat".
 *
 * <p>The config location defaults can be overridden via <code>getConfigLocations</code>,
 * Config locations can either denote concrete files like "/myfiles/context.xml"
 * or Ant-style patterns like "/myfiles/*-context.xml" (see PathMatcher javadoc for
 * pattern details).
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files. This can be leveraged to
 * deliberately override certain bean definitions via an extra XML file.
 *
 * <p><b>This is a simple, one-stop shop convenience ApplicationContext.
 * Consider using the GenericApplicationContext class in combination
 * with an XmlBeanDefinitionReader for more flexible context setup.</b>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getResource
 * @see #getResourceByPath
 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
 * @see GenericApplicationContext
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 */
public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {

	private String[] configLocations;


	/**
	 * Create a new ClassPathXmlApplicationContext, loading the definitions
	 * from the given XML file and automatically refreshing the context.
	 * @param configLocation file path
	 */
	public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
		this(new String[] {configLocation});
	}

	/**
	 * Create a new ClassPathXmlApplicationContext, loading the definitions
	 * from the given XML files and automatically refreshing the context.
	 * @param configLocations array of file paths
	 */
	public ClassPathXmlApplicationContext(String[] configLocations) throws BeansException {
		this(configLocations, true);
	}

	/**
	 * Create a new ClassPathXmlApplicationContext, loading the definitions
	 * from the given XML files.
	 * @param configLocations array of file paths
	 * @param refresh whether to automatically refresh the context,
	 * loading all bean definitions and creating all singletons.
	 * Alternatively, call refresh manually after further configuring the context.
	 * @see #refresh()
	 */
	public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh) throws BeansException {
		this.configLocations = configLocations;
		if (refresh) {
			refresh();
		}
	}

	/**
	 * Create a new ClassPathXmlApplicationContext with the given parent,
	 * loading the definitions from the given XML files and automatically
	 * refreshing the context.
	 * @param configLocations array of file paths
	 * @param parent the parent context
	 */
	public ClassPathXmlApplicationContext(String[] configLocations, ApplicationContext parent)
			throws BeansException {

		this(configLocations, true, parent);
	}

	/**
	 * Create a new ClassPathXmlApplicationContext with the given parent,
	 * loading the definitions from the given XML files.
	 * @param configLocations array of file paths
	 * @param refresh whether to automatically refresh the context,
	 * loading all bean definitions and creating all singletons.
	 * Alternatively, call refresh manually after further configuring the context.
	 * @param parent the parent context
	 * @see #refresh()
	 */
	public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent)
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

}
