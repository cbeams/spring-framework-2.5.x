/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy.metadata;



/**
 * @author Rod Johnson
 */
public class TxClass {
	
	private int invocations;
	
	/**
	 * @org.springframework.transaction.interceptor.DefaultTransaction ( timeout=-1 )
	 */
	public int defaultTxAttribute() {
		return ++invocations;
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
