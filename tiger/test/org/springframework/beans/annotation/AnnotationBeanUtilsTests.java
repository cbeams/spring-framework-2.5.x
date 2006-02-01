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

package org.springframework.beans.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import junit.framework.TestCase;

/**
 * @author Rob Harrop
 */
public class AnnotationBeanUtilsTests extends TestCase {

	public void testCopyProperties() {
		FooAnnotation annotation = Foo.class.getAnnotation(FooAnnotation.class);
		Foo foo = new Foo();
		AnnotationBeanUtils.copyPropertiesToBean(annotation, foo);
		assertEquals("Name not copied", annotation.name(), foo.getName());
		assertEquals("Age not copied", annotation.age(), foo.getAge());
	}

	public void testCopyPropertiesWithIgnore() {
		FooAnnotation annotation = Foo.class.getAnnotation(FooAnnotation.class);
		Foo foo = new Foo();
		foo.setName("Juergen Hoeller");
		foo.setAge(30);
		AnnotationBeanUtils.copyPropertiesToBean(annotation, foo, "name", "age");
		assertEquals("Name copied", "Juergen Hoeller", foo.getName());
		assertEquals("Age copied", 30, foo.getAge());
	}


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

}
