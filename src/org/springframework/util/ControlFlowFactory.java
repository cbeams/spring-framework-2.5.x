/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.util;

import org.apache.commons.logging.LogFactory;

/**
 * Singleton factory to conceal automatic choice of Java 1.4 or 1.3
 * ControlFlow implementation class. We want to use the more efficient Java 1.4
 * StackTraceElement if we can, and we don't want to impose a runtime dependency on
 * 1.4.
 * @author Rod Johnson
 * @version $Id: ControlFlowFactory.java,v 1.1 2003-12-21 11:49:39 johnsonr Exp $
 */
public class ControlFlowFactory {
	
	private static ControlFlowFactory instance = new ControlFlowFactory();

	
	public static ControlFlowFactory getInstance() {
		return instance;
	}
	
	private boolean isJava14;
	
	private ControlFlowFactory() {
		String javaVersion = System.getProperty("java.version");
		LogFactory.getLog(ControlFlowFactory.class).info("Java version is " + javaVersion);
		// Should look like "1.4.1_02"
		isJava14 = javaVersion.indexOf("1.4") != -1;
	}
	
	public ControlFlow createControlFlow() {
		return isJava14 ? 
					(ControlFlow) new Jdk14ControlFlow() : 
					(ControlFlow) new Jdk13ControlFlow();
	}

}
