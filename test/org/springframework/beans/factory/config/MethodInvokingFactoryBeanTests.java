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

package org.springframework.beans.factory.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.util.MethodInvoker;

/**
 * @author Colin Sampaleanu
 * @since 2003-11-21
 */
public class MethodInvokingFactoryBeanTests extends TestCase {

	public void testGetObject() throws Exception {

		// singleton, non-static
		TestClass1 tc1 = new TestClass1();
		MethodInvokingFactoryBean mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetObject(tc1);
		mcfb.setTargetMethod("method1");
		mcfb.afterPropertiesSet();
		Integer i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);

		// non-singleton, non-static
		tc1 = new TestClass1();
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetObject(tc1);
		mcfb.setTargetMethod("method1");
		mcfb.setSingleton(false);
		mcfb.afterPropertiesSet();
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 2);

		// singleton, static
		TestClass1._staticField1 = 0;
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetClass(TestClass1.class);
		mcfb.setTargetMethod("staticMethod1");
		mcfb.afterPropertiesSet();
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);

		// non-singleton, static
		TestClass1._staticField1 = 0;
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod("org.springframework.beans.factory.config.MethodInvokingFactoryBeanTests$TestClass1.staticMethod1");
		mcfb.setSingleton(false);
		mcfb.afterPropertiesSet();
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 2);

		// void return value
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetClass(TestClass1.class);
		mcfb.setTargetMethod("voidRetvalMethod");
		mcfb.afterPropertiesSet();
		assertTrue(MethodInvokingFactoryBean.VOID.equals(mcfb.getObject()));

		// now see if we can match methods with arguments that have supertype
		// arguments
		TestClass1._staticField1 = 0;
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetClass(TestClass1.class);
		mcfb.setTargetMethod("supertypes");
		mcfb.setArguments(new Object[]{new ArrayList(), new ArrayList(), "hello"});
		// should pass
		mcfb.afterPropertiesSet();

		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetClass(TestClass1.class);
		mcfb.setTargetMethod("supertypes");
		mcfb.setArguments(new Object[]{new ArrayList(), new ArrayList(), "hello", "bogus"});
		try {
			mcfb.afterPropertiesSet();
			fail("Matched method with wrong number of args");
		}
		catch (NoSuchMethodException ex) {
			// expected
		}

		// now ideally we would fail on improper argument types, but
		// unfortunately
		// our algorithm is too stupid, so we are just going to check that in
		// fact
		// we match improper argument types, and then fail on the actual call.
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetClass(TestClass1.class);
		mcfb.setTargetMethod("supertypes");
		mcfb.setArguments(new Object[]{"1", "2", "3"});
		mcfb.afterPropertiesSet();
		try {
			Object x = mcfb.getObject();
			fail("Should have failed on getObject with mismatched argument types");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}

		// verify fail if two matching methods with the same arg count
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetClass(TestClass1.class);
		mcfb.setTargetMethod("supertypes2");
		mcfb.setArguments(new Object[]{new ArrayList(), new ArrayList(), "hello", "bogus"});
		try {
			mcfb.afterPropertiesSet();
			fail("Matched method when shouldn't have matched");
		}
		catch (NoSuchMethodException ex) {
			// expected
		}

	}

	public void testGetObjectType() throws Exception {
		TestClass1 tc1 = new TestClass1();
		MethodInvokingFactoryBean mcfb = new MethodInvokingFactoryBean();
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetObject(tc1);
		mcfb.setTargetMethod("method1");
		mcfb.afterPropertiesSet();
		assertTrue(int.class.equals(mcfb.getObjectType()));

		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetClass(TestClass1.class);
		mcfb.setTargetMethod("voidRetvalMethod");
		mcfb.afterPropertiesSet();
		Class objType = mcfb.getObjectType();
		assertTrue(objType.equals(MethodInvokingFactoryBean.VoidType.class));

		// verify that we can call a method with args that are subtypes of the
		// target method arg types
		TestClass1._staticField1 = 0;
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetClass(TestClass1.class);
		mcfb.setTargetMethod("supertypes");
		mcfb.setArguments(new Object[]{new ArrayList(), new ArrayList(), "hello"});
		mcfb.afterPropertiesSet();
		mcfb.getObjectType();

		// now we should fail at runtime if they don't match
		// (ideally we would fail on improper argument types at
		// afterPropertiesSet time, but unfortunately our algorithm is too
		// stupid, so we are just going to check that in fact we match
		// improper argument types, and the test for getObject will check for the
		// runtime failure.
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetClass(TestClass1.class);
		mcfb.setTargetMethod("supertypes");
		mcfb.setArguments(new Object[]{"1", "2", "3"});
		mcfb.afterPropertiesSet();
	}

	public void testAfterPropertiesSet() throws NoSuchMethodException, ClassNotFoundException {
		String validationError = "improper validation of input properties";

		// assert that only static OR non static are set, but not both or none
		MethodInvokingFactoryBean mcfb = new MethodInvokingFactoryBean();
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetObject(this);
		mcfb.setTargetMethod("whatever");
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (NoSuchMethodException ex) {
			// expected
		}

		// bogus static method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetClass(TestClass1.class);
		mcfb.setTargetMethod("some.bogus.Method.name");
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (NoSuchMethodException ex) {
			// expected
		}

		// bogus static method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetClass(TestClass1.class);
		mcfb.setTargetMethod("method1");
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (IllegalArgumentException ex) {
			// expected
		}

		// missing method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetObject(this);
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (IllegalArgumentException ex) {
			// expected
		}

		// bogus method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetObject(this);
		mcfb.setTargetMethod("bogus");
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (NoSuchMethodException ex) {
			// expected
		}

		// static method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetClass(TestClass1.class);
		mcfb.setTargetMethod("staticMethod1");
		mcfb.afterPropertiesSet();

		// non-static method
		TestClass1 tc1 = new TestClass1();
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTargetObject(tc1);
		mcfb.setTargetMethod("method1");
		mcfb.afterPropertiesSet();
	}

	public void testInvokeWithNullArgument() throws Exception {
		MethodInvoker methodInvoker = new MethodInvoker();
		methodInvoker.setTargetClass(getClass());
		methodInvoker.setTargetMethod("nullArgument");
		methodInvoker.setArguments(new Object[] {null});
		methodInvoker.prepare();
		methodInvoker.invoke();
	}


	public static void nullArgument(Object arg) {
	}


	// a test class to work with
	public static class TestClass1 {

		public static int _staticField1;

		public int _field1 = 0;

		public int method1() {
			return ++_field1;
		}

		public static int staticMethod1() {
			return ++_staticField1;
		}

		public static void voidRetvalMethod() {
		}

		public static void supertypes(Collection c, List l, String s) {
		}

		public static void supertypes2(Collection c, List l, String s, Integer i) {
		}

		public static void supertypes2(Collection c, List l, String s, String s2) {
		}
	}

}
