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

package org.springframework.test.util;

import junit.framework.TestCase;

import org.springframework.test.AssertThrows;
import org.springframework.test.util.subpackage.Person;

/**
 * JUnit 3.8 based unit tests for {@link ReflectionTestUtils}.
 *
 * @author Sam Brannen
 * @since 2.5
 */
public class ReflectionTestUtilsTests extends TestCase {

	protected static final Float PI = new Float((float) 22 / 7);


	public ReflectionTestUtilsTests() {
		this(null);
	}

	public ReflectionTestUtilsTests(final String name) {
		super(name);
	}

	/**
	 * Test method for
	 * {@link org.springframework.test.util.ReflectionTestUtils#setField(Object, String, Object, Class)}.
	 */
	public final void testSetField() throws Exception {

		final Person person = new Person();

		// ---------------------------------------------------------------------
		// Standard

		ReflectionTestUtils.setField(person, "id", new Long(99), long.class);
		ReflectionTestUtils.setField(person, "name", "Tom", String.class);
		ReflectionTestUtils.setField(person, "age", new Integer(42), int.class);
		ReflectionTestUtils.setField(person, "eyeColor", "blue", String.class);
		ReflectionTestUtils.setField(person, "likesPets", Boolean.TRUE, boolean.class);
		ReflectionTestUtils.setField(person, "favoriteNumber", PI, Number.class);

		assertEquals("Verifying that the person's ID (private field in a superclass) was set.", 99, person.getId());
		assertEquals("Verifying that the person's name (protected field) was set.", "Tom", person.getName());
		assertEquals("Verifying that the person's age (private field) was set.", 42, person.getAge());
		assertEquals("Verifying that the person's eye color (package private field) was set.", "blue",
				person.getEyeColor());
		assertEquals("Verifying that the person's 'likes pets' flag (package private boolean field) was set.", true,
				person.likesPets());
		assertEquals("Verifying that the person's 'favorite number' (package field) was set.", PI,
				person.getFavoriteNumber());

		// ---------------------------------------------------------------------
		// Null - non-primitives

		ReflectionTestUtils.setField(person, "name", null, String.class);
		ReflectionTestUtils.setField(person, "eyeColor", null, String.class);
		ReflectionTestUtils.setField(person, "favoriteNumber", null, Number.class);

		assertNull("Verifying that the person's name (protected field) was set.", person.getName());
		assertNull("Verifying that the person's eye color (package private field) was set.", person.getEyeColor());
		assertNull("Verifying that the person's 'favorite number' (package field) was set.", person.getFavoriteNumber());

		// ---------------------------------------------------------------------
		// Null - primitives

		new AssertThrows(IllegalArgumentException.class,
				"Calling setField() with NULL for a primitive type should throw an IllegalArgumentException.") {

			public void test() throws Exception {
				ReflectionTestUtils.setField(person, "id", null, long.class);
			}
		}.runTest();

		new AssertThrows(IllegalArgumentException.class,
				"Calling setField() with NULL for a primitive type should throw an IllegalArgumentException.") {

			public void test() throws Exception {
				ReflectionTestUtils.setField(person, "age", null, int.class);
			}
		}.runTest();

		new AssertThrows(IllegalArgumentException.class,
				"Calling setField() with NULL for a primitive type should throw an IllegalArgumentException.") {

			public void test() throws Exception {
				ReflectionTestUtils.setField(person, "likesPets", null, boolean.class);
			}
		}.runTest();
	}

