/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.util;

import java.util.Collection;
import java.util.Map;

/**
 * Assert utility class that assists in validating arguments and internal invariants.
 * Useful for identifying programmer errors early and obviously at runtime.
 *
 * <p>For example, if the contract of a public setter method states it does not
 * allow null arguments, Assert can be used to validate that contract. Doing
 * this clearly indicates a contract violation when it occurs and protects the
 * class's invariants.
 *
 * <p>This class is similiar to JUnit's assertion library. If an argument value
 * is deemed invalid, an IllegalArgumentException is thrown. For example:
 *
 * <pre>
 * Assert.notNull(clazz, "The class must not be null");
 * Assert.isTrue(i > 0, "The value must be greater than zero");</pre>
 *
 * <p>Mainly for internal use within the framework; consider Jakarta's Commons Lang
 * for a more comprehensive suite of assertion utilities.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 1.1.2
 */
public abstract class Assert {

	/**
	 * Assert a boolean expression, throwing <code>IllegalArgumentException</code>
	 * if the test result is <code>false</code>.
	 * <pre>
	 * Assert.isTrue(i > 0, "The value must be greater than zero");</pre>
	 * @param expression a boolean expression
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if expression is <code>false</code>
	 */
	public static void isTrue(boolean expression, String message) {
		if (expression == false) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Assert that an object is not null.
	 * <pre>
	 * Assert.notNull(clazz, "The class must not be null");</pre>
	 * @param object the object to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the object is <code>null</code>
	 */
	public static void notNull(Object object, String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Assert that a string is not empty; that is, it must not be null and not empty.
	 * <pre>
	 * Assert.hasText(name, "Name must not be empty");</pre>
	 * @param text the string to check
	 * @see StringUtils#hasText
	 */
	public static void hasLength(String text, String message) {
		if (!StringUtils.hasLength(text)) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Assert that a string has valid text content; that is, it must not be null
	 * and must contain at least one non-whitespace character.
	 * <pre>
	 * Assert.hasText(name, "Name must not be empty");</pre>
	 * @param text the string to check
	 * @see StringUtils#hasText
	 */
	public static void hasText(String text, String message) {
		if (!StringUtils.hasText(text)) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Assert that an array has elements; that is, it must not be <code>null</code>
	 * and must have at least one element.
	 * <pre>
	 * Assert.notEmpty(array, "The array must have elements");</pre>
	 * @param array the array to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the object array is <code>null</code>
	 * or has no elements
	 */
	public static void notEmpty(Object[] array, String message) {
		if (array == null || array.length == 0) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Assert that a collection has elements; that is, it must not be <code>null</code>
	 * and must have at least one element.
	 * <pre>
	 * Assert.notEmpty(collection, "Collection must have elements");</pre>
	 * @param collection the collection to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the collection is <code>null</code>
	 * or has no elements
	 */
	public static void notEmpty(Collection collection, String message) {
		if (collection == null || !collection.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Assert that a Map has entries; that is, it must not be <code>null</code>
	 * and must have at least one entry.
	 * <pre>
	 * Assert.notEmpty(map, "Map must have entries");</pre>
	 * @param map the map to check
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if the map is <code>null</code>
	 * or has no entries
	 */
	public static void notEmpty(Map map, String message) {
		if (map == null || !map.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
	}

}
