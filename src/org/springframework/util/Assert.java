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
 * Assert utility class that assists in validating arguments.
 * Useful for identifying programmer errors early and obviously at runtime.
 *
 * <p>For example, if the contract of a public method states it does not allow
 * null arguments, Assert can be used to validate that contract. Doing this
 * clearly indicates a contract violation when it occurs and protects the
 * class's invariants.
 *
 * <p>Typically used to validate method arguments rather than configuration
 * properties, to check for cases that are usually programmer errors rather
 * than configuration errors. In contrast to config initialization code,
 * there is usally no point in falling back to defaults in such methods.
 *
 * <p>This class is similar to JUnit's assertion library. If an argument value
 * is deemed invalid, an IllegalArgumentException is thrown. For example:
 *
 * <pre>
 * Assert.notNull(clazz, "The class must not be null");
 * Assert.isTrue(i > 0, "The value must be greater than zero");</pre>
 *
 * Mainly for internal use within the framework; consider Jakarta's Commons
 * Lang >= 2.0 for a more comprehensive suite of assertion utilities.
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
		if (!expression) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Assert a boolean expression, throwing <code>IllegalArgumentException</code>
	 * if the test result is <code>false</code>.
	 * <pre>
	 * Assert.isTrue(i > 0);</pre>
	 * @param expression a boolean expression
	 * @throws IllegalArgumentException if expression is <code>false</code>
	 */
	public static void isTrue(boolean expression) {
        if (!expression) { throw new IllegalArgumentException(
        	"[Assertion failed] - this expression must be true");
        }
	}

	/**
     * Assert a boolean expression, throwing <code>IllegalStateException</code>
     * if the test result is <code>false</code>. Call isTrue if you wish to
     * throw IllegalArgumentException on an assertion failure.
	 * <pre>
	 * Assert.state(id == null, "The id property must not already be initialized");</pre>
	 * @param expression a boolean expression
	 * @param message the exception message to use if the assertion fails
	 * @throws IllegalArgumentException if expression is <code>false</code>
	 */
    public static void state(boolean expression, String message) {
		if (!expression) {
			throw new IllegalStateException(message);
		}
    }

	/**
     * Assert a boolean expression, throwing <code>IllegalStateException</code>
     * if the test result is <code>false</code>. Call isTrue if you wish to
     * throw IllegalArgumentException on an assertion failure.
	 * <pre>
	 * Assert.state(id == null);</pre>
	 * @param expression a boolean expression
	 * @throws IllegalArgumentException if expression is <code>false</code>
	 */
    public static void state(boolean expression) {
        if (!expression) { throw new IllegalStateException(
                "[Assertion failed] - this state invariant must be true"); }
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
	 * Assert that an object is not null.
	 * <pre>
	 * Assert.notNull(clazz);</pre>
	 * @param object the object to check
	 * @throws IllegalArgumentException if the object is <code>null</code>
	 */
	public static void notNull(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("[Assertion failed] - this argument is required; it cannot be null");
		}
	}

	/**
	 * Assert that a string is not empty; that is, it must not be null and not empty.
	 * <pre>
	 * Assert.hasLength(name, "Name must not be empty");</pre>
	 * @param text the string to check
	 * @see StringUtils#hasLength
	 */
	public static void hasLength(String text, String message) {
		if (!StringUtils.hasLength(text)) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Assert that a string is not empty; that is, it must not be null and not empty.
	 * <pre>
	 * Assert.hasLength(name);</pre>
	 * @param text the string to check
	 * @see StringUtils#hasLength
	 */
	public static void hasLength(String text) {
		if (!StringUtils.hasLength(text)) {
			throw new IllegalArgumentException("[Assertion failed] - This string argument must have length; it cannot be null or empty");
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
	 * Assert that a string has valid text content; that is, it must not be null
	 * and must contain at least one non-whitespace character.
	 * <pre>
	 * Assert.hasText(name, "Name must not be empty");</pre>
	 * @param text the string to check
	 * @see StringUtils#hasText
	 */
	public static void hasText(String text) {
		if (!StringUtils.hasText(text)) {
			throw new IllegalArgumentException("[Assertion failed] - This string argument must have text; it cannot be null, empty, or blank");
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
	 * Assert that an array has elements; that is, it must not be <code>null</code>
	 * and must have at least one element.
	 * <pre>
	 * Assert.notEmpty(array);</pre>
	 * @param array the array to check
	 * @throws IllegalArgumentException if the object array is <code>null</code>
	 * or has no elements
	 */
	public static void notEmpty(Object[] array) {
		if (array == null || array.length == 0) {
			throw new IllegalArgumentException("[Assertion failed] - this array must not be empty: it must contain at least 1 element");
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
		if (collection == null || collection.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Assert that a collection has elements; that is, it must not be <code>null</code>
	 * and must have at least one element.
	 * <pre>
	 * Assert.notEmpty(collection, "Collection must have elements");</pre>
	 * @param collection the collection to check
	 * @throws IllegalArgumentException if the collection is <code>null</code>
	 * or has no elements
	 */
	public static void notEmpty(Collection collection) {
		if (collection == null || collection.isEmpty()) {
			throw new IllegalArgumentException("[Assertion failed] - this collection must not be empty: it must contain at least 1 element");
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
		if (map == null || map.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Assert that a Map has entries; that is, it must not be <code>null</code>
	 * and must have at least one entry.
	 * <pre>
	 * Assert.notEmpty(map);</pre>
	 * @param map the map to check
	 * @throws IllegalArgumentException if the map is <code>null</code>
	 * or has no entries
	 */
	public static void notEmpty(Map map) {
		if (map == null || map.isEmpty()) {
			throw new IllegalArgumentException("[Assertion failed] - this map must not be empty; it must contain at least one entry");
		}
	}

}
