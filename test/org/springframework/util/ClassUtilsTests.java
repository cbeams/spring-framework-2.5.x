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

package org.springframework.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.DerivedTestBean;
import org.springframework.beans.IOther;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @author Rob Harrop
 */
public class ClassUtilsTests extends TestCase {

	public void setUp() {
		InnerClass.noArgCalled = false;
		InnerClass.argCalled = false;
		InnerClass.overloadedCalled = false;
	}

	public void testForName() throws ClassNotFoundException {
		assertEquals(String.class, ClassUtils.forName("java.lang.String"));
		assertEquals(String[].class, ClassUtils.forName("java.lang.String[]"));
		assertEquals(TestBean.class, ClassUtils.forName("org.springframework.beans.TestBean"));
		assertEquals(TestBean[].class, ClassUtils.forName("org.springframework.beans.TestBean[]"));
	}

	public void testForNameWithPrimitiveClasses() throws ClassNotFoundException {
		assertEquals(boolean.class, ClassUtils.forName("boolean"));
		assertEquals(byte.class, ClassUtils.forName("byte"));
		assertEquals(char.class, ClassUtils.forName("char"));
		assertEquals(short.class, ClassUtils.forName("short"));
		assertEquals(int.class, ClassUtils.forName("int"));
		assertEquals(long.class, ClassUtils.forName("long"));
		assertEquals(float.class, ClassUtils.forName("float"));
		assertEquals(double.class, ClassUtils.forName("double"));
	}

	public void testForNameWithPrimitiveArrays() throws ClassNotFoundException {
		assertEquals(boolean[].class, ClassUtils.forName("boolean[]"));
		assertEquals(byte[].class, ClassUtils.forName("byte[]"));
		assertEquals(char[].class, ClassUtils.forName("char[]"));
		assertEquals(short[].class, ClassUtils.forName("short[]"));
		assertEquals(int[].class, ClassUtils.forName("int[]"));
		assertEquals(long[].class, ClassUtils.forName("long[]"));
		assertEquals(float[].class, ClassUtils.forName("float[]"));
		assertEquals(double[].class, ClassUtils.forName("double[]"));
	}

	public void testGetQualifiedName() {
		String className = ClassUtils.getQualifiedName(getClass());
		assertEquals("Class name did not match", "org.springframework.util.ClassUtilsTests", className);
	}

	public void testGetQualifiedNameForObjectArrayClass() {
		String className = ClassUtils.getQualifiedName(Object[].class);
		assertEquals("Class name did not match", "java.lang.Object[]", className);
	}

	public void testGetQualifiedNameForPrimitiveArrayClass() {
		String className = ClassUtils.getQualifiedName(byte[].class);
		assertEquals("Class name did not match", "byte[]", className);
	}

	public void testGetShortName() {
		String className = ClassUtils.getShortName(getClass());
		assertEquals("Class name did not match", "ClassUtilsTests", className);
	}

	public void testGetShortNameForObjectArrayClass() {
		String className = ClassUtils.getShortName(Object[].class);
		assertEquals("Class name did not match", "Object[]", className);
	}

	public void testGetShortNameForPrimitiveArrayClass() {
		String className = ClassUtils.getShortName(byte[].class);
		assertEquals("Class name did not match", "byte[]", className);
	}

	public void testGetShortNameForInnerClass() {
		String className = ClassUtils.getShortName(InnerClass.class);
		assertEquals("Class name did not match", "ClassUtilsTests.InnerClass", className);
	}

	public void testGetShortNameForCglibClass() {
		TestBean tb = new TestBean();
		ProxyFactory pf = new ProxyFactory();
		pf.setTarget(tb);
		pf.setProxyTargetClass(true);
		TestBean proxy = (TestBean) pf.getProxy();
		String className = ClassUtils.getShortName(proxy.getClass());
		assertEquals("Class name did not match", "TestBean", className);
	}

	public void testGetShortNameAsProperty() {
		String shortName = ClassUtils.getShortNameAsProperty(this.getClass());
		assertEquals("Class name did not match", "classUtilsTests", shortName);
	}

	public void testCountOverloadedMethods() {
		assertFalse(ClassUtils.hasAtLeastOneMethodWithName(TestBean.class, "foobar"));
		// no args
		assertTrue(ClassUtils.hasAtLeastOneMethodWithName(TestBean.class, "hashCode"));
		// matches although it takes an arg
		assertTrue(ClassUtils.hasAtLeastOneMethodWithName(TestBean.class, "setAge"));
	}

	public void testNoArgsStaticMethod() throws IllegalAccessException, InvocationTargetException {
		Method method = ClassUtils.getStaticMethod(InnerClass.class, "staticMethod", (Class[]) null);
		method.invoke(null, (Object[]) null);
		assertTrue("no argument method was not invoked.",
				InnerClass.noArgCalled);
	}

	public void testArgsStaticMethod() throws IllegalAccessException, InvocationTargetException {
		Method method = ClassUtils.getStaticMethod(InnerClass.class, "argStaticMethod",
				new Class[] {String.class});
		method.invoke(null, new Object[] {"test"});
		assertTrue("argument method was not invoked.", InnerClass.argCalled);
	}

	public void testOverloadedStaticMethod() throws IllegalAccessException, InvocationTargetException {
		Method method = ClassUtils.getStaticMethod(InnerClass.class, "staticMethod",
				new Class[] {String.class});
		method.invoke(null, new Object[] {"test"});
		assertTrue("argument method was not invoked.",
				InnerClass.overloadedCalled);
	}

	public void testClassPackageAsResourcePath() {
		String result = ClassUtils.classPackageAsResourcePath(Proxy.class);
		assertTrue(result.equals("java/lang/reflect"));
	}

	public void testAddResourcePathToPackagePath() {
		String result = "java/lang/reflect/xyzabc.xml";
		assertEquals(result, ClassUtils.addResourcePathToPackagePath(Proxy.class, "xyzabc.xml"));
		assertEquals(result, ClassUtils.addResourcePathToPackagePath(Proxy.class, "/xyzabc.xml"));

		assertEquals("java/lang/reflect/a/b/c/d.xml",
				ClassUtils.addResourcePathToPackagePath(Proxy.class, "a/b/c/d.xml"));
	}

	public void testGetAllInterfaces() {
		DerivedTestBean testBean = new DerivedTestBean();
		List ifcs = Arrays.asList(ClassUtils.getAllInterfaces(testBean));
		assertEquals("Correct number of interfaces", 7, ifcs.size());
		assertTrue("Contains Serializable", ifcs.contains(Serializable.class));
		assertTrue("Contains ITestBean", ifcs.contains(ITestBean.class));
		assertTrue("Contains IOther", ifcs.contains(IOther.class));
	}

	public void testIsPresent() throws Exception {
		assertTrue(ClassUtils.isPresent("java.lang.String"));
		assertFalse(ClassUtils.isPresent("java.lang.MySpecialString"));
	}


	public static class InnerClass {

		static boolean noArgCalled;
		static boolean argCalled;
		static boolean overloadedCalled;

		public static void staticMethod() {
			noArgCalled = true;
		}

		public static void staticMethod(String anArg) {
			overloadedCalled = true;
		}

		public static void argStaticMethod(String anArg) {
			argCalled = true;
		}
	}

}
