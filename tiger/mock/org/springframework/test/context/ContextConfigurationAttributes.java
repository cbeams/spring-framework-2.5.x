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

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.ContextConfiguration;

/**
 * <p>
 * Strategy interface for configuration attributes for an
 * {@link ApplicationContext}.
 * </p>
 * <p>
 * Note: concrete implementations <strong>must</strong> implement sensible
 * {@link Object#equals(Object) equals()} and
 * {@link Object#hashCode() hashCode()} methods for caching purposes, etc. In
 * addition, concrete implementations <em>should</em> provide a sensible
 * {@link Object#toString() toString()} implementation.
 * </p>
 *
 * @see ContextConfiguration
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
public interface ContextConfigurationAttributes extends Serializable {

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
	 * @see #getResourceSuffix()
	 * @see #getLocations()
	 * @see #isGenerateDefaultLocations()
	 */
	public abstract Class<? extends ContextLoader> getLoaderClass();

	/**
	 * The suffix to append to application context resource paths when
	 * generating default locations.
	 *
	 * @see #getLoaderClass()
	 * @see #getLocations()
	 * @see #isGenerateDefaultLocations()
	 */
	public abstract String getResourceSuffix();

	/**
	 * The resource locations to use for loading an {@link ApplicationContext}.
	 *
	 * @see #getLoaderClass()
	 * @see #getResourceSuffix()
	 * @see #isGenerateDefaultLocations()
	 */
	public abstract String[] getLocations();

	/**
	 * Is dependency checking to be performed for autowired beans?
	 */
	public abstract boolean isCheckDependencies();

	/**
	 * Whether or not <em>default</em> locations should be generated if no
	 * {@link #getLocations() locations} are explicitly defined.
	 *
	 * @see #getLoaderClass()
	 * @see #getResourceSuffix()
	 * @see #getLocations()
	 */
	public abstract boolean isGenerateDefaultLocations();

}
