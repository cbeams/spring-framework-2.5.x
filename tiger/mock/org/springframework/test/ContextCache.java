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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

/**
 * <p>
 * Generic cache for Spring
 * {@link ConfigurableApplicationContext ConfigurableApplicationContexts}.
 * </p>
 * <p>
 * Maintains a cache of {@link ConfigurableApplicationContext contexts} by
 * {@link Serializable serializable} key. This has significant performance
 * benefit if initializing the context would take time. While initializing a
 * Spring context itself is very quick, some beans in a context, such as a
 * LocalSessionFactoryBean for working with Hibernate, may take some time to
 * initialize. Hence it often makes sense to do that initializing once.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.2
 */
public class ContextCache<KEY extends Serializable, CONTEXT extends ConfigurableApplicationContext> {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Map of context keys to Spring application contexts.
	 */
	private final Map<KEY, CONTEXT> contextKeyToContextMap = new HashMap<KEY, CONTEXT>();

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Explicitly add a ConfigurableApplicationContext instance to the cache
	 * under a given key.
	 * </p>
	 *
	 * @param key the context key
	 * @param context the ConfigurableApplicationContext instance
	 */
	public final void addContext(final KEY key, final CONTEXT context) {

		Assert.notNull(context, "ConfigurableApplicationContext must not be null");
		this.contextKeyToContextMap.put(key, context);
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Return whether there is a cached context for the given key.
	 * </p>
	 *
	 * @param contextKey the context key
	 */
	public final boolean hasCachedContext(final KEY contextKey) {

		return this.contextKeyToContextMap.containsKey(contextKey);
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Obtain a cached ConfigurableApplicationContext for the given key.
	 * </p>
	 *
	 * @param key the context key
	 * @return the corresponding ConfigurableApplicationContext instance, or
	 *         <code>null</code> if not found in the cache.
	 */
	public final CONTEXT getContext(final KEY key) throws Exception {

		return this.contextKeyToContextMap.get(key);
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Mark the context with the given key as dirty, effectively removing the
	 * context from the cache and explicitly
	 * {@link ConfigurableApplicationContext#close() closing} it.
	 * </p>
	 * <p>
	 * Call this method only if you change the state of a singleton bean,
	 * potentially affecting future interaction with the context.
	 * </p>
	 *
	 * @param key the context key
	 */
	public final void setDirty(final KEY key) {

		final CONTEXT ctx = this.contextKeyToContextMap.remove(key);
		if (ctx != null) {
			ctx.close();
		}
	}

	// ------------------------------------------------------------------------|

}
