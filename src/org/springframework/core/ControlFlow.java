/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.core;

/**
 * Interface to be implemented by objects that can return information about
 * the current call stack. Useful in AOP (as in AspectJ cflow concept)
 * but not AOP-specific.
 * @author Rod Johnson
 * @version $Id: ControlFlow.java,v 1.1 2004-02-02 11:22:31 jhoeller Exp $
 */
public interface ControlFlow {
	
	boolean under(Class clazz);

	/**
	 * Matches whole method name.
	 */
	boolean under(Class clazz, String methodName);
	
	boolean underToken(String token);

}
