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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

/**
 * <p>
 * Generic implementation of the {@link ContextCache} strategy, backed by a
 * {@link Map}.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.2
 * @param <KEY> {@link Serializable serializable} context key type
 * @param <CONTEXT> {@link ConfigurableApplicationContext application context}
 *        type
 */
public class MapBackedContextCache<KEY extends Serializable, CONTEXT extends ConfigurableApplicationContext> implements
		ContextCache<KEY, CONTEXT> {

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
	private final Map<KEY, CONTEXT> contextKeyToContextMap = Collections.synchronizedMap(new HashMap<KEY, CONTEXT>());

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

	public final void addContext(final KEY key, final CONTEXT context) {

		Assert.notNull(key, "Key must not be null.");
		Assert.notNull(context, "ConfigurableApplicationContext must not be null.");
		this.contextKeyToContextMap.put(key, context);
	}

	// ------------------------------------------------------------------------|

	public final boolean hasCachedContext(final KEY key) {

		Assert.notNull(key, "Key must not be null.");
		return this.contextKeyToContextMap.containsKey(key);
	}

	// ------------------------------------------------------------------------|

	public final CONTEXT getContext(final KEY key) throws Exception {

		Assert.notNull(key, "Key must not be null.");
		return this.contextKeyToContextMap.get(key);
	}

	// ------------------------------------------------------------------------|

	public final void setDirty(final KEY key) {

		Assert.notNull(key, "Key must not be null.");
		final CONTEXT context = this.contextKeyToContextMap.remove(key);
		if (context != null) {
			context.close();
		}
	}

	// ------------------------------------------------------------------------|

}
