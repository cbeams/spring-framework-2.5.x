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
 * Assert utility class that assists in validating arguments and internal
 * invariants. Useful for identifying programmer errors early and obviously at
 * runtime.
 * <p>
 * For example, if the contract of a public setter method states it does not
 * allow null arguments, Assert can be used to validate that contract. Doing
 * this clearly indicates a contract violation when it occurs and protects the
 * class's invariants.
 * <p>
 * This class is similiar to JUnit's assertion library. If an argument value is
 * deemed invalid, an IllegalArgumentException is thrown. For example:
 * 
 * <pre>
 *  Assert.isTrue(i > 0, "The value must be greater than zero ", i); Assert.notNull(clazz, "The class must not be null");
 * </pre>
 * 
 * @author Keith Donald, adapted from jakarta-commons-lang Validate
 */
public class Assert {

    /**
     * Class is not a static utility class; not instantiable.
     * <p>
     * Note: static utility classes, by convention, end with "Utils." The
     * 'Assert' class name is an exception.
     */
    private Assert() {
    }

    /**
     * Assert a boolean expression associatd with an object, throwing a <code>IllegalArgumentException</code>
     * if the test result is <code>false</code>. For example:
     * <p>
     * 
     * <pre>
     *  Assert.isTrue(object.compareTo(object2) > 0, "The object is not greater than ", object2);
     * </pre>
     * 
     * @param expression
     *            a boolean expression
     * @param message
     *            the exception message to display if the expression is <code>false</code>
     * @param value
     *            the value to append to the message if the expression is
     *            <code>false</code>
     * @throws IllegalArgumentException
     *             if expression is <code>false</code>
     */
    public static void isTrue(
        boolean expression,
        String message,
        Object value) {
        if (expression == false) {
            throw new IllegalArgumentException(message + "'" + value + "'");
        }
    }

    /**
     * Assert a boolean expression associated with a whole number primitive,
     * throwing <code>IllegalArgumentException</code> if the test result is
     * <code>false</code>.
     * 
     * @param expression
     *            a boolean expression
     * @param message
     *            the exception message to display if the expression is <code>false</code>
     * @param value
     *            the value to append to the message if the expression is
     *            <code>false</code>
     * @throws IllegalArgumentException
     *             if expression is <code>false</code>
     */
    public static void isTrue(boolean expression, String message, long value) {
        if (expression == false) {
            throw new IllegalArgumentException(message + value);
        }
    }

    /**
     * Assert a boolean expression associated with a decimal primitive,
     * throwing <code>IllegalArgumentException</code> if the test result is
     * <code>false</code>.
     * 
     * @param expression
     *            a boolean expression
     * @param message
     *            the exception message to display if the expression is <code>false</code>
     * @param value
     *            the value to append to the message if the expression is
     *            <code>false</code>
     * @throws IllegalArgumentException
     *             if expression is <code>false</code>
     */
    public static void isTrue(
        boolean expression,
        String message,
        double value) {
        if (expression == false) {
            throw new IllegalArgumentException(message + value);
        }
    }

