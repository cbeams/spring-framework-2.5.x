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
 * Tests for <code>EhCacheManagerFactoryBean</code>
 * 
 * @author Dmitriy Kopylenko
 * @since 1.1.1
 */
public class EhCacheManagerFactoryBeanTests extends TestCase {

    public void testLoadingCacheManagerFromConfigFile() throws Exception {
        EhCacheManagerFactoryBean cacheManagerFb = new EhCacheManagerFactoryBean();
        cacheManagerFb.setConfigLocation(new ClassPathResource("testEhcache.xml", getClass()));
        cacheManagerFb.afterPropertiesSet();
        CacheManager cm = (CacheManager)cacheManagerFb.getObject();
        assertTrue("Correct number of caches loaded", cm.getCacheNames().length == 2);

        Cache myCache1 = cm.getCache("myCache1");
        assertFalse("myCache1 is not eternal", myCache1.isEternal());
        assertTrue("myCache1.maxElements == 300", myCache1.getMaxElementsInMemory() == 300);

        cacheManagerFb.destroy();
    }

    public void testLoadingBlankCacheManager() throws Exception {
        EhCacheManagerFactoryBean cacheManagerFb = new EhCacheManagerFactoryBean();
        cacheManagerFb.afterPropertiesSet();
        CacheManager cm = (CacheManager)cacheManagerFb.getObject();
        System.out.println(cm.getCacheNames().length);
        assertTrue("Loaded CacheManager with no caches", cm.getCacheNames().length == 0);

        Cache myCache1 = cm.getCache("myCache1");
        assertTrue("No myCache1 defined", myCache1 == null);

        cacheManagerFb.destroy();
    }

}