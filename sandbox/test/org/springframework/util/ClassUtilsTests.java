package org.springframework.util;

import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * Tests the various functionality of the ClassUtils class.
 * 
 * @author R.J. Lorimer
 */
public class ClassUtilsTests extends TestCase {

    public void testGetShortName() {
        String className = ClassUtils.getShortName(getClass());
        assertEquals("Class name did not match", "ClassUtilsTests", className);
    }

    public void testGetInnerShortName() {
        String className = ClassUtils.getShortName(InnerClass.class);
        assertEquals("Class name did not match", "ClassUtilsTests.InnerClass",
                className);
    }

    public void testNullClassGetShortName() {
        try {
            ClassUtils.getShortName((Class)null);
            fail("Null class was accepted");
        } catch (IllegalArgumentException e) {
            // correct behavior
        }
    }

    public void testNullStringGetShortName() {
        try {
            ClassUtils.getShortName((String)null);
            fail("Null string was accepted.");
        } catch (IllegalArgumentException e) {
            // correct behavior
        } catch (Exception e) {
            fail("Null arguments weren't handled properly");
        }
    }

    public void testNoArgsStaticMethod() {
        Method method = ClassUtils.getStaticMethod("staticMethod",
                InnerClass.class, null);
        invokeStaticMethod(method, null);
        assertTrue("no argument method was not invoked.",
                InnerClass.noArgCalled);
    }

    public void testArgsStaticMethod() {
        Method method = ClassUtils.getStaticMethod("argStaticMethod",
                InnerClass.class, new Class[] { String.class });
        invokeStaticMethod(method, new Object[] { "test" });
        assertTrue("argument method was not invoked.", InnerClass.argCalled);
    }

    public void testOverloadedStaticMethod() {
        Method method = ClassUtils.getStaticMethod("staticMethod",
                InnerClass.class, new Class[] { String.class });
        invokeStaticMethod(method, new Object[] { "test" });
        assertTrue("argument method was not invoked.",
                InnerClass.overloadedCalled);
    }

    public void testNullNameStaticMethod() {
        try {
            ClassUtils.getStaticMethod(null, getClass(), null);
            fail("Null name was accepted");
        } catch (IllegalArgumentException e) {
            // correct behavior
        } catch (Exception e) {
            fail("Null arguments weren't handled properly!");
        }
    }

    public void testNullClassStaticMethod() {
        try {
            ClassUtils.getStaticMethod("staticMethod", null, null);
        } catch (IllegalArgumentException e) {
            // correct behaivior
        } catch (Exception e) {
            fail("Null arguments weren't handled properly!");
        }
    }

    public void setUp() {
        InnerClass.noArgCalled = false;
        InnerClass.argCalled = false;
        InnerClass.overloadedCalled = false;
    }

    private void invokeStaticMethod(Method m, Object[] args) {
        try {
            m.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
