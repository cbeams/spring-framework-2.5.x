/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.util;

/**
 * Tests with ControlFlowFactory return
 * @author Rod Johnson
 * @version $Id: Jdk14ControlFlowTests.java,v 1.1 2003-12-21 11:59:57 johnsonr Exp $
 */
public class Jdk14ControlFlowTests extends AbstractControlFlowTests {
	
	public Jdk14ControlFlowTests(String s) {
		super(s);
	}
	
	/** 
	 * Necessary only because
	 * Eclipse won't run test suite unless it declares some methods
	 * as well as inherited methods
	 */
	public void testThisClassPlease() {
	}

	/**
	 * @see org.springframework.aop.support.AbstractControlFlowTests#createControlFlow()
	 */
	protected ControlFlow createControlFlow() {
		return new Jdk14ControlFlow();
	}

}
