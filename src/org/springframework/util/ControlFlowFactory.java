/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.util;


/**
 * Singleton factory to conceal automatic choice of Java 1.4 or 1.3
 * ControlFlow implementation class. We want to use the more efficient Java 1.4
 * StackTraceElement if we can, and we don't want to impose a runtime dependency on
 * 1.4.
 * @author Rod Johnson
 * @version $Id: ControlFlowFactory.java,v 1.2 2004-01-31 10:14:28 johnsonr Exp $
 */
public class ControlFlowFactory {
	
	private static ControlFlowFactory instance = new ControlFlowFactory();

	public static ControlFlowFactory getInstance() {
		return instance;
	}
		
	public ControlFlow createControlFlow() {
		return JdkVersion.getMajorJavaVersion() >= JdkVersion.JAVA_14 ? 
					(ControlFlow) new Jdk14ControlFlow() : 
					(ControlFlow) new Jdk13ControlFlow();
	}

}
