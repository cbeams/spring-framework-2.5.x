/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.aopalliance.intercept.MethodInvocation;

/**
 * 
 * @author Rod Johnson
 * @version $Id: MapTransactionAttributeSource.java,v 1.1 2003-11-22 09:59:40 johnsonr Exp $
 */
public class MapTransactionAttributeSource implements TransactionAttributeSource {
	
	/**
	 * Map from Method to TransactionAttribute
	 */
	private HashMap methodMap = new HashMap();
	
	public void register(Method m, TransactionAttribute txAtt) {
		methodMap.put(m, txAtt);
	}

	/**
	 * @see org.springframework.transaction.interceptor.TransactionAttributeSource#getTransactionAttribute(org.aopalliance.intercept.MethodInvocation)
	 */
	public TransactionAttribute getTransactionAttribute(MethodInvocation invocation) {
		return (TransactionAttribute) methodMap.get(invocation.getMethod());
	}

}
