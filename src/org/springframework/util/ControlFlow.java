/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.util;

/**
 * Interface to be implemented by objects that can return information about
 * the current call stack. Useful in AOP (as in AspectJ cflow concept)
 * but not AOP-specific.
 * @author Rod Johnson
 * @version $Id: ControlFlow.java,v 1.2 2003-12-30 01:24:06 jhoeller Exp $
 */
public interface ControlFlow {
	
	boolean under(Class clazz);

	/**
	 * Matches whole method name.
	 */
	boolean under(Class clazz, String methodName);
	
	boolean underToken(String token);

}
