/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

import com.ibatis.common.exception.NestedRuntimeException;

/**
 * Exception that gets thrown on illegal AOP configuration arguments.
 * @author Rod Johnson
 * @since 13-Mar-2003
 * @version $Id: AopConfigException.java,v 1.3 2003-12-05 15:19:24 johnsonr Exp $ 
 */
public class AopConfigException extends NestedRuntimeException {

	public AopConfigException(String msg) {
		super(msg);
	}
	
	public AopConfigException(String msg, Throwable t) {
		super(msg, t);
	}

}
