/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

import org.springframework.transaction.TransactionDefinition;

/**
 * This interface adds a rollBackOn specification to TransactionDefinition.
 * As custom rollBackOn is only possible with AOP, this class resides
 * in the AOP transaction package.
 *
 * @author Rod Johnson
 * @since 16-Mar-2003
 * @version $Revision: 1.1.1.1 $
 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute
 */
public interface TransactionAttribute extends TransactionDefinition {
	
	/**
	 * Should we roll back on a checked exception?
	 * @param ex the exception to evaluate
	 * @return boolean rollback or not
	 */
	boolean rollbackOn(Throwable ex);
	
}
