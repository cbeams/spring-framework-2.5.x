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

package org.springframework.beans.factory.support;

import junit.framework.TestCase;

import java.util.Map;

/**
 * Unit tests for the ManagedMap class.
 *
 * @author Rick Evans
 */
public final class ManagedMapTests extends TestCase {

	public void testMergeSunnyDay() {
		final ManagedMap parent = new ManagedMap();
		parent.put("one", "one");
		parent.put("two", "two");
		final ManagedMap child = new ManagedMap();
		child.put("three", "three");
		child.setMergeEnabled(true);
		Map mergedMap = (Map) child.merge(parent);
		assertEquals("merge() obviously did not work.", 3, mergedMap.size());
	}

	public void testMergeWithNullParent() {
		final ManagedMap map = new ManagedMap();
		map.setMergeEnabled(true);
		try {
			map.merge(null);
			fail("Must have failed by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testMergeWithNonCompatibleParentType() {
		final ManagedMap map = new ManagedMap();
		map.setMergeEnabled(true);
		try {
			map.merge("hello");
			fail("Must have failed by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testMergeNotAllowedWhenMergeNotEnabled() {
		ManagedMap map = new ManagedMap();
		try {
			map.merge(null);
			fail("Must have failed by this point (cannot merge() when the mergeEnabled property is false.");
		}
		catch (IllegalStateException expected) {
		}
	}

	public void testMergeEmptyChild() {
		final ManagedMap parent = new ManagedMap();
		parent.put("one", "one");
		parent.put("two", "two");
		final ManagedMap child = new ManagedMap();
		child.setMergeEnabled(true);
		Map mergedMap = (Map) child.merge(parent);
		assertEquals("merge() obviously did not work.", 2, mergedMap.size());
	}

	public void testMergeChildValuesOverrideTheParents() {
		final ManagedMap parent = new ManagedMap();
		parent.put("one", "one");
		parent.put("two", "two");
		final ManagedMap child = new ManagedMap();
		child.put("one", "fork");
		child.setMergeEnabled(true);
		Map mergedMap = (Map) child.merge(parent);
		// child value for 'one' must override parent value...
		assertEquals("merge() obviously did not work.", 2, mergedMap.size());
		assertEquals("Parent value not being overridden during merge().", "fork", mergedMap.get("one"));
	}

}
