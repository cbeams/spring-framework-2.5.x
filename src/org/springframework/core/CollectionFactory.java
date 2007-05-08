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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.ClassUtils;

/**
 * Factory for collections, being aware of Commons Collection 3.x's
 * extended collections as well as of JDK 1.5+ concurrent collections
 * and backport-concurrent collections.
 * Mainly for internal use within the framework.
 *
 * <p>The goal of this class is to avoid runtime dependencies on JDK 1.5+
 * and Commons Collections 3.x, simply using the best collection
 * implementation that is available at runtime.
 *
 * @author Juergen Hoeller
 * @since 1.1.1
 */
public abstract class CollectionFactory {

	private static final Log logger = LogFactory.getLog(CollectionFactory.class);

	/** Whether the Commons Collections 3.x library is present on the classpath */
	private static final boolean commonsCollections3Available =
			ClassUtils.isPresent("org.apache.commons.collections.map.LinkedMap",
					CollectionFactory.class.getClassLoader());

	/** Whether the backport-concurrent library is present on the classpath */
	private static final boolean backportConcurrentAvailable =
			ClassUtils.isPresent("edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap",
					CollectionFactory.class.getClassLoader());


	/**
	 * Create a linked Set if possible: This implementation always
	 * creates a {@link java.util.LinkedHashSet}.
	 * @param initialCapacity the initial capacity of the Set
	 * @return the new Set instance
	 */
	public static Set createLinkedSetIfPossible(int initialCapacity) {
		return new LinkedHashSet(initialCapacity);
	}

	/**
	 * Create a linked Map if possible: This implementation always
	 * creates a {@link java.util.LinkedHashMap}.
	 * @param initialCapacity the initial capacity of the Map
	 * @return the new Map instance
	 */
	public static Map createLinkedMapIfPossible(int initialCapacity) {
		return new LinkedHashMap(initialCapacity);
	}

	/**
	 * Create a linked case-insensitive map if possible: if Commons Collections
	 * 3.x is available, a CaseInsensitiveMap with ListOrderedMap decorator will
	 * be created. Else, a JDK {@link java.util.LinkedHashMap} will be used.
	 * @param initialCapacity the initial capacity of the map
	 * @return the new map instance
	 * @see org.apache.commons.collections.map.CaseInsensitiveMap
	 * @see org.apache.commons.collections.map.ListOrderedMap
	 */
	public static Map createLinkedCaseInsensitiveMapIfPossible(int initialCapacity) {
		if (commonsCollections3Available) {
			logger.trace("Creating [org.apache.commons.collections.map.ListOrderedMap/CaseInsensitiveMap]");
			return CommonsCollectionFactory.createListOrderedCaseInsensitiveMap(initialCapacity);
		}
		else {
			logger.debug("Falling back to [java.util.LinkedHashMap] for linked case-insensitive map");
			return new LinkedHashMap(initialCapacity);
		}
	}

	/**
	 * Create a identity Map if possible: This implementation always
	 * creates a {@link java.util.IdentityHashMap}.
	 * @param initialCapacity the initial capacity of the Map
	 * @return the new Map instance
	 */
	public static Map createIdentityMapIfPossible(int initialCapacity) {
		return new IdentityHashMap(initialCapacity);
	}

	/**
	 * Create a concurrent map if possible: that is, if running on JDK >= 1.5
	 * or if the backport-concurrent library is available. Prefers a JDK 1.5+
	 * ConcurrentHashMap to its backport-concurrent equivalent.
	 * @param initialCapacity the initial capacity of the map
	 * @return the new map instance
	 * @see java.util.concurrent.ConcurrentHashMap
	 * @see edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap
	 */
	public static Map createConcurrentMapIfPossible(int initialCapacity) {
		if (JdkVersion.isAtLeastJava15()) {
			logger.trace("Creating [java.util.concurrent.ConcurrentHashMap]");
			return JdkConcurrentCollectionFactory.createConcurrentHashMap(initialCapacity);
		}
		else if (backportConcurrentAvailable) {
			logger.trace("Creating [edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap]");
			return BackportConcurrentCollectionFactory.createConcurrentHashMap(initialCapacity);
		}
		else {
			logger.debug("Falling back to plain synchronized [java.util.HashMap] for concurrent map");
			return Collections.synchronizedMap(new HashMap(initialCapacity));
		}
	}


	/**
	 * Create the most approximate collection for the given collection.
	 * <p>Creates an ArrayList, TreeSet or linked Set for a List, SortedSet
	 * or Set, respectively.
	 * @param collection the original collection object
	 * @param initialCapacity the initial capacity
	 * @return the new collection instance
	 * @see java.util.ArrayList
	 * @see java.util.TreeSet
	 * @see #createLinkedSetIfPossible
	 */
	public static Collection createApproximateCollection(Object collection, int initialCapacity) {
		if (collection instanceof List) {
			return new ArrayList(initialCapacity);
		}
		else if (collection instanceof SortedSet) {
			return new TreeSet(((SortedSet) collection).comparator());
		}
		else {
			return createLinkedSetIfPossible(initialCapacity);
		}
	}

	/**
	 * Create the most approximate map for the given map.
	 * <p>Creates a TreeMap or linked Map for a SortedMap or Map, respectively.
	 * @param map the original map object
	 * @param initialCapacity the initial capacity
	 * @return the new collection instance
	 * @see java.util.TreeMap
	 * @see #createLinkedMapIfPossible
	 */
	public static Map createApproximateMap(Object map, int initialCapacity) {
		if (map instanceof SortedMap) {
			return new TreeMap(((SortedMap) map).comparator());
		}
		else {
			return createLinkedMapIfPossible(initialCapacity);
		}
	}


	/**
	 * Actual creation of Commons Collections.
	 * In separate inner class to avoid runtime dependency on Commons Collections 3.x.
	 */
	private static abstract class CommonsCollectionFactory {

		private static Map createListOrderedCaseInsensitiveMap(int initialCapacity) {
			// Commons Collections does not support initial capacity of 0.
			return ListOrderedMap.decorate(new CaseInsensitiveMap(initialCapacity == 0 ? 1 : initialCapacity));
		}
	}


	/**
	 * Actual creation of JDK 1.5+ concurrent Collections.
	 * In separate inner class to avoid runtime dependency on JDK 1.5.
	 */
	private static abstract class JdkConcurrentCollectionFactory {

		private static Map createConcurrentHashMap(int initialCapacity) {
			return new ConcurrentHashMap(initialCapacity);
		}
	}


	/**
	 * Actual creation of backport-concurrent Collections.
	 * In separate inner class to avoid runtime dependency on the backport-concurrent library.
	 */
	private static abstract class BackportConcurrentCollectionFactory {

		private static Map createConcurrentHashMap(int initialCapacity) {
			return new edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap(initialCapacity);
		}
	}

}
