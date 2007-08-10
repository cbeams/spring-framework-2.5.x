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
package org.springframework.test.context.support;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextLoader;

/**
 * <p>
 * Abstract application context loader, which provides a basis for all concrete
 * implementations of the {@link ContextLoader} strategy.
 * </p>
 *
 * @see #loadContext()
 * @see #loadContextInternal()
 * @see #customizeBeanFactory(ConfigurableListableBeanFactory)
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
public abstract class AbstractContextLoader implements ContextLoader {

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final ContextConfigurationAttributes configAttributes;

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	public AbstractContextLoader(final ContextConfigurationAttributes configAttributes) {

		this.configAttributes = configAttributes;
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * @return The context configuration attributes.
	 */
	protected final ContextConfigurationAttributes getConfigAttributes() {

		return this.configAttributes;
	}

	// ------------------------------------------------------------------------|

}
