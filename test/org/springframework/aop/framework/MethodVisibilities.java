/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

/**
 * Test for different method visibilities to
 * test AOP proxying capabilities
 * @author Rod Johnson
 * @version $Id: MethodVisibilities.java,v 1.1 2004-02-21 08:38:41 johnsonr Exp $
 */
public class MethodVisibilities {
	
	public String publicMethod(String s) {
		return s;
	}
	
	protected String protectedMethod(String s) {
		return s;
	}
	
	private String privateMethod(String s) {
		return s;
	}

}
