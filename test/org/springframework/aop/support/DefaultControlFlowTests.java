/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

/**
 * Tests with ControlFlowFactory return
 * @author Rod Johnson
 * @version $Id: DefaultControlFlowTests.java,v 1.1 2003-12-15 14:39:11 johnsonr Exp $
 */
public class DefaultControlFlowTests extends AbstractControlFlowTests {
	
	public DefaultControlFlowTests(String s) {
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
	 *
	 * @see org.springframework.aop.support.AbstractControlFlowTests#createControlFlow()
	 */
	protected ControlFlow createControlFlow() {
		ControlFlow cf = ControlFlowFactory.getInstance().createControlFlow();
		boolean is14 = System.getProperty("java.version").indexOf("1.4") != -1;
		assertEquals("Autodetection of JVM succeeded", is14, cf instanceof Jdk14ControlFlow);
		return cf;
	}

}
