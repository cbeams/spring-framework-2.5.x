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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCache;
import org.springframework.util.Assert;

/**
 * <p>
 * Generic implementation of the {@link ContextCache} strategy, backed by a
 * {@link Map}.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 * @param <KEY> {@link Serializable serializable} context key type
 * @param <CONTEXT> {@link ConfigurableApplicationContext application context}
 *        type
 */
public class MapBackedContextCache<KEY extends Serializable, CONTEXT extends ConfigurableApplicationContext> implements
		ContextCache<KEY, CONTEXT> {

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Map of context keys to Spring application contexts.
	 */
	private final Map<KEY, CONTEXT> contextKeyToContextMap = Collections.synchronizedMap(new HashMap<KEY, CONTEXT>());

	private int hitCount;

	private int missCount;

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	public final void clear() {

		this.contextKeyToContextMap.clear();
	}

	// ------------------------------------------------------------------------|

	@Override
	public void clearStatistics() {

		this.hitCount = 0;
		this.missCount = 0;
	}

	// ------------------------------------------------------------------------|

	public final boolean contains(final KEY key) {

		Assert.notNull(key, "Key must not be null.");
		return this.contextKeyToContextMap.containsKey(key);
	}

	// ------------------------------------------------------------------------|

	public final CONTEXT get(final KEY key) throws Exception {

		Assert.notNull(key, "Key must not be null.");
		final CONTEXT context = this.contextKeyToContextMap.get(key);

		if (context == null) {
			incrementMissCount();
		}
		else {
			incrementHitCount();
		}

		return context;
	}

	// ------------------------------------------------------------------------|

	public final int getHitCount() {

		return this.hitCount;
	}

	// ------------------------------------------------------------------------|

	public final int getMissCount() {

		return this.missCount;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Increments the hit count by one. A <em>hit</em> is an access to the
	 * cache, which returned a non-null context for a queried key.
	 */
	protected final void incrementHitCount() {

		this.hitCount++;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Increments the miss count by one. A <em>miss</em> is an access to the
	 * cache, which returned a <code>null</code> context for a queried key.
	 */
	protected final void incrementMissCount() {

		this.missCount++;
	}

	// ------------------------------------------------------------------------|

	public final void put(final KEY key, final CONTEXT context) {

		Assert.notNull(key, "Key must not be null.");
		Assert.notNull(context, "ConfigurableApplicationContext must not be null.");
		this.contextKeyToContextMap.put(key, context);
	}

	// ------------------------------------------------------------------------|

	public final CONTEXT remove(final KEY key) {

		return this.contextKeyToContextMap.remove(key);
	}

	// ------------------------------------------------------------------------|

	public final void setDirty(final KEY key) {

		Assert.notNull(key, "Key must not be null.");
		final CONTEXT context = remove(key);
		if (context != null) {
			context.close();
		}
	}

	// ------------------------------------------------------------------------|

	public int size() {

		return this.contextKeyToContextMap.size();
	}

	// ------------------------------------------------------------------------|

	/**
	 * Generates a text string, which displays the number of contexts currently
	 * stored in the cache as well as the hit and miss counts.
	 *
	 * @see java.lang.Object#toString()
	 */
	public String toString() {

		return new ToStringBuilder(this)

		.append("size", size())

		.append("hitCount", getHitCount())

		.append("missCount", getMissCount())

		.toString();
	}

	// ------------------------------------------------------------------------|

}
