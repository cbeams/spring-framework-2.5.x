/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.util;

import org.springframework.beans.TestBean;

import junit.framework.TestCase;

/**
 * 
 * @author Rod Johnson
 */
public class ReflectionUtilsTests extends TestCase {
	
	public ReflectionUtilsTests(String s) {
		super(s);
	}
	
	public void testCopySrcToDestinationOfIncorrectClass() {
		TestBean src = new TestBean();
		String dest = new String();
		try {
			ReflectionUtils.shallowCopyFieldState(src, dest);
			fail();
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	
	
	public void testRejectsNullSrc() {
		TestBean src = null;
		String dest = new String();
		try {
			ReflectionUtils.shallowCopyFieldState(src, dest);
			fail();
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	
	public void testRejectsNullDest() {
		TestBean src = new TestBean();
		String dest = null;
		try {
			ReflectionUtils.shallowCopyFieldState(src, dest);
			fail();
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	
	public void testValidCopy() {
		TestBean src = new TestBean();
		TestBean dest = new TestBean();
		testValidCopy(src, dest);
	}
	
	public static class TestBeanSubclassWithNewField extends TestBean {
		private int magic;
		protected String prot = "foo";
	}
	
	public static class TestBeanSubclassWithFinalField extends TestBean {
		private final String foo = "will break naive copy that doesn't exclude statics";
	}
	
	public void testValidCopyOnSubTypeWithNewField() {
		TestBeanSubclassWithNewField src = new TestBeanSubclassWithNewField();
		TestBeanSubclassWithNewField dest = new TestBeanSubclassWithNewField();
		src.magic = 11;
		
		// Will check inherited fields are copied
		testValidCopy(src, dest);
		
		// Check subclass fields were copied
		assertEquals(src.magic, dest.magic);
		assertEquals(src.prot, dest.prot);
	}
	
	public void testValidCopyToSubType() {
		TestBean src = new TestBean();
		TestBeanSubclassWithNewField dest = new TestBeanSubclassWithNewField();
		dest.magic = 11;
		testValidCopy(src, dest);
		// Should have left this one alone
		assertEquals(11, dest.magic);
	}
	
	public void testValidCopyToSubTypeWithFinalField() {
		TestBeanSubclassWithFinalField src = new TestBeanSubclassWithFinalField();
		TestBeanSubclassWithFinalField dest = new TestBeanSubclassWithFinalField();
		// Check that this doesn't fail due to attempt to assign final
		testValidCopy(src, dest);
	}
	
	// skip statics
	
	private void testValidCopy(TestBean src, TestBean dest) {
		src.setName("freddie");
		src.setAge(15);
		src.setSpouse(new TestBean());
		assertFalse(src.getAge() == dest.getAge());
		
		ReflectionUtils.shallowCopyFieldState(src, dest);
		assertEquals(src.getAge(), dest.getAge());
		assertEquals(src.getSpouse(), dest.getSpouse());
		assertEquals(src.getDoctor(), dest.getDoctor());
	}

}
