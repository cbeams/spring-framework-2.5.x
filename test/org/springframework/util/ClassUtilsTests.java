package org.springframework.util;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.springframework.beans.TestBean;

/**
 * Tests the various functionality of the ClassUtils class.
 * @author Colin Sampaleanu
 */
public class ClassUtilsTests extends TestCase {

	public void setUp() {
		InnerClass.noArgCalled = false;
		InnerClass.argCalled = false;
		InnerClass.overloadedCalled = false;
	}

	public void testGetShortName() {
		String className = ClassUtils.getShortName(getClass());
		assertEquals("Class name did not match", "ClassUtilsTests", className);
	}

	public void testGetInnerShortName() {
		String className = ClassUtils.getShortName(InnerClass.class);
		assertEquals("Class name did not match", "ClassUtilsTests.InnerClass",
		    className);
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
		Method method = ClassUtils.getStaticMethod(InnerClass.class, "staticMethod", null);
		method.invoke(null, null);
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
