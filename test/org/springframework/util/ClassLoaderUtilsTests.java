/*
 * The Spring Framework	is published under the terms
 * of the Apache Software License.
 */
 
package	org.springframework.util;

import java.lang.reflect.Proxy;

import junit.framework.TestCase;

/**
 * todo write tests for all classes
 * @author Colin Sampaleanu
 * @since 30-Sep-2003
 */

public class ClassLoaderUtilsTests extends TestCase {

	public void	testClassPackageAsResourcePath() {
		String result =	ClassLoaderUtils.classPackageAsResourcePath(Proxy.class);
		assertTrue(result.equals("java/lang/reflect"));
	}

	public void	testAddResourcePathToPackagePath() {
		String result =	"java/lang/reflect/xyzabc.xml";
		assertEquals(result, ClassLoaderUtils.addResourcePathToPackagePath(Proxy.class,	"xyzabc.xml"));
		assertEquals(result, ClassLoaderUtils.addResourcePathToPackagePath(Proxy.class,	"/xyzabc.xml"));
	
		assertEquals("java/lang/reflect/a/b/c/d.xml",
						ClassLoaderUtils.addResourcePathToPackagePath(Proxy.class, "a/b/c/d.xml"));
	}

	public void testClassNameNoFqn() {
		Class clazz = Exception.class;
		assertEquals(ClassLoaderUtils.classNameWithoutPackagePrefix(clazz), "Exception");

		// Test with inner class
		clazz = MyInnerClass.class;
		assertEquals(ClassLoaderUtils.classNameWithoutPackagePrefix(clazz), "ClassLoaderUtilsTests$MyInnerClass");
	}

	// Purely for testing class name resolution
	public static class MyInnerClass {
	}

}
