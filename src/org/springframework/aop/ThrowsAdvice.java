/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

/**
 * Tag interface for throws advice.
 * There aren't any methods on this interface, as methods are invoked by reflection.
 * Implementing classes should implement methods of the form:
 * <code>
 * afterThrowing([Method], [args], [target], Throwable subclass) 
 * </code>
 * The first three arguments are optional, and only useful if
 * we want further information about the joinpoint, as in AspectJ
 * <b>after throwing</b> advice.
 * @author Rod Johnson
 * @version $Id: ThrowsAdvice.java,v 1.1 2003-12-11 09:18:48 johnsonr Exp $
 */
public interface ThrowsAdvice {

}
