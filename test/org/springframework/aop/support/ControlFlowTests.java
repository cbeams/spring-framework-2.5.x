/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import junit.framework.TestCase;

/**
 * 
 * @author Rod Johnson
 * @version $Id: ControlFlowTests.java,v 1.1 2003-12-07 10:19:10 johnsonr Exp $
 */
public class ControlFlowTests extends TestCase {

	/**
	 * Constructor for ControlFlowTests.
	 * @param arg0
	 */
	public ControlFlowTests(String arg0) {
		super(arg0);
	}

	/*
	 * Class to test for boolean under(Class)
	 */
	public void testUnderClassAndMethod() {
		new One().test();
		new Two().testing();
	}
	
	/*
	public void testUnderPackage() {
		ControlFlow cflow = new ControlFlow();
		assertFalse(cflow.underPackage("org.springframework.aop"));
		assertTrue(cflow.underPackage("org.springframework.aop.support"));
		assertFalse(cflow.underPackage("com.interface21"));
	}
	*/

	
	public class One {
		public void test() {
			ControlFlow cflow = new ControlFlow();
			assertTrue(cflow.under(One.class));
			assertTrue(cflow.under(ControlFlowTests.class));
			assertFalse(cflow.under(Two.class));
			assertTrue(cflow.under(One.class, "test"));
			assertFalse(cflow.under(One.class, "hashCode"));
		}
	}
	
	public class Two {
		public void testing() {
			ControlFlow cflow = new ControlFlow();
			assertTrue(cflow.under(Two.class));
			assertTrue(cflow.under(ControlFlowTests.class));
			assertFalse(cflow.under(One.class));
			assertFalse(cflow.under(Two.class, "test"));
			assertTrue(cflow.under(Two.class, "testing"));
		}
	}
}