    /**
     * Assert a boolean expression, throwing <code>IllegalArgumentException</code>
     * if the test result is <code>false</code>.
     * 
     * @param expression
     *            a boolean expression
     * @param message
     *            the exception message to display if the expression is <code>false</code>
     * @throws IllegalArgumentException
     *             if expression is <code>false</code>
     */
    public static void isTrue(boolean expression, String message) {
        if (expression == false) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert a boolean expression, throwing <code>IllegalArgumentException</code>
     * if the test result is <code>false</code>. The exception message is a
     * generic, default message.
     * 
     * @param expression
     *            a boolean expression
     * @param message
     *            the exception message to display if the expression is <code>false</code>
     * @throws IllegalArgumentException
     *             if expression is <code>false</code>
     */
    public static void isTrue(boolean expression) {
        if (expression == false) {
            throw new IllegalArgumentException("Assertion failed; this expression must be [true].");
        }
    }

    /**
     * Assert that an object is not null.
     * <p>
     * 
     * <pre>
     *  Assert.notNull(myObject, "myObject must not be null");
     * </pre>
     * 
     * @param object
     *            the object to assert.
     * @param message
     *            the exception message if the object is <code>null</code>
     * @throws IllegalArgumentException
     *             if the object is <code>null</code>
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object is not null. If the object is null, a <code>IllegalArgumentException</code>
     * is thrown and the default exception message is used.
     * <p>
     * 
     * <pre>
     *  Assert.notNull(myObject, "myObject must not be null");
     * </pre>
     * 
     * @param object
     *            the object to assert.
     * @throws IllegalArgumentException
     *             if the object is <code>null</code>
     */
    public static void notNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Assertion failure; this object cannot be [null].");
        }
    }

    /**
     * Assert that an object array has elements; that is, it must not be <code>null</code>
     * and must have a <code>length > 0</code>.
     * <p>
     * 
     * <pre>
     *  Assert.hasElements(myObjectArray, "myObjectArray must have elements.");
     * </pre>
     * 
     * @param array
     *            the object array to assert.
     * @param message
     *            the exception message to use if the assertion fails.
     * @throws IllegalArgumentException
     *             if the object array is <code>null</code> or has a <code>length > 0</code>
     */
    public static void hasElements(Object[] array, String message) {
        if (!ArrayUtils.hasElements(array)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object array has elements; that is, it must not be <code>null</code>
     * and must have a <code>length > 0</code>. If the assertion fails, an
     * <code>IllegalArgumentException</code> is thrown and the default
     * exception message is used.
     * <p>
     * 
     * <pre>
     *  Assert.hasElements(myObjectArray);
     * </pre>
     * 
     * @param array
     *            the object array to assert.
     * @throws IllegalArgumentException
     *             if the object array is <code>null</code> or has a <code>length > 0</code>
     */
    public static void hasElements(Object[] array) {
        if (!ArrayUtils.hasElements(array)) {
            throw new IllegalArgumentException("Assertion failed; this object array must have elements.");
        }
    }

    /**
     * Assert that a collection has elements; that is, it must not be <code>null</code>
     * and must have a <code>size > 0</code>.
     * <p>
     * 
     * <pre>
     *  Assert.hasElements(myCollection, "myCollection must have elements.");
     * </pre>
     * 
     * @param array
     *            the object array to assert.
     * @param message
     *            the exception message to use if the assertion fails.
     * @throws IllegalArgumentException
     *             if the collection is <code>null</code> or has a <code>size > 0</code>
     */
    public static void hasElements(Collection collection, String message) {
        if (collection == null || collection.size() == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a collection has elements; that is, it must not be <code>null</code>
     * and must have a <code>length > 0</code>. If the assertion fails, an
     * <code>IllegalArgumentException</code> is thrown and the default
     * exception message is used.
     * <p>
     * 
     * <pre>
     *  Assert.hasElements(myCollection);
     * </pre>
     * 
     * @param array
     *            the object array to assert.
     * @throws IllegalArgumentException
     *             if the collection is <code>null</code> or has a <code>size > 0</code>
     */
    public static void hasElements(Collection collection) {
        if (collection == null || collection.size() == 0) {
            throw new IllegalArgumentException("Assertion failed; this collection must have elements.");
        }
    }

    /**
     * Assert that a Map has entries; that is, it must not be <code>null</code>
     * and must have at least one <code>key-value pair</code>.
     * <p>
     * 
     * <pre>
     *  Assert.hasEntries(myCollection, "myMap must have entries.");
     * </pre>
     * 
     * @param map
     *            the map to assert.
     * @param message
     *            the exception message to use if the assertion fails.
     * @throws IllegalArgumentException
     *             if the map is <code>null</code> or has a <code>0</code>
     *             entries
     */
    public static void hasEntries(Map map, String message) {
        if (map == null || map.size() == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that a Map has entries; that is, it must not be <code>null</code>
     * and must have at least one <code>key-value pair</code>. If the
     * assertion fails, an <code>IllegalArgumentException</code> is thrown
     * and the default exception message is used.
     * <p>
     * 
     * <pre>
     *  Assert.hasEntries(myCollection, "myMap must have entries.");
     * </pre>
     * 
     * @param map
     *            the map to assert.
     * @param message
     *            the exception message to use if the assertion fails.
     * @throws IllegalArgumentException
     *             if the map is <code>null</code> or has a <code>0</code>
     *             entries
     */
    public static void hasEntries(Map map) {
        if (map == null || map.size() == 0) {
            throw new IllegalArgumentException("Assertion failed; this map must have at least one entry (key-value pair.)");
        }
    }

    /**
     * Asserts that this string has valid text content; that is, it is not null
     * and not empty or blank.
     * 
     * @param text
     *            The string to assert.
     */
    public static void hasText(String text) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("Assertion failed; this string is null or blank");
        }
    }

    /**
     * Asserts that a number is within a range, inclusive.
     * 
     * @param number
     *            The number to assert.
     * @param low
     *            The minimum allowed value.
     * @param high
     *            The maximum allowed value.
     * @throws IllegalArgumentException,
     *             if number is out of range.
     */
    public static void isInRange(int number, int low, int high) {
        if (number < low || number > high) {
            throw new IllegalArgumentException(
                "Assertion failed; "
                    + number
                    + " is not in range ["
                    + low
                    + ", "
                    + high
                    + "]");
        }
    }

}
