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
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A template encapsulating the workflow for caching expensive values in a map.
 * Supports caching weak or strong keys with soft values.
 * <p>
 * <p>
 * This class is abstract template; cache implementations should subclass and
 * override the create(key) method which encapsulates new expensive object
 * creation.
 * 
 * @author Keith Donald
 */
public abstract class CachingMapTemplate implements Serializable {
    private static Object NULL_VALUE = new Object();

    private transient final Log logger = LogFactory.getLog(getClass());

    private Map map;

    private transient ReferenceQueue queue = new ReferenceQueue();

    /**
     * Creates cache; defaults to weak keys.
     */
    public CachingMapTemplate() {
        this(true);
    }

    /**
     * Creates cache.
     * 
     * @param weakKeys
     *            Use weak references for keys.
     */
    public CachingMapTemplate(boolean weakKeys) {
        this.map = weakKeys ? (Map)new WeakHashMap() : new HashMap();
        this.map = Collections.synchronizedMap(this.map);
    }

    /**
     * Creates cache with initial size.
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
     * Gets value for key. Creates and caches value if it doesn't already exist
     * in the cache.
     */
    public Object get(Object key) {
        Object value = internalGet(key);
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
                logger.debug("Caching value '" + value + "'");
            }
            put(key, value);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Returning cached value with key '" + key);
            }
        }
        return (value == NULL_VALUE) ? null : value;
    }

    /**
     * Creates value for key. Called by get() if value isn't cached.
     */
    protected abstract Object create(Object key);

    private void put(Object key, Object value) {
        this.map.put(key, new ValueReference(key, value));
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

    /**
     * @return An iterator over the keys in this map.
     */
    public Iterator keys() {
        return map.keySet().iterator();
    }

    /**
     * @return An iterator over the values in this map.
     */
    public Iterator values() {
        return new ValuesIterator(map.values().iterator());
    }

    /**
     * Delegates to the underlying values iterator, retrieving the object stored
     * at each value reference.
     */
    static class ValuesIterator implements Iterator {
        private Iterator it;

        public ValuesIterator(Iterator it) {
            this.it = it;
        }

        public boolean hasNext() {
            return it.hasNext();
        }

        public Object next() {
            return ((ValueReference)it.next()).get();
        }

        public void remove() {
            it.remove();
        }
    }

    public Iterator entries() {
        return map.entrySet().iterator();
    }

    private Object internalGet(Object key) {
        cleanUp();
        Reference reference = (Reference)map.get(key);
        return (reference == null) ? null : reference.get();
    }

    private void cleanUp() {
        Reference reference;
        while ((reference = this.queue.poll()) != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Removing claimed soft reference '" + reference
                        + "'");
            }
            map.remove(((ValueReference)reference).getKey());
        }
    }

    private class ValueReference extends SoftReference {
        WeakReference keyReference;

        public ValueReference(Object key, Object value) {
            super(value, queue);
            this.keyReference = new WeakReference(key);
        }

        public Object getKey() {
            return this.keyReference.get();
        }

        public String toString() {
            return String.valueOf(super.get());
        }
    }

    public String toString() {
        return new ToStringCreator(this).append("weakKeys",
                (map instanceof WeakHashMap)).append("size", map.size())
                .append("contents", map).toString();
    }
}