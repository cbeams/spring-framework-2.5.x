package org.springframework.beans.factory.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.beans.FatalBeanException;

/**
 * Tests MethodInvokingFactoryBean
 * 
 * @author colin sampaleanu
 * @since 2003-11-21
 * @version $Id: MethodInvokingFactoryBeanTests.java,v 1.1 2003/11/22 00:45:34
 *          colins Exp $
 */
public class MethodInvokingFactoryBeanTests extends TestCase {

	public void testGetObject() throws Exception {

		// singleton, non-static
		TestClass1 tc1 = new TestClass1();
		MethodInvokingFactoryBean mcfb = new MethodInvokingFactoryBean();
		mcfb.setTarget(tc1);
		mcfb.setTargetMethod("method1");
		mcfb.afterPropertiesSet();
		Integer i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);

		// non-singleton, non-static
		tc1 = new TestClass1();
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTarget(tc1);
		mcfb.setTargetMethod("method1");
		mcfb.setSingleton(false);
		mcfb.afterPropertiesSet();
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 2);

		// singleton, static
		String fqmn = TestClass1.class.getName() + ".staticMethod1";
		TestClass1._staticField1 = 0;
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(fqmn);
		mcfb.afterPropertiesSet();
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);

		// non-singleton, static
		TestClass1._staticField1 = 0;
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(fqmn);
		mcfb.setSingleton(false);
		mcfb.afterPropertiesSet();
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 1);
		i = (Integer) mcfb.getObject();
		assertTrue(i.intValue() == 2);

		// void return value
		fqmn = TestClass1.class.getName() + ".voidRetvalMethod";
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(fqmn);
		mcfb.afterPropertiesSet();
		assertTrue(MethodInvokingFactoryBean.VOID.equals(mcfb.getObject()));

		// now see if we can match methods with arguments that have supertype
		// arguments
		fqmn = TestClass1.class.getName() + ".supertypes";
		TestClass1._staticField1 = 0;
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(fqmn);
		mcfb.setArgs(new Object[]{new ArrayList(), new ArrayList(), "hello"});
		// should pass
		mcfb.afterPropertiesSet();

		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(fqmn);
		mcfb.setArgs(new Object[]{new ArrayList(), new ArrayList(), "hello", "bogus"});
		try {
			mcfb.afterPropertiesSet();
			fail("Matched method with wrong number of args: " + fqmn);
		}
		catch (FatalBeanException e) {
			// expected
		}

		// now ideally we would fail on improper argument types, but
		// unfortunately
		// our algorithm is too stupid, so we are just going to check that in
		// fact
		// we match improper argument types, and then fail on the actual call.
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(fqmn);
		mcfb.setArgs(new Object[]{"1", "2", "3"});
		mcfb.afterPropertiesSet();
		try {
			Object x = mcfb.getObject();
			fail("Should have failed on getObject with mismatched argument types");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
		
        // verify fail if two matching methods with the same arg count
		fqmn = TestClass1.class.getName() + ".supertypes2";
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(fqmn);
		mcfb.setArgs(new Object[]{new ArrayList(), new ArrayList(), "hello", "bogus"});
		try {
			mcfb.afterPropertiesSet();
			fail("Matched method when shouldn't have matched: " + fqmn);
		}
		catch (FatalBeanException e) {
			// expected
		}
		
	}

	public void testGetObjectType() throws Exception {
		TestClass1 tc1 = new TestClass1();
		MethodInvokingFactoryBean mcfb = new MethodInvokingFactoryBean();
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTarget(tc1);
		mcfb.setTargetMethod("method1");
		mcfb.afterPropertiesSet();
		assertTrue(int.class.equals(mcfb.getObjectType()));

		String fqmn = TestClass1.class.getName() + ".voidRetvalMethod";
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(fqmn);
		mcfb.afterPropertiesSet();
		Class objType = mcfb.getObjectType();
		assertTrue(objType.equals(MethodInvokingFactoryBean.VoidType.class));

		// verify that we can call a method with args that are subtypes of the
		// target
		// method arg types
		fqmn = TestClass1.class.getName() + ".supertypes";
		TestClass1._staticField1 = 0;
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(fqmn);
		mcfb.setArgs(new Object[]{new ArrayList(), new ArrayList(), "hello"});
		mcfb.afterPropertiesSet();
		mcfb.getObjectType();

		// now we should fail at runtime if they don't match
		// (ideally we would fail on improper argument types at
		// afterPropertiesSet time, but unfortunately our algorithm is too
		// stupid, so we are just going to check that in fact we match
		// improper argument types, and the test for getObject will check for the
		// runtime failure.
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(fqmn);
		mcfb.setArgs(new Object[]{"1", "2", "3"});
		mcfb.afterPropertiesSet();

	}

	public void testAfterPropertiesSet() {

		String validationError = "improper validation of input properties";

		// assert that only static OR non static are set, but not both or none
		MethodInvokingFactoryBean mcfb = new MethodInvokingFactoryBean();
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (IllegalArgumentException e) {
			// expected
		}
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod("whatever");
		mcfb.setTarget(this);
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (IllegalArgumentException e) {
			// expected
		}

		// bogus static method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod("some.bogus.Method.name");
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (FatalBeanException e) {
			// expected
		}

		// bogus static method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(TestClass1.class.getName() + ".method1");
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (FatalBeanException e) {
			// expected
		}

		// missing method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTarget(this);
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (FatalBeanException e) {
			// expected
		}

		// bogus method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTarget(this);
		mcfb.setTargetMethod("bogus");
		try {
			mcfb.afterPropertiesSet();
			fail(validationError);
		}
		catch (FatalBeanException e) {
			// expected
		}

		// static method
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setStaticMethod(TestClass1.class.getName() + ".staticMethod1");
		mcfb.afterPropertiesSet();

		// non-static method
		TestClass1 tc1 = new TestClass1();
		mcfb = new MethodInvokingFactoryBean();
		mcfb.setTarget(tc1);
		mcfb.setTargetMethod("method1");
		mcfb.afterPropertiesSet();
	}

	// a test class to work with
	static class TestClass1 {

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