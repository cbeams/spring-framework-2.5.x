/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple template encapsulating the workflow for caching expensive values in
 * a map. Supports caching weak or strong keys.
 * <p>
 * <p>
 * This class is abstract template; caching map implementations should subclass
 * and override the create(key) method which encapsulates new expensive object
 * creation.
 * 
 * @author Keith Donald
 */
public abstract class CachingMapTemplate implements Map, Serializable {
    private static Object NULL_VALUE = new Object();

    private transient final Log logger = LogFactory.getLog(getClass());

    private Map map;

    /**
     * Creates a caching map template; defaults to strong keys.
     */
    public CachingMapTemplate() {
        this(false);
    }

    /**
     * Creates a caching map template.
     * 
     * @param weakKeys
     *            Use weak references for keys.
     */
    public CachingMapTemplate(boolean weakKeys) {
        this.map = weakKeys ? (Map)new WeakHashMap() : new HashMap();
        this.map = Collections.synchronizedMap(this.map);
    }

    /**
     * Creates a caching map template with an initial size.
     * 
     * @param weakKeys
     *            Use weak references for keys.
     * @param size
     *            The initial cache size.
     */
    public CachingMapTemplate(boolean weakKeys, int size) {
        this.map = weakKeys ? (Map)new WeakHashMap(size) : new HashMap(size);
        this.map = Collections.synchronizedMap(this.map);
    }

    /**
     * Creates a caching template decorating the provided map.
     * 
     * @param map
     *            The map
     */
    public CachingMapTemplate(Map map) {
        Assert.notNull(map, "Map cannot be null");
        this.map = map;
    }

    /**
     * Gets value for key. Creates and caches value if it doesn't already exist
     * in the cache.
     */
    public Object get(Object key) {
        Object value = map.get(key);
        if (value == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating new expensive value with key '" + key
                        + "'");
            }
            value = create(key);
            if (value == null) {
                value = NULL_VALUE;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Caching key value pair '" + key + "' -> '"
                        + value + "'");
            }
            put(key, value);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Returning cached value with key '" + key + "'");
            }
        }
        return (value == NULL_VALUE) ? null : value;
    }

    /**
     * Creates value for key. Called by get() if value isn't cached.
     */
    protected abstract Object create(Object key);

    public Object put(Object key, Object value) {
        return this.map.put(key, value);
    }

    /**
     * Returns the size of the map.
     * 
     * @return The cache size.
     */
    public int size() {
        return map.size();
    }

    /**
     * Empties the map, removing all entries.
     */
    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Set entrySet() {
        return map.entrySet();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set keySet() {
        return map.keySet();
    }

    public void putAll(Map t) {
        map.putAll(t);
    }

    public Object remove(Object key) {
        return map.remove(key);
    }

    public Collection values() {
        return map.values();
    }

    public String toString() {
        return new ToStringCreator(this).append("weakKeys",
                (map instanceof WeakHashMap)).append("size", map.size())
                .append("contents", map).toString();
    }
}