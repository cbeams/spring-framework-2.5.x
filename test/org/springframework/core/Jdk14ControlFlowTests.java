/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.core;

/**
 * Tests with ControlFlowFactory return.
 * @author Rod Johnson
 * @version $Id: Jdk14ControlFlowTests.java,v 1.1 2004-02-02 11:22:53 jhoeller Exp $
 */
public class Jdk14ControlFlowTests extends AbstractControlFlowTests {
	
	/**
	 * Necessary only because Eclipse won't run test suite unless it declares
	 * some methods as well as inherited methods
	 */
	public void testThisClassPlease() {
	}

	protected ControlFlow createControlFlow() {
		return new ControlFlowFactory.Jdk14ControlFlow();
	}

}
