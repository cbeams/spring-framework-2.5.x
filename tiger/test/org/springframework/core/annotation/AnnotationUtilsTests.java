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

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class AnnotationUtilsTests extends TestCase {

	public void testFindMethodAnnotationOnLeaf() throws SecurityException, NoSuchMethodException {
		Method m = Leaf.class.getMethod("annotatedOnLeaf", (Class[]) null);
		assertNotNull(m.getAnnotation(Order.class));
		assertNotNull(AnnotationUtils.getAnnotation(m, Order.class));
		assertNotNull(AnnotationUtils.findAnnotation(m, Order.class));
	}
	
	public void testFindMethodAnnotationOnRoot() throws SecurityException, NoSuchMethodException {
		Method m = Leaf.class.getMethod("annotatedOnRoot", (Class[]) null);
		assertNotNull(m.getAnnotation(Order.class));
		assertNotNull(AnnotationUtils.getAnnotation(m, Order.class));
		assertNotNull(AnnotationUtils.findAnnotation(m, Order.class));
	}
	
	public void testFindMethodAnnotationOnRootButOverridden() throws SecurityException, NoSuchMethodException {
		Method m = Leaf.class.getMethod("overrideWithoutNewAnnotation", (Class[]) null);
		assertNull(m.getAnnotation(Order.class));
		assertNull(AnnotationUtils.getAnnotation(m, Order.class));
		assertNotNull(AnnotationUtils.findAnnotation(m, Order.class));
	}
	
	public void testFindMethodAnnotationNotAnnotated() throws SecurityException, NoSuchMethodException {
		Method m = Leaf.class.getMethod("notAnnotated", (Class[]) null);
		assertNull(AnnotationUtils.findAnnotation(m, Order.class));
	}

	public void testFindMethodAnnotationOnBridgeMethod() throws Exception {
		Method m = SimpleFoo.class.getMethod("something", Object.class);
		assertTrue(m.isBridge());
		assertNull(m.getAnnotation(Order.class));
		assertNull(AnnotationUtils.getAnnotation(m, Order.class));
		assertNotNull(AnnotationUtils.findAnnotation(m, Order.class));
		assertNull(m.getAnnotation(Transactional.class));
		assertNotNull(AnnotationUtils.getAnnotation(m, Transactional.class));
		assertNotNull(AnnotationUtils.findAnnotation(m, Transactional.class));
	}

// TODO consider whether we want this to handle annotations on interfaces
//	public void testFindMethodAnnotationFromInterfaceImplementedByRoot() throws Exception {
//		Method m = Leaf.class.getMethod("fromInterfaceImplementedByRoot", (Class[]) null);
//		Order o = AnnotationUtils.findAnnotation(Order.class, m, Leaf.class);
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


	public static abstract class Foo<T> {

		@Order(1)
		public abstract void something(T arg);
	}


	public static class SimpleFoo extends Foo<String> {

		@Transactional
		public void something(String arg) {
		}
	}

}
