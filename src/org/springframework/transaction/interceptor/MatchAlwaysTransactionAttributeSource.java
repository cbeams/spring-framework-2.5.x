/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;

/**
 * Very simple implementation of TransactionAttributeSource which will always return
 * the same TransactionAttribute for all methods fed to it. The TransactionAttribute
 * may be specified, but will otherwise default to PROPOGATION_REQUIRED. This may be
 * used in the cases where you want to use the same transaction attribute with all
 * methods being handled by a transaction interceptor.
 * @author Colin Sampaleanu
 * @since 15.10.2003
 * @version $Id: MatchAlwaysTransactionAttributeSource.java,v 1.4 2004-01-01 23:31:26 jhoeller Exp $
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean
 * @see org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator
 */
public class MatchAlwaysTransactionAttributeSource implements TransactionAttributeSource {
  
	private TransactionAttribute transactionAttribute = new DefaultTransactionAttribute();

	/**
	 * Allows a transaction attribute to be specified, using the String form, for
	 * example, "PROPOGATION_REQUIRED".
	 * @param transactionAttribute The String form of the transactionAttribute to use.
	 * @see org.springframework.transaction.interceptor.TransactionAttributeEditor
	 */
	public void setTransactionAttribute(TransactionAttribute transactionAttribute) {
		this.transactionAttribute = transactionAttribute;
	}

	public TransactionAttribute getTransactionAttribute(Method method, Class targetClass) {
		return transactionAttribute;
	}

}
