/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;


/**
 * Superinterface for all after returning advice. Such advice is invoked
 * only on normal method return, not if an exception is thrown.
 * Such advice can see the return value, but cannot change it.
 * @see org.springframework.aop.MethodAfterReturningAdvice
 * @author Rod Johnson
 * @version $Id: AfterReturningAdvice.java,v 1.1 2004-01-05 18:47:00 johnsonr Exp $
 */
public interface AfterReturningAdvice {

}
