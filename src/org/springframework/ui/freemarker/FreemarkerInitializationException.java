/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ui.freemarker;

import org.springframework.core.NestedRuntimeException;


/**
 * Exception thrown on Freemarker initialization failure 
 * @author Darren Davison
 * @since 3/3/2004
 * @version $Id: FreemarkerInitializationException.java,v 1.1 2004-03-11 20:02:26 davison Exp $
 */
public class FreemarkerInitializationException extends NestedRuntimeException {
	
	/**
	 * create exception with a message
     * @param msg
     */
    public FreemarkerInitializationException(String msg) {
		super(msg);
	}
	
	/**
	 * create nested exception
     * @param msg
     * @param ex
     */
    public FreemarkerInitializationException(String msg, Exception ex) {
		super(msg, ex);
	}
}
