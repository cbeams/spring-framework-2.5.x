/*
 * Copyright 2007 the original author or authors.
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
package org.springframework.test.context;

import org.springframework.context.ApplicationContext;

/**
 * <p>
 * Strategy interface for loading an
 * {@link ApplicationContext application context} based on a supplied set of
 * {@link ContextConfigurationAttributes configuration attributes}.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.4 $
 * @since 2.1
 */
public interface ContextLoader {

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Loads a new {@link ApplicationContext context} based on the supplied
	 * {@link ContextConfigurationAttributes configuration attributes},
	 * configures the context, and finally returns the context, potentially
	 * <em>refreshed</em>.
	 * </p>
	 * <p>
	 * {@link ContextConfigurationAttributes#getLocations() Configuration locations}
	 * should be considered to be classpath resources by default.
	 * </p>
	 * <p>
	 * Concrete implementations should register annotation configuration
	 * processors with bean factories of
	 * {@link ApplicationContext application contexts} loaded by this
	 * ContextLoader. Beans will therefore automatically be candidates for
	 * annotation-based dependency injection using
	 * {@link org.springframework.beans.factory.annotation.Autowired @Autowired}
	 * and {@link javax.annotation.Resource @Resource}.
	 * </p>
	 * <p>
	 * Any ApplicationContext loaded by this method <strong>must</strong>
	 * register a JVM shutdown hook for itself. Unless the context gets closed
	 * early, all context instances will be automatically closed on JVM
	 * shutdown. This allows for freeing external resources held by beans within
	 * the context, e.g. temporary files.
	 * </p>
	 *
	 * @param contextConfigurationAttributes The configuration attributes to use
	 *        to determine how to load and configure the application context.
	 * @return a new application context
	 */
	public abstract ApplicationContext loadContext(ContextConfigurationAttributes contextConfigurationAttributes)
			throws Exception;

}
