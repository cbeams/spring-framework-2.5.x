/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.core;

import junit.framework.TestCase;

/**
 * @author Rod Johnson
 * @version $Id: AbstractControlFlowTests.java,v 1.1 2004-02-02 11:22:53 jhoeller Exp $
 */
public abstract class AbstractControlFlowTests extends TestCase {

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
