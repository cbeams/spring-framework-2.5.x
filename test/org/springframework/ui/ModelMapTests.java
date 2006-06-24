/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ui;

import junit.framework.TestCase;

import java.util.*;

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.beans.TestBean;

/**
 * Unit tests for the ModelMap class.
 *
 * @author Rick Evans
 */
public final class ModelMapTests extends TestCase {

    public void testNoArgCtorYieldsEmptyModel() throws Exception {
        assertEquals(0, new ModelMap().size());
    }

    public void testNamedObjectCtor() throws Exception {
        ModelMap model = new ModelMap("foo", "bing");
        assertEquals(1, model.size());
        String bing = (String) model.get("foo");
        assertNotNull(bing);
        assertEquals("bing", bing);
    }

    public void testUnnamedCtorScalar() throws Exception {
        ModelMap model = new ModelMap("foo", "bing");
        assertEquals(1, model.size());
        String bing = (String) model.get("foo");
        assertNotNull(bing);
        assertEquals("bing", bing);
    }

    public void testOneArgCtorWithScalar() throws Exception {
        ModelMap model = new ModelMap("bing");
        assertEquals(1, model.size());
        String string = (String) model.get("string");
        assertNotNull(string);
        assertEquals("bing", string);
    }

    public void testOneArgCtorWithNull() throws Exception {
        try {
            new ModelMap(null);
            fail("Null model arguments are not allowed.");
        }
        catch (IllegalArgumentException expected) {
        }
    }

    public void testOneArgCtorWithCollection() throws Exception {
        ModelMap model = new ModelMap(new String []{"foo", "boing"});
        assertEquals(1, model.size());
        String[] strings = (String[]) model.get("stringList");
        assertNotNull(strings);
        assertEquals(2, strings.length);
        assertEquals("foo", strings[0]);
        assertEquals("boing", strings[1]);
    }

    public void testOneArgCtorWithEmptyCollection() throws Exception {
        ModelMap model = new ModelMap(new HashSet());
        // must not add if collection is empty...
        assertEquals(0, model.size());
    }

    public void testAddObjectWithNull() throws Exception {
        try {
            ModelMap model = new ModelMap();
            model.addObject(null);
            fail("Null model arguments are not allowed.");
        }
        catch (IllegalArgumentException expected) {
        }
    }

    public void testAddObjectWithEmptyArray() throws Exception {
        ModelMap model = new ModelMap(new int[]{});
        assertEquals(1, model.size());
        int[] ints = (int[]) model.get("intList");
        assertNotNull(ints);
        assertEquals(0, ints.length);
    }

    public void testAddAllObjectsWithNullMap() throws Exception {
        ModelMap model = new ModelMap();
        model.addAllObjects((Map) null);
        assertEquals(0, model.size());
    }

    public void testAddAllObjectsWithNullCollection() throws Exception {
        ModelMap model = new ModelMap();
        model.addAllObjects((Collection) null);
        assertEquals(0, model.size());
    }

    public void testAddAllObjectsWithSparseArrayList() throws Exception {
        ModelMap model = new ModelMap();
        ArrayList list = new ArrayList();
        list.add("bing");
        list.add(null);
        try {
            model.addAllObjects(list);
            fail("Null model arguments are not allowed.");
        }
        catch (IllegalArgumentException expected) {
        }
    }

    public void testAddMap() throws Exception {
        Map map = new HashMap();
        map.put("one", "one-value");
        map.put("two", "two-value");

        ModelMap model = new ModelMap();
        model.addObject(map);

        assertEquals(1, model.size());
        String key = StringUtils.uncapitalize(ClassUtils.getShortName(map.getClass()));
        assertTrue(model.containsKey(key));
    }

    public void testAddObjectNoKeyOfSameTypeOverrides() throws Exception {
        ModelMap model = new ModelMap();
        model.addObject("foo");
        model.addObject("bar");
        assertEquals(1, model.size());
        String bar = (String) model.get("string");
        assertNotNull(bar);
        assertEquals("bar", bar);
    }

    public void testAddListOfTheSameObjects() throws Exception {
        List beans = new ArrayList();
        beans.add(new TestBean("one"));
        beans.add(new TestBean("two"));
        beans.add(new TestBean("three"));
        
        ModelMap model = new ModelMap();
        model.addAllObjects(beans);

        assertEquals(1, model.size());
    }

}
