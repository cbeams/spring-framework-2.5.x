/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy.metadata;

import org.springframework.transaction.interceptor.TransactionInterceptor;


/**
 * Illustrates class and method attributes
 * <br>The attribute syntax is that of Commons Attributes.
 * 
 * @org.springframework.aop.framework.autoproxy.target.PoolingAttribute (10)
 * 
 * @org.springframework.transaction.interceptor.DefaultTransactionAttribute ()
 * 
 * @author Rod Johnson
 */
public class TxClassWithClassAttribute {
	
	
	public int inheritClassTxAttribute(int i) {
		return i;
	}
	
	/**
	 * Inherits transaction attribute from class.
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
	 * @org.springframework.transaction.interceptor.RuleBasedTransactionAttribute ()
	 * @org.springframework.transaction.interceptor.RollbackRuleAttribute ("java.lang.Exception")
	 * @org.springframework.transaction.interceptor.NoRollbackRuleAttribute ("ServletException")
	 */
	public void echoException(Exception ex) throws Exception {
		if (ex != null)
			throw ex;
	}

}
