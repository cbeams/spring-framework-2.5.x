package org.springframework.util;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import junit.framework.TestCase;

/**
 * Tests the various functionality of the ClassUtils class.
 * 
 * @author R.J. Lorimer
 * @author Colin Sampaleanu
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
    
    public void testGetShortNameAsProperty() {
        String shortName = ClassUtils.getShortNameAsProperty(this.getClass());
        assertEquals("Class name did not match", "classUtilsTests", shortName);
    }

    public void testNoArgsStaticMethod() {
        Method method = ClassUtils.getStaticMethod(InnerClass.class, "staticMethod",
                                                   null);
        invokeStaticMethod(method, null);
        assertTrue("no argument method was not invoked.",
                InnerClass.noArgCalled);
    }

    public void testArgsStaticMethod() {
        Method method = ClassUtils.getStaticMethod(InnerClass.class, "argStaticMethod",
                                                   new Class[] { String.class });
        invokeStaticMethod(method, new Object[] { "test" });
        assertTrue("argument method was not invoked.", InnerClass.argCalled);
    }

    public void testOverloadedStaticMethod() {
        Method method = ClassUtils.getStaticMethod(InnerClass.class, "staticMethod",
                                                   new Class[] { String.class });
        invokeStaticMethod(method, new Object[] { "test" });
        assertTrue("argument method was not invoked.",
                InnerClass.overloadedCalled);
    }
    
	public void	testClassPackageAsResourcePath() {
		String result =	ClassUtils.classPackageAsResourcePath(Proxy.class);
		assertTrue(result.equals("java/lang/reflect"));
	}

	public void	testAddResourcePathToPackagePath() {
		String result =	"java/lang/reflect/xyzabc.xml";
		assertEquals(result, ClassUtils.addResourcePathToPackagePath(Proxy.class,	"xyzabc.xml"));
		assertEquals(result, ClassUtils.addResourcePathToPackagePath(Proxy.class,	"/xyzabc.xml"));
	
		assertEquals("java/lang/reflect/a/b/c/d.xml",
				ClassUtils.addResourcePathToPackagePath(Proxy.class, "a/b/c/d.xml"));
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
