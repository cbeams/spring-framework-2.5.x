/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.adapter;

import org.springframework.aop.framework.AopConfigException;

/**
 * Exception thrown when an attempt is made to use an unsupported
 * Advisor or Advice type.
 * @author Rod Johnson
 * @version $Id: UnknownAdviceTypeException.java,v 1.3 2004-02-22 10:39:01 johnsonr Exp $
 */
public class UnknownAdviceTypeException extends AopConfigException {
	
	public UnknownAdviceTypeException(Object advice) {
		super("No adapter for Advice of class '" + advice.getClass().getName() + "'");
	}

}
