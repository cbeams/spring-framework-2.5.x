/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utilities for cflow-style pointcuts. Note that such pointcuts are
 * 5-10 times more expensive to evaluate than other pointcuts, as they require
 * analysis of the stack trace (through constructing a new throwable).
 * However, they are useful in some cases.
 * This implementation uses the StackTraceElement class introduced in Java 1.4.
 * @author Rod Johnson
 * @version $Id: Jdk14ControlFlow.java,v 1.1 2003-12-15 14:39:29 johnsonr Exp $
 */
class Jdk14ControlFlow implements ControlFlow {

	private StackTraceElement[] stack;
	
	public Jdk14ControlFlow() {
		stack = new Throwable().getStackTrace();
	}
	
	public boolean under(Class clazz) {
		String className = clazz.getName();
		for (int i = 0; i < stack.length; i++) {
			//System.out.println(stack[i]);
			if (stack[i].getClassName().equals(className)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Matches whole method name
	 * @param clazz
	 * @param methodName
	 * @return
	 */
	public boolean under(Class clazz, String methodName) {
		String className = clazz.getName();
		for (int i = 0; i < stack.length; i++) {
			if (stack[i].getClassName().equals(className) && stack[i].getMethodName().equals(methodName)) {
				return true;
			}
		}
		return false;
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
		StringWriter sw = new StringWriter();
		new Throwable().printStackTrace(new PrintWriter(sw));
		//System.err.println(sw);
		String stackTrace = sw.toString();
		return stackTrace.indexOf(token) != -1;
	}

}
