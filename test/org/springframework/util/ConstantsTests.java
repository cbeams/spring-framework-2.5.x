/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.util;

import java.util.Set;

import junit.framework.TestCase;

/**
 * @author Rod Johnson
 * @since 28-Apr-2003
 * @version $Revision: 1.2 $
 */
public class ConstantsTests extends TestCase {

	public void testA() {
		Constants c = new Constants(A.class);
		assertTrue(c.getSize() == 3);
		
		assertEquals(c.asNumber("DOG").intValue(), A.DOG);
		assertEquals(c.asNumber("dog").intValue(), A.DOG);
		assertEquals(c.asNumber("cat").intValue(), A.CAT);

		try {
			c.asNumber("bogus");
			fail("Can't get bogus field");
		}
		catch (ConstantException ex) {
		}

		assertTrue(c.asString("S1").equals(A.S1));
		try {
			c.asNumber("S1");
			fail("Wrong type");
		}
		catch (ConstantException ex) {
		}

		assertEquals(c.toCode(new Integer(0), ""), "DOG");
		assertEquals(c.toCode(new Integer(0), "D"), "DOG");
		assertEquals(c.toCode(new Integer(0), "DO"), "DOG");
		assertEquals(c.toCode(new Integer(0), "DoG"), "DOG");
		assertEquals(c.toCode(new Integer(66), ""), "CAT");
		assertEquals(c.toCode(new Integer(66), "C"), "CAT");
		assertEquals(c.toCode(new Integer(66), "ca"), "CAT");
		assertEquals(c.toCode(new Integer(66), "cAt"), "CAT");
		assertEquals(c.toCode("", ""), "S1");
		assertEquals(c.toCode("", "s"), "S1");
		assertEquals(c.toCode("", "s1"), "S1");

		Set values = c.getValues("");
		assertEquals(values.size(), c.getSize());
		assertTrue(values.contains(new Integer(0)));
		assertTrue(values.contains(new Integer(66)));
		assertTrue(values.contains(""));
	}
	
	
	public static class A {
		
		public static final int DOG = 0;
		public static final int CAT = 66;
		public static final String S1 = "";
		
		/** ignore these */
		protected static final int P = -1;
		protected boolean f;
		static final Object o = new Object();
	}

}
