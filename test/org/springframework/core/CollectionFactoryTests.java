/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.core;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.commons.collections.map.IdentityMap;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.collections.set.ListOrderedSet;

/**
 * @author Darren Davison
 * @author Juergen Hoeller
 */
public class CollectionFactoryTests extends TestCase {

	private String javaVersion = System.getProperty("java.version");

	public void testLinkedSet() {
		Set set = CollectionFactory.createLinkedSetIfPossible(16);
		if (javaVersion.indexOf("1.3.") == -1) {
			assertTrue(set instanceof LinkedHashSet);
		}
		else {
			assertTrue(set instanceof ListOrderedSet);
		}
	}

	public void testLinkedMap() {
		Map map = CollectionFactory.createLinkedMapIfPossible(16);
		if (javaVersion.indexOf("1.3.") == -1) {
			assertTrue(map instanceof LinkedHashMap);
		}
		else {
			assertTrue(map instanceof LinkedMap);
		}
	}

	public void testIdentityMap() {
		Map map = CollectionFactory.createIdentityMapIfPossible(16);
		if (javaVersion.indexOf("1.3.") == -1) {
			assertTrue(map instanceof IdentityHashMap);
		}
		else {
			assertTrue(map instanceof IdentityMap);
		}
	}

	/**
	 * Initial capacity of 0 works with JDK 1.4 classes, but not the Commons
	 * Collections (which will be used under the hood in a JDK 1.3 platform).
	 */
	public void testLinkedSetWithZeroCapacity() {
		Set set = CollectionFactory.createLinkedSetIfPossible(0);
		assertNotNull(set);
	}

	/**
	 * Initial capacity of 0 works with JDK 1.4 classes, but not the Commons
	 * Collections (which will be used under the hood in a JDK 1.3 platform).
	 */
	public void testLinkedMapWithZeroCapacity() {
		Map map = CollectionFactory.createLinkedMapIfPossible(0);
		assertNotNull(map);
	}

	/**
	 * Initial capacity of 0 works with JDK 1.4 classes, but not the Commons
	 * Collections (which will be used under the hood in a JDK 1.3 platform).
	 */
	public void testIdentityMapWithZeroCapacity() {
		Map map = CollectionFactory.createIdentityMapIfPossible(0);
		assertNotNull(map);
	}

}
