/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;

/**
 * Interface used by TransactionInterceptor. Implementations know
 * how to source transaction attributes, whether from configuration,
 * metadata attributes at source level, or anywhere else.
 * @author Rod Johnson
 * @since 15-Apr-2003
 * @version $Id: TransactionAttributeSource.java,v 1.2 2003-11-28 11:57:28 johnsonr Exp $
 */
public interface TransactionAttributeSource {

	/**
	 * Return the transaction attribute for this method.
	 * Return null if the method is non-transactional.
	 * @param m method
	 * @param targetClass target class. May be null, in which case the declaring
	 * class of the method must be used.
	 * @return TransactionAttribute transaction attribute or null.
	 */
	TransactionAttribute getTransactionAttribute(Method m, Class targetClass);

}
