/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * 
 * @author Keith Donald
 */
public class AssertTests extends TestCase {
    public void testInstanceOf() {
        Set set = new HashSet();
        Assert.isInstanceOf(HashSet.class, set);
        try {
            Assert.isInstanceOf(HashMap.class, set);
            fail("Should have failed - a hash map is not a set");
        } catch (IllegalArgumentException e) {
            return;
        }
    }
}
