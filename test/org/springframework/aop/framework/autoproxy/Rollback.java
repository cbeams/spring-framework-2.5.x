/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy;

import org.springframework.transaction.interceptor.TransactionInterceptor;



/**
 * @org.springframework.enterpriseservices.Pooling (size=10)
 * @org.springframework.transaction.interceptor.DefaultTransaction ( timeout=-1 )
 */
public class Rollback {
	
	/**
	 * Inherits transaction attribute.
	 * Illustrates programmatic rollback.
	 * @param rollbackOnly
	 */
	public void rollbackOnly(boolean rollbackOnly) {
		if (rollbackOnly) {
			setRollbackOnly();
		}
	}
	
	/**
	 * Extracted in a protected method to facilitate testing
	 */
	protected void setRollbackOnly() {
		TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
	}

	/**
	 * @org.springframework.transaction.interceptor.RuleBasedTransaction ( timeout=-1 )
	 * @org.springframework.transaction.interceptor.RollbackRule ( "java.lang.Exception" )
	 * @org.springframework.transaction.interceptor.NoRollbackRule ( "ServletException" )
	 */
	public void echoException(Exception ex) throws Exception {
		if (ex != null)
			throw ex;
	}

}
