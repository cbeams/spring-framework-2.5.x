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
package org.springframework.test;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.ContextConfiguration;

/**
 * Strategy interface for accessing configuration attributes for a configured
 * {@link ApplicationContext}.
 *
 * @see ContextConfiguration
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.2
 */
public interface ContextConfigurationAttributes {

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Are dependencies to be injected via autowiring? Note that a value of
	 * {@link Autowire#NO no} does not necessarily preclude autowiring of
	 * <em>annotated</em> fields or methods.
	 */
	public abstract Autowire getAutowireMode();

	/**
	 * The {@link ContextLoader} type to use for loading the
	 * {@link ApplicationContext}.
	 *
	 * @see #getContextResourceSuffix()
	 * @see #getLocations()
	 * @see #isGenerateDefaultLocations()
	 */
	public abstract Class<? extends ContextLoader> getContextLoaderClass();

	/**
	 * The suffix to append to application context resource paths when
	 * generating default locations.
	 *
	 * @see #getContextLoaderClass()
	 * @see #getLocations()
	 * @see #isGenerateDefaultLocations()
	 */
	public abstract String getContextResourceSuffix();

	/**
	 * The resource locations to use for loading an {@link ApplicationContext}.
	 *
	 * @see #getContextLoaderClass()
	 * @see #getContextResourceSuffix()
	 * @see #isGenerateDefaultLocations()
	 */
	public abstract String[] getLocations();

	/**
	 * Is dependency checking to be performed for autowired beans?
	 */
	public abstract boolean isDependencyCheckEnabled();

	/**
	 * Whether or not <em>default</em> locations should be generated if no
	 * {@link #getLocations() locations} are explicitly defined.
	 *
	 * @see #getContextLoaderClass()
	 * @see #getContextResourceSuffix()
	 * @see #getLocations()
	 */
	public abstract boolean isGenerateDefaultLocations();

}