	/**
	 * Test method for
	 * {@link org.springframework.test.util.ReflectionTestUtils#invokeSetterMethod(Object, String, Object, Class)}.
	 */
	public final void testInvokeSetterMethod() throws Exception {

		final Person person = new Person();

		// ---------------------------------------------------------------------
		// Standard - properties

		ReflectionTestUtils.invokeSetterMethod(person, "id", new Long(99), long.class);
		ReflectionTestUtils.invokeSetterMethod(person, "name", "Tom", String.class);
		ReflectionTestUtils.invokeSetterMethod(person, "age", new Integer(42), int.class);
		ReflectionTestUtils.invokeSetterMethod(person, "eyeColor", "blue", String.class);
		ReflectionTestUtils.invokeSetterMethod(person, "likesPets", Boolean.TRUE, boolean.class);
		ReflectionTestUtils.invokeSetterMethod(person, "favoriteNumber", PI, Number.class);

		assertEquals("Verifying that the person's ID (protected method in a superclass) was set.", 99, person.getId());
		assertEquals("Verifying that the person's name (private method) was set.", "Tom", person.getName());
		assertEquals("Verifying that the person's age (protected method) was set.", 42, person.getAge());
		assertEquals("Verifying that the person's eye color (package private method) was set.", "blue",
				person.getEyeColor());
		assertEquals("Verifying that the person's 'likes pets' flag (protected method for a boolean) was set.", true,
				person.likesPets());
		assertEquals("Verifying that the person's 'favorite number' (protected method for a Number) was set.", PI,
				person.getFavoriteNumber());

		// ---------------------------------------------------------------------
		// Standard - setter methods

		ReflectionTestUtils.invokeSetterMethod(person, "setId", new Long(1), long.class);
		ReflectionTestUtils.invokeSetterMethod(person, "setName", "Jerry", String.class);
		ReflectionTestUtils.invokeSetterMethod(person, "setAge", new Integer(33), int.class);
		ReflectionTestUtils.invokeSetterMethod(person, "setEyeColor", "green", String.class);
		ReflectionTestUtils.invokeSetterMethod(person, "setLikesPets", Boolean.FALSE, boolean.class);
		ReflectionTestUtils.invokeSetterMethod(person, "setFavoriteNumber", new Integer(42), Number.class);

		assertEquals("Verifying that the person's ID (protected method in a superclass) was set.", 1, person.getId());
		assertEquals("Verifying that the person's name (private method) was set.", "Jerry", person.getName());
		assertEquals("Verifying that the person's age (protected method) was set.", 33, person.getAge());
		assertEquals("Verifying that the person's eye color (package private method) was set.", "green",
				person.getEyeColor());
		assertEquals("Verifying that the person's 'likes pets' flag (protected method for a boolean) was set.", false,
				person.likesPets());
		assertEquals("Verifying that the person's 'favorite number' (protected method for a Number) was set.",
				new Integer(42), person.getFavoriteNumber());

		// ---------------------------------------------------------------------
		// Null - non-primitives

		ReflectionTestUtils.invokeSetterMethod(person, "name", null, String.class);
		ReflectionTestUtils.invokeSetterMethod(person, "eyeColor", null, String.class);
		ReflectionTestUtils.invokeSetterMethod(person, "favoriteNumber", null, Number.class);

		assertNull("Verifying that the person's name (private method) was set.", person.getName());
		assertNull("Verifying that the person's eye color (package private method) was set.", person.getEyeColor());
		assertNull("Verifying that the person's 'favorite number' (protected method for a Number) was set.",
				person.getFavoriteNumber());

		// ---------------------------------------------------------------------
		// Null - primitives

		new AssertThrows(RuntimeException.class,
				"Calling invokeSetterMethod() with NULL for a primitive type should throw an IllegalArgumentException.") {

			public void test() throws Exception {
				ReflectionTestUtils.invokeSetterMethod(person, "id", null, long.class);
			}
		}.runTest();

		new AssertThrows(RuntimeException.class,
				"Calling invokeSetterMethod() with NULL for a primitive type should throw an IllegalArgumentException.") {

			public void test() throws Exception {
				ReflectionTestUtils.invokeSetterMethod(person, "age", null, int.class);
			}
		}.runTest();

		new AssertThrows(RuntimeException.class,
				"Calling invokeSetterMethod() with NULL for a primitive type should throw an IllegalArgumentException.") {

			public void test() throws Exception {
				ReflectionTestUtils.invokeSetterMethod(person, "likesPets", null, boolean.class);
			}
		}.runTest();
	}

}
