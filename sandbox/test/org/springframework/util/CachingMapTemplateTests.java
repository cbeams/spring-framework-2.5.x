/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */

package org.springframework.util;

import junit.framework.TestCase;

public class CachingMapTemplateTests extends TestCase {
    public void testValidCache() {
        MyCachingMap cache = new MyCachingMap();
        Object value;

        value = cache.get("value key");
        assertTrue(cache.createCalled());
        assertEquals(value, "expensive value to cache");
        
        cache.get("value key 2");
        assertTrue(cache.createCalled());

        value = cache.get("value key");
        assertEquals(cache.createCalled(), false);
        assertEquals(value, "expensive value to cache");
        
        cache.get("value key 2");
        assertEquals(cache.createCalled(), false);
        assertEquals(value, "expensive value to cache");
    }

    private static class MyCachingMap extends CachingMapTemplate {
        private boolean createCalled;
        
        protected Object create(Object key) {
            createCalled = true;
            return "expensive value to cache";
        }
        
        public boolean createCalled() {
            boolean c = createCalled;
            this.createCalled = false;
            return c;
        }
    }
}