/*
 * Copyright 2002-2004 the original author or authors.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.IdentityMap;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory for collections, being aware of JDK 1.4's extended collections
 * and Commons Collection 3.1's corresponding versions for older JDKs.
 * Mainly for internal use within the framework.
 *
 * <p>The goal of this class is to avoid runtime dependencies on JDK 1.4
 * or Commons Collections 3.x, simply using the best collection implementation
 * that is available. Prefers JDK 1.4 collection implementations to Commons
 * Collections 3.x versions.
 *
 * @author Juergen Hoeller
 * @since 1.1.1
 */
public class CollectionFactory {

	private static final String COMMONS_COLLECTIONS_CLASS_NAME =
			"org.apache.commons.collections.map.LinkedMap";

	private static final Log logger = LogFactory.getLog(CollectionFactory.class);

	private static boolean commonsCollections3xAvailable;

	static {
		// Check whether Commons Collections 3.x is available,
		// provided that we're not running on JDK >= 1.4 in the first place.
		if (JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_14) {
			logger.info("Using JDK 1.4 collections");
		}
		else {
			try {
				Class.forName(COMMONS_COLLECTIONS_CLASS_NAME);
				commonsCollections3xAvailable = true;
				logger.info("Using Commons Collections 3.x");
			}
			catch (ClassNotFoundException ex) {
				commonsCollections3xAvailable = false;
				logger.info("Using JDK 1.3 collections");
			}
		}
	}

	/**
	 * Create a linked set if possible: that is, if running on JDK >= 1.4
	 * or if Commons Collections 3.x is available. Prefers a JDK 1.4
	 * LinkedHashSet to a Commons Collections 3.x ListOrderedSet.
	 * @param initialCapacity the initial capacity of the set
	 * @return the new set instance
	 * @see java.util.LinkedHashSet
	 * @see org.apache.commons.collections.set.ListOrderedSet
	 */
	public static Set createLinkedSetIfPossible(int initialCapacity) {
		if (JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_14) {
			logger.debug("Creating java.util.LinkedHashSet");
			return Jdk14CollectionFactory.createLinkedHashSet(initialCapacity);
		}
		else if (commonsCollections3xAvailable) {
			logger.debug("Creating org.apache.commons.collections.set.ListOrderedSet");
			return CommonsCollectionFactory.createCommonsListOrderedSet(initialCapacity);
		}
		else {
			logger.debug("Falling back to java.util.HashSet for linked set");
			return new HashSet(initialCapacity);
		}
	}

	/**
	 * Create a linked map if possible: that is, if running on JDK >= 1.4
	 * or if Commons Collections 3.x is available. Prefers a JDK 1.4
	 * LinkedHashMap to a Commons Collections 3.x LinkedMap.
	 * @param initialCapacity the initial capacity of the map
	 * @return the new map instance
	 * @see java.util.LinkedHashMap
	 * @see org.apache.commons.collections.map.LinkedMap
	 */
	public static Map createLinkedMapIfPossible(int initialCapacity) {
		if (JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_14) {
			logger.debug("Creating java.util.LinkedHashMap");
			return Jdk14CollectionFactory.createLinkedHashMap(initialCapacity);
		}
		else if (commonsCollections3xAvailable) {
			logger.debug("Creating org.apache.commons.collections.map.LinkedMap");
			return CommonsCollectionFactory.createCommonsLinkedMap(initialCapacity);
		}
		else {
			logger.debug("Falling back to java.util.HashMap for linked map");
			return new HashMap(initialCapacity);
		}
	}

	/**
	 * Create an identity map if possible: that is, if running on JDK 1.4
	 * or if Commons Collections 3.x is available. Prefers a JDK 1.4
	 * IdentityHashMap to a Commons Collections 3.x IdentityMap.
	 * @param initialCapacity the initial capacity of the map
	 * @return the new map instance
	 * @see java.util.IdentityHashMap
	 * @see org.apache.commons.collections.map.IdentityMap
	 */
	public static Map createIdentityMapIfPossible(int initialCapacity) {
		if (JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_14) {
			logger.debug("Creating java.util.IdentityHashMap");
			return Jdk14CollectionFactory.createIdentityHashMap(initialCapacity);
		}
		else if (commonsCollections3xAvailable) {
			logger.debug("Creating org.apache.commons.collections.map.IdentityMap");
			return CommonsCollectionFactory.createCommonsIdentityMap(initialCapacity);
		}
		else {
			logger.debug("Falling back to java.util.HashMap for identity map");
			return new HashMap(initialCapacity);
		}
	}


	/**
	 * Actual creation of JDK 1.4 Collections.
	 * In separate inner class to avoid runtime dependency on JDK 1.4.
	 */
	private static abstract class Jdk14CollectionFactory {

		private static Set createLinkedHashSet(int initialCapacity) {
			return new LinkedHashSet(initialCapacity);
		}

		private static Map createLinkedHashMap(int initialCapacity) {
			return new LinkedHashMap(initialCapacity);
		}

		private static Map createIdentityHashMap(int initialCapacity) {
			return new IdentityHashMap(initialCapacity);
		}
	}


	/**
	 * Actual creation of Commons Collections.
	 * In separate inner class to avoid runtime dependency on Commons Collections 3.x.
	 */
	private static abstract class CommonsCollectionFactory {

		private static Set createCommonsListOrderedSet(int initialCapacity) {
			return ListOrderedSet.decorate(new HashSet(initialCapacity));
		}

		private static Map createCommonsLinkedMap(int initialCapacity) {
			// Commons Collections does not support initial capacity of 0.
			return new LinkedMap(initialCapacity == 0 ? 1 : initialCapacity);
		}

		private static Map createCommonsIdentityMap(int initialCapacity) {
			// Commons Collections does not support initial capacity of 0.
			return new IdentityMap(initialCapacity == 0 ? 1 : initialCapacity);
		}
	}

}
