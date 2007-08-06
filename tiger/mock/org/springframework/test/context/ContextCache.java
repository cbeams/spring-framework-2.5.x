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
 * @version $Revision: 1.1 $
 * @since 2.1
 * @param <KEY> {@link Serializable serializable} context key type
 * @param <CONTEXT> {@link ConfigurableApplicationContext application context}
 *        type
 */
public interface ContextCache<KEY extends Serializable, CONTEXT extends ConfigurableApplicationContext> {

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
	public abstract void addContext(final KEY key, final CONTEXT context);

	/**
	 * <p>
	 * Return whether there is a cached context for the given key.
	 * </p>
	 *
	 * @param key The context key, not <code>null</code>.
	 */
	public abstract boolean hasCachedContext(final KEY key);

	/**
	 * <p>
	 * Obtain a cached ConfigurableApplicationContext for the given key.
	 * </p>
	 *
	 * @param key The context key, not <code>null</code>.
	 * @return the corresponding ConfigurableApplicationContext instance, or
	 *         <code>null</code> if not found in the cache.
	 */
	public abstract CONTEXT getContext(final KEY key) throws Exception;

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
	 * @param key The context key, not <code>null</code>.
	 */
	public abstract void setDirty(final KEY key);

}