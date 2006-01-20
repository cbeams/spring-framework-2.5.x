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

import java.util.List;

/**
 * Unit tests for the ManagedList class.
 *
 * @author Rick Evans
 */
public final class ManagedListTests extends TestCase {

	public void testMergeSunnyDay() {
		ManagedList parent = new ManagedList();
		parent.add("one");
		parent.add("two");
		ManagedList child = new ManagedList();
		child.add("three");
		child.setMergeEnabled(true);
		List mergedList = (List) child.merge(parent);
		assertEquals("merge() obviously did not work.", 3, mergedList.size());
	}

	public void testMergeWithNullParent() {
		ManagedList child = new ManagedList();
		child.add("one");
		child.setMergeEnabled(true);
		try {
			child.merge(null);
			fail("Must have failed by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testMergeNotAllowedWhenMergeNotEnabled() {
		ManagedList child = new ManagedList();
		try {
			child.merge(null);
			fail("Must have failed by this point (cannot merge() when the mergeEnabled property is false.");
		}
		catch (IllegalStateException expected) {
		}
	}

	public void testMergeWithNonCompatibleParentType() {
		ManagedList child = new ManagedList();
		child.add("one");
		child.setMergeEnabled(true);
		try {
			child.merge("hello");
			fail("Must have failed by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testMergeEmptyChild() {
		ManagedList parent = new ManagedList();
		parent.add("one");
		parent.add("two");
		ManagedList child = new ManagedList();
		child.setMergeEnabled(true);
		List mergedList = (List) child.merge(parent);
		assertEquals("merge() obviously did not work.", 2, mergedList.size());
	}

	public void testMergeChildValuesOverrideTheParents() {
		// doesn't make a whole lotta sense in the context of a list...
		ManagedList parent = new ManagedList();
		parent.add("one");
		parent.add("two");
		ManagedList child = new ManagedList();
		child.add("one");
		child.setMergeEnabled(true);
		List mergedList = (List) child.merge(parent);
		assertEquals("merge() obviously did not work.", 3, mergedList.size());
	}

}
