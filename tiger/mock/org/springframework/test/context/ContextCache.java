/*
 * Copyright 2002-2007 the original author or authors.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * <p>
 * Cache for Spring {@link ApplicationContext ApplicationContexts}.
 * </p>
 * <p>
 * Maintains a cache of {@link ApplicationContext contexts} by
 * {@link Serializable serializable} key. This has significant performance
 * benefits if initializing the context would take time. While initializing a
 * Spring context itself is very quick, some beans in a context, such as a
 * {@link org.springframework.orm.hibernate3.LocalSessionFactoryBean LocalSessionFactoryBean}
 * for working with Hibernate, may take some time to initialize. Hence it often
 * makes sense to perform that initialization once.
 * </p>
 *
 * @author Sam Brannen
 * @since 2.5
 * @param <KEY> {@link Serializable serializable} context key type
 * @param <CONTEXT> {@link ApplicationContext application context} type
 */
class ContextCache<KEY extends Serializable, CONTEXT extends ApplicationContext> {

	/**
	 * Map of context keys to Spring application contexts.
	 */
	private final Map<KEY, CONTEXT> contextKeyToContextMap = Collections.synchronizedMap(new HashMap<KEY, CONTEXT>());

	private int hitCount;
	private int missCount;


	/**
	 * Clears all contexts from the cache.
	 */
	void clear() {
		this.contextKeyToContextMap.clear();
	}

	/**
	 * Clears hit and miss count statistics for the cache (i.e., resets counters
	 * to zero).
	 */
	void clearStatistics() {
		this.hitCount = 0;
		this.missCount = 0;
	}

	/**
	 * <p>
	 * Return whether there is a cached context for the given key.
	 * </p>
	 *
	 * @param key The context key, not <code>null</code>.
	 */
	final boolean contains(final KEY key) {
		Assert.notNull(key, "Key must not be null.");
		return this.contextKeyToContextMap.containsKey(key);
	}

	/**
	 * <p>
	 * Obtain a cached ApplicationContext for the given key.
	 * </p>
	 * <p>
	 * The {@link #getHitCount() hit} and {@link #getMissCount() miss} counts
	 * will be updated accordingly.
	 * </p>
	 *
	 * @param key The context key, not <code>null</code>.
	 * @return the corresponding ApplicationContext instance, or
	 *         <code>null</code> if not found in the cache.
	 * @see #remove(Serializable)
	 */
	final CONTEXT get(final KEY key) throws Exception {
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

	/**
	 * Increments the hit count by one. A <em>hit</em> is an access to the
	 * cache, which returned a non-null context for a queried key.
	 */
	protected final void incrementHitCount() {
		this.hitCount++;
	}

	/**
	 * Increments the miss count by one. A <em>miss</em> is an access to the
	 * cache, which returned a <code>null</code> context for a queried key.
	 */
	protected final void incrementMissCount() {
		this.missCount++;
	}

	/**
	 * Gets the overall hit count for this cache. A <em>hit</em> is an access
	 * to the cache, which returned a non-null context for a queried key.
	 *
	 * @return The hit count.
	 */
	final int getHitCount() {
		return this.hitCount;
	}

	/**
	 * Gets the overall miss count for this cache. A <em>miss</em> is an
	 * access to the cache, which returned a <code>null</code> context for a
	 * queried key.
	 *
	 * @return The miss count.
	 */
	final int getMissCount() {
		return this.missCount;
	}

	/**
	 * <p>
	 * Explicitly add a ApplicationContext instance to the cache under the given
	 * key.
	 * </p>
	 *
	 * @param key The context key, not <code>null</code>.
	 * @param context The ApplicationContext instance, not <code>null</code>.
	 */
	final void put(final KEY key, final CONTEXT context) {
		Assert.notNull(key, "Key must not be null.");
		Assert.notNull(context, "ConfigurableApplicationContext must not be null.");
		this.contextKeyToContextMap.put(key, context);
	}

	/**
	 * <p>
	 * Remove the context with the given key.
	 * </p>
	 *
	 * @param key The context key, not <code>null</code>.
	 * @return the corresponding ApplicationContext instance, or
	 *         <code>null</code> if not found in the cache.
	 * @see #setDirty(Serializable)
	 */
	final CONTEXT remove(final KEY key) {
		return this.contextKeyToContextMap.remove(key);
	}

	/**
	 * <p>
	 * Mark the context with the given key as dirty, effectively
	 * {@link #remove(Serializable) removing} the context from the cache and
	 * explicitly {@link ConfigurableApplicationContext#close() closing} it if
	 * it is an instance of {@link ConfigurableApplicationContext}.
	 * </p>
	 * <p>
	 * Generally speaking, you would only call this method only if you change
	 * the state of a singleton bean, potentially affecting future interaction
	 * with the context.
	 * </p>
	 *
	 * @param key The context key, not <code>null</code>.
	 * @see #remove(Serializable)
	 */
	final void setDirty(final KEY key) {
		Assert.notNull(key, "Key must not be null.");
		final CONTEXT context = remove(key);

		if (context instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) context).close();
		}
	}

	/**
	 * Returns the number of contexts currently stored in the cache. If the
	 * cache contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
	 * <tt>Integer.MAX_VALUE</tt>.
	 *
	 * @return the number of contexts stored in the cache.
	 */
	int size() {
		return this.contextKeyToContextMap.size();
	}

	/**
	 * Generates a text string, which contains the {@link #size() size} as well
	 * as the {@link #hitCount hit} and {@link #missCount miss} counts.
	 */
	public String toString() {
		return new ToStringCreator(this)
			.append("size", size())
			.append("hitCount", getHitCount())
			.append("missCount",getMissCount())
			.toString();
	}

}
