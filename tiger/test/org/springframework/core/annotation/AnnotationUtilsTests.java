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

package org.springframework.core.annotation;

import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * @author Rod Johnson
 */
public class AnnotationUtilsTests extends TestCase {

	public void testFindMethodAnnotationOnLeaf() throws SecurityException, NoSuchMethodException {
		Method m = Leaf.class.getMethod("annotatedOnLeaf", (Class[]) null);
		Order o = AnnotationUtils.findMethodAnnotation(Order.class, m, Leaf.class);
		assertNotNull(o);
	}
	
	public void testFindMethodAnnotationOnRoot() throws SecurityException, NoSuchMethodException {
		Method m = Leaf.class.getMethod("annotatedOnRoot", (Class[]) null);
		Order o = AnnotationUtils.findMethodAnnotation(Order.class, m, Leaf.class);
		assertNotNull(o);
	}
	
	public void testFindMethodAnnotationOnRootButOverridden() throws SecurityException, NoSuchMethodException {
		Method m = Leaf.class.getMethod("overrideWithoutNewAnnotation", (Class[]) null);
		Order o = AnnotationUtils.findMethodAnnotation(Order.class, m, Leaf.class);
		assertNotNull(o);
	}
	
	public void testFindMethodAnnotationNotAnnotated() throws SecurityException, NoSuchMethodException {
		Method m = Leaf.class.getMethod("notAnnotated", (Class[]) null);
		Order o = AnnotationUtils.findMethodAnnotation(Order.class, m, Leaf.class);
		assertNull(o);
	}

	public void testFindMethodAnnotationOnBridgeMethod() throws Exception {
		Method m = SimpleFoo.class.getDeclaredMethod("something", Object.class);
		assertTrue(m.isBridge());
		Order o = AnnotationUtils.findMethodAnnotation(Order.class, m, SimpleFoo.class);
	}
	// TODO consider whether we want this to handle annotations on interfaces
//	public void testFindMethodAnnotationFromInterfaceImplementedByRoot() throws SecurityException, NoSuchMethodException {
//		Method m = Leaf.class.getMethod("fromInterfaceImplementedByRoot", (Class[]) null);
//		Order o = AnnotationUtils.findMethodAnnotation(Order.class, m, Leaf.class);
//		assertNotNull(o);
//	}


	public interface AnnotatedInterface {

		@Order(0)
		void fromInterfaceImplementedByRoot();
	}


	public static class Root implements AnnotatedInterface {

		@Order(27)
		public void annotatedOnRoot() {}

		public void overrideToAnnotate() {}

		@Order(27)
		public void overrideWithoutNewAnnotation() {}

		public void notAnnotated() {}

		public void fromInterfaceImplementedByRoot() {}
	}


	public static class Leaf extends Root {

		@Order(25)
		public void annotatedOnLeaf() {}

		@Override
		@Order(1)
		public void overrideToAnnotate() { }

		@Override
		public void overrideWithoutNewAnnotation() {}
	}

	public static interface Foo<T> {
		@Order(1)
		void something(T arg);
	}

	public static class SimpleFoo implements Foo<String> {

		public void something(String arg) {

		}
	}

}
