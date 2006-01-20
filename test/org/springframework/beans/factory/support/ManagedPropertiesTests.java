package org.springframework.beans.factory.support;

import junit.framework.TestCase;

import java.util.Map;

/**
 * Unit tests for the ManagedProperties class.
 *
 * @author Rick Evans
 */
public final class ManagedPropertiesTests extends TestCase {

	public void testMergeSunnyDay() {
		final ManagedProperties parent = new ManagedProperties();
		parent.setProperty("one", "one");
		parent.setProperty("two", "two");
		final ManagedProperties child = new ManagedProperties();
		child.setProperty("three", "three");
		child.setMergeEnabled(true);
		Map mergedMap = (Map) child.merge(parent);
		assertEquals("merge() obviously did not work.", 3, mergedMap.size());
	}

	public void testMergeWithNullParent() {
		final ManagedProperties map = new ManagedProperties();
		map.setMergeEnabled(true);
		try {
			map.merge(null);
			fail("Must have failed by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testMergeWithNonCompatibleParentType() {
		final ManagedProperties map = new ManagedProperties();
		map.setMergeEnabled(true);
		try {
			map.merge("hello");
			fail("Must have failed by this point.");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testMergeNotAllowedWhenMergeNotEnabled() {
		ManagedProperties map = new ManagedProperties();
		try {
			map.merge(null);
			fail("Must have failed by this point (cannot merge() when the mergeEnabled property is false.");
		}
		catch (IllegalStateException expected) {
		}
	}

	public void testMergeEmptyChild() {
		final ManagedProperties parent = new ManagedProperties();
		parent.setProperty("one", "one");
		parent.setProperty("two", "two");
		final ManagedProperties child = new ManagedProperties();
		child.setMergeEnabled(true);
		Map mergedMap = (Map) child.merge(parent);
		assertEquals("merge() obviously did not work.", 2, mergedMap.size());
	}

	public void testMergeChildValuesOverrideTheParents() {
		final ManagedProperties parent = new ManagedProperties();
		parent.setProperty("one", "one");
		parent.setProperty("two", "two");
		final ManagedProperties child = new ManagedProperties();
		child.setProperty("one", "fork");
		child.setMergeEnabled(true);
		Map mergedMap = (Map) child.merge(parent);
		// child value for 'one' must override parent value...
		assertEquals("merge() obviously did not work.", 2, mergedMap.size());
		assertEquals("Parent value not being overridden during merge().", "fork", mergedMap.get("one"));
	}

}
