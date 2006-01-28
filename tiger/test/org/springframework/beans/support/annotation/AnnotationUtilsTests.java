/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.beans.support.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.springframework.core.annotation.Order;

import junit.framework.TestCase;

/**
 * @author Rob Harrop
 * @author Rod Johnson
 */
public class AnnotationUtilsTests extends TestCase {

	public void testCopyProperties() {
		FooAnnotation annotation = Foo.class.getAnnotation(FooAnnotation.class);
		Foo foo = new Foo();
		AnnotationUtils.copyPropertiesToBean(annotation, foo);
		assertEquals("Name not copied", annotation.name(), foo.getName());
		assertEquals("Age not copied", annotation.age(), foo.getAge());
	}

	public void testCopyPropertiesWithIgnore() {
		FooAnnotation annotation = Foo.class.getAnnotation(FooAnnotation.class);
		Foo foo = new Foo();
		foo.setName("Juergen Hoeller");
		foo.setAge(30);
		AnnotationUtils.copyPropertiesToBean(annotation, foo, "name", "age");
		assertEquals("Name copied", "Juergen Hoeller", foo.getName());
		assertEquals("Age copied", 30, foo.getAge());
	}

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
	
	// TODO consider whether we want this to handle annotations on interfaces
//	public void testFindMethodAnnotationFromInterfaceImplementedByRoot() throws SecurityException, NoSuchMethodException {
//		Method m = Leaf.class.getMethod("fromInterfaceImplementedByRoot", (Class[]) null);
//		Order o = AnnotationUtils.findMethodAnnotation(Order.class, m, Leaf.class);
//		assertNotNull(o);
//	}


	@Retention(RetentionPolicy.RUNTIME)
	public static @interface FooAnnotation {

		String name();

		int age();
	}


	@FooAnnotation(name = "Rob Harrop", age = 23)
	public static class Foo {

		private String name;

		private int age;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
	}


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

}
