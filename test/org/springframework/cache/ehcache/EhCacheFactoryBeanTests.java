/*
 * Copyright 2002-2004 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package org.springframework.cache.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.springframework.core.io.ClassPathResource;

import junit.framework.TestCase;

/**
 * Tests for <code>EhCacheFactoryBean</code>
 * 
 * @author Dmitriy Kopylenko
 * @since 1.1.1
 */
public class EhCacheFactoryBeanTests extends TestCase {

    public void testEhCacheMananagerRequiredProperties() throws Exception {
        EhCacheFactoryBean cacheFb = new EhCacheFactoryBean();
        try {
            cacheFb.afterPropertiesSet();
            fail("Missing all required properties - should have thrown IllegalStateException");
        }
        catch (IllegalStateException ex) {
            //Expected
        }

        cacheFb.setCacheName("myCache");
        try {
            cacheFb.afterPropertiesSet();
            fail("Missing on of the required properties - should have thrown IllegalStateException");
        }
        catch (IllegalStateException ex) {
            //Expected
        }
    }

    public void testEhCacheFactoryBeanBehavior() throws Exception {
        Cache cache = null;

        EhCacheManagerFactoryBean cacheManagerFb = new EhCacheManagerFactoryBean();
        cacheManagerFb.setConfigLocation(new ClassPathResource("testEhcache.xml", getClass()));
        cacheManagerFb.afterPropertiesSet();

        EhCacheFactoryBean cacheFb = new EhCacheFactoryBean();
        cacheFb.setCacheManager((CacheManager)cacheManagerFb.getObject());
        cacheFb.setCacheName("myCache1");
        cacheFb.afterPropertiesSet();
        cache = getCache(cacheFb);
        assertFalse("myCache1 is not eternal", cache.isEternal());
        assertTrue("myCache1.maxElements == 300", cache.getMaxElementsInMemory() == 300);

        //Cache region is not defined. Should create one with default properties
        cacheFb.setCacheName("undefinedCache");
        cacheFb.afterPropertiesSet();
        cache = getCache(cacheFb);
        assertTrue("default maxElements is correct", cache.getMaxElementsInMemory() == 10000);
        assertTrue("default overflowToDisk is correct", cache.isOverflowToDisk());
        assertFalse("default eternal is correct", cache.isEternal());
        assertTrue("default timeToLive is correct", cache.getTimeToLiveSeconds() == 120);
        assertTrue("default timeToIdle is correct", cache.getTimeToIdleSeconds() == 120);

        //Overriding the default properties
        cacheFb.setCacheName("undefinedCache2");
        cacheFb.setEternal(true);
        cacheFb.setOverflowToDisk(false);
        cacheFb.setMaxElementsInMemory(5);
        cacheFb.afterPropertiesSet();
        cache = getCache(cacheFb);
        assertTrue("overriden maxElements is correct", cache.getMaxElementsInMemory() == 5);
        assertFalse("overriden overflowToDisk is correct", cache.isOverflowToDisk());
        assertTrue("overriden eternal is correct", cache.isEternal());

        cacheManagerFb.destroy();
    }

    private Cache getCache(EhCacheFactoryBean fb) {
        return (Cache)fb.getObject();
    }

}