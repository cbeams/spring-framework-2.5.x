/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utilities for cflow-style pointcuts. Note that such pointcuts are
 * 10-15 times more expensive to evaluate than other pointcuts, as they require
 * analysis of the stack trace (through constructing a new throwable).
 * However, they are useful in some cases.
 * @author Rod Johnson
 * @version $Id: ControlFlow.java,v 1.1 2003-12-07 10:18:07 johnsonr Exp $
 */
public class ControlFlow {

	
	private String stackTrace;
	
	public ControlFlow() {
		StringWriter sw = new StringWriter();
		new Throwable().printStackTrace(new PrintWriter(sw));
		//System.err.println(sw);
		stackTrace = sw.toString();
	}
	
	public boolean under(Class clazz) {
		return stackTrace.indexOf(clazz.getName()) != -1;
	}
	
	/**
	 * Matches whole method name
	 * @param clazz
	 * @param methodName
	 * @return
	 */
	public boolean under(Class clazz, String methodName) {
		return stackTrace.indexOf(clazz.getName() + "." + methodName + "(") != -1;
	}
	
	// TODO need to analyse stack trace to get exact match:
	// otherwise com or org would match anything
	// Need to eliminate original class
	//public boolean underPackage(String packageName) {
	//	return stackTrace.indexOf(" " + packageName) != -1;
	//}
	
	/**
	 * Leave it up to the caller to decide what matches.
	 * Caller must understand stack trace format, so there's less abstraction.
	 * @param token
	 * @return
	 */
	public boolean underToken(String token) {
		return stackTrace.indexOf(token) != -1;
	}

}
