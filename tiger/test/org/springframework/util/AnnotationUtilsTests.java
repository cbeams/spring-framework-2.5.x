package org.springframework.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import junit.framework.TestCase;

/**
 * @author Rob Harrop
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
