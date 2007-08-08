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

import org.springframework.context.ConfigurableApplicationContext;

/**
 * <p>
 * Caching strategy for Spring
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
 * <p>
 * Implementations should be thread-safe.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.2 $
 * @since 2.1
 * @param <KEY> {@link Serializable serializable} context key type
 * @param <CONTEXT> {@link ConfigurableApplicationContext application context}
 *        type
 */
public interface ContextCache<KEY extends Serializable, CONTEXT extends ConfigurableApplicationContext> {

	/**
	 * Clears all contexts from the cache.
	 */
	public abstract void clear();

	/**
	 * <p>
	 * Return whether there is a cached context for the given key.
	 * </p>
	 *
	 * @param key The context key, not <code>null</code>.
	 */
	public abstract boolean contains(final KEY key);

	/**
	 * <p>
	 * Obtain a cached ConfigurableApplicationContext for the given key.
	 * </p>
	 * <p>
	 * The {@link #getHitCount() hit} and {@link #getMissCount() miss} counts
	 * will be updated accordingly.
	 * </p>
	 *
	 * @see #remove(Serializable)
	 * @param key The context key, not <code>null</code>.
	 * @return the corresponding ConfigurableApplicationContext instance, or
	 *         <code>null</code> if not found in the cache.
	 */
	public abstract CONTEXT get(final KEY key) throws Exception;

	/**
	 * Gets the overall hit count for this cache. A <em>hit</em> is an access
	 * to the cache, which returned a non-null context for a queried key.
	 *
	 * @return The hit count.
	 */
	public abstract int getHitCount();

	/**
	 * Gets the overall miss count for this cache. A <em>miss</em> is an
	 * access to the cache, which returned a <code>null</code> context for a
	 * queried key.
	 *
	 * @return The miss count.
	 */
	public abstract int getMissCount();

	/**
	 * <p>
	 * Explicitly add a ConfigurableApplicationContext instance to the cache
	 * under the given key.
	 * </p>
	 *
	 * @param key The context key, not <code>null</code>.
	 * @param context The ConfigurableApplicationContext instance, not
	 *        <code>null</code>.
	 */
	public abstract void put(final KEY key, final CONTEXT context);

	/**
	 * <p>
	 * Remove the context with the given key.
	 * </p>
	 *
	 * @see #setDirty(Serializable)
	 * @param key The context key, not <code>null</code>.
	 * @return the corresponding ConfigurableApplicationContext instance, or
	 *         <code>null</code> if not found in the cache.
	 */
	public abstract CONTEXT remove(final KEY key);

	/**
	 * <p>
	 * Mark the context with the given key as dirty, effectively
	 * {@link #remove(Serializable) removing} the context from the cache and
	 * explicitly {@link ConfigurableApplicationContext#close() closing} it.
	 * </p>
	 * <p>
	 * Generally speaking, you would only call this method only if you change
	 * the state of a singleton bean, potentially affecting future interaction
	 * with the context.
	 * </p>
	 *
	 * @see #remove(Serializable)
	 * @param key The context key, not <code>null</code>.
	 */
	public abstract void setDirty(final KEY key);

	/**
	 * Returns the number of contexts currently stored in the cache. If the
	 * cache contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
	 * <tt>Integer.MAX_VALUE</tt>.
	 *
	 * @return the number of contexts stored in the cache.
	 */
	public abstract int size();

}