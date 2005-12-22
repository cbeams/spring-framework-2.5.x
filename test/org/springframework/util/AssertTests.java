/*
 * Copyright 2004-2005 the original author or authors.
 */

package org.springframework.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Tests for the Assert class.
 *
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class AssertTests extends TestCase {

	public void testInstanceOf() {
		Set set = new HashSet();
		Assert.isInstanceOf(HashSet.class, set);
		try {
			Assert.isInstanceOf(HashMap.class, set);
			fail("Should have failed - a hash map is not a set");
		}
		catch (IllegalArgumentException ex) {
			// ok
		}
	}

	public void testIsNull() {
		Assert.isNull(null, "Bla");
		try {
			Assert.isNull(new Object(), "Bla");
			fail("Should have failed - object is not null");
		}
		catch (IllegalArgumentException e) {
			assertEquals("Bla", e.getMessage());
		}

		Assert.isNull(null);
		try {
			Assert.isNull(new Object());
			fail("Should have failed - object is not null");
		}
		catch (IllegalArgumentException ex) {
			// ok
		}
	}

}
