/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception that gets thrown on illegal AOP configuration arguments.
 * @author Rod Johnson
 * @since 13-Mar-2003
 * @version $Id: AopConfigException.java,v 1.4 2003-12-06 12:38:28 jhoeller Exp $ 
 */
public class AopConfigException extends NestedRuntimeException {

	public AopConfigException(String msg) {
		super(msg);
	}
	
	public AopConfigException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
