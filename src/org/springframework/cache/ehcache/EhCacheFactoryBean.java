/*
 * Copyright 2002-2004 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package org.springframework.cache.ehcache;

import java.io.IOException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * FactoryBean that creates a named EHCache Cache singleton for a given classloader-wide CacheManager singleton.
 * <p>
 * If the specified named Cache is not configured in the cache configuration descriptor, this FactoryBean will construct an
 * instance of a Cache with the provided name and the specified cache properties and add it to the CacheManager for later
 * retrieveval. If some or all properties are not set at configuration time, this FactoryBean will use default values.
 * <p>
 * Note, if the named Cache instance is found, the properties will be ignored and that instance will be retrieveved from
 * CacheManager.
 * 
 * @author Dmitriy Kopylenko
 * @since 1.1.1
 */
public class EhCacheFactoryBean implements FactoryBean, InitializingBean {

    /**
     * The name of the cache. This is used to identify the cache. It must be unique.
     * This is a required property.
     */
    private String cacheName;

    /**
     * The maximum number of objects that will be created in memory. Default is 10000 elements.
     */
    private int maxElementsInMemory = 10000;

    /**
     * The flag to indicate whether elements can overflow to disk 
     * when the in-memory cache has reached the maxInMemory limit Default is true.
     */
    private boolean overflowToDisk = true;

    /**
     * The flag to indicate whether elements are eternal. 
     * If true, timeouts are ignored and the element is never expired. Default is false.
     */
    private boolean eternal = false;

    /**
     * The time in seconds to live for an element before it expires. 
     * i.e. the maximum time between creation time and when an element expires. 
     * It is only used if the element is not eternal. Default is 120 seconds.
     */
    private int timeToLive = 120;

    /**
     * The time in seconds to idle for an element before it expires. 
     * i.e. the maximum amount of time between accesses before an element expires.
     * It is only used if the element is not eternal. Default is 120 seconds.
     */
    private int timeToIdle = 120;

    /**
     * The classloader-wide CacheManager singleton from which to fetch named cache regions.
     * This is a required property.
     */
    private CacheManager cacheManager;

    /**
     * Set a name for which to retrieve the cache instance.
     */
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    /**
     * @param eternal The eternal to set.
     */
    public void setEternal(boolean eternal) {
        this.eternal = eternal;
    }

    /**
     * @param maxElementsInMemory The maxElementsInMemory to set.
     */
    public void setMaxElementsInMemory(int maxMemory) {
        this.maxElementsInMemory = maxMemory;
    }

    /**
     * @param overflowToDisk The overflowToDisk to set.
     */
    public void setOverflowToDisk(boolean overflowToDisk) {
        this.overflowToDisk = overflowToDisk;
    }

    /**
     * @param timeToIdle The timeToIdle to set.
     */
    public void setTimeToIdle(int timeToIdle) {
        this.timeToIdle = timeToIdle;
    }

    /**
     * @param timeToLive The timeToLive to set.
     */
    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * Set a CacheManager from which to retrieve a named Cache instance.
     */
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void afterPropertiesSet() throws IOException, CacheException {
        if (this.cacheName == null) {
            throw new IllegalStateException("A name for a cache must be specified.");
        }
        if (this.cacheManager == null) {
            throw new IllegalStateException("A CacheManager is required.");
        }
        if (!this.cacheManager.cacheExists(this.cacheName)) {
            //Manually construct the named Cache instance and add it to CacheManager
            Cache cache = new Cache(this.cacheName, this.maxElementsInMemory, this.overflowToDisk, this.eternal, this.timeToLive, this.timeToIdle);
            this.cacheManager.addCache(cache);
        }
    }

    public Object getObject() {
        return this.cacheManager.getCache(this.cacheName);
    }

    public Class getObjectType() {
        return (Cache.class);
    }

    public boolean isSingleton() {
        return true;
    }
}