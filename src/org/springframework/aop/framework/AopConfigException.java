/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

/**
 * Exception that gets thrown on illegal AOP configuration arguments.
 * @author Rod Johnson
 * @since 13-Mar-2003
 * @version $Revision: 1.2 $
 */
public class AopConfigException extends IllegalArgumentException {

	public AopConfigException(String msg) {
		super(msg);
	}

}
