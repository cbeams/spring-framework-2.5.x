/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy.metadata;


/**
 * Interface for transaction class. Attributes are on the class,
 * not this interface.
 * 
 * @author Rod Johnson
 * @version $Id: TxClass.java,v 1.3 2003-12-17 09:25:42 johnsonr Exp $
 */
public interface TxClass {
	
	
	public int defaultTxAttribute();
	
	
	public void echoException(Exception ex) throws Exception;

}
