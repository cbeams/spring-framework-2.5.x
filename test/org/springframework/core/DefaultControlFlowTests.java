/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.core;

/**
 * Tests with ControlFlowFactory return
 * @author Rod Johnson
 * @version $Id: DefaultControlFlowTests.java,v 1.1 2004-02-02 11:22:53 jhoeller Exp $
 */
public class DefaultControlFlowTests extends AbstractControlFlowTests {
	
	/**
	 * Necessary only because
	 * Eclipse won't run test suite unless it declares some methods
	 * as well as inherited methods
	 */
	public void testThisClassPlease() {
	}

	protected ControlFlow createControlFlow() {
		ControlFlow cf = ControlFlowFactory.createControlFlow();
		boolean is14 = System.getProperty("java.version").indexOf("1.4") != -1;
		assertEquals("Autodetection of JVM succeeded", is14, cf instanceof ControlFlowFactory.Jdk14ControlFlow);
		return cf;
	}

}
