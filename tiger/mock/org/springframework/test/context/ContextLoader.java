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

import org.springframework.context.ConfigurableApplicationContext;

/**
 * <p>
 * Strategy interface for loading an
 * {@link ConfigurableApplicationContext application context} based on a
 * supplied set of
 * {@link ContextConfigurationAttributes configuration attributes}.
 * </p>
 * <p>
 * Concrete implementations must declare a public constructor which accepts a
 * single {@link ContextConfigurationAttributes} argument.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
public interface ContextLoader {

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Loads a new {@link ConfigurableApplicationContext context} based on the
	 * {@link ContextConfigurationAttributes configuration attributes} provided
	 * to this ContextLoader, possibly configures the context, and finally
	 * returns the {@link ConfigurableApplicationContext#refresh() refreshed}
	 * context.
	 * </p>
	 * <p>
	 * Any ApplicationContext loaded by this method <strong>must</strong> be
	 * asked to register a JVM shutdown hook for itself. Unless the context gets
	 * closed early, all context instances will be automatically closed on JVM
	 * shutdown. This allows for freeing external resources held by beans within
	 * the context, e.g. temporary files.
	 * </p>
	 *
	 * @return a new application context
	 */
	public abstract ConfigurableApplicationContext loadContext() throws Exception;

}
