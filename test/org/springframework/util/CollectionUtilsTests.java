package org.springframework.util;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Rob Harrop
 */
public class CollectionUtilsTests extends TestCase {

	public void testIsEmpty() {
		assertTrue(CollectionUtils.isEmpty((Set)null));
		assertTrue(CollectionUtils.isEmpty((Map)null));
		assertTrue(CollectionUtils.isEmpty(new HashMap()));
		assertTrue(CollectionUtils.isEmpty(new HashSet()));

		List list = new ArrayList();
		list.add(new Object());

		assertFalse(CollectionUtils.isEmpty(list));

		Map map = new HashMap();
		map.put("foo", "bar");

		assertFalse(CollectionUtils.isEmpty(map));
	}
}
