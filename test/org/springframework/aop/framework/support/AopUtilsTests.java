package org.springframework.aop.framework.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.beans.DerivedTestBean;
import org.springframework.beans.IOther;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * @author Juergen Hoeller
 * @since 29.09.2003
 */
public class AopUtilsTests extends TestCase {

	public void testGetAllInterfaces() {
		DerivedTestBean testBean = new DerivedTestBean();
		List ifcs = Arrays.asList(AopUtils.getAllInterfaces(testBean));
		assertTrue("Correct number of interfaces", ifcs.size() == 3);
		assertTrue("Contains Serializable", ifcs.contains(Serializable.class));
		assertTrue("Contains ITestBean", ifcs.contains(ITestBean.class));
		assertTrue("Contains IOther", ifcs.contains(IOther.class));
	}
	
	
	public void testIsMethodDeclaredOnOneOfTheseInterfaces() throws Exception {
		Method m = Object.class.getMethod("hashCode", null);
		assertFalse(AopUtils.methodIsOnOneOfTheseInterfaces(m, null));
		assertFalse(AopUtils.methodIsOnOneOfTheseInterfaces(m, new Class[] { ITestBean.class }));
		m = TestBean.class.getMethod("getName", null);
		assertTrue(AopUtils.methodIsOnOneOfTheseInterfaces(m, new Class[] { ITestBean.class }));
		assertTrue(AopUtils.methodIsOnOneOfTheseInterfaces(m, new Class[] { Comparable.class, ITestBean.class }));
		assertFalse(AopUtils.methodIsOnOneOfTheseInterfaces(m, new Class[] { Comparable.class }));
	}
	
	/*
	public void testIsMethodDeclaredOnOneOfTheseInterfacesWithSameNameMethodNotFromInterface() throws Exception {
		class Unconnected {
			public String getName() { throw new UnsupportedOperationException(); }
		}
		Method m = Unconnected.class.getMethod("getName", null);
		// TODO What do do?
	}
	*/
	
	public void testIsMethodDeclaredOnOneOfTheseInterfacesRequiresInterfaceArguments() throws Exception {
		Method m = Object.class.getMethod("hashCode", null);
		try {
			assertFalse(AopUtils.methodIsOnOneOfTheseInterfaces(m, new Class[] { TestBean.class }));
			fail();
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}

}
