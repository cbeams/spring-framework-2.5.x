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
 * 
 * @author Dmitriy Kopylenko
 * @since 1.1.1
 */
public class EhCacheFactoryBean implements FactoryBean, InitializingBean {

    private String cacheName;

    private CacheManager cacheManager;

    /**
     * Set a name for which to retrieve the cache instance.
     */
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
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
    }

    public Object getObject() {
        /*What if the named cache is not configured?
        Is it ok to "expose" null? This would be certainly a misconfiguration by a user.
        Or we could go further than that and if the Cache is not configured, we could manually construct it
        and add it via CacheManager. Here is the fragment from one of the Acegi's EHCaches:
        
        public void afterPropertiesSet() throws Exception {
        if (CacheManager.getInstance().cacheExists(CACHE_NAME)) {
            // don’t remove the cache
        } else {
            manager = CacheManager.create();

            // Cache name, max memory, overflowToDisk, eternal, timeToLive, timeToIdle
            cache = new Cache(CACHE_NAME, Integer.MAX_VALUE, false, false,
                    minutesToIdle * 60, minutesToIdle * 60);

            manager.addCache(cache);
        }
    }*/ 
        return this.cacheManager.getCache(this.cacheName);
    }

    public Class getObjectType() {
        return (Cache.class);
    }

    public boolean isSingleton() {
        return true;
    }
}