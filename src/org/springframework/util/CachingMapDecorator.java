/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.util;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A simple decorator for a Map, encapsulating the workflow for caching
 * expensive values in a target Map. Supports caching weak or strong keys.
 *
 * <p>This class is also an abstract template. Caching Map implementations
 * should subclass and override the <code>create(key)</code> method which
 * encapsulates expensive creation of a new object.
 * 
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 1.2.2
 */
public abstract class CachingMapDecorator implements Map, Serializable {

	private static Object NULL_VALUE = new Object();

	private final Map targetMap;


	/**
	 * Create a CachingMapDecorator with strong keys,
	 * using an underlying synchronized Map.
	 */
	public CachingMapDecorator() {
		this(false);
	}

	/**
	 * Create a CachingMapDecorator,
	 * using an underlying synchronized Map.
	 * @param weakKeys whether to use weak references for keys
	 */
	public CachingMapDecorator(boolean weakKeys) {
		Map internalMap = weakKeys ? (Map) new WeakHashMap() : new HashMap();
		this.targetMap = Collections.synchronizedMap(internalMap);
	}

	/**
	 * Create a CachingMapDecorator with initial size,
	 * using an underlying synchronized Map.
	 * @param weakKeys whether to use weak references for keys
	 * @param size the initial cache size
	 */
	public CachingMapDecorator(boolean weakKeys, int size) {
		Map internalMap = weakKeys ? (Map) new WeakHashMap(size) : new HashMap(size);
		this.targetMap = Collections.synchronizedMap(internalMap);
	}

	/**
	 * Create a CachingMapDecorator for the given Map.
	 * <p>The passed-in Map won't get synchronized explicitly,
	 * so make sure to pass in a properly synchronized Map, if desired.
	 * @param targetMap the Map to decorate
	 */
	public CachingMapDecorator(Map targetMap) {
		Assert.notNull(targetMap, "Target Map is required");
		this.targetMap = targetMap;
	}


	public int size() {
		return this.targetMap.size();
	}

	public boolean isEmpty() {
		return this.targetMap.isEmpty();
	}

	public boolean containsKey(Object key) {
		return this.targetMap.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return this.targetMap.containsValue(value);
	}

	public Object put(Object key, Object value) {
		return this.targetMap.put(key, value);
	}

	public Object remove(Object key) {
		return this.targetMap.remove(key);
	}

	public void putAll(Map t) {
		this.targetMap.putAll(t);
	}

	public void clear() {
		this.targetMap.clear();
	}

	public Set keySet() {
		return this.targetMap.keySet();
	}

	public Collection values() {
		return this.targetMap.values();
	}

	public Set entrySet() {
		return this.targetMap.entrySet();
	}


	/**
	 * Get value for key.
	 * Creates and caches value if it doesn't already exist in the cache.
	 * <p>This implementation is <i>not</i> synchronized: This is highly
	 * concurrent but does not guarantee unique instances in the cache,
	 * as multiple values for the same key could get created in parallel.
	 * Consider overriding this method to synchronize it, if desired.
	 * @see #create(Object)
	 */
	public Object get(Object key) {
		Object value = this.targetMap.get(key);
		if (value instanceof Reference) {
			value = ((Reference) value).get();
		}
		if (value == null) {
			Object newValue = create(key);
			value = (newValue instanceof Reference ? ((Reference) newValue).get() : newValue);
			if (newValue == null) {
				newValue = NULL_VALUE;
			}
			put(key, newValue);
		}
		return (value == NULL_VALUE ? null : value);
	}

	/**
	 * Create a value to cache for the given key.
	 * Called by <code>get</code> if there is no value cached already.
	 * @param key the cache key
	 * @see #get(Object)
	 */
	protected abstract Object create(Object key);


	public String toString() {
		return "CachingMapDecorator [" + getClass().getName() + "]:" + this.targetMap;
	}

}
