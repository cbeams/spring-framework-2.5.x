/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.util;

import junit.framework.TestCase;

/**
 * 
 * @author Rod Johnson
 * @version $Id: AbstractControlFlowTests.java,v 1.1 2003-12-21 11:59:57 johnsonr Exp $
 */
public abstract class AbstractControlFlowTests extends TestCase {

	/**
	 * Constructor for ControlFlowTests.
	 * @param arg0
	 */
	public AbstractControlFlowTests(String arg0) {
		super(arg0);
	}
	
	protected abstract ControlFlow createControlFlow();

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
			ControlFlow cflow = createControlFlow();
			assertTrue(cflow.under(One.class));
			assertTrue(cflow.under(AbstractControlFlowTests.class));
			assertFalse(cflow.under(Two.class));
			assertTrue(cflow.under(One.class, "test"));
			assertFalse(cflow.under(One.class, "hashCode"));
		}

	}
	
	public class Two {
		public void testing() {
			ControlFlow cflow = createControlFlow();
			assertTrue(cflow.under(Two.class));
			assertTrue(cflow.under(AbstractControlFlowTests.class));
			assertFalse(cflow.under(One.class));
			assertFalse(cflow.under(Two.class, "test"));
			assertTrue(cflow.under(Two.class, "testing"));
		}
	}
}
